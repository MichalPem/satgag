package cz.mipemco.satgag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author Michal Pemčák
 */
@Configuration
public class DatabaseProcedures
{
	@Autowired
	private DataSource dataSource;

	@Bean
	public void create() throws SQLException
	{
		try (Connection conn = dataSource.getConnection()) {
			String query = "	drop procedure if exists `updateBalance`";
			String query2 = "	"
					+ ""
					+ "CREATE PROCEDURE `updateBalance`(uf varchar(200), ut BIGINT,article_id BIGINT,amount int, OUT success BOOL) "
					+ "BEGIN "
					+ "IF ((select balance from user where `password` = uf) > amount) THEN"
					+ "	update user set balance = balance + amount where `id` = ut;"
					+ "	update user set balance = balance - amount where `password` = uf;"
					+ "	update article set sats = sats + amount where `id` = article_id;"
					+ "	INSERT INTO `payment`(`amount`,`is_incoming`,`payee`,`local_date_time`,`processed`) VALUES (amount,0,(select id from user where `password` = uf),now(),1);"
					+ "	INSERT INTO `payment`(`amount`,`is_incoming`,`payee`,`local_date_time`,`processed`) VALUES (amount,1,(select user_id from article where `id` = article_id),now(),1);"
					+ "	set success = 1;"
					+ "else"
					+ "	set success = 0;"
					+ "end if;"
					+ "select @success;"
					+ ""+
					"END";
			Statement stmt = conn.createStatement();
			stmt.execute(query);
			stmt.execute(query2);


		}
	}
}
