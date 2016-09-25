package com.gz.tool.server.security;

/**
 * <p>
 * Constant常量类。
 * </p>
 * <p>
 * 说明：该类存储了加密相关的一些常量<br>
 * </p>
 */
public final class Constant {
	// public final static String HMAC_VERIFY_ERROR = "HMAC完整性较验出错.";
	// public final static String ERR_PARSE_XML_01 = "数据解析失败。";
	// public final static String ERR_PARSE_XML_02 = "数据解析失败。请咨询"
	// .concat(EMPConfig.newInstance().getClientContactNumber()).concat("。");
	// public final static String ERR_DATA_FORMAT = "错误的数据格式。";
	// public final static String ERR_PARSE_CHANNEL = "解析频道出错。";
	// public final static String ERR_ACCOUNT_LIST = "卡号列表错误。";
	// public final static String ERR_BASE64 = "网络繁忙，请稍候。EMP_C_64";
	// public final static String ERR_NULL_POI = "没有相关内容。";
	// public final static String ERR_NULL_CHANNEL = "无法取得相关内容。";
	// public final static String ERR_ENCRYPT_SERVER_01 = "通讯异常。请咨询"
	// .concat(EMPConfig.newInstance().getClientContactNumber()).concat("。");
	// public final static String ERR_ENCRYPT_SERVER_02 = "暂时无法获取服务器的响应。";
	// // HttpManager.
	// public final static String ERR_CONNECTION_FAIL = "无法连接服务器。";
	// public final static String ERR_NO_GATEWAY = "通信异常，请检查您的网络设置及通讯情况!";
	// public final static String ERR_GENERAL_EXCEPTION = "通讯异常。请咨询"
	// .concat(EMPConfig.newInstance().getClientContactNumber()).concat("。");
	// public final static String ERR_SYSTEM_RES = "系统资源不足，请退出重新登录。";
	// public final static String ERR_NO_RESULT = "无法正确读取数据。";
	// // data manage.
	// public final static String ERR_GUNZIP_DATA_FORMAT = "压缩数据格式错误。";
	// public final static String ERR_GUNZIP_FAIL = "不能正确解压数据。";
	// public final static String ERR_DOWNLOAD_DISCONN = "下载数据时网络被中断。";
	// // public final static String ERR_NULL_XML = "解析出错，企图解析一个空数据。";
	// public final static String ERR_NULL_XML = "网络繁忙，请稍候。";
	//
	// public final static String ERR_INVALID_POSITION = "无效的经度和纬度。";
	// public final static String ERR_POSITION_FORMAT = "位置格式错误。";
	// public final static String ERR_NO_POSITION = "位置无效。";
	// public final static String ERR_POS_NOT_FOUND = "找不到地址。";
	// public final static String ERR_RMS_FULL = "RMS存储空间不足。";
	// public final static String ERR_GET_PUB_KEY = "无法取得公钥，或者是使用了不支持的公钥证书。";
	// public final static String ERR_CREATE_KEY = "创建密钥出错。";
	// public final static String ERR_RSA_ENCRYPT = "RSA加密出错。";
	// public final static String ERR_NETWORK_BUSY = "网络繁忙。";
	// public final static String ERR_LOGIN_NO_RESPONSE = "无法获取服务器登录响应。";
	// public static final String SERVER_ERROR = "服务器暂时不能使用.";
	//
	// public static final String ERR_LOGIN_UNKNOWN = "登录失败！未知的响应状态。";
	// public static final String ERR_LOGIN_FAILURE = "登录失败，请重试。";

	// 消息类型
	// HandshakeType 【hello_request】 = { 0x00 };
	// HandshakeType 【client_hello】 = { 0x01 };
	// HandshakeType 【server_hello】 = { 0x02 };
	// HandshakeType 【certificate】 = { 0x04 };
	// HandshakeType 【server_key_exchange】 = { 0x05 };
	// HandshakeType 【certificate_request】 = { 0x06 };
	// HandshakeType 【ChangeCipherSpec】 = { 0x07 };
	// HandshakeType 【certificate_verify】 = { 0x08 };
	// HandshakeType 【client_key_exchange】 = { 0x09 };
	// HandshakeType 【finished】 = { 0x0A };
	// HandshakeType 【InitContent】 = { 0x0B };
	public static enum htIndex {
		hello_request, client_hello, server_hello, certificate, server_key_exchange, certificate_request, ChangeCipherSpec, certificate_verify, client_key_exchange, finished, InitContent, ResHashMessageType
	};

	public static final byte[] HandshakeType = { 0x00, 0x01, 0x02, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0E};

	// 默认算法集（一次会话一密）
	public static final byte[] TLS_NULL_WITH_NULL_NULL = { 0x00, 0x00 };
	public static final byte[] TLS_RSA_WITH_NULL_MD5 = { 0x00, 0x01 };
	public static final byte[] TLS_RSA_WITH_NULL_SHA = { 0x00, 0x02 };
	public static final byte[] TLS_RSA_WITH_AES_128_CBC_MD5 = { 0x00, 0x04 };
	public static final byte[] TLS_RSA_WITH_AES_128_CBC_SHA = { 0x00, 0x05 };
	public static final byte[] TLS_RSA_WITH_AES_256_CBC_MD5 = { 0x00, 0x06 };
	public static final byte[] TLS_RSA_WITH_AES_256_CBC_SHA = { 0x00, 0x07 };
	public static final byte[] TLS_RSA_WITH_DES_CBC_MD5 = { 0x00, 0x08 };
	public static final byte[] TLS_RSA_WITH_DES_CBC_SHA = { 0x00, 0x09 };
	public static final byte[] TLS_RSA_WITH_3DES_EDE_CBC_MD5 = { 0x00, 0x0A };
	public static final byte[] TLS_RSA_WITH_3DES_EDE_CBC_SHA = { 0x00, 0x0B };
	public static final byte[] TLS_SM2_WITH_SM4_128_CBC_SM2 = { 0x00, 0x0C };

	// 高级算法集（一次一密）
	public static final byte[] TLS_RSA_WITH_AES_128_CBC_MD5_T = { 0x01, 0x04 };
	public static final byte[] TLS_RSA_WITH_AES_128_CBC_SHA_T = { 0x01, 0x05 };
	public static final byte[] TLS_RSA_WITH_AES_256_CBC_MD5_T = { 0x01, 0x06 };
	public static final byte[] TLS_RSA_WITH_AES_256_CBC_SHA_T = { 0x01, 0x07 };
	public static final byte[] TLS_RSA_WITH_DES_CBC_MD5_T = { 0x01, 0x08 };
	public static final byte[] TLS_RSA_WITH_DES_CBC_SHA_T = { 0x01, 0x09 };
	public static final byte[] TLS_RSA_WITH_3DES_EDE_CBC_MD5_T = { 0x01, 0x0A };
	public static final byte[] TLS_RSA_WITH_3DES_EDE_CBC_SHA_T = { 0x01, 0x0B };

}
