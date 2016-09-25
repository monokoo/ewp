package com.gz.tool.server.security;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * <p>
 * HMac加密类。<br>
 * </p>
 * <p>
 * 说明：HMAC是密钥相关的哈希运算消息认证码（Hash-based Message Authentication Code）,HMAC运算利用哈希算法，以一个密钥和一个消息为输入，生成一个消息摘要作为输出<br>
 * </p>
 * <p>
 * 功能：<br>
 * 1.提供HMac加密接口<br>
 * 2.提供SHA1和MD5加密接口<br>
 * 3.提供RPF加密接口(建议使用{@link PRFCipher#PRF(byte[], byte[], byte[], int)}})<br>
 * 4.提供一些生成二进制常量的接口<br>
 * </p>
 */
public final class HMac {
	private static HashMap<String, HashMap<String, String>> mKeyIvMap = new HashMap<String, HashMap<String, String>>();

	public static final String HMAC_CLIENT_KEY = "hmacClientKey";
	public static final String HMAC_SERVER_KEY = "hmacServerKey";
	/**
	 * <p>
	 * MAC算法可选以下多种算法：<br>
	 * 
	 * HmacMD5<br>
	 * HmacSHA1
	 * </p>
	 * 
	 * @see #KEY_MAC_SHA1
	 */
	public static final String KEY_MAC_MD5 = "HmacMD5";
	/**
	 * <p>
	 * MAC算法可选以下多种算法：<br>
	 * 
	 * HmacMD5<br>
	 * HmacSHA1
	 * </p>
	 * 
	 * @see #KEY_MAC_MD5
	 */
	public static final String KEY_MAC_SHA1 = "HmacSHA1";

	public static byte[] getInfoBySessionIdAndKey(String sessionID, String key) {
		HashMap<String, String> hm = mKeyIvMap.get(sessionID);
		if(hm!=null) {
			String base64Value = hm.get(key);
			if(base64Value!=null) {
				return Base64.decodeToBytes(base64Value);
			}
		}
		return null;
	}
	
	public static void setInfoBySessionIdAndKey(String sessionID, String key, byte[] value) {
		HashMap<String, String> hm = mKeyIvMap.get(sessionID);
		if (hm==null) {
			hm = new HashMap<String, String>();
		} 
		hm.remove(key);
		hm.put(key, Base64.encode(value));
		
		mKeyIvMap.remove(sessionID);
		mKeyIvMap.put(sessionID, hm);
	}
	
	public static void removeInfoByKey(String key) {
		mKeyIvMap.remove(key);
	}
	
	/**
	 * <p>
	 * HMAC encrypt.
	 * </p>
	 * 
	 * @param data source string.
	 * @param key the key.
	 * @param keyMacMode mode of encrypt.
	 * @return encrypted data.
	 * @throws Exception.
	 */
	public static String encryptHMAC(String data, byte[] key, String keyMacMode) throws Exception {
		byte[] dataByt = data.getBytes("UTF-8");
		byte[] hmacByt = encryptHMAC(dataByt, key, keyMacMode);
		String hmacStr = Base64.encode(hmacByt);
		return hmacStr;
	}

	/**
	 * <p>
	 * HMAC encrypt.
	 * </p>
	 * 
	 * @param data source string.
	 * @param key the key.
	 * @param keyMacMode mode of encrypt.
	 * @return encrypted data.
	 * @throws Exception.
	 */
	public static byte[] encryptHMAC(byte[] data, byte[] key, String keyMacMode) throws Exception {
		SecretKey secretKey = new SecretKeySpec(key, keyMacMode);
		Mac mac = Mac.getInstance(secretKey.getAlgorithm());
		mac.init(secretKey);
		byte[] hmacByt = mac.doFinal(data);
		return hmacByt;
	}

	/**
	 * <p>
	 * get a data by MD5.
	 * </p>
	 * 
	 * @param data source data.
	 * @return data transformed by MD5.
	 */
	public static byte[] MD5(byte[] data) {
		try {
			return MD5.getMD5(data);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * <p>
	 * SHA1 encrypt.
	 * </p>
	 * 
	 * @param data the byte array.
	 * @return data transformed by SHA1.
	 */
	public static byte[] SHA1(byte[] data) {
		try {
			MessageDigest sha1 = MessageDigest.getInstance("SHA1");
			sha1.update(data);
			return sha1.digest();
		} catch (NoSuchAlgorithmException e) {
		}
		return null;
	}

	/**
	 * <p>
	 * 获取SHA1值，离线资源存储、读取时会用到。
	 * </p>
	 * 
	 * @param buffer 文件内容
	 * 
	 * @return byte缓冲区的SHA1值。
	 */
	public static String getSHA1(byte[] buffer) {
		String serveRev = null;
		if (buffer != null) {
			byte[] temp = SHA1(buffer);
			if (temp != null) {
				serveRev = ByteUtils.byteArrayToHexString(temp);
			}
		}
		return serveRev;
	}

	/**
	 * <p>
	 * PRF(secret, label, seed) = P_MD5(S1, label + seed) XOR P_SHA-1(S2, label + seed).
	 * </p>
	 * 
	 * @param secret the key array.
	 * @param label the label array.
	 * @param seed the seed array.
	 * @param length length of result.
	 * @return data transformed by PRF.
	 * @deprecated {@link PRFCipher#PRF(byte[], byte[], byte[], int)}
	 */
	public static byte[] PRF(byte[] secret, byte[] label, byte[] seed, int length) throws Exception {
		return PRFCipher.PRF(secret, label, seed, length);
	}

	/*************************************** 下面为一些生成常量的方法 ********************************/

	/**
	 * <p>
	 * get client finished infomation or null.
	 * </p>
	 * 
	 * @return client finished infomation or null.
	 */
	public static final byte[] TLS_MD_CLIENT_FINISH_CONST() {
		try {
			return "client finished".getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
		}
		return null;
	}

	/**
	 * <p>
	 * 用于Finished。
	 * </p>
	 * 
	 * @return server finished information or null.
	 */
	public static final byte[] TLS_MD_SERVER_FINISH_CONST() {
		try {
			return "server finished".getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
		}
		return null;
	}

	/**
	 * <p>
	 * 用于计算MS。
	 * </p>
	 * 
	 * @return master secret information or null.
	 */
	public static final byte[] TLS_MD_MASTER_SECRET_CONST() {
		try {
			return "master secret".getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
		}
		return null;
	}

	/**
	 * <p>
	 * 用于计算MS2。
	 * </p>
	 * 
	 * @return master secret2 information or null.
	 */
	public static final byte[] TLS_MD_MASTER_SECRET2_CONST() {
		try {
			return "master secret2".getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
		}
		return null;
	}

	/**
	 * <p>
	 * 生成PMS。
	 * </p>
	 * 
	 * @return premaster secret information or null.
	 */
	public static final byte[] TLS_MD_PREMASTER_SECRET_CONST() {
		try {
			return "premaster secret".getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
		}
		return null;
	}

	/**
	 * <p>
	 * 生成PMS2。
	 * </p>
	 * 
	 * @return premaster secret2 information or null.
	 */
	public static final byte[] TLS_MD_PREMASTER_SECRET2_CONST() {
		try {
			return "premaster secret2".getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
		}
		return null;
	}

	/**
	 * <p>
	 * 用于客户端与服务器PRF计算后输出最终密钥值（KEY、IV、MACKEY）。
	 * </p>
	 * 
	 * @return key expansion information or null.
	 */
	public static final byte[] TLS_MD_CLIENT_SERVER_KEYIVMAC_CONST() {
		try {
			return "key expansion".getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
		}
		return null;
	}

	/**
	 * <p>
	 * 用于一次一秘。
	 * </p>
	 * 
	 * @return once secret information or null.
	 */
	public static final byte[] TLS_ONCE_SECRET_CONST() {
		try {
			return "once secret".getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
		}
		return null;
	}

	/**
	 * @deprecated {@link ByteUtils#byteArrayToHexString(byte[])}
	 */
	public static String byteArrayToHexString(byte[] bytearray) {
		return ByteUtils.byteArrayToHexString(bytearray);
	}

	/**
	 * @deprecated {@link ByteUtils#jogBytes(byte[])}
	 */
	public static byte[] jogBytes(byte[]... byts) throws Exception {
		return ByteUtils.jogBytes(byts);
	}
}
