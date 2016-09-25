package com.gz.tool.filter;

import java.io.Serializable;
import java.net.URLDecoder;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.bouncycastle.util.encoders.Base64;

import com.gz.tool.server.security.AESCipher;
import com.gz.tool.server.tool.Utils;

public class ContextParameterFilter implements Filter {
	private SessionInfo sessionInfo = new SessionInfo();
	private FilterConfig config_;
	
	public void init(FilterConfig config) throws ServletException {
		this.config_ = config;
	}

	public void doFilter(ServletRequest request, ServletResponse response,FilterChain chain) {
		try {
			request.setCharacterEncoding("utf-8");
			
			String fileUpload = request.getParameter("file_upload");
			if(null!=fileUpload) {
				chain.doFilter(request, response);
				return;
			}
			
			String _bodyBuffer = "";
			//读取body流
	        ServletInputStream sis = request.getInputStream();
			byte[] buffer = new byte[1024];
			while (sis.read(buffer, 0, 1024) != -1) {
				_bodyBuffer += new String(buffer, "utf-8");
			}
			
//			//url解码
//			_bodyBuffer = URLDecoder.decode(_bodyBuffer, "UTF-8"); 
			//将流参数封装到请求里
			Map<String,String[]> parameMap = new HashMap<String,String[]>(request.getParameterMap());  
			if (!"".equals(_bodyBuffer)) {
				//url解码
				_bodyBuffer = URLDecoder.decode(_bodyBuffer, "UTF-8"); 
				
//				//尝试解决
//				byte[] serverKey = AESCipher.getInfoBySessionIdAndKey(Utils.getSessionID(request), AESCipher.CLIENT_KEY);
//				byte[] serverIV = AESCipher.getInfoBySessionIdAndKey(Utils.getSessionID(request), AESCipher.CLIENT_IV);
//				if(serverKey!=null && serverIV!=null) {
//					System.out.println("_bodyBuffer=======:"+_bodyBuffer);
//					System.out.println("serverKey=======:"+serverKey);
//					System.out.println("serverIV=======:"+serverIV);
//					byte[] aa = Base64.decode(_bodyBuffer);//.substring(11, _bodyBuffer.length());
//					System.out.println("_bodyBuffer=======:"+aa);
//					byte[] ecryptedData = AESCipher.decrypt(aa, serverKey, serverIV);
//					if(ecryptedData!=null) {
//						String _bodyBufferTemp = new String(ecryptedData);
//						System.out.println("_bodyBufferTemp=======");
//						if(_bodyBuffer.equals(_bodyBufferTemp)) {
//							_bodyBuffer = _bodyBufferTemp;
//						}
//					}
//				}
				
				//分解参数
				String[] parames = _bodyBuffer.split("&");
				for (int i=0; i<parames.length; i++) {
					if ("".equals(parames[i])) continue;
					
					String[] keyValue = parames[i].split("=");
					String key = keyValue[0].trim();
					String value = "";
					
					if (keyValue.length>1) value = keyValue[1].trim();
//					System.out.println("parme====>>>>key="+key+",  value="+value);
					parameMap.put(key, new String[]{value});  
				}
			}
			
			String sessionId =  ((HttpServletRequest)request).getSession(true).getId();
			//将全局的os标志设置回请求里
			String osKey = "os";
			String osValue = null;
			if (!parameMap.containsKey(osKey)) {
				osValue = sessionInfo.getSessionInfo(sessionId, osKey);
				if (osValue!=null) {
					parameMap.put(osKey, new String[]{osValue});
				}
			} else {
				String[] value = (String [])parameMap.get(osKey);
				sessionInfo.setSessionInfo(sessionId, osKey, arrayToString(value));
			}
			
			
			request = new ParameterRequestWrapper((HttpServletRequest)request, parameMap);
			
			chain.doFilter(request, response);
		} catch (Exception e) {
			System.out.println("");
			e.printStackTrace();
		}
	}
	
	private String arrayToString(String[] str) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < str.length; i++) {
			sb.append(str[i]);
		}
		return sb.toString();
	}

	public void destroy() {
		this.config_ = null;
	}
	
    class ParameterRequestWrapper extends HttpServletRequestWrapper {
        
        private Map<String, String[]> params;  
      
        public ParameterRequestWrapper(HttpServletRequest request, Map<String, String[]> newParams) {  
            super(request);  
              
            this.params = newParams;  
        }  
      
        @Override  
        public String getParameter(String name) {  
            String result = "";  
              
            Object v = params.get(name);  
            if (v == null) {  
                result = null;  
            } else if (v instanceof String[]) {  
                String[] strArr = (String[]) v;  
                if (strArr.length > 0) {  
                    result =  strArr[0];  
                } else {  
                    result = null;  
                }  
            } else if (v instanceof String) {  
                result = (String) v;  
            } else {  
                result =  v.toString();  
            }  
              
            return result;  
        }  
      
        @Override  
        public Map getParameterMap() {  
            return params;  
        }  
      
        @Override  
        public Enumeration getParameterNames() {  
            return new Vector(params.keySet()).elements();  
        }  
      
        @Override  
        public String[] getParameterValues(String name) {  
            String[] result = null;  
              
            Object v = params.get(name);  
            if (v == null) {  
                result =  null;  
            } else if (v instanceof String[]) {  
                result =  (String[]) v;  
            } else if (v instanceof String) {  
                result =  new String[] { (String) v };  
            } else {  
                result =  new String[] { v.toString() };  
            }  
            return result;  
        }  
      
    } 
    
    class SessionInfo implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private HashMap<String, HashMap<String, String>> map = new HashMap<String, HashMap<String, String>>();
		
		public void setSessionInfo(String sessionId, String key, String value) {
			HashMap<String, String> tempMap = map.get(sessionId);
			if (tempMap==null) {
				tempMap = new HashMap<String, String>();
				map.put(sessionId, tempMap);
			}
			
			tempMap.put(key, value);
			map.put(sessionId, tempMap);
		}
		
		public String getSessionInfo(String session, String key) {
			HashMap<String, String> tempMap = map.get(session);
			if (tempMap!=null) {
				return (String)tempMap.get(key);
			}
			return null;
		}
	}
}
