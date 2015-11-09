package com.homework.auth.easeim;

import com.homework.auth.util.PropertiesUtils;


/**
 * Constants
 * 
 * @author Lynch 2014-09-15
 *
 */
public interface Constants {

	// API_HTTP_SCHEMA
	public static String API_HTTP_SCHEMA = "https";
	// API_SERVER_HOST
	public static String API_SERVER_HOST = PropertiesUtils.getProperties().getProperty("API_SERVER_HOST");
	// APPKEY
	public static String APPKEY = PropertiesUtils.getProperties().getProperty("APPKEY");
	// APP_CLIENT_ID
	public static String APP_CLIENT_ID = PropertiesUtils.getProperties().getProperty("APP_CLIENT_ID");
	// APP_CLIENT_SECRET
	public static String APP_CLIENT_SECRET = PropertiesUtils.getProperties().getProperty("APP_CLIENT_SECRET");
	// DEFAULT_PASSWORD
	public static String DEFAULT_PASSWORD = "123456";
	/** METHOD_DELETE value:GET */
	public static String METHOD_GET = "GET";
	/** METHOD_DELETE value:POST */
	public static String METHOD_POST = "POST";
	/** METHOD_DELETE value:PUT */
	public static String METHOD_PUT = "PUT";
	/** METHOD_DELETE value:DELETE */
	public static String METHOD_DELETE = "DELETE";
	/** USER_ROLE_APPADMIN value: appAdmin */
	public static String USER_ROLE_APPADMIN = "appAdmin";
}
