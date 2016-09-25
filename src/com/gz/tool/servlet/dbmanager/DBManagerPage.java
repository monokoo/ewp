/**
 * @author tanguozhi
 */
package com.gz.tool.servlet.dbmanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.gz.tool.db.DBStructManager;
import com.gz.tool.db.DBStructManagerModel;

public class DBManagerPage{
	private final static String SCRIPT_TAG = "#SCRIPT_TAG#";
	
	/**
	 * 公共报文头
	 * @return 返回公共报文头
	 */
	private static String getPageCommon() {
		return "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">"+
						"<html xmlns=\"http://www.w3.org/1999/xhtml\">"+
				"<head>"+
					"<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />"+
					getPageCommonCSS()+
					SCRIPT_TAG+
				"</head>";
	}
	/**
	 * 公共样式
	 * @return 返回公共样式
	 */
	private static String getPageCommonCSS() {
		return "<style type=\"text/css\">"+
					"table{"+
					    "table-layout:inherit;"+
					    "empty-cells:show; "+
					    "border-collapse: collapse;"+
					    "margin:0 auto;"+
					    "font-size:12px;"+
					"}"+
					"td {"+
					    "height:30px;"+
					"}"+
					"table.t1{"+
					    "border:1px solid #cad9ea;"+
					    "color:#666;"+
					"}"+
					"table.t1 td{"+
					    "border:1px solid #cad9ea;"+
					    "padding:0 1em 0;"+
					"}"+
					"table.t1 tr.a1{"+
					    "background-color:#f5fafe;"+
					"}"+
					"td.a1{"+
					    "background-color:#f5fafe;"+
					"}"+
				"</style>";
	}
	
	/**
	 * 获取表格列选择界面
	 * @return 返回表格列选择界面
	 */
	public static String getDBColumnAdnDataSelectPage(List<Map<String, String>> columnAndDataList, String privateKeyColumn) {
		//写入公共报文头
		String pageStr = getPageCommon();
		//自定义脚本
		String script = "<script>"+
							"function toBack() {"+
								"document.getElementsByName('"+DBManagerServlet.SUBMIT_FLAG+"')[0].value = '"+DBManagerServlet.TABLE_DATA_BACK+"';"+
								"document.getElementById('dbmanager_table_data_form').submit();"+
							"}"+
							"function toCreateTableData(columnStr, index) {"+
								"document.getElementsByName('"+DBManagerServlet.SUBMIT_FLAG+"')[0].value = '"+DBManagerServlet.TABLE_DATA_CREATE_OR_UPDATE+"';"+
								"var columns = columnStr.split(\";\");"+
								"var columnAndData = \"\";"+
								"for (var i=0; i<columns.length; i++) {"+
								      "var column = columns[i];"+
								      "if(column==\"\") continue;"+
								      "if(index==-1) index=\"\";"+
								      "columnAndData += column+\"###=###\"+document.getElementsByName(column+index)[0].value+\"###;###\";"+
								"};"+
								"document.getElementsByName('columnAndData')[0].value = columnAndData;"+
								"document.getElementById('dbmanager_table_data_form').submit();"+
							"}"+
							"function toDeleteTableData(privateKeyColumn, privateKeyColumnData) {"+
								"if(confirm('确定要删除主键为 '+privateKeyColumn+' 的数据吗？')){"+
									"document.getElementsByName('"+DBManagerServlet.SUBMIT_FLAG+"')[0].value = '"+DBManagerServlet.TABLE_DATA_DELETE+"';"+
									"document.getElementsByName('columnName')[0].value = privateKeyColumn;"+
									"document.getElementsByName('columnData')[0].value = privateKeyColumnData;"+
									"document.getElementById('dbmanager_table_data_form').submit();"+
								"}"+
							"}"+
						"</script>";
		//写入脚本
		pageStr = pageStr.replace(SCRIPT_TAG, script);
		
		int columnNumber = 0;
		//组织表列
		String headerTr = "";
		String createTr = "";
		if(columnAndDataList.size()>0) {
			headerTr += "<tr align='center' class='a1'>";
			createTr += "<tr align='center' class='a1'>";
			String arg = "";
			Map<String, String> columnAndDataMap = columnAndDataList.get(0);
			Iterator<String> it = columnAndDataMap.keySet().iterator();
			while (it.hasNext()) {
				String column = it.next();
				String privateKey = "";
				if(column.equals(privateKeyColumn)) {
					privateKey = "(主键)";
				}
				headerTr += "<td>"+column+privateKey+"</td>";
				createTr += "<td><input type='text' name='"+column+"'</td>";
				arg += column+";";
				columnNumber++;
			}
			headerTr += "<td></td></tr>";
			createTr += "<td><input type='button' value='新增' onclick='toCreateTableData(\""+arg+"\", -1)'/></td></tr>";
			
			columnNumber++;
		}
		
		String privateKeyColumnData = "";
		//组织表数据
		int lineNumber = 0;
		String dataTr = "";
		for (int i=0; i<columnAndDataList.size(); i++) {
			boolean isHaveData = false;
			String tempTr = "<tr align='center' class='a1'>";
			Map<String, String> columnAndDataMap = columnAndDataList.get(i);
			Iterator<String> it = columnAndDataMap.keySet().iterator();
			String arg = "";
			while (it.hasNext()) {
				String column = it.next();
				String value = columnAndDataMap.get(column);
				if(value==null) {
					continue;
				}
				if(column.equals(privateKeyColumn)) {
					privateKeyColumnData = value;
				}
				isHaveData = true;
				tempTr += "<td><input type='text' name='"+column+lineNumber+"' value='"+value+"'</td>";
				arg += column+";";
			}
			tempTr += "<td>"+
					"<input type='button' value='更新' onclick='toCreateTableData(\""+arg+"\", "+lineNumber+")'/>"+
					"<input type='button' value='删除' onclick='toDeleteTableData(\""+privateKeyColumn+"\", \""+privateKeyColumnData+"\")'/>"+
					"</tr>";
			lineNumber++;
			if(!isHaveData) {
				tempTr = "";
			}
			dataTr += tempTr;
		}
		
		//自定义BODY界面
		String body = "<body>"+
						"<form method='post' id='dbmanager_table_data_form' action='"+DBManagerServlet.REQUEST_URI+"'>"+
							"<input type='hidden' name='"+DBManagerServlet.SUBMIT_FLAG+"' value=''/>"+
							"<input type='hidden' name='columnAndData' value=''/>"+
							"<input type='hidden' name='columnName' value=''/>"+
							"<input type='hidden' name='columnData' value=''/>"+
							"<table align='center' class='t1' border=1'>"+
								"<tr align='center'>"+
									"<td colspan='"+columnNumber+"'><label>表数据</label></td>"+
								"</tr>"+
								headerTr+
								createTr+
								dataTr+
								"<tr align='center'>"+
									"<td colspan='"+columnNumber+"'>"+
										"<input type='button' value='返回' onclick='toBack()'/>"+
									"</td>"+
								"</tr>"+
							"</table>"+
						"</form>"+
					  "</body>";
		pageStr += body;
		return pageStr;
	}
	
	/**
	 * 获取表格列选择界面
	 * @return 返回表格列选择界面
	 */
	public static String getDBColumnSelectPage(ArrayList<DBStructManagerModel> columnList) {
		//写入公共报文头
		String pageStr = getPageCommon();
		//自定义脚本
		String script = "<script>"+
							"function toBack() {"+
								"document.getElementsByName('"+DBManagerServlet.SUBMIT_FLAG+"')[0].value = '"+DBManagerServlet.COLUMN_BACK+"';"+
								"document.getElementById('dbmanager_column_form').submit();"+
							"}"+
							"function toCreateColumn() {"+
								"document.getElementsByName('"+DBManagerServlet.SUBMIT_FLAG+"')[0].value = '"+DBManagerServlet.COLUMN_CREATE+"';"+
								"document.getElementById('dbmanager_column_form').submit();"+
							"}"+
							"function toDropColumn(columnName) {"+
								"if(confirm('确定要删除 '+columnName+' 表列吗？')){"+
									"document.getElementsByName('"+DBManagerServlet.SUBMIT_FLAG+"')[0].value = '"+DBManagerServlet.COLUMN_DROP+"';"+
									"document.getElementsByName('columnName')[0].value = columnName;"+
									"document.getElementById('dbmanager_column_form').submit();"+
								"}"+
							"}"+
						"</script>";
		//写入脚本
		pageStr = pageStr.replace(SCRIPT_TAG, script);
		
		String trStr = "";
		for (int i=0; i<columnList.size(); i++) {
			DBStructManagerModel dbsmm = columnList.get(i);
			String selectedChar = "$selectedChar"+i;
			String selectedInt = "$selectedInt"+i;
			
			String notNull = "$notnull"+i;
			String privateKey = "$privateKey"+i;
			trStr += "<tr align='center' class='a1'>"+
						"<td><input type='text' name='columnName' value='"+dbsmm.getColumnName()+"'/></td>"+
						"<td>"+
							"<select name='columnType'>"+
								"<option value='varchar'>varchar</option>"+
								"<option value='char' "+selectedChar+">char</option>"+
								"<option value='int' "+selectedInt+">int</option>"+
							"</select>"+
						"</td>"+
						"<td><input type='text' name='columnLength' value='"+dbsmm.getColumnLength()+"'/></td>"+
						"<td>"+
							"<select name='columnIsNull'>"+
								"<option value=''></option>"+
								"<option value='NOT NULL' "+notNull+">NOT NULL</option>"+
							"</select>"+
						"</td>"+
						"<td><input type='text' name='columnDefault' value='"+dbsmm.getColumnDefault()+"'/></td>"+
						"<td>"+
							"<select name='columnPrimaryKey'>"+
								"<option value=''></option>"+
								"<option value='PRIMARY KEY' "+privateKey+">PRIMARY KEY</option>"+
							"</select>"+
						"</td>"+
						"<td>"+
							"<input type='button' value='删除' onclick='toDropColumn(\""+dbsmm.getColumnName()+"\")' />"+
						"</td>"+
					"</tr>";
			if(dbsmm.getColumnType().equals("int")) {
				trStr = trStr.replace(selectedChar, "");
				trStr = trStr.replace(selectedInt, "selected");
			} else if(dbsmm.getColumnType().equals("char")) {
				trStr = trStr.replace(selectedChar, "selected");
				trStr = trStr.replace(selectedInt, "");
			}
			
			if("NO".equals(dbsmm.getColumnIsNull())) {
				trStr = trStr.replace(notNull, "selected");
			} else {
				trStr = trStr.replace(notNull, "");
			}
			
			if("PRIMARY KEY".equals(dbsmm.getColumnPrimaryKey())) {
				trStr = trStr.replace(privateKey, "selected");
			} else {
				trStr = trStr.replace(privateKey, "");
			}
		}
		
		//自定义BODY界面
		String body = "<body>"+
						"<form method='post' id='dbmanager_column_form' action='"+DBManagerServlet.REQUEST_URI+"'>"+
							"<input type='hidden' name='"+DBManagerServlet.SUBMIT_FLAG+"' value=''/>"+
							"<table align='center' class='t1' border=1'>"+
								"<tr align='center'>"+
									"<td colspan='7'><label>表列</label></td>"+
								"</tr>"+
								"<tr align='center' class='a1'>"+
									"<td>列名</td>"+
									"<td>类型</td>"+
									"<td>长度</td>"+
									"<td>是否为空</td>"+
									"<td>默认值</td>"+
									"<td>主键</td>"+
									"<td>操作</td>"+
								"</tr>"+
								"<tr align='center' class='a1'>"+
									"<td><input type='text' name='columnName' /></td>"+
									"<td>"+
										"<select name='columnType'>"+
											"<option value='varchar'>varchar</option>"+
											"<option value='char'>char</option>"+
											"<option value='int'>int</option>"+
										"</select>"+
									"</td>"+
									"<td><input type='text' name='columnLength'/></td>"+
									"<td>"+
										"<select name='columnIsNull'>"+
											"<option value=''></option>"+
											"<option value='NOT NULL'>NOT NULL</option>"+
										"</select>"+
									"</td>"+
									"<td><input type='text' name='columnDefault'/></td>"+
									"<td>"+
										"<select name='columnPrimaryKey'>"+
											"<option value=''></option>"+
											"<option value='PRIMARY KEY'>PRIMARY KEY</option>"+
										"</select>"+
									"</td>"+
									"<td><input type='button' value='创建' onclick='toCreateColumn()'/></td>"+
								"</tr>"+
								trStr+
								"<tr align='center'>"+
									"<td colspan='7'>"+
										"<input type='button' value='返回' onclick='toBack()'/>"+
									"</td>"+
								"</tr>"+
							"</table>"+
						"</form>"+
					  "</body>";
		pageStr += body;
		return pageStr;
	}
	
	/**
	 * 获取表格选择界面
	 * @return 返回表格选择界面
	 */
	public static String getDBTablesSelectPage(ArrayList<String> tableList) {
		//写入公共报文头
		String pageStr = getPageCommon();
		//自定义脚本
		String script = "<script>"+
							"function toBack() {"+
								"document.getElementsByName('"+DBManagerServlet.SUBMIT_FLAG+"')[0].value = '"+DBManagerServlet.TABLE_BACK+"';"+
								"document.getElementById('dbmanager_table_form').submit();"+
							"}"+
							"function gotoTable(tableName) {"+
								"document.getElementsByName('"+DBManagerServlet.SUBMIT_FLAG+"')[0].value = '"+DBManagerServlet.TABLE_SELECT+"';"+
								"document.getElementsByName('tableName')[0].value = tableName;"+
								"document.getElementById('dbmanager_table_form').submit();"+
							"}"+
							"function toCreateTable() {"+
								"document.getElementsByName('"+DBManagerServlet.SUBMIT_FLAG+"')[0].value = '"+DBManagerServlet.TABLE_CREATE+"';"+
								"document.getElementById('dbmanager_table_form').submit();"+
							"}"+
							"function toDropTable(tableName) {"+
								"if(confirm('确定要删除 '+tableName+' 表格吗？')){"+
									"document.getElementsByName('"+DBManagerServlet.SUBMIT_FLAG+"')[0].value = '"+DBManagerServlet.TABLE_DROP+"';"+
									"document.getElementsByName('tableName')[0].value = tableName;"+
									"document.getElementById('dbmanager_table_form').submit();"+
								"}"+
							"}"+
							"function toTableData(tableName) {"+
								"document.getElementsByName('"+DBManagerServlet.SUBMIT_FLAG+"')[0].value = '"+DBManagerServlet.TABLE_DATA_SELECT+"';"+
								"document.getElementsByName('tableName')[0].value = tableName;"+
								"document.getElementById('dbmanager_table_form').submit();"+
							"}"+
						"</script>";
		//写入脚本
		pageStr = pageStr.replace(SCRIPT_TAG, script);
		
		String trStr = "";
		for (int i=0; i<tableList.size(); i++) {
			trStr += "<tr align='center' class='a1'>"+
						"<td><label>"+tableList.get(i)+"</label></td>"+
						"<td>"+
							"<input type='button' value='进入' onclick='gotoTable(\""+tableList.get(i)+"\")' />"+
							"<input type='button' value='查询' onclick='toTableData(\""+tableList.get(i)+"\")' />"+
							"<input type='button' value='删除' onclick='toDropTable(\""+tableList.get(i)+"\")' />"+
						"</td>"+
					"</tr>";
		}
		
		//自定义BODY界面
		String body = "<body>"+
						"<form method='post' id='dbmanager_table_form' action='"+DBManagerServlet.REQUEST_URI+"'>"+
							"<input type='hidden' name='"+DBManagerServlet.SUBMIT_FLAG+"' value=''/>"+
							"<table align='center' class='t1' border=1'>"+
								"<tr align='center'>"+
									"<td colspan='2'><label>表</label></td>"+
								"</tr>"+
								"<tr align='center' class='a1'>"+
									"<td>"+
										"<label>表名</label>"+
									"</td>"+
									"<td>"+
										"<input type='text' name='tableName'/>"+
										"<input type='button' value='创建' onclick='toCreateTable()'/>"+
									"</td>"+
								"</tr>"+
								trStr+
								"<tr align='center'>"+
									"<td colspan='2'>"+
										"<input type='button' value='返回' onclick='toBack()'/>"+
									"</td>"+
								"</tr>"+
							"</table>"+
						"</form>"+
					  "</body>";
		pageStr += body;
		return pageStr;
	}
	
	/**
	 * 获取数据库登录成功界面
	 * @return 返回数据库登录成功界面
	 */
	public static String getDBDabasesSelectPage(ArrayList<String> databaseList) {
		//写入公共报文头
		String pageStr = getPageCommon();
		//自定义脚本
		String script = "<script>"+
							"function toBack() {"+
								"if(confirm('确定要注销当前登录吗？')){"+
									"document.getElementById('dbmanager_db_form').submit();"+
								"}"+
							"}"+
							"function toCreateDatabase() {"+
								"document.getElementsByName('"+DBManagerServlet.SUBMIT_FLAG+"')[0].value = '"+DBManagerServlet.DATABASES_CREATE+"';"+
								"document.getElementById('dbmanager_db_form').submit();"+
							"}"+
							"function toDeleteDatabase(databaseName) {"+
								"if(confirm('确定要删除 '+databaseName+' 数据库吗？')){"+
									"document.getElementsByName('"+DBManagerServlet.SUBMIT_FLAG+"')[0].value = '"+DBManagerServlet.DATABASES_DROP+"';"+
									"document.getElementsByName('databaseName')[0].value = databaseName;"+
									"document.getElementById('dbmanager_db_form').submit();"+
								"}"+
							"}"+
							"function gotoDatabase(databaseName) {"+
								"document.getElementsByName('"+DBManagerServlet.SUBMIT_FLAG+"')[0].value = '"+DBManagerServlet.DATABASES_SELECT+"';"+
								"document.getElementsByName('databaseName')[0].value = databaseName;"+
								"document.getElementById('dbmanager_db_form').submit();"+
							"}"+
						"</script>";
		//写入脚本
		pageStr = pageStr.replace(SCRIPT_TAG, script);
		
		String trStr = "";
		for (int i=0; i<databaseList.size(); i++) {
			trStr += "<tr align='center' class='a1'>"+
						"<td><label>"+databaseList.get(i)+"</label></td>"+
						"<td>" +
							"<input type='button' value='进入' onclick='gotoDatabase(\""+databaseList.get(i)+"\")' />"+
							"<input type='button' value='删除' onclick='toDeleteDatabase(\""+databaseList.get(i)+"\")' />"+
						"</td>"+
					"</tr>";
		}
		
		//自定义BODY界面
		String body = "<body>"+
						"<form method='post' id='dbmanager_db_form' action='"+DBManagerServlet.REQUEST_URI+"'>"+
							"<input type='hidden' name='"+DBManagerServlet.SUBMIT_FLAG+"' value=''/>"+
							"<table align='center' class='t1' border=1'>"+
								"<tr align='center'>"+
									"<td colspan='2'><label>数据库选择</label></td>"+
								"</tr>"+
								"<tr align='center' class='a1'>"+
									"<td>"+
										"<label>数据库名</label>"+
									"</td>"+
									"<td>"+
										"<input type='text' name='databaseName'/>"+
										"<input type='button' value='创建' onclick='toCreateDatabase()'/>"+
									"</td>"+
								"</tr>"+
								trStr+
								"<tr align='center'>"+
									"<td colspan='2'><input type='button' value='注销' onclick='toBack()'/></td>"+
								"</tr>"+
							"</table>"+
						"</form>"+
					  "</body>";
		pageStr += body;
		return pageStr;
	}
	
	/**
	 * 获取数据库登录成功界面
	 * @return 返回数据库登录成功界面
	 */
	public static String getDBUserSelectPage(ArrayList<String> userList) {
		//写入公共报文头
		String pageStr = getPageCommon();
		//自定义脚本
		String script = "<script>"+
							"function toBack() {"+
								"if(confirm('确定要注销当前登录吗？')){"+
								"document.getElementsByName('"+DBManagerServlet.SUBMIT_FLAG+"')[0].value = '';"+
									"document.getElementById('dbmanager_user_form').submit();"+
								"}"+
							"}"+
							"function toCreateUser() {"+
								"document.getElementsByName('"+DBManagerServlet.SUBMIT_FLAG+"')[0].value = '"+DBManagerServlet.USER_CREATE+"';"+
								"document.getElementById('dbmanager_user_form').submit();"+
							"}"+
							"function toDeleteUser(userName) {"+
								"if(confirm('确定要删除 '+userName+' 用户吗？')){"+
									"document.getElementsByName('"+DBManagerServlet.SUBMIT_FLAG+"')[0].value = '"+DBManagerServlet.USER_DROP+"';"+
									"document.getElementsByName('userName')[0].value = userName;"+
									"document.getElementById('dbmanager_user_form').submit();"+
								"}"+
							"}"+
						"</script>";
		//写入脚本
		pageStr = pageStr.replace(SCRIPT_TAG, script);
		
		String trStr = "";
		for (int i=0; i<userList.size(); i++) {
			if("".equals(userList.get(i).trim())) {
				continue;
			}
			trStr += "<tr align='center' class='a1'>"+
						"<td><label>"+userList.get(i)+"</label></td>"+
						"<td><label></label></td>"+
						"<td>" +
							"<input type='button' value='删除' onclick='toDeleteUser(\""+userList.get(i)+"\")' />"+
						"</td>"+
					"</tr>";
		}
		
		//自定义BODY界面
		String body = "<body>"+
						"<form method='post' id='dbmanager_user_form' action='"+DBManagerServlet.REQUEST_URI+"'>"+
							"<input type='hidden' name='"+DBManagerServlet.SUBMIT_FLAG+"' value=''/>"+
							"<table align='center' class='t1' border=1'>"+
								"<tr align='center'>"+
									"<td colspan='3'><label>用户管理</label></td>"+
								"</tr>"+
								"<tr align='center' class='a1'>"+
									"<td>"+
										"<label>用户名</label>"+
									"</td>"+
									"<td>"+
										"<label>密码</label>"+
									"</td>"+
										"<td>"+
										"<label>操作</label>"+
									"</td>"+
								"</tr>"+
								"<tr align='center' class='a1'>"+
									"<td>"+
										"<input type='text' name='userName'/>"+
									"</td>"+
									"<td>"+
										"<input type='text' name='userPassword'/>"+
									"</td>"+
									"<td>"+
										"<input type='button' value='创建' onclick='toCreateUser()'/>"+
									"</td>"+
								"</tr>"+
								trStr+
								"<tr align='center'>"+
									"<td colspan='3'><input type='button' value='注销' onclick='toBack()'/></td>"+
								"</tr>"+
							"</table>"+
						"</form>"+
					  "</body>";
		pageStr += body;
		return pageStr;
	}
	
	/**
	 * 获取数据库登录界面
	 * @return	返回数据库登录界面
	 */
	public static String getDBLoginPage() {
		//写入公共报文头
		String pageStr = getPageCommon();
		//自定义脚本
		String script = "<script>"+
							"function toLogin() {"+
								"document.getElementsByName('"+DBManagerServlet.SUBMIT_FLAG+"')[0].value = '"+DBManagerServlet.DATABASES_LOGIN+"';"+
								"document.getElementById('dbmanager_login_form').submit();"+
							"}"+
							"function toUserManager() {"+
								"document.getElementsByName('"+DBManagerServlet.SUBMIT_FLAG+"')[0].value = '"+DBManagerServlet.USER_SELECT+"';"+
								"document.getElementById('dbmanager_login_form').submit();"+
							"}"+
						"</script>";
		//写入脚本
		pageStr = pageStr.replace(SCRIPT_TAG, script);
		//自定义BODY界面
		String body = "<body>"+
						"<form method='post' id='dbmanager_login_form' action='"+DBManagerServlet.REQUEST_URI+"'>"+
							"<input type='hidden' name='"+DBManagerServlet.SUBMIT_FLAG+"' value=''/>"+
							"<table align='center' class='t1' border=1'>"+
								"<tr align='center'>"+
									"<td colspan='2'><label>欢迎登录数据管理平台</label></td>"+
								"</tr>"+
								"<tr align='center' class='a1'>"+
									"<td><label>用户账号：</label></td>"+
									"<td><input type='text' name='userName'/></td>"+
								"</tr>"+
								"<tr align='center' class='a1'>"+
									"<td><label>用户密码：</label></td>"+
									"<td><input type='password' name='userPassword'/></td>"+
								"</tr>"+
								"<tr align='center' class='a1'>"+
									"<td><label>数据库类型：</label></td>"+
									"<td>"+
										"<select name='dbType'>"+
											"<option value='com.mysql.jdbc.Driver;jdbc:mysql://'>MySql</option>"+
											"<option value=''>Oracle</option>"+
										"</select>"+
									"</td>"+
								"</tr>"+
								"<tr align='center' class='a1'>"+
									"<td><label>数据库地址：</label></td>"+
									"<td><input type='text' name='dbIP' value='127.0.0.1'/></td>"+
								"</tr>"+
								"<tr align='center' class='a1'>"+
									"<td><label>数据库端口：</label></td>"+
									"<td><input type='text' name='dbPort' value='3306'/></td>"+
								"</tr>"+
								"<tr align='center'>"+
									"<td colspan='2'>"+
										"<input type='button' value='登录' onclick='toLogin()'/>"+
										"<input type='button' value='用户管理' onclick='toUserManager()'/>"+
									"</td>"+
								"</tr>"+
							"</table>"+
						"</form>"+
					  "</body>";
		pageStr += body;
		
		return pageStr;
	}
	
	/**
	 * 获取数据库登录失败界面
	 * @return 返回数据库登录失败界面
	 */
	public static String getDBErrorPage(String errorTxt) {
		//写入公共报文头
		String pageStr = getPageCommon();
		//自定义脚本
		String script = "<script>"+
							"function toBack() {"+
								"history.back();"+
							"}"+
						"</script>";
		//写入脚本
		pageStr = pageStr.replace(SCRIPT_TAG, script);
		
		//自定义BODY界面
		String body = "<body>"+
						"<table align='center' class='t1' border=1'>"+
							"<tr align='center'>"+
								"<td><label>提示信息</label></td>"+
							"</tr>"+
							"<tr align='center' class='a1'>"+
								"<td><label>"+errorTxt+"</label></td>"+
							"</tr>"+
							"<tr align='center'>"+
								"<td><input type='button' value='返回' onclick='toBack()'/></td>"+
							"</tr>"+
						"</table>"+
					  "</body>";
		pageStr += body;
		return pageStr;
	}
	
	/**
	 * 获取数据库登录超时界面
	 * @return 返回数据库登录超时界面
	 */
	public static String getDBTimeoutPage() {
		//写入公共报文头
		String pageStr = getPageCommon();
		//自定义脚本
		String script = "<script>"+
						"</script>";
		//写入脚本
		pageStr = pageStr.replace(SCRIPT_TAG, script);
		
		//自定义BODY界面
		String body = "<body>"+
						"<form method='post' action='"+DBManagerServlet.REQUEST_URI+"'>"+
							"<input type='hidden' name='relogin' value='reloginDBManagerServlet'/>"+
							"<table align='center' class='t1' border=1'>"+
								"<tr align='center'>"+
									"<td><label>提示信息</label></td>"+
								"</tr>"+
								"<tr align='center' class='a1'>"+
									"<td><label>登录超时，请重新登录！</label></td>"+
								"</tr>"+
								"<tr align='center'>"+
									"<td><input type='submit' value='重新登录'/></td>"+
								"</tr>"+
							"</table>"+
						"</form>"+
					  "</body>";
		pageStr += body;
		return pageStr;
	}
	
}
