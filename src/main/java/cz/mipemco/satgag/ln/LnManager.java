package cz.mipemco.satgag.ln;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.mipemco.satgag.dto.InvoiceResponse;
import cz.mipemco.satgag.dto.Payment;
import cz.mipemco.satgag.dto.User;
import cz.mipemco.satgag.jpa.PaymentRepository;
import cz.mipemco.satgag.jpa.UserRepository;
import org.bitcoinj.core.Base58;
import org.lightningj.lnd.wrapper.ClientSideException;
import org.lightningj.lnd.wrapper.StatusException;
import org.lightningj.lnd.wrapper.SynchronousLndAPI;
import org.lightningj.lnd.wrapper.ValidationException;
import org.lightningj.lnd.wrapper.message.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * @author Michal Pemčák
 */
@Component public class LnManager
{

	private final PaymentRepository paymentRepository;
	private final UserRepository userRepository;
	private final SynchronousLndAPI synchronousLndAPI;

	public LnManager(PaymentRepository paymentRepository, UserRepository userRepository, SynchronousLndAPI synchronousLndAPI) throws ClientSideException, SSLException
	{
		this.paymentRepository = paymentRepository;
		this.userRepository = userRepository;
		this.synchronousLndAPI = synchronousLndAPI;
		;
	}

	public InvoiceResponse createInvoice(User from, Integer amount, String metadata) throws Exception
	{
		if (amount != null && from.balance > (amount))
		{
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			Invoice invoice = new Invoice();
			invoice.setMemo(from.lnid);
			invoice.setValue(amount);
			invoice.setExpiry(100);
			invoice.setDescriptionHash(digest.digest(metadata.getBytes(StandardCharsets.UTF_8)));
			//todo catch exception
			AddInvoiceResponse o = synchronousLndAPI.addInvoice(invoice);
			InvoiceResponse invoiceResponse = new InvoiceResponse();
			invoiceResponse.pr = o.getPaymentRequest();
			invoiceResponse.status = "OK";
			invoiceResponse.successAction.tag = "message";
			invoiceResponse.successAction.message = "Success!";

			Payment payment = new Payment();
			payment.amount = amount;
			payment.payee = from.id;
			payment.isIncoming = true;

			payment.paymentHash = Base58.encode(o.getRHash());
			payment.expiry = 100L;
			payment.processed = false;
			payment.localDateTime = LocalDateTime.now();
			paymentRepository.saveAndFlush(payment);

			return invoiceResponse;
		}
		throw new Exception("Wrong amount or not enough balance");
	}

	public void pay(User from, String to, Integer amount) throws Exception
	{
		if (from.balance < amount)
			return;
		List<Payment> unprocessedPaymens = paymentRepository.findPaymentByProcessedAndPayee(false, from.id);
		if(!unprocessedPaymens.isEmpty())
			throw new IOException("Please wait until are all transactions settled");
		int index = to.lastIndexOf("@");
		if (index == -1)
			throw new IOException("Not lightning address");
		String domain = to.substring(index + 1);

		if (domain.isEmpty())
			throw new IOException("Not lightning address");
		String user = to.substring(0, index);
		ObjectMapper objectMapper = new ObjectMapper();
		InvoiceResponse invoiceResponse;
		try
		{
			invoiceResponse = objectMapper.readValue(
					new URL("https://" + domain + "/.well-known/lnurlp/" + user + "?amount=" + (amount * 1000)), InvoiceResponse.class);

			if (!invoiceResponse.status.equals("OK"))
			{
				throw new Exception(invoiceResponse.status + " " + invoiceResponse.reason);
			}
			;
		} catch (Exception e)
		{
			throw new Exception(e.getClass().getSimpleName() + ": " + e.getMessage());
		}

		PayReq o = synchronousLndAPI.decodePayReq(invoiceResponse.pr);

		QueryRoutesRequest queryRoutesRequest = new QueryRoutesRequest();
		queryRoutesRequest.setPubKey(o.getDestination());
		queryRoutesRequest.setAmt(amount);
		QueryRoutesResponse r = synchronousLndAPI.queryRoutes(queryRoutesRequest);
		Optional<Long> maxPayment = r.getRoutes().stream().map(Route::getTotalAmtMsat).max(Long::compareTo);

		if(!maxPayment.isPresent())
			throw new Exception("Cant find route");
		if(maxPayment.get()/1000L > from.balance)
			throw new Exception("Routing fee too high");

		int iAmount = Math.toIntExact(o.getNumSatoshis());
		if (iAmount != amount)
		{
			throw new Exception("Server generated payment request with incorrect amount");
		}
		userRepository.debitUser(maxPayment.get()/1000L, from.id);

		Payment payment = new Payment();
		payment.amount = amount;
		payment.payee = from.id;
		payment.isIncoming = false;
		payment.paymentHash = o.getPaymentHash();
		payment.localDateTime = LocalDateTime.now();
		payment.expiry = o.getExpiry();
		payment.expectedFee = (maxPayment.get()/1000L)-amount;
		payment.processed = false;

		SendRequest sendPaymentRequest = new SendRequest();
		sendPaymentRequest.setPaymentRequest(invoiceResponse.pr);
		try
		{
			synchronousLndAPI.sendPaymentSync(sendPaymentRequest);
		} catch (StatusException se)
		{
			//rollback
			return;
		}
		paymentRepository.saveAndFlush(payment);
	}

	@Scheduled(cron = "*/5 * * * * *") public void validatePayments()
	{
		List<Payment> payments = paymentRepository.findPaymentByProcessed(false);
		if (payments.isEmpty())
			return;


		ListPaymentsResponse p;
		try
		{

			p = synchronousLndAPI.listPayments(true, 0L, 10000L, false, false);
		} catch (StatusException | ValidationException e)
		{
			e.printStackTrace();
			return;
		}

		for (Payment payment : payments)
		{

			Optional<org.lightningj.lnd.wrapper.message.Payment> ep = Optional.empty();
			try
			{
				List<org.lightningj.lnd.wrapper.message.Payment> e = p.getPayments();
				//					e.forEach(rrr -> System.out.println(rrr.getPaymentHash()));
				ep = e.stream().filter(pp -> pp.getPaymentHash().equals(payment.paymentHash)).findFirst();
			} catch (ClientSideException e)
			{
				e.printStackTrace();
			}
			if (ep.isPresent())
			{
				org.lightningj.lnd.wrapper.message.Payment.PaymentStatus status = ep.get().getStatus();
				if (status.equals(org.lightningj.lnd.wrapper.message.Payment.PaymentStatus.SUCCEEDED))
				{
					if (payment.isIncoming)
					{
						userRepository.creditUser(payment.amount.longValue(), payment.payee);
					} else {
						userRepository.creditUser((payment.expectedFee-ep.get().getFeeMsat()/1000L), payment.payee);
					}
					payment.processed = true;
					paymentRepository.saveAndFlush(payment);
				} else if (status.equals(org.lightningj.lnd.wrapper.message.Payment.PaymentStatus.FAILED))
				{
					if (!payment.isIncoming)
					{
						userRepository.creditUser(payment.amount.longValue(), payment.payee);
						payment.processed = true;
						paymentRepository.saveAndFlush(payment);
					}
				}
			} else
			{
				PaymentHash paymentHash = new PaymentHash();
				paymentHash.setRHash(Base58.decode(payment.paymentHash));
				try
				{
					Invoice invoice = synchronousLndAPI.lookupInvoice(paymentHash);
					if (invoice.getState().equals(Invoice.InvoiceState.SETTLED))
					{
						if (payment.isIncoming)
						{
							userRepository.creditUser(invoice.getAmtPaidMsat()/1000L, payment.payee);
						}
						payment.processed = true;
						paymentRepository.saveAndFlush(payment);
					} else if (invoice.getState().equals(Invoice.InvoiceState.CANCELED))
					{
						if (!payment.isIncoming)
						{
							userRepository.creditUser(invoice.getAmtPaidMsat()/1000L, payment.payee);
							payment.processed = true;
							paymentRepository.saveAndFlush(payment);
						}
					}
				} catch (StatusException | ValidationException e)
				{
					e.printStackTrace();
				}
			}
		}
	}
}
