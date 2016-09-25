package com.gz.tool.server.security;

import java.io.ByteArrayOutputStream;

/**
 * <p>
 *数据处理工具
 * </p>
 */
public class ByteUtils {
	
	/**
	 * <p>
	 * 将字节数组转换为十六进制字符串。
	 * </p>
	 * 
	 * @param bytearray 待转换的字节数组。
	 * @return 转换之后的Hex字符串。
	 */
	public static String byteArrayToHexString(byte[] bytearray) {
		String strDigest = "";
		for (int i = 0; i < bytearray.length; i++) {
			strDigest += byteToHexString(bytearray[i]);
		}
		return strDigest;
	}

	// 将字节转换为十六进制字符串
	private static String byteToHexString(byte ib) {
		char[] Digit = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
		char[] ob = new char[2];
		ob[0] = Digit[(ib >>> 4) & 0X0F];
		ob[1] = Digit[ib & 0X0F];
		String s = new String(ob);
		return s;
	}
	
	/**
	 * jog byte array.
	 * 
	 * @param byts source byte array.
	 * @return result.
	 */
	public static byte[] jogBytes(byte[]... byts) throws Exception {
		if (byts.length == 0) {
			return null;
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		for (byte[] byt : byts) {
			if (byt == null) {
				continue;
			}
			out.write(byt);
		}
		try {
			out.flush();
		} catch (Exception e) {
		}
		byte[] result = out.toByteArray();
		try {
			out.close();
		} catch (Exception e) {
		}
		out = null;
		return result;
	}
	
	/**
	 * <p>
	 * transform string to byte array.
	 * </p>
	 * 
	 * @param s source string.
	 * @return a byte array.
	 */
	public static final byte[] toBytes(String s) {
		byte[] bytes;
		bytes = new byte[s.length() >> 1];
		for (int i = 0; i < bytes.length; i++) {
			bytes[i] = (byte) Integer.parseInt(s.substring(i << 1, (i << 1) + 2), 16);
		}
		return bytes;
	}

}
