package cz.mipemco.satgag.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * @author Michal Pemčák
 */
@Entity
public class User
{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Long id;

	public String password;
	public String imgurl;
	public String nick;
	public Long balance = 0L;
	public Integer satAmount = 1;
	public String lnid;

	public User()
	{
	}

	public User(Long id, String password, String imgurl, String nick, Long balance, String lnid,Integer satAmount)
	{
		this.id = id;
		this.password = password;
		this.imgurl = imgurl;
		this.nick = nick;
		this.balance = balance;
		this.lnid = lnid;
		this.satAmount = satAmount;
	}

	public User toDto()
	{
		return  new User(id,password,imgurl,nick,balance,lnid,satAmount);
	}

	public User toDto(String origKey)
	{
		return  new User(id,origKey,imgurl,nick,balance,lnid,satAmount);
	}
}
