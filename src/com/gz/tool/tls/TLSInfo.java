package com.gz.tool.tls;

import java.util.HashMap;

public class TLSInfo {
	private static HashMap<String, HashMap<String, String>> mKeyIvMap = new HashMap<String, HashMap<String, String>>();

	public static String getInfoBySessionIdAndKey(String sessionID, String key) {
		HashMap<String, String> hm = mKeyIvMap.get(sessionID);
		if(hm!=null) {
			String value = hm.get(key);
			if(value!=null) {
				return value;
			}
		}
		return null;
	}
	
	public static void setInfoBySessionIdAndKey(String sessionID, String key, String value) {
		HashMap<String, String> hm = mKeyIvMap.get(sessionID);
		if (hm==null) {
			hm = new HashMap<String, String>();
		} 
		hm.remove(key);
		hm.put(key, value);
		
		mKeyIvMap.remove(sessionID);
		mKeyIvMap.put(sessionID, hm);
	}
	
	public static void removeInfoByKey(String key) {
		mKeyIvMap.remove(key);
	}
}
