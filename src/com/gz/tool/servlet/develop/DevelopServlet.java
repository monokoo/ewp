/**
 * @author tanguozhi
 */
package com.gz.tool.servlet.develop;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.gz.tool.server.security.HMac;
import com.gz.tool.server.tool.Base64;
import com.gz.tool.server.tool.Utils;
import com.gz.tool.tls.TLS;
import com.gz.tool.tls.TLSInfo;

public class DevelopServlet extends HttpServlet{
	private static final long serialVersionUID = 1L;
	
	/*
	 * 表现层进入
	 * BXC00001		
	 */
	 @Override  
	 public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException { 
		 String filePathName = request.getParameter("name");
		 
		 if(filePathName!=null && !"".equals(filePathName)) {
			 try {
				 filePathName = Utils.getWebRoot()+"develop_resource/"+filePathName;
				 byte[] byt = Utils.readFileByBytes(filePathName);
				 if(byt!=null) {
					 byte[] serverHmacKey = HMac.getInfoBySessionIdAndKey(Utils.getSessionID(request), HMac.HMAC_SERVER_KEY);
					 byte[] bytHMac = HMac.encryptHMAC(byt, serverHmacKey, HMac.KEY_MAC_SHA1);
					 
					 String tlsVersion = TLSInfo.getInfoBySessionIdAndKey(Utils.getSessionID(request), TLS.TLS_VERSION);
					 if(Float.parseFloat(tlsVersion)>=TLS.TLS_VERSION_1_4) {	//1.4信道处理
						byt = Utils.joinBytes(bytHMac, byt);
		
						byt = Base64.encodeBase64Byte(byt);
					 } else {
					 	response.addHeader("X-Emp-Signature", Base64.encodeBase64String(bytHMac));
					 }
					response.addHeader("Content-Type", "");
					response.getOutputStream().write(byt);
				 }
			 } catch (Exception e) {
			}
		 }
		 
	 }
	 
}

