package com.gz.tool.tls;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.HashMap;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import com.gz.tool.server.security.AESCipher;
import com.gz.tool.server.security.Base64;
import com.gz.tool.server.security.Constant;
import com.gz.tool.server.security.HMac;
import com.gz.tool.server.security.PRFCipher;
import com.gz.tool.server.security.RSACipher;
import com.gz.tool.server.tool.Utils;

import javax.servlet.http.HttpServletResponse;

public class TLS {
	private static HashMap<String, HashMap<String, String>> hm_ = new HashMap<String, HashMap<String, String>>();
	
	private final String TLS_INTERFACE_CLIENT_HELLO = "/user/hello";
	private final String TLS_INTERFACE_KEY_EXCHANGE = "/user/exchange";
	private final String TLS_INTERFACE_FACILITY_URL = "/user/handshake";
	
	private final String CLIENT_HELLO_BODY = "clientHelloBody";
	private final String SERVER_HELLO_BODY = "serverHelloBody";
	private final String CLIENT_KEY_EX_CHANGE_BODY = "clientKeyExChangeBody";
	private final String SERVER_KEY_EX_CHANGE_BODY = "serverKeyExChangeBody";
	
	private final String PMS = "pms";
	private final String RNC = "rnc";
	private final String RNS = "rns";
	private final String MS = "ms";
	private final String MS2 = "ms2";
	public static final String TLS_VERSION = "tlsVersion";
	
	public static float TLS_VERSION_1_0 = 1.0f;
    public static float TLS_VERSION_1_1 = 1.1f;
    public static float TLS_VERSION_1_2 = 1.2f;
    public static float TLS_VERSION_1_3 = 1.3f;
    public static float TLS_VERSION_1_4 = 1.4f;

	private ServletRequest request_;
	
	public void run(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		try {
			request_ = request;
			// TODO Auto-generated method stub
			String _bodyBuffer = "";
			//读取body流
	        ServletInputStream sis = request.getInputStream();
			byte[] buffer = new byte[1024];
			while (sis.read(buffer, 0, 1024) != -1) {
				_bodyBuffer += new String(buffer, "utf-8");
			}
			byte[] requestData = Base64.decodeToBytes(_bodyBuffer);
			
			String uri = ((HttpServletRequest)request).getRequestURI();
			System.out.println("TLSFilter-RUN======"+uri);
			if (uri.contains(TLS_INTERFACE_CLIENT_HELLO)) {	
				//处理客户端的hello请求
				handleFullClientHelloRequest(requestData);
				
				byte[] fullServerHelloBody = sendFullServerHello();
				System.out.println("TLS-Hello-response======"+fullServerHelloBody.length);
				((HttpServletResponse)response).addHeader("Content-Type", "application/octet-stream");
				response.getOutputStream().write(fullServerHelloBody);
			} else if (uri.contains(TLS_INTERFACE_KEY_EXCHANGE)) {
				handleFullClientKeyExchangeRequest(requestData);
				byte[] fullServerKeyExchangeRequest = sendFullServerKeyExchangeRequest();
				
				clearInfo();
				
				((HttpServletResponse)response).addHeader("Content-Type", "application/octet-stream");
				response.getOutputStream().write(fullServerKeyExchangeRequest);
			} else if (uri.contains(TLS_INTERFACE_FACILITY_URL)) {
				byte[] byt = handleSimpleClientHelloAndKeyExchangeResponse(requestData);
				
				clearInfo();
				
				((HttpServletResponse)response).addHeader("Content-Type", "application/octet-stream");
				response.getOutputStream().write(byt);
			}
		} catch (Exception ev) {
			ev.printStackTrace();
		}
	}
	
	
	
	private byte[] handleSimpleClientHelloAndKeyExchangeResponse(byte[] requestData) throws Exception {
		int offset = handleFullClientHelloRequest(requestData);
		//client key exchange.
        offset = handlerClientKeyExchange(requestData, offset);
        
        // ChangeCipherSpec
		offset = handleChangeCipherSpec(requestData, offset);
		
		// write 【ServerKeyExchange】【ChangeCipherSpec】 into mServerKeyExchangeBody.
		byte[] clientHelloBody = new byte[offset];
		System.arraycopy(requestData, 0, clientHelloBody, 0, offset);
		saveInfo(CLIENT_HELLO_BODY, clientHelloBody);
		
		// Finished
		offset = handleFinish(requestData, offset);
		
		clientHelloBody = new byte[offset];
		System.arraycopy(requestData, 0, clientHelloBody, 0, offset);
		saveInfo(CLIENT_HELLO_BODY, clientHelloBody);
		
		if(offset==-1) {
			return null;
		}
		
		byte[] fullServerHelloBody = sendFullServerHello();
		removeInfo(SERVER_HELLO_BODY);
		
		//keyEXchange
		byte[] fullServerKeyExchangeRequest = serverKeyExchangeRequest();
		
		byte[] changeCipherSpecType = changeCipherSpecType();
		
		byte[] serverExchangeBody = Utils.joinBytes(fullServerHelloBody, fullServerKeyExchangeRequest, changeCipherSpecType);
		saveInfo(SERVER_KEY_EX_CHANGE_BODY, serverExchangeBody);
		
		byte[] finishData = getFinish();
		
		byte[] initContentAndHashRes = initContentAndHashRes();
		
		fullServerKeyExchangeRequest = Utils.joinBytes(fullServerKeyExchangeRequest, changeCipherSpecType, finishData, initContentAndHashRes);
		
		return Utils.joinBytes(fullServerHelloBody, fullServerKeyExchangeRequest);
	}
	
	private byte[] serverKeyExchangeRequest() throws Exception {
		//server key exchange.
		byte[] messageType = new byte[1];
		messageType[0] = Constant.HandshakeType[Constant.htIndex.server_key_exchange.ordinal()];
		
		// serverRandom
		byte[] clientGmtUnixTime = Utils.getClientGMTUnixTime();
		byte[] clientRandom = Utils.getServerRandom(28);
		byte[] mRNS2_ = Utils.joinBytes(clientGmtUnixTime, clientRandom);
		//server version
		byte[] mPMS2_ = getPreMasterSecret();
		
		byte[] origByts = Utils.joinBytes(mRNS2_, mPMS2_);
		byte[] hmacSha1 = getHMacSha1(getInfo(MS), origByts);
		
		byte[] serverKeyExchangeData = Utils.joinBytes(mRNS2_, mPMS2_, hmacSha1);
		
		// create Master Secret
		byte[] msSeed = Utils.joinBytes(getInfo(RNC), getInfo(RNS));
		byte[] ms2 = createMS2(mPMS2_, msSeed);
		getFinalKey(ms2, msSeed);
		saveInfo(MS2, ms2);
		
		byte[] key = getAESKey(getInfo(MS));
		byte[] iv = getAESIv(getInfo(MS));
		byte[] ecryptedServerKeyExchangeData = AESCipher.encrypt(serverKeyExchangeData, key, iv);
		int mLen = ecryptedServerKeyExchangeData.length;
		byte[] messageLength = Utils.intToByteArrayInNBO(mLen);
		
		byte[] fullServerKeyExchangeRequest = Utils.joinBytes(messageType, messageLength, ecryptedServerKeyExchangeData);
		
		return fullServerKeyExchangeRequest;
	}
	
	private byte[] changeCipherSpecType() throws Exception {
		byte[] changeCipherSpecType = new byte[1];
		changeCipherSpecType[0] = Constant.HandshakeType[Constant.htIndex.ChangeCipherSpec.ordinal()];
		// Cipher Suite
		byte[] cipherSuiteInfor = Constant.TLS_RSA_WITH_AES_256_CBC_SHA;
		int mLen = cipherSuiteInfor.length;
		byte[] messageLength = Utils.intToByteArrayInNBO(mLen);
		
		return Utils.joinBytes(changeCipherSpecType, messageLength, cipherSuiteInfor);
	}
	
	private byte[] sendFullServerKeyExchangeRequest() throws Exception {
		byte[] fullServerKeyExchangeRequest = serverKeyExchangeRequest();
		
		byte[] changeCipherSpecType = changeCipherSpecType();
		
		byte[] serverExchangeBody_ = Utils.joinBytes(fullServerKeyExchangeRequest, changeCipherSpecType);
		saveInfo(SERVER_KEY_EX_CHANGE_BODY, serverExchangeBody_);
		
		byte[] finishData = getFinish();
		
		byte[] initContentAndHashRes = initContentAndHashRes();
		
		fullServerKeyExchangeRequest = Utils.joinBytes(fullServerKeyExchangeRequest, changeCipherSpecType, finishData, initContentAndHashRes);
		
		return fullServerKeyExchangeRequest;
	}
	
	/**
	 * 
	 * @param ms2
	 * @param ms2RncRnsSeed
	 * 
	 * @throws Exception
	 */
	private final void getFinalKey(byte[] ms2, byte[] ms2RncRnsSeed) throws Exception {
		int totalen = 136;
		byte[] allSecret = PRFCipher.PRF(ms2, HMac.TLS_MD_CLIENT_SERVER_KEYIVMAC_CONST(), 
				ms2RncRnsSeed, totalen);
		byte[] clientKeySet = new byte[totalen / 2];
		System.arraycopy(allSecret, 0, clientKeySet, 0, totalen / 2);
		byte[] serverKeySet = new byte[totalen / 2];
		System.arraycopy(allSecret, totalen / 2, serverKeySet, 0, totalen / 2);
		// save key.
		AESCipher.setInfoBySessionIdAndKey(getSessionID(request_), AESCipher.CLIENT_KEY, getAESKey(clientKeySet));
		AESCipher.setInfoBySessionIdAndKey(getSessionID(request_), AESCipher.CLIENT_IV, getAESIv(clientKeySet));
		AESCipher.setInfoBySessionIdAndKey(getSessionID(request_), AESCipher.SERVER_KEY, getAESKey(serverKeySet));
		AESCipher.setInfoBySessionIdAndKey(getSessionID(request_), AESCipher.SERVER_IV, getAESIv(serverKeySet));
		// hmac key
		HMac.setInfoBySessionIdAndKey(getSessionID(request_), HMac.HMAC_CLIENT_KEY, getHMacKey(clientKeySet));
		HMac.setInfoBySessionIdAndKey(getSessionID(request_), HMac.HMAC_SERVER_KEY, getHMacKey(serverKeySet));
	}
	
	private byte[] initContentAndHashRes() throws Exception {
		byte[] messageType = new byte[1];
		messageType[0] = Constant.HandshakeType[Constant.htIndex.InitContent.ordinal()];
		
		byte[] initData = Utils.readFileByBytes(Utils.getWebRoot()+"initcontent.jsp");
		byte[] serverKey = AESCipher.getInfoBySessionIdAndKey(getSessionID(request_), AESCipher.SERVER_KEY);
		byte[] serverIV = AESCipher.getInfoBySessionIdAndKey(getSessionID(request_), AESCipher.SERVER_IV);
		byte[] ecryptedInitData = AESCipher.encrypt(initData, serverKey, serverIV);
		byte[] messageLength = Utils.intToByteArrayInNBO(ecryptedInitData.length);
		
		byte[] messageType2 = new byte[1];
		messageType2[0] = Constant.HandshakeType[Constant.htIndex.ResHashMessageType.ordinal()];
		
		byte[] hashRes = "".getBytes();
		byte[] messageLength2 = Utils.intToByteArrayInNBO(hashRes.length);
		
		return Utils.joinBytes(messageType, messageLength, ecryptedInitData, messageType2, messageLength2, hashRes);
	}
	
	private byte[] getFinish() throws Exception {
		
		byte[] messageType = new byte[1];
		messageType[0] = Constant.HandshakeType[Constant.htIndex.finished.ordinal()];
		
		// get label
		byte[] label = HMac.TLS_MD_SERVER_FINISH_CONST();
		
		byte[] byts = getInfo(CLIENT_HELLO_BODY);;
		byte[] serverHelloBody = getInfo(SERVER_HELLO_BODY);
		if (serverHelloBody!=null) {
			byts = Utils.joinBytes(byts, serverHelloBody);
		}
		
		byte[] clientKeyExChangeBody = getInfo(CLIENT_KEY_EX_CHANGE_BODY);
		if (clientKeyExChangeBody!=null) {
			byts = Utils.joinBytes(byts, clientKeyExChangeBody);
		}
		
		byte[] serverKeyExChangeBody = getInfo(SERVER_KEY_EX_CHANGE_BODY);
		if (serverKeyExChangeBody!=null) {
			byts = Utils.joinBytes(byts, serverKeyExChangeBody);
		}
		byte[] encryptedMd5 = HMac.MD5(byts);
		byte[] encryptedSha1 = HMac.SHA1(byts);
		byte[] seed = Utils.joinBytes(encryptedMd5, encryptedSha1);
		
		// calculate VerifyData
		byte[] verifyData = PRFCipher.PRF(getInfo(MS2), label, seed, 12);
		byte[] messageLength = Utils.intToByteArrayInNBO(verifyData.length);
		
		return Utils.joinBytes(messageType, messageLength, verifyData);
	}
	
	/**
	 * 
	 * @param pms2
	 * @param ms2RncRnsSeed
	 * 
	 * @return
	 * 
	 * @throws Exception
	 */
	private final byte[] createMS2(byte[] pms2, byte[] ms2RncRnsSeed) throws Exception {
		byte[] ms2Label = HMac.TLS_MD_MASTER_SECRET2_CONST();
		byte[] ms2 = createMasterSecret(pms2, ms2Label, ms2RncRnsSeed, 48);
		return ms2;
	}
	
	/**
	 * 
	 * @param secret
	 * @param origByts
	 * @param hmacSha1
	 * 
	 * @throws Exception
	 */
	private final byte[] getHMacSha1(byte[] secret, byte[] origByts) throws Exception {
		byte[] hmacKey = getHMacKey(secret);
		byte[] encrypt = HMac.encryptHMAC(origByts, hmacKey, HMac.KEY_MAC_SHA1);
		if (encrypt == null) {
//			String msg = EMPTips.getTLSVerifyHMacFail();
//			String errorCode = EMPTips.getErrorCode();
//			if (!Utils.isEmpty(errorCode)) {
//				msg += errorCode + "9020";
//			}
//			throw new TlsException(msg);
		}
		return encrypt;
	}
	
	/**
	 * 
	 * @param secret
	 * 
	 * @return
	 * 
	 * @throws Exception
	 */
	private byte[] getHMacKey(final byte[] secret) throws Exception {
		// Hmac_SM3[32] Hmac_Sha1[20]
		int len = 20;
		int offset = 48;
		byte[] hmacKey = new byte[len];
		System.arraycopy(secret, offset, hmacKey, 0, len);
		return hmacKey;
	}
	
	/**
	 * 
	 * @return
	 * 
	 * @throws Exception
	 */
	private final byte[] getPreMasterSecret() throws Exception {
		byte[] protocolVersion = getServerProtocolVersion();
		byte[] clientRandom = Utils.getServerRandom(46);
		byte[] PreMasterSecret = Utils.joinBytes(protocolVersion, clientRandom);
		return PreMasterSecret;
	}
	
	private int handlerClientKeyExchange(byte[] requestData, int offset) throws Exception {
		//type
		byte messageType = requestData[offset];
		offset += 1;
		
		// Message Length
		byte[] mlByts = getMessageLength(requestData, offset);
		offset += 4;
		int messageLength = Utils.byteArrayToIntInNBO(mlByts, 0);
		byte[] encryptedClientKeyExchange = null;
		if (messageType == Constant.HandshakeType[Constant.htIndex.client_key_exchange.ordinal()]) {
			encryptedClientKeyExchange = new byte[messageLength];
			System.arraycopy(requestData, offset, encryptedClientKeyExchange, 0, messageLength);
			handleClientKeyExchange(encryptedClientKeyExchange);
			offset += messageLength;
		}
		
		return offset;
	}
	
	private RSAPrivateKey getRSAPrivateKey() throws Exception {
		//openssl pkcs8 -topk8 -inform PEM -outform DER -in Key.pem -out key.key -nocrypt
		File file = new File(Utils.getCertificatePath()+"key.key");     //keyfile key文件的地址
		FileInputStream in = new FileInputStream(file);
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		byte[] tmpbuf = new byte[1024];
		int count = 0;
		while ((count = in.read(tmpbuf)) != -1) {
		bout.write(tmpbuf, 0, count);
		tmpbuf = new byte[1024];
		}
		in.close();
		
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(bout.toByteArray());
		RSAPrivateKey privateKey = (RSAPrivateKey)keyFactory.generatePrivate(privateKeySpec);
		
		return privateKey;
	}
	/**
	 * 
	 * @param encryptedServerKeyExchange
	 * 
	 * @throws Exception
	 */
	private final void handleClientKeyExchange(final byte[] encryptedServerKeyExchange) throws Exception {
		byte[] decryptKeyExData = RSACipher.doDecrypt(encryptedServerKeyExchange, getRSAPrivateKey(), RSACipher.TRANSFORMATION_RSA_ECB_PKCS1);
		int offset = 0;
		byte[] pms = new byte[48];
		System.arraycopy(decryptKeyExData, offset, pms, 0, 48);
		offset += 48;
		
		saveInfo(PMS, pms);
		
		byte[] rns = new byte[32];
		System.arraycopy(decryptKeyExData, offset, rns, 0, 32);
		offset += 32;
		saveInfo(RNS, rns);
	}
	
	/**
	 * 
	 * @param secret
	 * @param label
	 * @param seed
	 * @param len
	 * 
	 * @return
	 * 
	 * @throws Exception
	 */
	private final byte[] createMasterSecret(byte[] secret, byte[] label, byte[] seed, int len) 
			throws Exception {
		byte[] ms = PRFCipher.PRF(secret, label, seed, len);
		return ms;
	}
	
	private int handleFullClientKeyExchangeRequest(byte[] requestData) {
		return handleFullClientKeyExchangeRequest(0, requestData);
	}
	
	private int handleFullClientKeyExchangeRequest(int offset, byte[] requestData) {
		try {
			//client key exchange.
	        offset = handlerClientKeyExchange(requestData, offset);
	        
	        // ChangeCipherSpec
			offset = handleChangeCipherSpec(requestData, offset);
			
			// write 【ClientKeyExchange】【ChangeCipherSpec】 into mServerKeyExchangeBody.
			byte[] clientKeyExchangeBody = new byte[offset];
			System.arraycopy(requestData, 0, clientKeyExchangeBody, 0, offset);
			saveInfo(CLIENT_KEY_EX_CHANGE_BODY, clientKeyExchangeBody);			
			
			// Finished
			offset = handleFinish(requestData, offset);
			
			// write 【ClientKeyExchange】【ChangeCipherSpec】【Finish】 into mServerKeyExchangeBody.
			clientKeyExchangeBody = new byte[offset];
			System.arraycopy(requestData, 0, clientKeyExchangeBody, 0, offset);
			saveInfo(CLIENT_KEY_EX_CHANGE_BODY, clientKeyExchangeBody);	
			
			return offset;
		} catch (Exception ev) {
			ev.printStackTrace();
		}
		return -1;
	}
	
	/**
	 * 
	 * @param response
	 * @param offset
	 * 
	 * @return
	 * 
	 * @throws Exception
	 */
	private int handleFinish(final byte[] response, int offset) throws Exception {
		byte messageType;
		byte[] mlByts;
		int messageLength;
		messageType = response[offset];
		offset += 1;
		// Message Length
		mlByts = getMessageLength(response, offset);
		offset += 4;
		messageLength = Utils.byteArrayToIntInNBO(mlByts, 0);
		byte[] verifyData = null;
		if (messageType == Constant.HandshakeType[Constant.htIndex.finished.ordinal()]) {
			verifyData = new byte[messageLength];
			System.arraycopy(response, offset, verifyData, 0, messageLength);
			offset += messageLength;
		}

		// Verify finish
		verifyFinishData(verifyData);
		return offset;
	}
	
	/**
	 * <p>
	 * PRF(master_secret, finished_label, MD5(handshake_messages) + SHA-1(handshake_messages)) [0..11];<br>
	 * handshake_messages则为这个Finished消息之前客户端与服务器端的请求与响应报文的原始消息二进制字节流<br>
	 * request1 + reponse1 + request2 + reponse2（服务器Finished之前的消息）。
	 * 
	 * @param verifyData2
	 * 
	 * @throws Exception
	 */
	private final void verifyFinishData(byte[] verifyData2) throws Exception {
		// get label
		byte[] label = HMac.TLS_MD_CLIENT_FINISH_CONST();

		// get seed
		byte[] handshakeMsg = getHandshakeMessage();
		byte[] encryptedMd5 = HMac.MD5(handshakeMsg);
		byte[] encryptedSha1 = HMac.SHA1(handshakeMsg);
		byte[] seed = Utils.joinBytes(encryptedMd5, encryptedSha1);

		// create Master Secret
		byte[] secret = getInfo(PMS);
		byte[] msLabel = HMac.TLS_MD_MASTER_SECRET_CONST();
		byte[] msSeed = Utils.joinBytes(getInfo(RNC), getInfo(RNS));
		byte[] ms = createMasterSecret(secret, msLabel, msSeed, 68);
		saveInfo(MS, ms);
		// calculate VerifyData
		byte[] verifyData = PRFCipher.PRF(ms, label, seed, 12);
		if (verifyData == null) {
			throw new Exception("HMac verify finish failed!");
		}
		for (int i = 0; i < verifyData.length; i++) {
			if (verifyData[i] != verifyData2[i]) {
				throw new Exception("HMac verify finish failed!");
			}
		}
	}
	
	/**
	 * 
	 * @return
	 * 
	 * @throws Exception
	 */
	private final byte[] getHandshakeMessage() throws Exception {
		byte[] byts = getInfo(CLIENT_HELLO_BODY);
		
		byte[] serverHelloBody = getInfo(SERVER_HELLO_BODY);
		if(serverHelloBody!=null) {
			byts = Utils.joinBytes(byts, serverHelloBody);
		} 
		
		byte[] clientKeyExChangeBody = getInfo(CLIENT_KEY_EX_CHANGE_BODY);
		if (clientKeyExChangeBody!=null) {
			byts = Utils.joinBytes(byts, clientKeyExChangeBody);
		}
		
		return byts;
	}
	
	/**
	 * 
	 * @param response
	 * @param offset
	 * 
	 * @return
	 * 
	 * @throws Exception
	 */
	private int handleChangeCipherSpec(final byte[] requestData, int offset) throws Exception {
		byte messageType;
		byte[] mlByts;
		int messageLength;
		// Message Type
		messageType = requestData[offset];
		offset += 1;
		// Message Length
		mlByts = getMessageLength(requestData, offset);
		offset += 4;
		messageLength = Utils.byteArrayToIntInNBO(mlByts, 0);
		@SuppressWarnings("unused")
		byte cipherSpec = -1;
		if (messageType == Constant.HandshakeType[Constant.htIndex.ChangeCipherSpec.ordinal()]) {
			cipherSpec = requestData[offset];
			offset += messageLength;
		}
		
		return offset;
	}
	
	private byte[] sendFullServerHello() throws Exception {
		byte[] serverHelloBody = serverHello();
		
		byte[] serverCertificateBody = serverCertificate();
		
		byte[] fullServerHelloBody = Utils.joinBytes(serverHelloBody, serverCertificateBody);
		
		saveInfo(SERVER_HELLO_BODY, fullServerHelloBody);
		
		return fullServerHelloBody;
	}
	
	private byte[] serverCertificate() throws Exception {
		byte[] messageType = new byte[1];
		messageType[0] = Constant.HandshakeType[Constant.htIndex.certificate.ordinal()];

		byte[] certificate = Utils.readPublicKeyCertificate(Utils.getCertificatePath()+"Cert.crt");
		
		byte[] certificateLength = Utils.intToByteArrayInNBO(certificate.length);
		
		byte[] serverCertificateBody = Utils.joinBytes(messageType, certificateLength, certificate);
		return serverCertificateBody;
	}
	
	private byte[] serverHello() throws Exception {
		
		// serverVesion
		byte[] protocolVersion = getServerProtocolVersion();
		
		// serverRandom
		byte[] clientGmtUnixTime = Utils.getClientGMTUnixTime();
		byte[] clientRandom = Utils.getServerRandom(28);
		byte[] rns = Utils.joinBytes(clientGmtUnixTime, clientRandom);
		saveInfo(RNS, rns);
		
		byte[] sessionId = getSessionID(request_).getBytes();
		Integer iO = new Integer(sessionId.length);
		byte[] sessionLength = new byte[1];
		sessionLength[0] = iO.byteValue();
		
		// Cipher Suite
		byte[] cipherSuiteInfor = Constant.TLS_RSA_WITH_AES_256_CBC_SHA;
				
		// get server hello data
		byte[] serverHelloData = Utils.joinBytes(protocolVersion, getInfo(RNS), sessionLength, sessionId, cipherSuiteInfor);
				
		
		byte[] messageType = new byte[1];
		messageType[0] = Constant.HandshakeType[Constant.htIndex.server_hello.ordinal()];
		
		int mLen = serverHelloData.length;
		byte[] messageLength = Utils.intToByteArrayInNBO(mLen);
		
		// add MessageType
		byte[] serverHelloBody = Utils.joinBytes(messageType, messageLength, serverHelloData);
				
		return serverHelloBody;
	}
	
	private int handleFullClientHelloRequest(byte[] requestData) throws Exception{
		int offset = 0;
		byte messageType = requestData[offset];
		offset += 1;
		
		byte[] mlByts = getMessageLength(requestData, offset);
		offset += 4;
		int messageLength = Utils.byteArrayToIntInNBO(mlByts, 0);
		
		if (messageType == Constant.HandshakeType[Constant.htIndex.client_hello.ordinal()]) {
			byte[] clientHelloBody = new byte[messageLength];
			System.arraycopy(requestData, offset, clientHelloBody, 0, messageLength);
			handleClientHelloBody(clientHelloBody);
		}
		offset += messageLength;
		byte[] clientHelloBody = new byte[offset];
		System.arraycopy(requestData, 0, clientHelloBody, 0, offset);
		saveInfo(CLIENT_HELLO_BODY, clientHelloBody);
		
		return offset;
	}
	
	private void handleClientHelloBody (byte[] clientHelloBody) throws Exception {
		int offset = 0;
		// client version
		byte[] cvByts = new byte[2];
		System.arraycopy(clientHelloBody, offset, cvByts, 0, 2);
		offset += 2;
		//保存客户端信道版本信息
		String tlsVersion = cvByts[0]+"."+cvByts[1];
		saveInfoStr(TLS_VERSION, tlsVersion);
		
		TLSInfo.setInfoBySessionIdAndKey(getSessionID(request_), TLS_VERSION, tlsVersion);
		
		// client random
		byte[] gmtUnixTimeByts = new byte[4];
		System.arraycopy(clientHelloBody, offset, gmtUnixTimeByts, 0, 4);
		offset += 4;
		byte[] clByts = new byte[28];
		System.arraycopy(clientHelloBody, offset, clByts, 0, 28);
		offset += 28;
		byte[] rnc = Utils.joinBytes(gmtUnixTimeByts, clByts);
		saveInfo(RNC, rnc);
		
		// session
		
		// 算法集
		
		// 证书序列号
		
	}
	
	/**
	 * 密信道版本号，tlsVersion
	 * <p>
	 * 1.0 -- 基础版<br>
	 * 1.1 -- 加了防重放<br>
	 * 1.2 -- 使用双向认证<br>
	 * 1.3 -- 加了防篡改、 数据加密与请求一块<br>
	 * 1.4 -- 加并发改造
	 * </p>
	 * 
	 * @return
	 */
	private byte[] getServerProtocolVersion() {
		// 获取配置中设置的信道版本号

		byte[] clientVersion = new byte[2];
		clientVersion[0] = 1;
		clientVersion[1] = 4;
		return clientVersion;
	}
	
	/**
	 * 
	 * @param shByts
	 * @param offset
	 * 
	 * @return
	 */
	private byte[] getMessageLength(byte[] shByts, int offset) {
		byte[] mlByts = new byte[4];
		System.arraycopy(shByts, offset, mlByts, 0, 4);
		return mlByts;
	}
	
	/**
	 * 
	 * @param secret
	 * 
	 * @return
	 * 
	 * @throws Exception
	 */
	public static byte[] getAESKey(final byte[] secret) throws Exception {
		int len = 32;
		int offset = 0;
		byte[] key = new byte[len];
		System.arraycopy(secret, offset, key, 0, len);
		return key;
	}
	
	/**
	 * 
	 * @param secret
	 * 
	 * @return
	 * 
	 * @throws Exception
	 */
	public static byte[] getAESIv(final byte[] secret) throws Exception {
		int len = 16;
		int offset = 32;
		byte[] iv = new byte[len];
		System.arraycopy(secret, offset, iv, 0, len);
		return iv;
	}
	
	private void saveInfoStr(String key, String value) {
		saveInfo(key, value.getBytes());
	}
	
	private String getInfoStr(String key) {
		byte[] valueByte = getInfo(key);
		return new String(valueByte);
	}
	
	private void saveInfo(String key, byte[] valueByte) {
		String value = Base64.encode(valueByte);
		
		String sessionID = getSessionID(request_);
		HashMap<String, String> hm = getInfoByKey(sessionID);
		if(hm==null) {
			hm = new HashMap<String, String>();
		}
		String valueTemp = hm.get(key);
		if (valueTemp!=null) {
			hm.remove(key);
		}
		
		hm.put(key, value);
		
		removeInfoByKey(sessionID);
		setInfoByKey(sessionID, hm);
	}
	
	private byte[] getInfo(String key) {
		String sessionID = getSessionID(request_);
		HashMap<String, String> hm = getInfoByKey(sessionID);
		if(hm==null) {
			hm = new HashMap<String, String>();
		}
		String valueTemp = hm.get(key);
		if (valueTemp!=null) {
			return Base64.decodeToBytes(valueTemp);
		}
		
		return null;
	}
	
	private void removeInfo(String key) {
		String sessionID = getSessionID(request_);
		HashMap<String, String> hm = getInfoByKey(sessionID);
		if(hm==null) {
			hm = new HashMap<String, String>();
		}
		String valueTemp = hm.get(key);
		if (valueTemp!=null) {
			hm.remove(key);
		}
		removeInfoByKey(sessionID);
		setInfoByKey(sessionID, hm);
	}
	
	private void clearInfo() {
		String sessionID = getSessionID(request_);
		HashMap<String, String> hm = getInfoByKey(sessionID);
		if(hm==null) {
			hm = new HashMap<String, String>();
		} else {
			removeInfoByKey(sessionID);
		}
		
	}
	
	public static HashMap<String, String> getInfoByKey(String key) {
		return hm_.get(key);
	}
	
	public static void setInfoByKey(String key, HashMap<String, String> value) {
		hm_.put(key, value);
	}
	
	public static void removeInfoByKey(String key) {
		hm_.remove(key);
	}
	
	private String getSessionID(ServletRequest request) {
		String sessionId =  ((HttpServletRequest)request).getSession(true).getId();
		return sessionId;
	}

}
