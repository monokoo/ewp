package com.gz.tool.server.security;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Random;

import javax.crypto.Cipher;

/**
 * <p>
 * RSA加密类。
 * </p>
 * <p>
 * 说明： RSA公开密钥密码体制。所谓的公开密钥密码体制就是使用不同的加密密钥与解密密钥，是一种“由已知加密密钥推导出解密密钥在计算上是不可行的”密码体制。 <br>
 * </p>
 */
public class RSACipher {

	/**
	 * <p>
	 * public key.
	 * </p>
	 */
	public static final int PUBLIC_KEY = 0;
	/**
	 * <p>
	 * private key.
	 * </p>
	 */
	public static final int PRIVATE_KEY = 1;

	/**
	 * <p>
	 * public certificate modulus file name.
	 * </p>
	 */
	public static final String PUBLIC_CER_MODULUS_FILENAME = "pubmodulus";
	/**
	 * <p>
	 * public certificate exponent file name.
	 * </p>
	 */
	public static final String PUBLIC_CER_EXPONENT_FILENAME = "pubexponent";
	/**
	 * <p>
	 * private certificate modulus file name.
	 * </p>
	 */
	public static final String PRIVATE_CER_MODULUS_FILENAME = "primodulus";
	/**
	 * <p>
	 * private certificate exponent file name.
	 * </p>
	 */
	public static final String PRIVATE_CER_EXPONENT_FILENAME = "priexponent";

	/**
	 * <p>
	 * transformation of RSA , ECB or PKCS1.
	 * </p>
	 */
	public static final String TRANSFORMATION_RSA_ECB_PKCS1 = "RSA/ECB/PKCS1Padding";
	/**
	 * <p>
	 * transformation of RSA , CBC or PKCS1.
	 * </p>
	 */
	public static final String TRANSFORMATION_RSA_CBC_PKCS1 = "RSA/CBC/PKCS1Padding";

	/**
	 * <p>
	 * 获取公钥
	 * </p>
	 * 
	 * @param modulus the modulus n.
	 * @param publicExponent the public exponent d.
	 * @return the public key.
	 * @throws Exception.
	 */
	public static PublicKey getPublicKey(String modulus, String publicExponent) throws Exception {
		BigInteger m = new BigInteger(modulus);
		BigInteger e = new BigInteger(publicExponent);
		RSAPublicKeySpec keySpec = new RSAPublicKeySpec(m, e);
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		PublicKey publicKey = keyFactory.generatePublic(keySpec);
		return publicKey;
	}

	/**
	 * <p>
	 * 获取私钥
	 * </p>
	 * 
	 * @param modulus the modulus n.
	 * @param privateExponent the private exponent e.
	 * @return the private key.
	 * @throws Exception.
	 */
	public static PrivateKey getPrivateKey(String modulus, String privateExponent) throws Exception {
		BigInteger m = new BigInteger(modulus);
		BigInteger e = new BigInteger(privateExponent);
		RSAPrivateKeySpec keySpec = new RSAPrivateKeySpec(m, e);
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
		return privateKey;
	}

	/**
	 * <p>
	 * 加密,key可以是公钥，也可以是私钥，如果是公钥加密就用私钥解密，反之亦然。
	 * </p>
	 * 
	 * @param message 待加密信息。
	 * @param key 加密时候用到的key。
	 * @return 密文。
	 * @throws Exception 普通异常。
	 * 
	 */
	public static byte[] doEncrypt(final byte[] message, final Key key, final String transformation) throws Exception {
		Cipher cipher = Cipher.getInstance(transformation);
		cipher.init(Cipher.ENCRYPT_MODE, key);
		byte[] cipherMsg = cipher.doFinal(message);
		return cipherMsg;
	}

	/**
	 * <p>
	 * 解密，key可以是公钥，也可以是私钥，如果是公钥加密就用私钥解密，反之亦然。
	 * </p>
	 * 
	 * @param message 待解密信息。
	 * @param key 解密时候用到的key。
	 * @return 明文。
	 * @throws Exception 普通异常。
	 * 
	 */
	public static byte[] doDecrypt(final byte[] message, final Key key, final String transformation) throws Exception {
		Cipher cipher = Cipher.getInstance(transformation);
		cipher.init(Cipher.DECRYPT_MODE, key);
		byte[] plainMsg = cipher.doFinal(message);
		return plainMsg;
	}

	/**
	 * <p>
	 * 通过证书字节数组得到证书
	 * </p>
	 * 
	 * @param serverCerByts
	 * @return
	 * @throws Exception
	 */
	public static final X509Certificate getCertificate(byte[] cerByts) throws CertificateException {
		ByteArrayInputStream cerIn = new ByteArrayInputStream(cerByts);
		X509Certificate cert = getCertificate(cerIn);
		return cert;
	}

	/**
	 * <p>
	 * 通过证书输入流得到证书
	 * </p>
	 * 
	 * @param streamCert stream of certificate.
	 * @return certificate data.
	 * @throws CertificateException.
	 */
	public static X509Certificate getCertificate(InputStream streamCert) throws CertificateException {
		CertificateFactory factory = CertificateFactory.getInstance("X.509");
		X509Certificate cert = (X509Certificate) factory.generateCertificate(streamCert);
		return cert;
	}

	/**
	 * <p>
	 * 由公钥来验证证书的有效性;<br>
	 * Verifies that this certificate was signed with the given public key.<br>
	 * </p>
	 * 
	 * @param cer certificate.
	 * @param pubkey the public key.
	 * @return true if success to verify.
	 */
	public static boolean verifyCertificateWithPubKey(Certificate cer, PublicKey pubkey) {
		try {
			cer.verify(pubkey);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	/**
	 * <p>
	 * 根据keyInfo产生公钥和私钥，并且保存到pk.dat和sk.dat文件中。
	 * </p>
	 * 
	 * @param context Context对象。
	 * @param keyInfo 加密种子信息。
	 * @throws Exception 普通异常。
	 */
	public static void genKeys(String keyInfo) throws Exception {
//		KeyPairGenerator keygen = KeyPairGenerator.getInstance("RSA");
//		SecureRandom random = new SecureRandom();
//		random.setSeed(keyInfo.getBytes());
//		// 初始加密，长度为1024，必须是大于1024才可以的
//		keygen.initialize(1024, random);
//		// 取得密钥对
//		KeyPair kp = keygen.generateKeyPair();
//		// 取得公钥
//		RSAPublicKey publicKey = (RSAPublicKey) kp.getPublic();
//		// 由于在4.1系统中，publicKey的实例类发生了变化，其中一个变量因为没有实现Serializable接口，而导致写入文件时失败。
//		// 所以改为直接保存publicKey中需要的信息，在将来使用的时候，通过这些信息重新生成RSAPublicKey实例。
//		String savePublicKey = publicKey.getModulus().toString() + "@" + publicKey.getPublicExponent().toString();
//		FileManager.saveFileByAD(context, savePublicKey, ClientHello.PUBLIC_KEY_PATH);
//		// 取得私钥
//		RSAPrivateKey privateKey = (RSAPrivateKey) kp.getPrivate();
//		// 基于相同的原因，privateKey做与publicKey相同处理。
//		String savePrivateKey = privateKey.getModulus().toString() + "@" + privateKey.getPrivateExponent().toString();
//		FileManager.saveFileByAD(context, savePrivateKey, ClientHello.PRIVATE_KEY_PATH);
	}

	/**
	 * <p>
	 * 用私钥签名。
	 * </p>
	 * 
	 * @param message 待签名信息。
	 * @param key 私钥。
	 * @return 签名结果。
	 * @throws Exception 普通异常。
	 * 
	 */
	public static byte[] sign(byte[] message, PrivateKey key) throws Exception {
		Signature signetcheck = Signature.getInstance("MD5withRSA");
		signetcheck.initSign(key);
		signetcheck.update(message);
		return signetcheck.sign();
	}

	/**
	 * <p>
	 * 用公钥验证签名的正确性。
	 * </p>
	 * 
	 * @param message the string to update with.
	 * @param signBytes the signature to verify.
	 * @param key the public key.
	 * @return true if the signature was verified, false otherwise.
	 * @throws Exception.
	 */
	public static boolean verifySign(byte[] message, byte[] signBytes, PublicKey key) throws Exception {
		if (message == null || signBytes == null || key == null) {
			return false;
		}
		Signature signetcheck = Signature.getInstance("MD5withRSA");
		signetcheck.initVerify(key);
		signetcheck.update(message);
		return signetcheck.verify(signBytes);
	}

	/**
	 * <p>
	 * 用私钥签名。
	 * </p>
	 * 
	 * @param message 待签名信息。
	 * @param key 私钥。
	 * @return 签名结果。
	 * @throws Exception 普通异常。
	 * 
	 */
	public byte[] sign(String message, PrivateKey key) throws Exception {
		byte[] messageBytes = message.getBytes("ISO-8859-1");
		return sign(messageBytes, key);
	}

	/**
	 * <p>
	 * 用公钥验证签名的正确性。
	 * </p>
	 * 
	 * @param message the string to update with.
	 * @param signStr the signature to verify.
	 * @param key the public key.
	 * @return true if the signature was verified, false otherwise.
	 * @throws Exception.
	 * 
	 * @deprecated
	 */
	public boolean verifySign(String message, String signStr, PublicKey key) throws Exception {
		byte[] messageBytes = message.getBytes("ISO-8859-1");
		return verifySign(messageBytes, signStr.getBytes("UTF-8"), key);
	}

	/********************************************* 下面方法被废弃 *******************************************************/

	/**
	 * @deprecated {@link ByteUtils#byteArrayToHexString(b)}
	 */
	public static String toHexString(byte[] b) {
		return ByteUtils.byteArrayToHexString(b);
	}

	/**
	 * <p>
	 * encrypt with certificate.
	 * </p>
	 * 
	 * @param paintText source byte array to encrypt.
	 * @param transformation the name of the transformation to create a cipher for.
	 * @return encrypted the final bytes from the transformation.
	 * @throws Exception
	 * 
	 * @deprecated
	 */
	public static String doEncryptWithCertUserArr(byte[] paintText, String transformation) throws Exception {
		RSAPublicKey rsaPubKey = getPublicKey();
		String cipherText = doEncryptWithBase64(paintText, rsaPubKey, transformation);
		return cipherText;
	}

	/**
	 * <p>
	 * encrypt with BASE64.
	 * </p>
	 * 
	 * @param paintText source byte array to encrypt.
	 * @param rsaPubKey a RSA public key.
	 * @param transformation the name of the transformation to create a cipher for.
	 * @return the final bytes from the transformation.
	 * @throws Exception.
	 * 
	 * @deprecated
	 */
	public static String doEncryptWithBase64(byte[] paintText, RSAPublicKey rsaPubKey, String transformation) throws Exception {
		String cipherText = null;
		try {
			byte[] original = doEncrypt(paintText, rsaPubKey, transformation);
			String enData = Base64.encode(original);
			cipherText = removeRN(enData);
		} catch (Exception ex) {
			throw new Exception("RSA加密出出错！");
		}
		return cipherText;
	}

	/**
	 * @deprecated
	 */
	final static byte[] modulus = new byte[] { (byte) 0x00, (byte) 0xb3, (byte) 0x2e, (byte) 0x64, (byte) 0xfd, (byte) 0x74, (byte) 0x76, (byte) 0x2a, (byte) 0x28, (byte) 0x40, (byte) 0x4b,
			(byte) 0x6f, (byte) 0xdb, (byte) 0x2a, (byte) 0xd9, (byte) 0x2d, (byte) 0x10, (byte) 0x98, (byte) 0xf8, (byte) 0x2a, (byte) 0x3e, (byte) 0x27, (byte) 0xdb, (byte) 0x80, (byte) 0xf5,
			(byte) 0x26, (byte) 0xf0, (byte) 0x13, (byte) 0x67, (byte) 0xb4, (byte) 0xd9, (byte) 0x72, (byte) 0xa1, (byte) 0xc6, (byte) 0x9b, (byte) 0x5e, (byte) 0x97, (byte) 0x7c, (byte) 0x5d,
			(byte) 0xee, (byte) 0xc8, (byte) 0xb5, (byte) 0x46, (byte) 0x07, (byte) 0xf8, (byte) 0x7a, (byte) 0x3e, (byte) 0x10, (byte) 0x38, (byte) 0x4d, (byte) 0xad, (byte) 0x17, (byte) 0xf4,
			(byte) 0x36, (byte) 0xd9, (byte) 0xdb, (byte) 0xaf, (byte) 0xea, (byte) 0x0d, (byte) 0xac, (byte) 0x7c, (byte) 0x7f, (byte) 0xb3, (byte) 0xf3, (byte) 0x5c, (byte) 0x96, (byte) 0x00,
			(byte) 0x58, (byte) 0xa4, (byte) 0x89, (byte) 0xa8, (byte) 0x85, (byte) 0xdd, (byte) 0xa9, (byte) 0x8e, (byte) 0xb9, (byte) 0xab, (byte) 0x57, (byte) 0xa2, (byte) 0x8c, (byte) 0x73,
			(byte) 0x0b, (byte) 0x2c, (byte) 0xda, (byte) 0xe1, (byte) 0xb3, (byte) 0x1e, (byte) 0xd5, (byte) 0x45, (byte) 0xf8, (byte) 0xe9, (byte) 0x3b, (byte) 0x02, (byte) 0x97, (byte) 0x46,
			(byte) 0xcf, (byte) 0xd2, (byte) 0xa3, (byte) 0x48, (byte) 0xff, (byte) 0xff, (byte) 0x79, (byte) 0xd6, (byte) 0x7f, (byte) 0x02, (byte) 0x61, (byte) 0x38, (byte) 0x75, (byte) 0xf7,
			(byte) 0xfd, (byte) 0xea, (byte) 0x86, (byte) 0xec, (byte) 0xf9, (byte) 0xe1, (byte) 0xaa, (byte) 0xa6, (byte) 0xec, (byte) 0xd0, (byte) 0xfe, (byte) 0x3d, (byte) 0xb2, (byte) 0x72,
			(byte) 0x27, (byte) 0x4b, (byte) 0xec, (byte) 0x87, (byte) 0x45, (byte) 0x49 };
	/**
	 * @deprecated
	 */
	final static String exponent = "65537";

	/**
	 * <p>
	 * get the public key.
	 * </p>
	 * 
	 * @return RSA pubKey.
	 * @throws NoSuchAlgorithmException.
	 * @throws Exception.
	 * @throws InvalidKeySpecException.
	 * 
	 * @deprecated
	 */
	public static RSAPublicKey getPublicKey() throws NoSuchAlgorithmException, Exception, InvalidKeySpecException {
		KeyFactory keyFac = KeyFactory.getInstance("RSA");
		BigInteger mypubkey_modulus = new BigInteger(modulus);
		BigInteger mypubkey_exponent = new BigInteger(exponent);
		RSAPublicKeySpec pubKey = new RSAPublicKeySpec(mypubkey_modulus, mypubkey_exponent);
		RSAPublicKey rsaPubKey = (RSAPublicKey) keyFac.generatePublic(pubKey);
		return rsaPubKey;
	}

	/**
	 * <p>
	 * 加密。
	 * </p>
	 * 
	 * @param key 加密的密钥。
	 * @param data 待加密的明文数据。
	 * @return 加密后的数据。
	 * @throws Exception 普通异常。
	 * 
	 * @deprecated
	 */
	public static byte[] encrypt(Key key, byte[] data) throws Exception {
		try {
			Cipher cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.ENCRYPT_MODE, key);
			int blockSize = cipher.getBlockSize();
			int outputSize = cipher.getOutputSize(data.length);// 获得加密块加密后块大小
			int leavedSize = data.length % blockSize;
			int blocksSize = leavedSize != 0 ? data.length / blockSize + 1 : data.length / blockSize;
			byte[] raw = new byte[outputSize * blocksSize];
			int i = 0;
			while (data.length - i * blockSize > 0) {
				if (data.length - i * blockSize > blockSize)
					cipher.doFinal(data, i * blockSize, blockSize, raw, i * outputSize);
				else
					cipher.doFinal(data, i * blockSize, data.length - i * blockSize, raw, i * outputSize);
				// 这里面doUpdate方法不可用，查看源代码后发现每次doUpdate后并没有什么实际动作除了把byte[]放到ByteArrayOutputStream中，而最后doFinal的时候才将所有的byte[]进行加密，可是到了此时加密块大小很可能已经超出了OutputSize所以只好用dofinal方法。
				i++;
			}
			return raw;
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
	}

	/**
	 * <p>
	 * get envelope key.
	 * </p>
	 * 
	 * @param userCode the user code.
	 * @return generated key string.
	 * @throws Exception.
	 * 
	 * @deprecated
	 */
	public static byte[] genEnvelopeKey(String userCode) throws Exception {
		byte[] bytes = null;
		try {
			bytes = new byte[8];
			Random rd = new Random();
			long time = System.currentTimeMillis();
			rd.setSeed(time);
			// the value of first byte is between 128 and 255.
			int key = 129 + Math.abs(rd.nextInt()) % 128;
			bytes[0] = (byte) key;

			int cnt = 7;
			do {
				int tmp = Math.abs(rd.nextInt()) % 255;
				bytes[8 - cnt] = (byte) tmp;
				cnt--;
			} while (cnt > 0);

			// there should NOT '=' at the end of the string, so remove '='.
			bytes = Base64.encode(bytes).substring(0, 8).getBytes("UTF-8");

			return bytes;
			/*
			 * String str = Integer.toString(rd.nextInt()) + Long.toString(time) + userCode; String hash = LPUtils.sha1(str); LPUtils.printOutToConsole("hash-->" + hash); return hash.substring(0, 8);
			 */

		} catch (Exception ex) {
			throw new Exception("创建密钥出错!");
		}
	}

	/*
	 * remove all "\r\n"
	 * 
	 * @param str
	 * 
	 * @return
	 * 
	 * @throws Exception
	 */
	private static String removeRN(String str) throws Exception {
		if (str == null)
			return null;
		int start = 0;
		int len = str.length();
		int id = str.indexOf("\r\n");
		StringBuffer sb = new StringBuffer();
		while (id != -1 && start < len) {
			sb.append(str.substring(start, id));
			start = id + 2;
			id = str.indexOf("\r\n", start);
		}
		if (start < len)
			sb.append(str.substring(start));
		return sb.toString();
	}

	/**
	 * <p>
	 * do the verify.
	 * </p>
	 * 
	 * @param originData origin data.
	 * @param signedData signed data.
	 * @return verify infomation.
	 * @throws Exception.
	 * 
	 * @deprecated
	 */
	public static boolean verifySign(byte[] originData, byte[] signedData) throws Exception {
		String cerPath = "test.cer";
		Certificate cert = getCertificate(cerPath);
		Signature signature = null;
		signature = Signature.getInstance("SHA1withDSA");
		signature.initVerify(cert.getPublicKey());
		signature.update(originData);
		return signature.verify(signedData);
	}

	/**
	 * <p>
	 * create key pair.
	 * </p>
	 * 
	 * @return RSA key.
	 * @throws NoSuchAlgorithmException.
	 * 
	 * @deprecated
	 */
	public static byte[][] createKeyPair() throws NoSuchAlgorithmException {
		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
		keyPairGenerator.initialize(1024);
		KeyPair keyPair = keyPairGenerator.genKeyPair();
		byte[] publicKeyBytes = keyPair.getPublic().getEncoded();
		byte[] privateKeyBytes = keyPair.getPrivate().getEncoded();
		byte[][] keys = new byte[2][];
		keys[PUBLIC_KEY] = publicKeyBytes;
		keys[PRIVATE_KEY] = privateKeyBytes;
		return keys;
	}

	/**
	 * <p>
	 * Create KeyPair.
	 * </p>
	 * 
	 * @return KeyPair.
	 * @throws Exception.
	 * 
	 * @deprecated
	 */
	public static KeyPair generateKeyPair() throws Exception {
		try {
			KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
			final int KEY_SIZE = 1024;
			keyPairGen.initialize(KEY_SIZE, new SecureRandom());
			KeyPair keyPair = keyPairGen.genKeyPair();
			return keyPair;
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
	}

	/**
	 * <p>
	 * Create Certificate File.
	 * </p>
	 * 
	 * @param byts target byte array, used to writting result.
	 * @param cerFile path of certificate file.
	 * @throws IOException.
	 * 
	 * @deprecated
	 */
	public static void genCerFile(byte byts[], String cerFilePath) throws FileNotFoundException, IOException {
		FileOutputStream out = new FileOutputStream(cerFilePath);
		out.write(byts);
		out.flush();
		out.close();
	}

	/**
	 * <p>
	 * transform string to byte array.
	 * </p>
	 * 
	 * @param s source string.
	 * @return a byte array.
	 * 
	 * @deprecated 移到工具类
	 */
	public static final byte[] toBytes(String s) {
		byte[] bytes;
		bytes = new byte[s.length() >> 1];
		for (int i = 0; i < bytes.length; i++) {
			bytes[i] = (byte) Integer.parseInt(s.substring(i << 1, (i << 1) + 2), 16);
		}
		return bytes;
	}

	/**
	 * <p>
	 * create certificate file.
	 * </p>
	 * 
	 * @param cerFile path of certificate file.
	 * @return certificate content.
	 * @throws FileNotFoundException.
	 * 
	 * @deprecated 该方法实际就是传入文件路径得到byte数组，FileManager中有相同功能方法
	 */
	public static byte[] readCerFile(String cerFile) throws FileNotFoundException, IOException {
		@SuppressWarnings("resource")
		FileInputStream in = new FileInputStream(cerFile);
		int size = in.available();
		byte[] cerByts = new byte[size];
		in.read(cerByts);
		return cerByts;
	}

	/**
	 * <p>
	 * 通过证书路径得到证书
	 * </p>
	 * 
	 * @param cerPath certificate path.
	 * @return certificate data.
	 * @throws FileNotFoundException.
	 * @throws CertificateException.
	 * 
	 * @deprecated
	 */
	public static Certificate getCertificate(String cerPath) throws FileNotFoundException, CertificateException {
		InputStream streamCert = new java.io.FileInputStream(cerPath);
		Certificate cert = getCertificate(streamCert);
		return cert;
	}

	/**
	 * <p>
	 * Create RSA PublicKey.
	 * </p>
	 * 
	 * @param modulus byte array of modulus.
	 * @param publicExponent byte array of public exponent.
	 * @return RSAPublicKey.
	 * @throws Exception.
	 * 
	 * @deprecated
	 */
	public static RSAPublicKey generateRSAPublicKey(byte[] modulus, byte[] publicExponent) throws Exception {
		KeyFactory keyFac = null;
		try {
			keyFac = KeyFactory.getInstance("RSA");
		} catch (NoSuchAlgorithmException ex) {
			throw new Exception(ex.getMessage());
		}
		RSAPublicKeySpec pubKeySpec = new RSAPublicKeySpec(new BigInteger(modulus), new BigInteger(publicExponent));
		try {
			return (RSAPublicKey) keyFac.generatePublic(pubKeySpec);
		} catch (InvalidKeySpecException ex) {
			throw new Exception(ex.getMessage());
		}
	}

	/**
	 * <p>
	 * Create RSA PrivateKey.
	 * </p>
	 * 
	 * @param modulus byte array of modulus.
	 * @param privateExponent byte array of private exponent.
	 * @return RSAPrivateKey.
	 * @throws Exception.
	 * 
	 * @deprecated
	 */
	public static RSAPrivateKey generateRSAPrivateKey(byte[] modulus, byte[] privateExponent) throws Exception {
		KeyFactory keyFac = null;
		try {
			keyFac = KeyFactory.getInstance("RSA");
		} catch (NoSuchAlgorithmException ex) {
			throw new Exception(ex.getMessage());
		}
		RSAPrivateKeySpec priKeySpec = new RSAPrivateKeySpec(new BigInteger(modulus), new BigInteger(privateExponent));
		try {
			return (RSAPrivateKey) keyFac.generatePrivate(priKeySpec);
		} catch (InvalidKeySpecException ex) {
			throw new Exception(ex.getMessage());
		}
	}

	/**
	 * <p>
	 * 加密,key可以是公钥，也可以是私钥。
	 * </p>
	 * 
	 * @param message 待加密信息。
	 * @param key 加密时候用到的key。
	 * @return 密文。
	 * @throws Exception 普通异常。
	 * 
	 * @deprecated {@link #doEncrypt(byte[], key, String)}
	 */
	public byte[] encrypt(String message, Key key) throws Exception {
		Cipher cipher = Cipher.getInstance(TRANSFORMATION_RSA_ECB_PKCS1);
		cipher.init(Cipher.ENCRYPT_MODE, key);
		return cipher.doFinal(ByteUtils.toBytes(message));
		// return cipher.doFinal(message.getBytes());
	}

	/**
	 * <p>
	 * 加密,key可以是公钥，也可以是私钥。
	 * </p>
	 * 
	 * @param message 待加密信息。
	 * @param key 加密时候用到的key。
	 * @return 密文。
	 * @throws Exception 普通异常。
	 * 
	 * @deprecated {@link #doEncrypt(byte[], key, String)}
	 */
	public byte[] encrypt(byte[] message, Key key) throws Exception {
		Cipher cipher = Cipher.getInstance(TRANSFORMATION_RSA_ECB_PKCS1);
		cipher.init(Cipher.ENCRYPT_MODE, key);
		return cipher.doFinal(message);
	}

	/**
	 * <p>
	 * 解密，key可以是公钥，也可以是私钥，如果是公钥加密就用私钥解密，反之亦然。
	 * </p>
	 * 
	 * @param message 待解密信息。
	 * @param key 解密时候用到的key。
	 * @return 明文。
	 * @throws Exception 普通异常。
	 * 
	 * @deprecated {@link #doEncrypt(byte[], key, String)}
	 */
	public byte[] decrypt(byte[] message, Key key) throws Exception {
		Cipher cipher = Cipher.getInstance(TRANSFORMATION_RSA_ECB_PKCS1);
		cipher.init(Cipher.DECRYPT_MODE, key);
		return cipher.doFinal(message);
	}

	/**
	 * <p>
	 * 解密，key可以是公钥，也可以是私钥，如果是公钥加密就用私钥解密，反之亦然。
	 * </p>
	 * 
	 * @param message 待解密信息。
	 * @param key 解密时候用到的key。
	 * @return 明文。
	 * @throws Exception 普通异常。
	 * 
	 * @deprecated {{@link #doEncrypt(byte[], key, String)}
	 */
	public byte[] decrypt(String message, Key key) throws Exception {
		Cipher cipher = Cipher.getInstance(TRANSFORMATION_RSA_ECB_PKCS1);
		cipher.init(Cipher.DECRYPT_MODE, key);
		return cipher.doFinal(toBytes(message));
	}

	/**
	 * <p>
	 * 该方法名字不合适，请使用{@link #verifyCertificateWithPubKey(Certificate, PublicKey)}<br>
	 * 验证证书的有效性; <br>
	 * Verifies that this certificate was signed with the given public key.<br>
	 * </p>
	 * 
	 * @param cer certificate.
	 * @param pubkey the public key.
	 * @return true if success to verify.
	 * 
	 * @deprecated {@link #verifyCertificateWithPubKey(Certificate, PublicKey)}
	 */
	public static boolean verifySign(Certificate cer, PublicKey pubkey) {
		try {
			cer.verify(pubkey);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

}
