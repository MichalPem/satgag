package cz.mipemco.satgag.dto;

/**
 * @author Michal Pemčák
 */
public class Lnurl
{
	public String callback;
	public Integer maxSendable;
	public Integer minSendable;
	public String metadata;
	public Integer commentAllowed = 0;
	public String tag = "payRequest";
}
