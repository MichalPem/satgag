package cz.mipemco.satgag.util;

import org.bitcoinj.core.Base58;

/**
 * @author Michal Pemčák
 */
public class Hash
{

	public static String getDbKey(String key)
	{
		Blake3 h = Blake3.newInstance();
		h.update(key.getBytes());
		return Base58.encode(h.hexdigest(67).getBytes());
	}
}
