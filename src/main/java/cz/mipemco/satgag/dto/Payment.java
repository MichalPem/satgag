package cz.mipemco.satgag.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDateTime;

/**
 * @author Michal Pemčák
 */
@Entity
public class Payment
{

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@JsonIgnore
	public Long id;

	public Boolean isIncoming;
	public Long payee;
	public Integer amount;
	public String paymentHash;
	public LocalDateTime localDateTime;
	public Long expiry;
	public Boolean processed;
	public Long expectedFee;

}
