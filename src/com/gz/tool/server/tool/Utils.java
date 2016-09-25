package com.gz.tool.server.tool;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

public class Utils {
	public static String PROJ_NAME = "";
	/**
	 * Transform bytes.
	 * 
	 * @param byts source byte array
	 * 
	 * @return transformed byte array.
	 * 
	 * @throws Exception.
	 */
	public static final byte[] joinBytes(byte[]... byts) throws Exception {
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
	 * 获得客户端时区信息。
	 * </p>
	 * 
	 * @return byte[]客户端时区信息的比特数组。
	 */
	public static final byte[] getClientGMTUnixTime() {
		// get local time. 4 bytes.
		Calendar cal = Calendar.getInstance();
		Date date = cal.getTime();
		int hours = date.getHours();
		byte[] clientGmtUnixTime = new byte[4];
		clientGmtUnixTime[0] = (byte) ((hours & 0x0000FF00) >> 8);
		clientGmtUnixTime[1] = (byte) ((hours & 0x000000FF));
		int minutes = date.getMinutes();
		clientGmtUnixTime[2] = (byte) ((minutes & 0x0000FF00) >> 8);
		clientGmtUnixTime[3] = (byte) ((minutes & 0x000000FF));
		return clientGmtUnixTime;
	}
	
	/**
	 * 
	 * @param len
	 * 
	 * @return
	 * 
	 * @throws Exception
	 */
	public static byte[] getServerRandom(int len) throws Exception {
		byte[] clientRandom = new byte[len];
		for (int i = 0; i < len; i++) {
			long timeMillis = System.currentTimeMillis();
			int rd = new Random().nextInt();
			byte rdNum = (byte) ((timeMillis + rd) % 256);
			clientRandom[i] = rdNum;
		}
		return clientRandom;
	}
	
	/**
	 * <p>
	 * Network Byte Order.
	 * </p>
	 * 
	 * @param intValue source value
	 * 
	 * @return transformed byte array
	 * 
	 * @see #byteArrayToIntInNBO(byte[], int)
	 */
	public static final byte[] intToByteArrayInNBO(int intValue) {
		byte[] byt = new byte[4];
		for (int i = 0; i < 4; i++) {
			byt[i] = (byte) (intValue >>> (24 - i * 8));
		}
		return byt;
	}
	
	public static byte[] readPublicKeyCertificate(String remoteFile) {
		byte[] bytes = new byte[1];
		try {
           FileInputStream is = new FileInputStream(remoteFile);
           
			CertificateFactory factory = CertificateFactory.getInstance("X.509");
			X509Certificate cert = (X509Certificate) factory.generateCertificate(is);
			bytes = cert.getEncoded();
           
           is.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return bytes;
	}
	
	/**
	 * <p>
	 * Network Byte Order.
	 * </p>
	 * 
	 * @param byts source byte array.
	 * @param offset the offset.
	 * 
	 * @return transformed value.
	 * 
	 * @see #intToByteArrayInNBO(int)
	 */
	public static final int byteArrayToIntInNBO(byte[] byts, int offset) {
		int intValue = 0;
		for (int i = 0; i < 4; i++) {
			int shift = (4 - 1 - i) * 8;
			intValue += (byts[i + offset] & 0x000000FF) << shift;
		}
		return intValue;
	}
	
	public static byte[] readFileByBytes(String fileName) {
		File f = new File(fileName);  
        if(!f.exists()){  
        	System.out.println("file is no exist:"+fileName);
            throw null;  
        }  
  
        ByteArrayOutputStream bos = new ByteArrayOutputStream((int)f.length());  
        BufferedInputStream in = null;  
        try{  
            in = new BufferedInputStream(new FileInputStream(f));  
            int buf_size = 1024;  
            byte[] buffer = new byte[buf_size];  
            int len = 0;  
            while(-1 != (len = in.read(buffer,0,buf_size))){  
                bos.write(buffer,0,len);  
            }  
            return bos.toByteArray();  
        }catch (IOException e) {  
        	System.out.println("文件读取失败："+fileName);
        }finally{  
            try{  
                in.close();  
                bos.close();
            }catch (IOException e) {  
            	System.out.println("文件读取失败："+fileName); 
            }  
        }  
        return null;
	}
	
	public static String getWebRoot() {
		String path = "/"+Thread.currentThread().getContextClassLoader().getResource("").getPath().substring(1);
		
		path = path.replaceAll("WEB-INF/classes/", "");
		
		return path;
	}
	
	public static String getCertificatePath() {
		String path = "/"+Thread.currentThread().getContextClassLoader().getResource("").getPath().substring(1);
		
		path = path.replaceAll("WEB-INF/classes/", "certificate/");
		
		return path;
	}
	
	public static String getPathDB() {
		String path = "/"+Thread.currentThread().getContextClassLoader().getResource("").getPath().substring(1);
		
		path = path.replaceAll("WEB-INF/classes/", "server/web/install/");
		
		path += "db.xml";
		
		return path;
	}
	
	public static String getSessionID(ServletRequest request) {
		String sessionId =  ((HttpServletRequest)request).getSession(true).getId();
		return sessionId;
	}
}
