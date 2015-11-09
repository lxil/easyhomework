package com.homework.auth.web;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.alibaba.fastjson.JSONObject;
import com.homework.auth.exception.ValifyException;
import com.homework.auth.util.CloudCallUtils;

/**
 * 接口请求助手，处理请求中的一些事宜
 * @author Kevin.xie
 */
public final class RequestHelper {
	
	private static Logger logger = Logger.getLogger("RequestHelper");

	
	/**
	 * 接口请求验证
	 * @param req
	 * @throws ValifyException
	 */
	public static void valifyRequest (HttpServletRequest req) throws ValifyException {
		CloudCallUtils ccUtils = getApplicationContext(req).getBean(CloudCallUtils.class);
		try {
			JSONObject jo = ccUtils.getJSONObject(req);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new ValifyException();
		}
	}
	
	/**
	 * 获取应用上下文
	 * @param req
	 * @return
	 */
	public static WebApplicationContext getApplicationContext(HttpServletRequest req) {
		return WebApplicationContextUtils.getWebApplicationContext(req.getSession().getServletContext());
	}
	
	/**
	 * 获取请求的IP
	 * @param request
	 * @return
	 */
	public static String getRequestIp(HttpServletRequest request) {
		String ip = request.getHeader("x-forwarded-for");  
	    if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
	        ip = request.getHeader("Proxy-Client-IP");  
	    }  
	    if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
	        ip = request.getHeader("WL-Proxy-Client-IP");  
	    }  
	    if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
	        ip = request.getRemoteAddr();
	    }  
	    return ip;
	}
	
}
