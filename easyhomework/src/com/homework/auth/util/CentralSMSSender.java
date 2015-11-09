package com.homework.auth.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class CentralSMSSender {
	private static Logger logger = Logger.getLogger("parameterlog");
	public static final int DIAL_RESULT_SUCCESS = 0 ;
	public static final int DIAL_RESULT_FAILED = 400 ;
	
	public static String sendSMS_fujian_authcode(String userNumber,String authCode){
		//发送请求给ccapi
		
		String respoString = null;
		
		JSONObject jsonParam = new JSONObject();
		jsonParam.put("appkey", "yuntong");
		jsonParam.put("phone", userNumber);
		jsonParam.put("moduleID", Config.getString("YUNTONG_AUTHCODE_SMS_MODULE_ID"));
		List param = new ArrayList();
		param.add(authCode);
		param.add(Config.getInt("AUTH_CODE_OVER_TIME")/ 60);
		param.add(Config.getString("LOGO"));
		jsonParam.put("params",	JSONArray.toJSON(param));
		logger.debug("jsonParam:"+jsonParam.toJSONString());
		logger.debug("SMS_SERVER_URL:"+Config.getString("SMS_SERVER_URL"));
		respoString = HttpPost.postHttpReq(jsonParam.toJSONString().toString(),Config.getString("SMS_SERVER_URL"));
		
		return respoString;
		
	}
	
	public static JSONObject sendSMS_internation_authcode(String userNumber,String content){
		//发送请求给ccapi
		JSONObject jo = new JSONObject() ;
		
		//公共信息组装
		String spid = Config.getString("SPID");
		String appid = Config.getString("APPID");
		String passwd = Config.getString("PASSWD");
		long timeStamp = System.currentTimeMillis()/1000;
		String signature = MD5Oper.md5_encode(spid + "#" + passwd + "#" + timeStamp);
		
		jo.put("Spid", spid);
		jo.put("Appid", appid);
		jo.put("Passwd", passwd);
		jo.put("TimeStamp", timeStamp);		
		jo.put("Signature", signature);
		
		//加入主要信息
		jo.put("Number", userNumber);
		jo.put("Content", content);
		
		String req = jo.toString() ;
		logger.debug("[sendSMS_internation_authcode] pre-post req:" + req);

		String url = Config.getString("SMS_INTERNATIONAL_URL");  // ConstantValue.VOICE_NOTICE_URL;
		logger.debug("[sendSMS_internation_authcode] post url:" + url);
		
		//信息发送
		String postResult = postHttpReq(req, url);
		logger.debug("[sendSMS_internation_authcode] postResult:" + postResult);
		
		JSONObject resultJo = JSONObject.parseObject(postResult);

		return resultJo;
	//	return resultJo.getString("result") ;
	}
	
	public static JSONObject sendSMS_twilio_authcode(String userNumber,String content){
		//发送请求给ccapi
		JSONObject jo = new JSONObject() ;
		
		//公共信息组装
		String spid = Config.getString("SPID");
		String appid = Config.getString("APPID");
		String passwd = Config.getString("PASSWD");
		long timeStamp = System.currentTimeMillis()/1000;
		String signature = MD5Oper.md5_encode(spid + "#" + passwd + "#" + timeStamp);
		
		jo.put("Spid", spid);
		jo.put("Appid", appid);
		jo.put("Passwd", passwd);
		jo.put("TimeStamp", timeStamp);		
		jo.put("Signature", signature);
		
		//加入主要信息
		jo.put("Number", userNumber);
		jo.put("Content", content);
		
		String req = jo.toString() ;
		logger.debug("[sendSMS_twilio_authcode] pre-post req:" + req);

		String url = Config.getString("SMS_TWILIO_URL");  // ConstantValue.VOICE_NOTICE_URL;
		logger.debug("[sendSMS_twilio_authcode] post url:" + url);
		
		//信息发送
		String postResult = postHttpReq(req, url);
		logger.debug("[sendSMS_twilio_authcode] postResult:" + postResult);
		
		JSONObject resultJo = JSONObject.parseObject(postResult);

		return resultJo;
	//	return resultJo.getString("result") ;
	}
	
	public static JSONObject sendSMS_5csms_authcode(String userNumber,String content){
		//发送请求给ccapi
		JSONObject jo = new JSONObject() ;
		
		//公共信息组装
		String spid = Config.getString("SPID");
		String appid = Config.getString("APPID");
		String passwd = Config.getString("PASSWD");
		long timeStamp = System.currentTimeMillis()/1000;
		String signature = MD5Oper.md5_encode(spid + "#" + passwd + "#" + timeStamp);
		
		jo.put("Spid", spid);
		jo.put("Appid", appid);
		jo.put("Passwd", passwd);
		jo.put("TimeStamp", timeStamp);		
		jo.put("Signature", signature);
		
		//加入主要信息
		jo.put("Number", userNumber);
		jo.put("Content", content);
		
		String req = jo.toString() ;
		logger.debug("[sendSMS_5csms_authcode] pre-post req:" + req);

		String url = Config.getString("SMS_5CSMS_URL");  // ConstantValue.VOICE_NOTICE_URL;
		logger.debug("[sendSMS_5csms_authcode] post url:" + url);
		
		//信息发送
		String postResult = postHttpReq(req, url);
		logger.debug("[sendSMS_5csms_authcode] postResult:" + postResult);
		
		JSONObject resultJo = JSONObject.parseObject(postResult);

		return resultJo;
	//	return resultJo.getString("result") ;
	}
	public static String postHttpReq(String json, String url) {
		HttpClient httpClient = new HttpClient();

		byte b[] = null;
		try {
			b = json.getBytes("UTF-8");// 把字符串转换为二进制数据

		} catch (Exception e) {
			// TODO: handle exception
		}

		RequestEntity requestEntity = new ByteArrayRequestEntity(b);

		EntityEnclosingMethod postMethod = new PostMethod();

		postMethod.setRequestEntity(requestEntity);// 设置数据
		postMethod.setPath(url);// 设置服务的url
		postMethod.setRequestHeader("Content-Type", "text/html;charset=utf-8");// 设置请求头编码

		// 设置连接超时
		httpClient.getHttpConnectionManager().getParams()
				.setConnectionTimeout(5 * 1000);
		// 设置读取超时
		httpClient.getHttpConnectionManager().getParams()
				.setSoTimeout(20 * 1000);

		String responseMsg = "";
		int statusCode = 0;
		try {
			statusCode = httpClient.executeMethod(postMethod);// 发送请求
			//responseMsg = postMethod.getResponseBodyAsString();// 获取返回值
			
			InputStream in = postMethod.getResponseBodyAsStream();// 获取返回值

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
    		int i = -1;
    		while ((i = in.read()) != -1) {
    			baos.write(i);
    		}

    		byte[] resultByte = baos.toByteArray();            
            responseMsg = new String(resultByte, "UTF-8");	
			
		} catch (HttpException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			postMethod.releaseConnection();// 释放连接
		}

		if (statusCode != HttpStatus.SC_OK) {
			System.out.println("HTTP服务异常" + statusCode);
		}
		return responseMsg;
	}
	
	public static String parseResult(String postResult){
		Document doc = null;

		try {
			doc = DocumentHelper.parseText(postResult);
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // 将字符串转为XML

		Element rootElt = doc.getRootElement(); // 获取根节点
		Iterator iter = rootElt.elementIterator("Head");
		String resultCode = null ;
		while (iter.hasNext()) {
			Element recordEle = (Element) iter.next();
			resultCode = recordEle.elementTextTrim("Result"); // 拿到head节点下的子节点title值
		}
		return resultCode;
	}
}
