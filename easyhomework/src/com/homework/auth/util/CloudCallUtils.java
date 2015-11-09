package com.homework.auth.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

@Service
public class CloudCallUtils {
	private static Logger logger = Logger.getLogger("parameterlog");
	//下发短信的配置
	private String sms_url="http://webservice.hctcom.com/service.asmx/SendSMS?";//发送短信的地址
	private String x_id="easyhome_sms";//账号
	private String x_pwd="easyhome123456!";//密码

	public int getRandInt(int left, int right) {
		int range = right - left;

		return left
				+ (int) ((float) Math.abs(new Random().nextInt()) % 1000 / 1000 * range);
	}

	public JSONObject getJSONObject(HttpServletRequest req) throws IOException, ServletRequestBindingException {
		String scheme = req.getScheme();
		JSONObject jo = null;
		if ("https".equalsIgnoreCase(scheme)) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			int i = -1;
			InputStream in = req.getInputStream();
			while ((i = in.read()) != -1) {
				baos.write(i);
			}
			String body = baos.toString();
			logger.info("[getJSONObject] post:" + body);
			jo = JSON.parseObject(body);
		} else {
			String decPostStr = this.decryptParam(req);
			jo = JSON.parseObject(decPostStr);
		}
		
		logger.info("[getJSONObject] post:" + jo);
		return jo;
	}

	public String getPostString(HttpServletRequest req) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int i = -1;
		InputStream in = req.getInputStream();
		while ((i = in.read()) != -1) {
			baos.write(i);
		}
		//String body = baos.toString();
		byte[] resultByte = baos.toByteArray();            
		String body = new String(resultByte, "UTF-8");	
        
		// System.out.println("[getPostStirng] post:" + body);
		return body;
	}

	public boolean checkHeads(WebRequest arg0) {
		// 必填的头部
		String appKey = arg0.getHeader("appkey");
		String deviceType = arg0.getHeader("devicetype");
		String deviceId = arg0.getHeader("deviceid");
		String mac = arg0.getHeader("mac");
		String language = arg0.getHeader("language");

		logger.debug("appkey=" + appKey + ",deviceType=" + deviceType
				+ ",deviceId=" + deviceId + ",mac=" + mac + ",language="
				+ language);

		boolean bValidHeader = verifyAppKey(appKey, deviceType, deviceId, mac,
				language);

		logger.debug("checkHeads: " + bValidHeader);
		return bValidHeader;

	}
	
	public boolean checkHttpHeader(JSONObject header){
		String appKey = header.getString("appkey");
		String deviceType = header.getString("devicetype");
		String deviceId = header.getString("deviceid");
		String mac = header.getString("mac");
		String language = header.getString("language");

		logger.debug("appkey=" + appKey + ",deviceType=" + deviceType
				+ ",deviceId=" + deviceId + ",mac=" + mac + ",language="
				+ language);

		/*
		 * // 可选的头部 try { String deviceName=arg0.getHeader("divicename"); String
		 * marketId = arg0.getHeader("marketid"); String osVersion =
		 * arg0.getHeader("osversion"); String appVersion =
		 * arg0.getHeader("appversion"); String protocol =
		 * arg0.getHeader("protocol"); String token=arg0.getHeader("token");
		 * 
		 * System.out.println("deviceName="+deviceName);
		 * System.out.println("marketId=" + marketId);
		 * System.out.println("osVersion=" + osVersion);
		 * System.out.println("appVersion=" + appVersion);
		 * System.out.println("protocol=" + protocol);
		 * System.out.println("token=" + token); } catch (Exception ex) { }
		 */

		boolean bValidHeader = verifyAppKey(appKey, deviceType, deviceId, mac,
				language);

		logger.debug("checkHeads: " + bValidHeader);
		return bValidHeader;
	}

	/**
	 * @param appKey
	 * @param deviceType
	 * @param deviceId
	 * @param mac
	 * @param language
	 * @return
	 */
	private boolean verifyAppKey(String appKey, String deviceType,
			String deviceId, String mac, String language) {
		// 检查头部值
		String appkey_cc = "CloudCallBill"; // 云呼客户端的
		String appkey_gcl = "GroupCallBill"; // 群呼客户端的
		String appkey_gct = "GroupChatBill"; // 群聊客户端的
		String appkey_cct = "CloudCallApp"; // 群聊客户端的?
		String appkey_co = "CloudComBill"; //
		String appkey_svr = "CCSERVER"; // 云呼的服务器
		String appkey_hot = "HotAppBill" ;//HotApp
		boolean bValidHeader = (((appKey != null) && (appKey.equals(appkey_cc)
				|| appKey.equals(appkey_gcl) || appKey.equals(appkey_gct) || appKey.equals(appkey_co)
				|| appKey.equals(appkey_cct)  || appKey.equals(appkey_svr)) || appKey.equals(appkey_hot))
				&& deviceType != null
				&& deviceId != null && mac != null && language != null);
		return bValidHeader;
	}
	
	
	public boolean checkHttpsHeader(HttpServletRequest req){
		String appKey = req.getHeader("appkey");
		String deviceType = req.getHeader("devicetype");
		String deviceId = req.getHeader("deviceid");
		String mac = req.getHeader("mac");
		String language = req.getHeader("language");

		logger.debug("appkey=" + appKey + ",deviceType=" + deviceType
				+ ",deviceId=" + deviceId + ",mac=" + mac + ",language="
				+ language);


		boolean bValidHeader = verifyAppKey(appKey, deviceType, deviceId, mac,
				language);

		logger.debug("checkHeads: " + bValidHeader);
		return bValidHeader;
	}
	public String generateToken(String num) {
		int head = 0;
		double tail = 0;
		BigInteger number = new BigInteger(num);
		String reverseNumber;
		int token = 0;
		head = number.multiply(new BigInteger("71")).add(new BigInteger("351"))
				.remainder(new BigInteger("999")).intValue();
		StringBuffer sb = new StringBuffer(String.valueOf(num));
		reverseNumber = sb.reverse().substring(0, sb.length() - 1).toString();
		BigInteger reverseNum = new BigInteger(reverseNumber);
		tail = reverseNum.multiply(new BigInteger("17"))
				.add(new BigInteger("137")).remainder(new BigInteger("999"))
				.intValue();
		token = (int) ((head * 100) + tail);
		token %= 100000;
		
		StringBuffer password = new StringBuffer(token + "") ;
		while(password.length() < 5){
			password.insert(0,"0");
		}
		return password.toString();
	}

	private String decryptParam(HttpServletRequest request) throws IOException,
			ServletRequestBindingException {

		
		String decString = null;
		if (request instanceof MultipartHttpServletRequest) {
			MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
			CommonsMultipartFile file = (CommonsMultipartFile) multipartRequest.getFile("param");

			if (file != null && !file.isEmpty()) {
				byte[] encParam = file.getBytes();
//				decString = new String(EncryptTool.encdec(encParam), "UTF-8");
				decString = new String(encParam, "UTF-8");
				
			} else {
				logger.info("[decryptParam]param NOT found");
			}
		}
		else {
			logger.info("[decryptParam] Not a MultipartHttpServletRequest");
		}
		return decString;
	}
	/**
	 * 9位随机数
	 * @return
	 */
	private Long generateMsgId(){
		Random random = new Random(); 
        String result="";

        for(int i=0;i<9;i++){
            result+=random.nextInt(10);    
        }
		return new Long(result);
	}
	/**
		 * 下发短信
		 * @param mobile
		 * @param content
		 * @param smsType
		 * @throws Exception
		 */
		public boolean sendSms(String mobile, String content) throws Exception{
			boolean isSuccess = false;
			HttpURLConnection httpconn = null;
			String result="";
			try{
				if( StringUtils.isEmpty(mobile) ){
					throw new Exception("手机号码不能为空！");
				}
				if( StringUtils.isEmpty(content) ){
					throw new Exception("短信内容不能为空！");
				}
				
				StringBuilder sb = new StringBuilder();
				sb.append(sms_url);
				sb.append("uc=").append(x_id);
				sb.append("&pwd=").append(x_pwd);
				sb.append("&callee=").append(mobile);
				sb.append("&cont=").append(URLEncoder.encode(content, "utf-8")); 
				sb.append("&msgid=").append(generateMsgId()); 
				sb.append("&otime=").append("");
				logger.debug("[sendSms] message  :"+sb.toString());
				URL url = new URL(sb.toString());
				
				httpconn = (HttpURLConnection) url.openConnection();
				BufferedReader br = new BufferedReader(new InputStreamReader(httpconn.getInputStream()));
				
				String line = null;
				while ((line = br.readLine()) != null) {
					result += line;
				} 
				br.close();
				isSuccess = true;
				logger.debug("[sendSms] result:"+result);
				result = "0";
			}catch(Exception e){
				result = "-99";
				e.printStackTrace();
				isSuccess = false;
			}finally{
				if(httpconn!=null){
					httpconn.disconnect();
					httpconn=null;
				}
			}
			return isSuccess;
		}
}
