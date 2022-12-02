package cz.mipemco.satgag.jpa;

import cz.mipemco.satgag.dto.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @author Michal Pemčák
 */
public interface PaymentRepository extends JpaRepository<Payment,Long>
{

	List<Payment> findPaymentByProcessed(Boolean processed);
	List<Payment> findPaymentByProcessedAndPayee(Boolean processed,Long payee);
}
