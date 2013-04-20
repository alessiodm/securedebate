package org.alessiodm.securedebate;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.security.Key;

import javax.crypto.Cipher;

public class Utility {
	public static void inviaByteArray(byte[] c, DataOutputStream d) throws IOException
	{
		int len = c.length;
		
		for (int i = 0; i<len; i++)
			d.writeByte(c[i]);
	}
	
	public static byte[] riceviByteArray(int len, DataInputStream d) throws IOException
	{
		byte[] c = new byte[len];
		
		for (int i = 0; i<len; i++)
			c[i] = d.readByte();
		
		return c;
	}
	
	public static byte[] cifra(String text, Key key)
	{
		try
		{
			Cipher cipher = Cipher.getInstance("TripleDES/ECB/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, key);
			
			byte[] plaintext = text.getBytes("UTF8");
			byte[] ciphertext = cipher.doFinal(plaintext);
			return ciphertext;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	public static String decifra (byte[] ciphertext, Key key)
	{
		try
		{
			Cipher cipher = Cipher.getInstance("TripleDES/ECB/PKCS5Padding");
			cipher.init(Cipher.DECRYPT_MODE, key);

			byte[] decryptedtext = cipher.doFinal(ciphertext);
			return new String(decryptedtext, "UTF8");
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return "";
		}
	}
	
}
