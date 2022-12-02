package cz.mipemco.satgag.jpa;

import cz.mipemco.satgag.dto.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.Optional;

/**
 * @author Michal Pemčák
 */
public interface UserRepository extends JpaRepository<User,Long>
{

	Optional<User> findUserByPassword(String password);
	Optional<User> findUserByLnid(String lnid);

	@Query(value = "update User set balance = balance + :amount where id = :id")
	@Modifying
	@Transactional void creditUser(Long amount, Long id);

	@Query(value = "update User set balance = balance - :amount where id = :id")
	@Modifying
	@Transactional void debitUser(Long amount, Long id);

	@Query(value = "update User set balance = 0 where id = :id")
	@Modifying
	@Transactional void nullUserBalance(Long id);

	@Query(value = "call updateBalance(:from,:to,:article_id,:amount, @outp);",nativeQuery = true)
	void updateBalance(String from,Integer to,Integer article_id,Integer amount);

	@Query(value = "select @outp;",nativeQuery = true) BigInteger verify();

	boolean existsByLnid(String lnid);
}
