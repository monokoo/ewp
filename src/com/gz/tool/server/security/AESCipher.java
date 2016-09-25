package com.gz.tool.server.security;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.spec.AlgorithmParameterSpec;
import java.util.HashMap;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * <p>
 * AES加密类。
 * </p>
 * <p>
 * 说明：Advanced Encryption Standard，高级加密标准<br>
 * </p>
 */
public final class AESCipher {
	private static HashMap<String, HashMap<String, String>> mKeyIvMap = new HashMap<String, HashMap<String, String>>();
	
	public static final String CLIENT_KEY = "clientKey";
	public static final String CLIENT_IV = "clientIV";
	public static final String SERVER_KEY = "serverKey";
	public static final String SERVER_IV = "serverIV";
	public static final String CUSTOMER_KEY = "customerKey";
	public static final String CUSTOMER_IV = "customerIV";
	
	private static final String CIPHER_TRANSFORMATION = "AES/CBC/PKCS7Padding";
	private static final String CIPHER_TRANSFORMATION_NOPADDING = "AES/CBC/NOPadding";
	
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
	 * 加密。
	 * </p>
	 * 
	 * @param plantText 待加密字符串。
	 * @param key the key data.
	 * @param iv the buffer used as initialization vector.
	 * @return encrypted data.
	 * @throws Exception.
	 */
	public static String encrypt(String plantText, byte[] key, byte[] iv) throws Exception {
		byte[] plantBytes = plantText.getBytes("UTF-8");
		byte[] cipherBytes = encrypt(plantBytes, key, iv);

		return Base64.encode(cipherBytes);
	}

	/**
	 * <p>
	 * 加密。
	 * </p>
	 * 
	 * @param cipherByts 待加密字符串。
	 * @param key the key data.
	 * @param iv the buffer used as initialization vector.
	 * @return encrypted data.
	 * @throws Exception.
	 */
	public static byte[] encrypt(byte[] cipherByts, byte[] key, byte[] iv) throws Exception {
		byte[] plantByts = null;
		Cipher cipher = initCipher(key, iv, Cipher.ENCRYPT_MODE);
		plantByts = cipher.doFinal(cipherByts);
		return plantByts;
	}

	/**
	 * <p>
	 * 解密。
	 * </p>
	 * 
	 * @param cipherText 待解密字符串。
	 * @param key the key data.
	 * @param iv the buffer used as initialization vector.
	 * @return decrypted data.
	 * @throws Exception.
	 */
	public static String decrypt(String cipherText, byte[] key, byte[] iv) throws Exception {
		byte[] cipherBytes = Base64.decodeToBytes(cipherText);
		byte[] plantBytes = decrypt(cipherBytes, key, iv);

		return new String(plantBytes, "UTF-8");
	}

	/**
	 * <p>
	 * 解密。
	 * </p>
	 * 
	 * @param cipherByts 待解密字符串。
	 * @param key the key data.
	 * @param iv the buffer used as initialization vector.
	 * @return decrypted data.
	 * @throws Exception.
	 */
	public static byte[] decrypt(byte[] cipherByts, byte[] key, byte[] iv) throws Exception {
		byte[] plantByts = null;
		Cipher cipher = initCipher(key, iv, Cipher.DECRYPT_MODE);
		plantByts = cipher.doFinal(cipherByts);
		return plantByts;
	}

	/**
	 * <p>
	 * Init cipher.
	 * </p>
	 * 
	 * @param key the key data.
	 * @param iv the buffer used as initialization vector.
	 * @param opMode the operation this cipher instance should be initialized for (one of: ENCRYPT_MODE, DECRYPT_MODE, WRAP_MODE or UNWRAP_MODE).
	 * @return a cipher instance.
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeyException
	 * @throws InvalidAlgorithmParameterException
	 */
	private static Cipher initCipher(byte[] key, byte[] iv, int opMode) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
		Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
		AlgorithmParameterSpec spec = new IvParameterSpec(iv);
		SecretKey skey = new SecretKeySpec(key, "AES");
		cipher.init(opMode, skey, spec);
		return cipher;
	}
}
