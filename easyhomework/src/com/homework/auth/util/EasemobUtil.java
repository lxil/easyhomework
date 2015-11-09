package com.homework.auth.util;
import java.awt.Image;
import java.io.File;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;
import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.glassfish.jersey.client.JerseyWebTarget;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.homework.auth.easeim.ClientSecretCredential;
import com.homework.auth.easeim.Constants;
import com.homework.auth.easeim.Credential;
import com.homework.auth.easeim.EndPoints;
import com.homework.auth.easeim.Token;


/**
 * <p>Title:环信通信工具类</p>
 * <p>Description:</p>
 *
 * @author lixiangling
 * @date Otc 30, 2015 03:29:53 PM
 * @since 1.0
 */
public class EasemobUtil {
	private static Logger LOGGER = Logger.getLogger("parameterlog");
	private static final String APPKEY = Constants.APPKEY;
	private static final JsonNodeFactory factory = new JsonNodeFactory(false);
	/** 目标类型：1 个人*/
	private static final int TARGET_TYPE_USER = 1;
	/** 目标类型：2 群组*/
	private static final int TARGET_TYPE_GROUP = 2;
	/** 文件类型：1 图片*/
	private static final int FILE_TYPE_IMG = 1;
	/** 文件类型：2 Mp3*/
	private static final int FILE_TYPE_MP3 = 2;
    // 通过app的client_id和client_secret来获取app管理员token
    private static Credential credential = new ClientSecretCredential(Constants.APP_CLIENT_ID,
            Constants.APP_CLIENT_SECRET, Constants.USER_ROLE_APPADMIN);
    static{
    	if (!match("^(?!-)[0-9a-zA-Z\\-]+#[0-9a-zA-Z]+", APPKEY)) {
			try {
				throw new Exception("错误的Appkey格式，请检查："+APPKEY);
			} catch (Exception e) {
				throw new ExceptionInInitializerError(e); 
			}
		}
    }
	/**
	 * <p>Title:注册IM用户[单个]</p>
     * <p>Description:给指定AppKey创建一个新的用户</p>
	 * @param userName String 用户名
	 * @param password String 密码
	 * @throws Exception
	 * 
	 * @author lixiangling
	 * @date Otc 30, 2015 03:29:53 PM
	 */
	public static void createNewIMUserSingle(String userName,String password)throws Exception {
		ObjectNode objectNode = factory.objectNode();
		objectNode.removeAll();
		
		if (StringUtils.isBlank(userName)) {
			LOGGER.error("用户名不能为空！");
			throw new Exception("用户名不能为空！");
		}
		if (StringUtils.isBlank(password)) {
			LOGGER.error("密码不能为空！");
			throw new Exception("密码不能为空！");
		}
		
		ObjectNode dataNode = JsonNodeFactory.instance.objectNode();
		dataNode.put("username", userName);
		dataNode.put("password", password);

		JerseyWebTarget webTarget = EndPoints.USERS_TARGET.resolveTemplate("org_name",
				APPKEY.split("#")[0]).resolveTemplate("app_name",
				APPKEY.split("#")[1]);

		objectNode = sendRequest(webTarget, dataNode, credential, Constants.METHOD_POST, null);
		if(objectNode == null){
			throw new Exception("创建用户失败");
		}
		if(LOGGER.isDebugEnabled()){
			LOGGER.debug("创建用户请求结束："+objectNode.toString());
		}
		if(objectNode.has("error") && objectNode.get("error").textValue().equals("duplicate_unique_property_exists")){
			throw new Exception("用户名"+userName+"已经存在！");
		}
	}
	/**
	 * <p>Title:发送消息</p>
     * <p>Description:</p>
	 * @param targetType int 目标类型： 1 个人  2群组
	 * @param message String 信息
	 * @param formUser String 发送者帐号
	 * @param receiveList List<String> 接收方列表
	 * @throws Exception
	 * 
	 * @author lixiangling
	 * @date Otc 30, 2015 03:29:53 PM
	 */
	public static void sendMessages(int targetType,String message,String formUser,List<String> receiveList)throws Exception{
		if(TARGET_TYPE_USER != targetType && TARGET_TYPE_GROUP != targetType){
			LOGGER.error("发送目标类型必须是个人或群组！");
		}else if(receiveList == null || receiveList.isEmpty()){
			LOGGER.error("发送目标用户不能为空！");
		}
		String targetFlag = TARGET_TYPE_USER == targetType?"users":"chatgroups";
		ArrayNode targetusers = factory.arrayNode();
		for (int i = 0,length = receiveList.size(); i < length; i++) {
			targetusers.add(receiveList.get(i));
		}
		ObjectNode txtmsg = factory.objectNode();
	    txtmsg.put("msg", message);
	    txtmsg.put("type","txt");
	    ObjectNode ext = factory.objectNode();
	    ObjectNode sendMessage = sendInfo(targetFlag, targetusers, txtmsg, formUser, ext);
	    if(sendMessage != null){
	    	if(LOGGER.isDebugEnabled()){
	    		LOGGER.debug("发送消息结束: " + sendMessage.toString());
	    	}
	    }
	}
	/**
	 * <p>Title:发送图片</p>
     * <p>Description:</p>
	 * @param targetType int 目标类型： 1 个人  2群组
	 * @param path String 图片路径
	 * @param formUser String 发送者帐号
	 * @param receiveList List<String> 接收方列表
	 * @throws Exception
	 * 
	 * @author lixiangling
	 * @date Otc 30, 2015 03:29:53 PM
	 */
	public static void sendImgs(int targetType,String formUser,List<String> receiveList,String path)throws Exception{
		if(TARGET_TYPE_USER != targetType && TARGET_TYPE_GROUP != targetType){
			LOGGER.error("发送目标类型必须是个人或群组！");
		}else if(receiveList == null || receiveList.isEmpty()){
			LOGGER.error("发送目标用户不能为空！");
		}
		
		File uploadImgFile = new File(path);
        ObjectNode imgDataNode = mediaUpload(uploadImgFile,FILE_TYPE_IMG);
        if (null != imgDataNode) {
            String imgFileUUID = imgDataNode.path("entities").get(0).path("uuid").asText();
            String shareSecret = imgDataNode.path("entities").get(0).path("share-secret").asText();
            if(LOGGER.isDebugEnabled()){
            	LOGGER.debug("上传图片文件: " + imgDataNode.toString());
            }
            ObjectNode imgmsg = factory.objectNode();
            imgmsg.put("type","img");
            imgmsg.put("url",  EndPoints.CHATFILES_TARGET.resolveTemplate("org_name", APPKEY.split("#")[0]).resolveTemplate("app_name",
                    APPKEY.split("#")[1]).getUri().toString() + imgFileUUID);
            imgmsg.put("filename", uploadImgFile.getName());
            imgmsg.put("length", uploadImgFile.length());
            imgmsg.put("secret", shareSecret);
            
            String targetFlag = TARGET_TYPE_USER == targetType?"users":"chatgroups";
    		ArrayNode targetusers = factory.arrayNode();
    		for (int i = 0,length = receiveList.size(); i < length; i++) {
    			targetusers.add(receiveList.get(i));
    		}
    		ObjectNode ext = factory.objectNode();
            ObjectNode sendMessage = sendInfo(targetFlag, targetusers, imgmsg, formUser, ext);
            if (null != sendMessage) {
            	if(LOGGER.isDebugEnabled()){
    	    		LOGGER.debug("发送消息结束: " + sendMessage.toString());
    	    	}
            }
        }
	}
	/**
	 * <p>Title:发送语音</p>
     * <p>Description:</p>
	 * @param targetType int 目标类型： 1 个人  2群组
	 * @param path String 语音文件路径
	 * @param formUser String 发送者帐号
	 * @param receiveList List<String> 接收方列表
	 * @throws Exception
	 * 
	 * @author lixiangling
	 * @date Otc 30, 2015 03:29:53 PM
	 */
	public static void sendAudio(int targetType,String formUser,List<String> receiveList,String path)throws Exception{
		File uploadAudioFile = new File(path);
        ObjectNode audioDataNode = mediaUpload(uploadAudioFile,FILE_TYPE_MP3);
        if (null != audioDataNode) {
            String audioFileUUID = audioDataNode.path("entities").get(0).path("uuid").asText();
            String audioFileShareSecret = audioDataNode.path("entities").get(0).path("share-secret").asText();
            if(LOGGER.isDebugEnabled()){
            	LOGGER.info("上传语音文件: " + audioDataNode.toString());
            }
            ObjectNode audiomsg = factory.objectNode();
            audiomsg.put("type","audio");
            audiomsg.put("url", EndPoints.CHATFILES_TARGET.resolveTemplate("org_name", APPKEY.split("#")[0]).resolveTemplate("app_name",
                    APPKEY.split("#")[1]).getUri().toString() + audioFileUUID);
            audiomsg.put("filename", uploadAudioFile.getName());
            audiomsg.put("length", uploadAudioFile.length());
            audiomsg.put("secret", audioFileShareSecret);
            String targetFlag = TARGET_TYPE_USER == targetType?"users":"chatgroups";
    		ArrayNode targetusers = factory.arrayNode();
    		for (int i = 0,length = receiveList.size(); i < length; i++) {
    			targetusers.add(receiveList.get(i));
    		}
    		ObjectNode ext = factory.objectNode();
            ObjectNode sendMessage = sendInfo(targetFlag, targetusers, audiomsg, formUser, ext);
            if (null != sendMessage) {
                LOGGER.info("发送消息结束: " + sendMessage.toString());
            }
        }
	}
	/**
	 * 发送消息
	 * 
	 * @param targetType
	 *            消息投递者类型：users 用户, chatgroups 群组
	 * @param target
	 *            接收者ID 必须是数组,数组元素为用户ID或者群组ID
	 * @param msg
	 *            消息内容
	 * @param from
	 *            发送者
	 * @param ext
	 *            扩展字段
	 * 
	 * @return 请求响应
	 */
	public static ObjectNode sendInfo(String targetType, ArrayNode target, ObjectNode msg, String from,
			ObjectNode ext) throws Exception{

		ObjectNode objectNode = factory.objectNode();

		ObjectNode dataNode = factory.objectNode();

		// check properties that must be provided
		if (!("users".equals(targetType) || "chatgroups".equals(targetType))) {
			LOGGER.error("TargetType must be users or chatgroups .");

			objectNode.put("message", "TargetType must be users or chatgroups .");

			return objectNode;
		}

		// 构造消息体
		dataNode.put("target_type", targetType);
		dataNode.put("target", target);
		dataNode.put("msg", msg);
		dataNode.put("from", from);
		dataNode.put("ext", ext);

		JerseyWebTarget webTarget = EndPoints.MESSAGES_TARGET.resolveTemplate("org_name", APPKEY.split("#")[0]).resolveTemplate(
				"app_name", APPKEY.split("#")[1]);

		objectNode = sendRequest(webTarget, dataNode, credential, Constants.METHOD_POST, null);

		objectNode = (ObjectNode) objectNode.get("data");
		for (int i = 0; i < target.size(); i++) {
			String resultStr = objectNode.path(target.path(i).asText()).asText();
			if ("success".equals(resultStr)) {
				if(LOGGER.isDebugEnabled()){
					LOGGER.debug(String.format("Message has been send to user[%s] successfully .", target.path(i).asText()));
				}
			} else if (!"success".equals(resultStr)) {
				LOGGER.error(String.format("Message has been send to user[%s] failed .", target.path(i).asText()));
				throw new Exception("发送信息失败！");
			}
		}

		return objectNode;
	}
	
	/**
	 * Send HTTPS request with Jersey
	 * 
	 * @return
	 */
	public static ObjectNode sendRequest(JerseyWebTarget jerseyWebTarget, Object body, Credential credential,
			String method, List<NameValuePair> headers) throws RuntimeException {

		ObjectNode objectNode = factory.objectNode();

		if (!match("http(s)?://([\\w-]+\\.)+[\\w-]+(/[\\w- ./?%&=]*)?", jerseyWebTarget.getUri().toString())) {
			LOGGER.error("请求地址格式不正确："+jerseyWebTarget.getUri().toString());

			objectNode.put("message", "请求地址格式不正确");

			return objectNode;
		}

		try {

			javax.ws.rs.client.Invocation.Builder inBuilder = jerseyWebTarget.request();
			if (credential != null) {
				 Token.applyAuthentication(inBuilder, credential);
			}

			if (null != headers && !headers.isEmpty()) {

				for (NameValuePair nameValuePair : headers) {
					inBuilder.header(nameValuePair.getName(), nameValuePair.getValue());
				}

			}

			Response response = null;
			if (Constants.METHOD_GET.equals(method)) {

				response = inBuilder.get(Response.class);

			} else if (Constants.METHOD_POST.equals(method)) {

				response = inBuilder.post(Entity.entity(body, MediaType.APPLICATION_JSON), Response.class);

			} else if (Constants.METHOD_PUT.equals(method)) {

				response = inBuilder.put(Entity.entity(body, MediaType.APPLICATION_JSON), Response.class);

			} else if (Constants.METHOD_DELETE.equals(method)) {

				response = inBuilder.delete(Response.class);

			}

			objectNode = response.readEntity(ObjectNode.class);
			objectNode.put("statusCode", response.getStatus());

		} catch (Exception e) {
			e.printStackTrace();
		}

		return objectNode;
	}
	
	/**
	 * 检测用户是否在线
	 * 
	 * @param targetUserName
	 * @return
	 */
	public static boolean getUserStatus(String targetUserName) throws Exception{
		if (StringUtils.isEmpty(targetUserName)) {
			LOGGER.error("帐号不能为空！");
			throw new Exception("帐号不能为空！");
			
		}
		JerseyWebTarget webTarget = EndPoints.USERS_TARGET.resolveTemplate("org_name", APPKEY.split("#")[0])
				.resolveTemplate("app_name", APPKEY.split("#")[1]).path(targetUserName).path("status");

		ObjectNode objectNode = sendRequest(webTarget, null, credential, Constants.METHOD_GET, null);
		if(objectNode.has("error")&&objectNode.get("error").textValue().equals("service_resource_not_found")){
			LOGGER.error("帐号"+targetUserName+"不存在！");
		}
		String userStatus = objectNode.get("data").path(targetUserName).asText();
		if ("online".equals(userStatus)) {
			if(LOGGER.isDebugEnabled()){
				LOGGER.debug(String.format("The status of user[%s] is : [%s] .", targetUserName, userStatus));
			}
			return true;
		} else if ("offline".equals(userStatus)) {
			if(LOGGER.isDebugEnabled()){
				LOGGER.debug(String.format("The status of user[%s] is : [%s] .", targetUserName, userStatus));
			}
			return false;
		}
		return false;
	}
	
	/**
	 * 图片/语音文件上传
	 * 
	 * @param uploadFile
	 */
	private static ObjectNode mediaUpload(File uploadFile,int fileType) throws Exception{

		ObjectNode objectNode = factory.objectNode();

		if (!uploadFile.exists()) {

			LOGGER.error("file: " + uploadFile.toString() + " is not exist!");

			objectNode.put("message", "文件不存在");

			return objectNode;
		}else if(!checkFileType(uploadFile, fileType)){
			objectNode.put("message", "文件格式不正确");

			return objectNode;
		}
		
		JerseyWebTarget webTarget = EndPoints.CHATFILES_TARGET.resolveTemplate("org_name", APPKEY.split("#")[0]).resolveTemplate(
				"app_name", APPKEY.split("#")[1]);

		List<NameValuePair> headers = new ArrayList<NameValuePair>();
		headers.add(new BasicNameValuePair("restrict-access", "true"));

		return uploadFile(webTarget, uploadFile, credential, headers);
	}
	
	private static boolean checkFileType(File uploadFile,int fileType)throws Exception{
		if(fileType == FILE_TYPE_IMG){
			Image img = ImageIO.read(uploadFile); 
			if(img == null){
				LOGGER.error("文件: " + uploadFile.toString() + " 不是图片!");
				return false;
			}
		}else if(fileType == FILE_TYPE_MP3){
			if(!uploadFile.getName().toLowerCase().endsWith(".mp3")){
				LOGGER.error("文件: " + uploadFile.toString() + " 不是Mp3!");
				return false;
			}
		}else{
			LOGGER.error("文件类型: " + fileType + " 不支持!");
		}
		return true;
	}

	/**
	 * UploadFile whit Jersey
	 * 
	 * @return
	 */
	private static ObjectNode uploadFile(JerseyWebTarget jerseyWebTarget, File file, Credential credential,
			List<NameValuePair> headers) throws RuntimeException {
		Invocation.Builder inBuilder = jerseyWebTarget.request();
		if (credential != null) {
			Token.applyAuthentication(inBuilder, credential);
		}

		if (null != headers && !headers.isEmpty()) {

			for (NameValuePair nameValuePair : headers) {
				inBuilder.header(nameValuePair.getName(), nameValuePair.getValue());
			}

		}

		FormDataMultiPart multiPart = new FormDataMultiPart();
		multiPart.bodyPart(new FileDataBodyPart("file", file, MediaType.APPLICATION_OCTET_STREAM_TYPE));
		return inBuilder.post(Entity.entity(multiPart, MediaType.MULTIPART_FORM_DATA), ObjectNode.class);
	}

	/**
	 * Check illegal String
	 * 
	 * @param regex
	 * @param str
	 * @return
	 */
	private static boolean match(String regex, String str) {
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(str);
		return matcher.lookingAt();
	}
	
	/**
	 * Obtain a JerseyClient whit SSL
	 * 
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws KeyManagementException
	 */
	public static JerseyClient getJerseyClient(boolean isSSL) {
		ClientBuilder clientBuilder = JerseyClientBuilder.newBuilder().register(MultiPartFeature.class);

		// Create a secure JerseyClient
		if (isSSL) {
			try {
				HostnameVerifier verifier = new HostnameVerifier() {
					public boolean verify(String hostname, SSLSession session) {
						return true;
					}
				};

				TrustManager[] tm = new TrustManager[] { new X509TrustManager() {

					public X509Certificate[] getAcceptedIssuers() {
						return null;
					}

					public void checkServerTrusted(X509Certificate[] chain, String authType)
							throws CertificateException {
					}

					public void checkClientTrusted(X509Certificate[] chain, String authType)
							throws CertificateException {
					}
				} };

				SSLContext sslContext = SSLContext.getInstance("SSL");

				sslContext.init(null, tm, new SecureRandom());

				clientBuilder.sslContext(sslContext).hostnameVerifier(verifier);
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			} catch (KeyManagementException e) {
				e.printStackTrace();
			}
		}

		return (JerseyClient) clientBuilder.build().register(JacksonJsonProvider.class);
	}
	
	public static void main(String[] args) throws Exception{
		createNewIMUserSingle("kenshinnuser100","123456");
	}
}
