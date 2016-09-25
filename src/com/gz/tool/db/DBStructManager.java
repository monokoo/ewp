/**
 * @author tanguozhi
 */
package com.gz.tool.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.gz.tool.db.DBConnection;


public class DBStructManager{

	public static ArrayList<String> getAllUser(Connection conn) {
		ArrayList<String> userList = new ArrayList<String>();
		
		try {
			String sql = "SELECT DISTINCT CONCAT(user) AS query FROM mysql.user";
			PreparedStatement pstmt = conn.prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
			
			while(rs.next()) {
				String userName = rs.getString(1);
				userList.add(userName);
			}
			pstmt.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return userList;
	}
	
	/**
	 * 检测用户是否存在
	 * @param conn			数据库连接通道
	 * @param im			用户信息
	 * @return 返回处理后的结果信息（true为用户已经存在，false为用户不存在）
	 */
	public static boolean userIsExit(Connection conn, String userName) {
		boolean bFlag = false;
		
		try {
			String sql = "SELECT DISTINCT CONCAT(user) AS query FROM mysql.user";
			PreparedStatement pstmt = conn.prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
			
			while(rs.next()) {
				String _userName = rs.getString(1);
				if(_userName.equals(userName)) {
					bFlag = true;
					break;
				}
			}
			pstmt.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return bFlag;
	}
	
	/**
	 * 删除用户
	 * @param conn			数据库连接通道
	 * @param im			用户信息
	 * @return 返回处理后的结果信息（空为正常，非空为错误信息）
	 */
	public static String dropUser(Connection conn, String userName) {
		String errorInfo = "";
		//删除用户
		try {
			String sql = "drop user "+userName+"@'localhost'";
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.execute();
			
			pstmt.close();
			
		} catch (SQLException e) {
			e.printStackTrace();
			errorInfo = e.getMessage();
		}
		
		//刷新用户权限
		try {
			String sql = "flush privileges";
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.execute();
			
			pstmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
			errorInfo = e.getMessage();
		}
		
		return errorInfo;
	}
	
	/**
	 * 创建用户
	 * @param conn			数据库连接通道
	 * @param im			用户信息
	 * @return 返回处理后的结果信息（空为正常，非空为错误信息）
	 */
	public static String createUser(Connection conn, String userName, String userPassword) {
		String errorInfo = "";
		//创建用户
		try {
			String sql = "create user ?@'localhost' identified by ?";
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, userName);
//			pstmt.setString(2, im.getHost());
			pstmt.setString(2, userPassword);
			pstmt.execute();
			
			pstmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
			errorInfo = e.getMessage();
		}
		
		//用户授权
		try {
//			String sql = "grant all privileges on *.* to "+userName+"@'localhost' identified by '"+userPassword+"'";
			String sql = "grant select,insert,update,delete,create,drop on *.* to "+userName+"@'localhost' identified by '"+userPassword+"'";
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.execute();
			
			pstmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
			errorInfo = e.getMessage();
		}
		
		//刷新用户权限
		try {
			String sql = "flush privileges";
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.execute();
			
			pstmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
			errorInfo = e.getMessage();
		}
		
		return errorInfo;
	}
	
	/**
	 * 获取所有数据库列表
	 * @param conn			数据库连接通道
	 * @return 返回数据库列表
	 */
	public static ArrayList<String> getAllDatabase(Connection conn) {
		ArrayList<String> list = new ArrayList<String>();;
		
		try {
			String sql = "show databases";
			PreparedStatement pstmt = conn.prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
			
			while(rs.next()) {
				String name = rs.getString(1);
				list.add(name);
			}
			pstmt.close();
		} catch (Exception e) {
			System.out.println("");
			e.printStackTrace();
		}
		
		return list;
	}
	
	/**
	 * 检测数据库否已经存在
	 * @param conn			数据库连接通道
	 * @param databaseName	数据库名称
	 * @return 返回处理后的结果信息（true为已存在，false为不存在）
	 */
	public static boolean databaseIsExit(Connection conn, String databaseName) {
		boolean bFlag = false;
		try {
			String sql = "show databases";
			PreparedStatement pstmt = conn.prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
			
			while(rs.next()) {
				String name = rs.getString(1);
				if(name.equals(databaseName)) {
					bFlag = true;
					break;
				}
			}
			pstmt.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return bFlag;
	}
	
	/**
	 * 删除数据库
	 * @param conn			数据库连接通道
	 * @param databaseName	数据库名称
	 * @return 返回处理后的结果信息（空为正常，非空为错误信息）
	 */
	public static String dropDatabase(Connection conn, String databaseName) {
		String errorInfo = "";
		try {
			String sql = "drop database "+databaseName;
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.execute();
			
			pstmt.close();
			
		} catch (SQLException e) {
			e.printStackTrace();
			errorInfo = e.getMessage();
		}
		
		return errorInfo;
	}
	
	/**
	 * 创建数据库
	 * @param conn			数据库连接通道
	 * @param databaseName	数据库名称
	 * @return 返回处理后的结果信息（空为正常，非空为错误信息）
	 */
	public static String createDatabase(Connection conn, String databaseName) {
		String errorInfo = "";
		try {
			String sql = "create database "+databaseName;
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.execute();
			
			pstmt.close();
			
		} catch (SQLException e) {
			e.printStackTrace();
			errorInfo = e.getMessage();
		}
		
		return errorInfo;
	}

	/**
	 * 根据数据库获取所有表格列表
	 * @param conn			数据库连接通道
	 * @param databaseName	数据库名称
	 * @return	返回表格列表
	 */
	public static ArrayList<String> getAllTable(Connection conn, String databaseName) {
		ArrayList<String> list = new ArrayList<String>();
		
		try {
			String sql = "USE "+databaseName;
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.execute();
			pstmt.close();
			
			list = getAllTable(conn);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return list;
	}
	
	/**
	 * 获取所有表格列表
	 * @param conn			数据库连接通道
	 * @return 返回表格列表
	 */
	public static ArrayList<String> getAllTable(Connection conn) {
		ArrayList<String> list = new ArrayList<String>();
		
		try {
			String sql = "show tables";
			PreparedStatement pstmt = conn.prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
			
			while(rs.next()) {
				String name = rs.getString(1);
				list.add(name);
			}
			pstmt.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return list;
	}
	
	/**
	 * 检测表格是否已经创建
	 * @param conn			数据库连接通道
	 * @param tableName		表名
	 * @return 返回处理后的结果信息（true为已存在，false为不存在）
	 */
	public static boolean tableIsExit(Connection conn, String tableName) {
		boolean bFlag = false;
		
		try {
			String sql = "show tables";
			PreparedStatement pstmt = conn.prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
			
			while(rs.next()) {
				String name = rs.getString(1);
				if(name.equals(tableName)) {
					bFlag = true;
					break;
				}
			}
			pstmt.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return bFlag;
	}
	
	/**
	 * 删除表格
	 * @param conn			数据库连接通道
	 * @param tableName		表名
	 * @return 返回处理后的结果信息（空为正常，非空为错误信息）
	 */
	public static boolean dropTable(Connection conn, String tableName) {
		boolean bFlag = false;
		
		try {
			String sql = "drop table "+tableName;
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.execute();

			pstmt.close();

			bFlag = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return bFlag;
	}
	
	/**
	 * 创建表格
	 * @param conn			数据库连接通道
	 * @param tableName		表名
	 * @return 返回处理后的结果信息（空为正常，非空为错误信息）
	 */
	public static String createTable(Connection conn, String tableName) {
		String errorInfo = "";
		
		String sql = "CREATE TABLE "+tableName+" (uuid varchar(255) PRIMARY KEY)";
 
		try {
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.execute();

			pstmt.close();
			
		} catch (Exception e) {
			e.printStackTrace();
			errorInfo = e.getMessage();
		}
		
		return errorInfo;
	}
	
	/**
	 * 插入表名
	 * @param conn	数据库连接通道
	 * @param tm	表列数据
	 * @return 返回处理后的结果信息（空为正常，非空为错误信息）
	 */
	public static String insertColumn(Connection conn ,DBStructManagerModel tm) {
		String errorInfo = "";
		
		try {
			String name = tm.getColumnName();
			String type = tm.getColumnType();
			String length = tm.getColumnLength();
			String is_null = tm.getColumnIsNull();
			String default_value = tm.getColumnDefault();
			String primary_key = tm.getColumnPrimaryKey();
			
			if(length==null || "".equals(length)) {
				length = "255";
			}
 			
			String sql = "ALTER TABLE "+tm.getTableName()+" ADD "+name+" "+type+"("+length+") "+primary_key+" "+is_null+" ";
			 if(null!=default_value && !"".equals(default_value)) {
				 sql += " default '"+default_value+"'";
			 }
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.execute();
			
			pstmt.close();
			
		} catch (SQLException e) {
			e.printStackTrace();
			errorInfo = e.getMessage();
		}
		
		return errorInfo;
	}
	
	/**
	 * 获取表格的主键列名
	 * @param conn			数据库连接通道
	 * @param tableName		表名
	 * @return 返回表格的主键列名
	 */
	public static String getKeyColumn(Connection conn, String tableName) {
		String privateKeyColumn = "";
		try {
			//获取主键
			String sql = "SELECT TABLE_NAME,COLUMN_NAME FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE  WHERE TABLE_NAME='"+tableName+"'";
			PreparedStatement pstmt = conn.prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
			while(rs.next()) {
				privateKeyColumn = rs.getString("COLUMN_NAME");
			}
			pstmt.close();
		} catch (Exception ev) {
			ev.printStackTrace();
		}
		return privateKeyColumn;
	}
	
	/**
	 * 获取表格所有的列名
	 * @param conn			数据库连接通道
	 * @param tableName		表名
	 * @return 返回所有表列名称
	 */
	public static ArrayList<DBStructManagerModel> getAllColumn(Connection conn, String tableName) {
		ArrayList<DBStructManagerModel> list = new ArrayList<DBStructManagerModel>();

		try {
			//获取主键
			String privateKeyColumn = getKeyColumn(conn, tableName);
			
			String sql = "select * from information_schema.columns where table_name='"+tableName+"'";
			PreparedStatement pstmt = conn.prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
			
			while(rs.next()) {
				DBStructManagerModel dbsmm = new DBStructManagerModel();
				dbsmm.setColumnName(rs.getString("COLUMN_NAME"));
				dbsmm.setColumnDefault(rs.getString("COLUMN_DEFAULT"));
				dbsmm.setColumnIsNull(rs.getString("IS_NULLABLE"));
				dbsmm.setColumnLength(rs.getString("CHARACTER_MAXIMUM_LENGTH"));
				dbsmm.setColumnType(rs.getString("DATA_TYPE"));
				if(rs.getString("COLUMN_NAME").equals(privateKeyColumn)) {
					dbsmm.setColumnPrimaryKey("PRIMARY KEY");
				}
				
				list.add(dbsmm);
			}
			pstmt.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return list;
	}
	
	/**
	 * 判断表列是否存在
	 * @param conn			数据库连接通道
	 * @param tableName		表名
	 * @param columnName	列名	
	 * @return	返回处理后的结果信息
	 */
	public static boolean columnIsExit(Connection conn, String tableName, String columnName) {
		boolean bFlag = false;

		try {

			String sql = "select column_name from information_schema.columns where table_name='"+tableName+"'";
			PreparedStatement pstmt = conn.prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
			
			while(rs.next()) {
				String name = rs.getString(1);
				if(name.equals(columnName)) {
					bFlag = true;
					break;
				}
			}
			pstmt.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return bFlag;
	}
	
	/**
	 * 删除表格列
	 * @param conn			数据库连接通道
	 * @param tableName		表格名称
	 * @param columnName	列名
	 * @return 返回处理结果 true为成功 false为失败
	 */
	public static String dropColumn(Connection conn, String tableName, String columnName) {
		String errorInfo = "";
		
		try {
			String sql = "ALTER TABLE "+tableName+" drop "+columnName;
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.execute();
			
			pstmt.close();
			
		} catch (SQLException e) {
			e.printStackTrace();
			errorInfo = e.getMessage();
		}
		
		return errorInfo;
	}
	/**
	 * 获取表格所有的表列和数据
	 * @param conn			数据库连接通道
	 * @param tableName		表格名称
	 * @return	返回表格所有的表列和数据
	 */
	public static List<Map<String, String>> getTableAllColumnAndData(Connection conn, String tableName) {
		List<Map<String, String>> _list = new  ArrayList<Map<String, String>>();
		try {
			List<DBStructManagerModel> columnList = getAllColumn(conn, tableName);
			
			String sql = "select * from "+tableName;
			PreparedStatement pstmt = conn.prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
			
			while(rs.next()) {
				Map<String, String> mapTemp = new HashMap<String, String>();
				for(int i=0; i<columnList.size(); i++) {
					String columnName = columnList.get(i).getColumnName();
					mapTemp.put(columnName, rs.getString(columnName));
				}
				if(mapTemp.size()>0)
					_list.add(mapTemp);
			}
			
			if(_list.size()==0 && columnList.size()>0) {
				Map<String, String> mapTemp = new HashMap<String, String>();
				for(int i=0; i<columnList.size(); i++) {
					String columnName = columnList.get(i).getColumnName();
					mapTemp.put(columnName, null);
				}
				_list.add(mapTemp);
			}
			pstmt.close();
		} catch (Exception e) {
			System.out.println("");
			e.printStackTrace();
		}
		
		return _list;
	}
	
	/**
	 * 插入表格数据
	 * @param conn				数据库连接通道
	 * @param tableName			表名
	 * @param columnAndDataMap	列名和数据集合
	 * @return	返回处理后的结果信息（空为正常，非空为错误信息）
	 */
	public static String insertTableData(Connection conn, String tableName, Map<String, String> columnAndDataMap) {
		String errorInfo = "";
		
		try {
			String sName = "";
			String sValue ="";
			Iterator<String> iter = columnAndDataMap.keySet().iterator();
			while(iter.hasNext()) {
				String name = iter.next();
				String value = columnAndDataMap.get(name);
				
				sName += name;
				sValue += "'"+value+"'";
				
				if(iter.hasNext()) {
					sName += ",";
					sValue += ",";
				}
			}
			
			String sql = "insert into "+tableName+" ("+sName+") values("+sValue+")";
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.execute();
			
			pstmt.close();
		} catch (Exception e) {
			e.printStackTrace();
			errorInfo = e.getMessage();
		}
		
		return errorInfo;
	}
	
	/**
	 * 更新表格数据
	 * @param conn			数据库连接通道
	 * @param tableName		表名
	 * @param columnName	列名	
	 * @param value			列值
	 * @return	返回处理后的结果信息（空为正常，非空为错误信息）
	 */
	public static String updateTableData(Connection conn, String tableName, Map<String, String> dataMap, String columnName, String value) {
		String errorInfo = "";
		
		try {
			String upateContent = "";
			Iterator<String> iter = dataMap.keySet().iterator();
			while(iter.hasNext()) {
				String nameTemp = iter.next();
				String valueTemp = dataMap.get(nameTemp);
				
				upateContent += nameTemp+"='"+valueTemp+"'";
				
				if(iter.hasNext()) {
					upateContent += ",";
				}
			}
			
			String sql = "update "+tableName+" set "+upateContent+" where "+columnName+"=\""+value+"\"";
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.execute();
			
			pstmt.close();
			
		} catch (Exception e) {
			e.printStackTrace();
			errorInfo = e.getMessage();
		}
		
		return errorInfo;
	}
	
	/**
	 * 删除表格数据
	 * @param conn			数据库连接通道
	 * @param tableName		表名
	 * @param columnName	列名	
	 * @param value			列值
	 * @return	返回处理后的结果信息（空为正常，非空为错误信息）
	 */
	public static String removeTableData(Connection conn, String tableName, String columnName, String value) {
		String errorInfo = "";
		try {
			String sql = "delete from "+tableName+" where "+columnName+"='"+value+"'";
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.execute();
			
			pstmt.close();
			
		} catch (Exception e) {
			e.printStackTrace();
			errorInfo = e.getMessage();
		}
		
		return errorInfo;
	}
	
	/**
	 * 获取表格数据
	 * @param conn			数据库连接通道
	 * @param tableName		表名
	 * @param columnName	列名	
	 * @param value			列值
	 * @return	返回相应的数据记录
	 */
	public static Map<String, String> getTableData(Connection conn, String tableName, String columnName, String value) {
		Map<String, String> _list = new HashMap<String, String>();
		try {
			List<DBStructManagerModel> columnList = getAllColumn(conn, tableName);

			String sql = "select * from "+tableName+" where "+columnName+"=\""+value+"\"";
			PreparedStatement pstmt = conn.prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
			
			while(rs.next()) {
				for(int i=0; i<columnList.size(); i++) {
					String _columnName = columnList.get(i).getColumnName();
					_list.put(_columnName, rs.getString(_columnName));
				}
			}
			pstmt.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return _list;
	}
}
