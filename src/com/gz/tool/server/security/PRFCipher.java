package com.gz.tool.server.security;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

/**
 * <p>
 * PRF加密类。
 * </p>
 * <p>
 * 说明：PRF(secret, label, seed) = P_MD5(S1, label + seed) XOR P_SHA-1(S2, label + seed).<br>
 * 其中使用到的加密方法（MD5,RSA,HMac,AES,PRF）是否使用对应的国密算法替换，取决配置项{@link EMPConfig#isUseSM()}<br>
 * </p>
 */
public class PRFCipher {

	/**
	 * <p>
	 * PRF(secret, label, seed) = P_MD5(S1, label + seed) XOR P_SHA-1(S2, label + seed).
	 * </p>
	 * 
	 * @param secret the key array.
	 * @param label the label array.
	 * @param seed the seed array.
	 * @param length length of result.
	 * @param isSM weather use SM3 instead of MD5 and SHA
	 * 
	 * @return data transformed by PRF.
	 */
	public static byte[] PRF(byte[] secret, byte[] label, byte[] seed, int length) throws Exception {
		int secretLen = secret.length;
		boolean is2Times = (secretLen % 2) == 0 ? true : false;
		int splittingLen = secretLen / 2 + (is2Times ? 0 : 1);
		byte[] s1 = new byte[splittingLen];
		byte[] s2 = new byte[splittingLen];
		System.arraycopy(secret, 0, s1, 0, splittingLen);
		System.arraycopy(secret, splittingLen, s2, 0, splittingLen);
		// label + seed
		byte[] labelAndSeed = new byte[label.length + seed.length];
		System.arraycopy(label, 0, labelAndSeed, 0, label.length);
		System.arraycopy(seed, 0, labelAndSeed, label.length, seed.length);
		byte[] prfMd5;
		byte[] prfSha1;
		// PRF MD5
		prfMd5 = prfHash(s1, labelAndSeed, length, HMac.KEY_MAC_MD5);
		// PRF SHA1
		prfSha1 = prfHash(s2, labelAndSeed, length, HMac.KEY_MAC_SHA1);
		// PRF
		byte[] prf = xor(prfMd5, prfSha1);
		return prf;
	}

	/*
	 * @param s1
	 * 
	 * @param labelAndSeed
	 * 
	 * @param length
	 * 
	 * @param keyMacMode
	 * 
	 * @return
	 * 
	 * @throws Exception
	 */
	private static byte[] prfHash(byte[] s1, byte[] labelAndSeed, int length, String keyMacMode) throws Exception {
		ArrayList<byte[]> A = new ArrayList<byte[]>();
		A.add(labelAndSeed);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		/*
		 * P_hash(secret, seed) = HMAC_hash(secret, A(1) + seed) + HMAC_hash(secret, A(2) + seed) + HMAC_hash(secret, A(3) + seed) + ...
		 */
		while (out.size() < length) {
			int arrSize = A.size();
			byte[] lastA = A.get(arrSize - 1);
			// A(i) = HMAC_hash(secret, A(i-1))
			byte[] currA;
            currA = HMac.encryptHMAC(lastA, s1, keyMacMode);
			A.add(currA);
			byte[] neoSeed = ByteUtils.jogBytes(currA, labelAndSeed);
			byte[] byts;
			byts = HMac.encryptHMAC(neoSeed, s1, keyMacMode);
			out.write(byts);
		}
		out.flush();
		byte[] outByts = out.toByteArray();
		byte[] prfMd5 = new byte[length];
		System.arraycopy(outByts, 0, prfMd5, 0, prfMd5.length);
		return prfMd5;
	}

	/*
	 * @param byts1
	 * 
	 * @param byts2
	 * 
	 * @return
	 */
	private static byte[] xor(byte[] byts1, byte[] byts2) throws Exception {
		int len1 = byts1.length;
		int len2 = byts2.length;
		if (len1 != len2) {
			return null;
		}
		byte[] xor = new byte[len1];
		for (int i = 0; i < len1; i++) {
			xor[i] = (byte) (byts1[i] ^ byts2[i]);
		}
		return xor;
	}

}
