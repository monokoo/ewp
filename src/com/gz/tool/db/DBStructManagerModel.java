/**
 * @author tanguozhi
 */
package com.gz.tool.db;

import java.io.Serializable;


public class DBStructManagerModel implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String userName, userPassword, host;
	private String databaseName;
	private String tableName;
	private String columnName, columnType, columnLength, columnIsNull, columnDefault, columnPrimaryKey;

	public String getDatabaseName() {
		if(databaseName==null) {
			return "";
		}
		return databaseName;
	}

	public void setDatabaseName(String databaseName) {
		this.databaseName = databaseName;
	}

	public String getHost() {
		if(host==null) {
			return "";
		}
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getUserName() {
		if(userName==null) {
			return "";
		}
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getUserPassword() {
		if(userPassword==null) {
			return "";
		}
		return userPassword;
	}

	public void setUserPassword(String userPassword) {
		this.userPassword = userPassword;
	}
	
	public String getTableName() {
		if(tableName==null) {
			return "";
		}
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getColumnName() {
		if(columnName==null) {
			return "";
		}
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public String getColumnType() {
		if(columnType==null) {
			return "";
		}
		return columnType;
	}

	public void setColumnType(String columnType) {
		this.columnType = columnType;
	}

	public String getColumnLength() {
		if(columnLength==null) {
			return "";
		}
		return columnLength;
	}

	public void setColumnLength(String columnLength) {
		this.columnLength = columnLength;
	}

	public String getColumnIsNull() {
		if(columnIsNull==null) {
			return "";
		}
		return columnIsNull;
	}

	public void setColumnIsNull(String columnIsNull) {
		this.columnIsNull = columnIsNull;
	}

	public String getColumnDefault() {
		if(columnDefault==null) {
			return "";
		}
		return columnDefault;
	}

	public void setColumnDefault(String columnDefault) {
		this.columnDefault = columnDefault;
	}

	public String getColumnPrimaryKey() {
		if(columnPrimaryKey==null) {
			return "";
		}
		return columnPrimaryKey;
	}

	public void setColumnPrimaryKey(String columnPrimaryKey) {
		this.columnPrimaryKey = columnPrimaryKey;
	}
	
}
