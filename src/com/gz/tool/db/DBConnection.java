package com.gz.tool.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import org.dom4j.Element;

import com.gz.tool.server.tool.Utils;
import com.gz.tool.xml.ParseXml;


public class DBConnection {
	private final static String USER_NAME = "USERNAME";
	private final static String USER_PASSWORD = "USERPASSWORD";
	private final static String DB_DIRVER = "DIRVER";
	private final static String DB_URL = "URL";
	
	private static HashMap<String, Connection> mConnectionMap = new HashMap<String, Connection>();
	private static HashMap<String, HashMap<String, String>> mConnUserMap = new HashMap<String, HashMap<String, String>>();
	private static HashMap<String, String> mStackTraceMap = new HashMap<String, String>();
	
	private Connection conn;
	
    //数据库名称
    private String mDatabase = "";

    // MySQL配置时的用户名
    private String mUserName = "";
    // MySQL配置时的密码
    private String mUserPassword = ""; 
    
    private String mDirver = "";
    
    private String mUrl = "";
    
    public DBConnection() {
    }
    
    public String getDirver() {
    	checkIsInstall();
    	
    	return mDirver;
    }
    
    public String getUrl() {
    	checkIsInstall();
    	
    	return mUrl;
    }
    
    public String getUserName() {
    	checkIsInstall();
    	return mUserName;
    }
    
    public String getUserPassword() {
    	checkIsInstall();
    	
    	return mUserPassword;
    }
    
    public String getDatabaseName() {
    	checkIsInstall();
    	
    	return mDatabase;
    }
    
    /**
     * 检测是用户和数据库是否已经安装
     */
    private void checkIsInstall() {
    	initRoot();
    	
    	List<Element> userElementList = ParseXml.getInstance().getElements(Utils.getPathDB(), "user", null);
		for(int i=0; i<userElementList.size(); i++) {
			Element element = (Element) userElementList.get(i);
			String install = element.attributeValue("install");
			if("1".equals(install)) {
				mUserName = element.attributeValue("name");
				mUserPassword = element.attributeValue("password");
			}
		}
		
		List<Element> databaseElementList = ParseXml.getInstance().getElements(Utils.getPathDB(), "database", null);
		for(int i=0; i<databaseElementList.size(); i++) {
			Element element = (Element) databaseElementList.get(i);
			String install = element.attributeValue("install");
			if("1".equals(install)) {
				mDatabase = element.attributeValue("name");
			}
		}
    }
    
    /**
     * 初始超级管理员用户登录模式
     */
    private void initRoot() {
    	List<Element> rootElementList = ParseXml.getInstance().getElements(Utils.getPathDB(), "root", null);
		for(int i=0; i<rootElementList.size(); i++) {
			Element element = (Element) rootElementList.get(i);
			mUserName = element.attributeValue("name");
			mUserPassword = element.attributeValue("password");
			mDatabase = element.attributeValue("database");
			mDirver = element.attributeValue("dirver");
			mUrl = element.attributeValue("url");
		}
    }
    
    /**
     * 打开并连接数据库
     * @return
     */
	public Connection open() {
		//初始化超级管理员
    	initRoot();
    	//初始化注册用户权限
    	checkIsInstall();
        return open(mDirver, mUrl+mDatabase, mUserName, mUserPassword);
	}
	
	/**
     * 打开并连接数据库
     * @return
     */
	public Connection open(String dirver, String url, String userName, String userPassword) {
        try {
        	if(null!=conn) {
    			if(!conn.isClosed()) {
    				conn.close();
    				conn = null;
    			}
    		}
        	
        	System.out.println("Create New DBConnection=="+dirver+";"+url+";"+userName+";"+userPassword+";");
        	
			Class.forName(dirver);
			// 连续数据库
	        conn = DriverManager.getConnection(url, userName, userPassword); 
		} catch (Exception e) {
			conn = null;
			e.printStackTrace();
		}
        
        return conn;
	}
	
	public void close() {
		try {
        	if(null!=conn) {
    			if(!conn.isClosed()) {
    				conn.close();
    				conn = null;
    			}
    		}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
     * 打开并连接数据库
     * @return
     */
	private Connection startConn(String dirver, String url, String userName, String userPassword) throws Exception {
		Connection conn = null;
    	System.out.println("Create New DBConnection=="+dirver+";"+url+";"+userName+";"+userPassword+";");

    	Class.forName(dirver);
		// 连续数据库
		conn = DriverManager.getConnection(url, userName, userPassword); 
        
        return conn;
	}
	
	/**
	  * 获取数据库连接通道
	  * @param request			客户端请求对象
	  * @param dbDirver			数据库驱动
	  * @param dbUrl			数据库地址
	  * @param userName			用户名
	  * @param userPassword		密码
	  * @return	返回缓存中的连接通道或返回新建的连接通道
	  */
	 public static Connection openConnection(String key, 
			 							String dbDirver, 
			 							String dbUrl, 
			 							String userName, 
			 							String userPassword) {
		 HashMap<String, String> tempUserHM = new HashMap<String, String>();
		 tempUserHM.put(USER_NAME, userName);
		 tempUserHM.put(USER_PASSWORD, userPassword);
		 tempUserHM.put(DB_DIRVER, dbDirver);
		 tempUserHM.put(DB_URL, dbUrl);
		 
		 //从缓存中取出数据库连接通道
		 Connection conn = mConnectionMap.get(key);
		 HashMap<String, String> userHM = mConnUserMap.get(key);
		 if (conn!=null && tempUserHM.equals(userHM)) {
			 return conn;
		 } else if (conn!=null && !tempUserHM.equals(userHM)) {
			 removeInfoByKey(key);
		 }
		 
		 try {
			 //创建新的数据库连接通道
			 DBConnection dbConnection = new DBConnection();
			 conn =  dbConnection.startConn(dbDirver, dbUrl, userName, userPassword);
			 //
			 mConnUserMap.put(key, tempUserHM);
			 //缓存当前数据库连接通道
			 mConnectionMap.put(key, conn);
		 } catch (Exception ev) {
			 ev.printStackTrace();
			 mStackTraceMap.put(key, ev.getMessage());
		 }
		 return conn;
	 }
	 
	 /**
	  * 
	  * @param key
	  * @return
	  */
	 public static Connection getConnectionByKey(String key) {
		 return mConnectionMap.get(key);
	 }
	 
	 public static String getStackTraceByKey(String key) {
		 String message = mStackTraceMap.get(key);
		 mStackTraceMap.remove(key);
		 return message;
	 }
	 /**
	  * 清空全局缓存数据
	  * @param key	关键字
	  */
	 public static void removeInfoByKey(String key) {
		 //清空数据库连接通道
		 Connection conn = mConnectionMap.get(key);
		 if(conn!=null) {
			 try {
				 //关闭数据库连接通道
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			 mConnectionMap.remove(key);
		 }
		 
		 //清空登录数据
		 mConnUserMap.remove(key);
		 
		 //清空异常信息
		 mStackTraceMap.remove(key);
	 }
	 
}
