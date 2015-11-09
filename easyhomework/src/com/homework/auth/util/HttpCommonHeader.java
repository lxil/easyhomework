/*
 *  copy from maple's bean/HttpsHead.java
 *  处理计费接口里自定义的 HTTPS 公共头部
 */

package com.homework.auth.util;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

/**
 * 处理Http请求的公共头部
 */
public class HttpCommonHeader {
	private static Logger logger = Logger.getLogger("parameterlog");
	
	private String appkey;
	private String devicetype;
	private String deviceid;
	private String devicename;
	private String mac;
	private String osversion;
	private String appversion;
	private String marketid;
	private int protocol;
	private String token;
	private String language;

	public String getAppkey() {
		return appkey;
	}

	public void setAppkey(String appkey) {
		this.appkey = appkey;
	}

	public String getDevicetype() {
		return devicetype;
	}

	public void setDevicetype(String devicetype) {
		this.devicetype = devicetype;
	}

	public String getDeviceid() {
		return deviceid;
	}

	public void setDeviceid(String deviceid) {
		this.deviceid = deviceid;
	}

	public String getDevicename() {
		return devicename;
	}

	public void setDevicename(String devicename) {
		this.devicename = devicename;
	}

	public String getMac() {
		return mac;
	}

	public void setMac(String mac) {
		this.mac = mac;
	}

	public String getOsversion() {
		return osversion;
	}

	public void setOsversion(String osversion) {
		this.osversion = osversion;
	}

	public String getAppversion() {
		return appversion;
	}

	public void setAppversion(String appversion) {
		this.appversion = appversion;
	}

	public int getProtocol() {
		return protocol;
	}

	public void setProtocol(int protocol) {
		this.protocol = protocol;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getMarketid() {
		return marketid;
	}

	public void setMarketid(String marketid) {
		this.marketid = marketid;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	@Override
	public String toString() {
		return "HttpsHead [appkey=" + appkey + ", devicetype=" + devicetype
				+ ", deviceid=" + deviceid + ", devicename=" + devicename
				+ ", mac=" + mac + ", osversion=" + osversion + ", appversion="
				+ appversion + ", marketid=" + marketid + ", protocol="
				+ protocol + ", token=" + token + ", language=" + language
				+ "]";
	}

	/*
	 * 获取https的公共头部数据，并作检查 成功返回true， 失败返回false
	 */
	public boolean getHeaders(HttpServletRequest request) {

		// 必填字段
		try {
			setAppkey(request.getHeader("appkey"));
			setDevicetype(request.getHeader("devicetype"));
			setDeviceid(request.getHeader("deviceid"));			
			setMac(request.getHeader("mac"));
			setLanguage(request.getHeader("language"));
		} catch (Exception ex) {
			logger.error("[getHeaders]Common header error");
			ex.printStackTrace();
			return false;
		}

		// 可选字段
		try {
			setDevicename(request.getHeader("devicename")); // 这个在文档里应该要有的
			setOsversion(request.getHeader("osversion"));
			setAppversion(request.getHeader("appversion"));
			setProtocol(request.getIntHeader("protocol"));
			setMarketid(request.getHeader("marketid"));
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		// 检查头部值
		String appkey_cc = "CloudCallBill"; // 云呼客户端的	
		String appkey_gcl = "GroupCallBill"; // 群呼客户端的
		String appkey_gct = "GroupChatBill"; // 群聊客户端的
		String appkey_cct = "CloudCallApp"; // 群聊客户端的?
		String appkey_yt = "YunTongBill"; // 云通客户端的
		String appkey_svr = "CCSERVER"; // 云呼的服务器
		String appkey_co = "CloudComBill"; // 云呼客户端的
    	String appkey_ctl = "21CTLBill"; // 21CTL客户端的
    	String appkey_hot = "HotAppBill"; // HotApp客户端的
		boolean bValidHeader = (
				((appkey != null) 
						&& (appkey.equals(appkey_cc)
						|| appkey.equals(appkey_gcl)
						|| appkey.equals(appkey_gct)
						|| appkey.equals(appkey_cct)
						|| appkey.equals(appkey_yt)
						|| appkey.equals(appkey_co)
						|| appkey.equals(appkey_ctl)
						|| appkey.equals(appkey_svr)
						|| appkey.equals(appkey_hot)))
				&& devicetype!=null
				&& deviceid!=null
				&& mac!=null
				&&language!=null);

		return bValidHeader;
	}
}