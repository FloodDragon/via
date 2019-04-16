package com.via.light.rpc.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;

/**
 * UUID
 *
 * @author LiuJing
 */
public class UUID {

	private static final String hexChars = "0123456789ABCDEF";

	private static Random myRand;

	private static SecureRandom mySecureRand;

	private String s_id;

	public String valueBeforeMD5 = "";

	public String valueAfterMD5 = "";

	static {
		mySecureRand = new SecureRandom();
		long secureInitializer = mySecureRand.nextLong();
		myRand = new Random(secureInitializer);
	}

	public UUID(String s_id){
		this.s_id = s_id;
		getRandomGUID(false);
	}

	public void getRandomGUID(boolean secure) {
		MessageDigest md5 = null;
		StringBuilder sbValueBeforeMD5 = new StringBuilder();

		try {
			md5 = MessageDigest.getInstance("MD5");
			long time = System.currentTimeMillis();
			long rand = 0;

			if (secure) {
				rand = mySecureRand.nextLong();
			} else {
				rand = myRand.nextLong();
			}

			sbValueBeforeMD5.append(s_id);
			sbValueBeforeMD5.append(':');
			sbValueBeforeMD5.append(Long.toString(time));
			sbValueBeforeMD5.append(':');
			sbValueBeforeMD5.append(Long.toString(rand));

			valueBeforeMD5 = sbValueBeforeMD5.toString();
			md5.update(valueBeforeMD5.getBytes());

			byte[] array = md5.digest();
			StringBuilder sb = new StringBuilder();
			for (int j = 0; j < array.length; ++j) {
				int b = array[j] & 0xFF;
				if (b < 0x10)
					sb.append('0');
				sb.append(Integer.toHexString(b));
			}

			valueAfterMD5 = sb.toString();
		} catch (NoSuchAlgorithmException e) {
			System.out.println("Error: " + e);
		} catch (Exception e) {
			System.out.println("Error:" + e);
		}
	}

	public final static byte[] toByteArray(String uid) {
		byte[] result = new byte[16];
		char[] chars = uid.toCharArray();
		int r = 0;
		for (int i = 0; i < chars.length; ++i) {
			if (chars[i] == '-') {
				continue;
			}
			int h1 = Character.digit(chars[i], 16);
			++i;
			int h2 = Character.digit(chars[i], 16);
			result[(r++)] = (byte) ((h1 << 4 | h2) & 0xFF);
		}
		return result;
	}

	public static String fromByteArray(byte[] ba) {
		if ((ba != null) && (ba.length == 16)) {
			StringBuilder result = new StringBuilder(36);
			for (int i = 0; i < 16; ++i) {
				if ((i == 4) || (i == 6) || (i == 8) || (i == 10)) {
					result.append('-');
				}
				result.append(hexChars.charAt(((ba[i] & 0xF0) >>> 4)));
				result.append(hexChars.charAt((ba[i] & 0xF)));
			}
			return result.toString();
		}
		return null;
	}

	public static String getPrettyFormatted(String str) {
		return String.format(
				"%s-%s-%s-%s-%s",
				new Object[] { str.substring(0, 8), str.substring(8, 12),
						str.substring(12, 16), str.substring(16, 20),
						str.substring(20) });
	}

	public String toString() {
		return UUID.getPrettyFormatted(valueAfterMD5.toUpperCase());
	}

	public static UUID randomUUID()
	{
		return new UUID(null);
	}
}