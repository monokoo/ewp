package com.gz.tool.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.*;
import javax.servlet.http.*;

import com.gz.tool.db.DBConnection;
import com.gz.tool.server.security.AESCipher;
import com.gz.tool.server.security.HMac;
import com.gz.tool.servlet.dbmanager.DBManagerServlet;
import com.gz.tool.tls.TLS;
import com.gz.tool.tls.TLSInfo;

public class ContextSessionFilter implements Filter {
	private static HashMap<String, String> mSessionMap = new HashMap<String, String>();
	private FilterConfig config_;
	
	private Timer mSessionCheckTimer; 
	
	private final int TIMEOUT = 50 *6000;
	
	public void init(FilterConfig config) throws ServletException {
		this.config_ = config;
		
		if (mSessionCheckTimer==null) {
			mSessionCheckTimer = new Timer();
			mSessionCheckTimer.schedule(new TimerTask() {  
		        public void run() {  
		        	cleanTimeOutSession();
		        }  
		    }, 10000, 10000);
		}
		
	}
	
	public void doFilter(ServletRequest request, ServletResponse response,FilterChain chain) {
		try {
			System.out.println("====>>>"+request.getServletContext());
			
			HttpSession session = ((HttpServletRequest)request).getSession(true);
			
			String requestCookie = getRequestHeader(request, "cookie");
			if (requestCookie!=null && !"".equals(requestCookie)) {
				if(checkRequestSessionIDIsTimeout(requestCookie)) {
					String timeoutPage = handleSessionTimeout(request, response);
					
					//当前返回的超时结果为非1时直接输出
					if(!"1".equals(timeoutPage))  {
						System.out.println("session_timeout========>>>>>>"+requestCookie);
						response.setContentType("text/html;charset=UTF-8"); 
						((HttpServletResponse)response).addHeader("X-Emp-Signature", "timeout");
						response.getWriter().write(timeoutPage);
						return;
					}
				}
			}
			
			//更新客户端的访问时间
			updateSessionTime(session.getId());
			
			chain.doFilter(request, response);
		} catch (Exception e) {
			System.out.println("ContextSessionFilter_error========>>>>>>");
			e.printStackTrace();
		}
	}
	
	/**
	 * 超时处理
	 * @param request
	 * @param response
	 * @return	返回超时界面 当返回1时超时后重新请求的 
	 */
	public String handleSessionTimeout(ServletRequest request, ServletResponse response) {
		
		String relogin = request.getParameter("relogin");
		System.out.println("relogin==="+relogin);
		if(relogin!=null && "reloginDBManagerServlet".equals(relogin)) {
			return "1";
		}
		
		String requestUri = ((HttpServletRequest)request).getRequestURI().split("\\?")[0];
		if(requestUri.equals(DBManagerServlet.REQUEST_URI)) {	//数据库登录超时操作
			return DBManagerServlet.getTimeoutPage();
		}
		
		return "";
	}
	
	private boolean checkRequestSessionIDIsTimeout(String requestSessionID) {
		if(mSessionMap.containsKey(requestSessionID)) {
			return false;
		}
		System.out.println("checkRequestSessionIDIsTimeout==="+requestSessionID);
		Iterator it = mSessionMap.keySet().iterator();
		while(it.hasNext()) {
			String key = (String) it.next();
			System.out.println("key==="+key);
			System.out.println("requestSessionID==="+requestSessionID);
			if(requestSessionID.contains(key)) {
				return false;
			}
		}
		
		return true;
	}
	
	private String getRequestHeader(ServletRequest request, String name) {
		Enumeration<?> headerNames = ((HttpServletRequest) request).getHeaderNames();
        while(headerNames.hasMoreElements()) {
            String headerName = (String)headerNames.nextElement();
            if(headerName.equals(name)) {
            	Enumeration<?> values = ((HttpServletRequest) request).getHeaders(headerName);
                while(values.hasMoreElements()) {
                   return (String)values.nextElement();
                }
            }
        }
        return "";
	}
	
	private void updateSessionTime(String sessionID) {
		if (mSessionMap.containsKey(sessionID)) {
			mSessionMap.remove(sessionID);
		}
		mSessionMap.put(sessionID, System.currentTimeMillis()+"");
//		System.out.println("Session_Update===="+sessionID+", session_size=="+mSessionMap.size());
	}
	
	private void cleanTimeOutSession() {
		ArrayList<String> tempArrayList = new ArrayList<String>();
		
		Set<String> set = mSessionMap.keySet();
		Iterator<String> it = set.iterator();
		while (it.hasNext()) {
			String key = it.next();
			String timeStr = mSessionMap.get(key);
			long time = Long.parseLong(timeStr);
			if(System.currentTimeMillis()-time>TIMEOUT) {
				tempArrayList.add(key);
			}
		}
		
		for(int i=0; i<tempArrayList.size(); i++) {
			String key = tempArrayList.get(i);
			//清空会话
			mSessionMap.remove(key);
			System.out.println("Session_Timeout===="+key+", session_size=="+mSessionMap.size());
			//清空加密信息
			AESCipher.removeInfoByKey(key);
//			System.out.println("AESCipher==removeInfoByKey==="+key);
			//清空加密信息
			HMac.removeInfoByKey(key);
//			System.out.println("HMac==removeInfoByKey==="+key);
			//清空信道信息
			TLS.removeInfoByKey(key);
//			System.out.println("TLS==removeInfoByKey==="+key);
			//清空信道信息
			TLSInfo.removeInfoByKey(key);
//			System.out.println("TLSInfo==removeInfoByKey==="+key);
			//清空DBConnection数据库连接信息
			DBConnection.removeInfoByKey(key);
//			System.out.println("DBConnection==removeInfoByKey==="+key);
		}
	}
	
	public String getParam(ServletRequest request, String key) {
		String value = (String) request.getParameter(key);
		if (value==null) {
			String _bodyBuffer = "";
			
			try {
				ServletInputStream sis = request.getInputStream();
				byte[] buffer = new byte[1024];
				while (sis.read(buffer, 0, 1024) != -1) {
					_bodyBuffer += new String(buffer, "GB2312");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			//将流参数封装到请求里
			Map<String,String[]> parameMap = new HashMap<String,String[]>(request.getParameterMap());  
			if (!"".equals(_bodyBuffer)) {
				String[] parames = _bodyBuffer.split("&");
				for (int i=0; i<parames.length; i++) {
					if ("".equals(parames[i])) continue;
					
					String[] keyValue = parames[i].split("=");
					if (keyValue.length<2)
						return null;
					
					if (keyValue[0].equals(key)) {
						return keyValue[1];
					}
				}
			}
		}
	
		return value;
	}

	public void destroy() {
		this.config_ = null;
	}
}
