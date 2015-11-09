package com.homework.auth.util;
/*
 * 向https链接post数据，不需要证书、密码
 */

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;


/**
 * 自定义一个 证书信任管理器类 ，就不用证书、密码了
 */
class MyTrustManager implements X509TrustManager {
	@Override
	public void checkClientTrusted(X509Certificate[] arg0, String arg1)
			throws CertificateException {
	}
	@Override
	public void checkServerTrusted(X509Certificate[] arg0, String arg1)
			throws CertificateException {
	}
	@Override
	public X509Certificate[] getAcceptedIssuers() {
		return null;
	}

}

/**
 * 实现用于主机名验证的基接口。 
 * 在握手期间，如果 URL 的主机名和服务器的标识主机名不匹配，则验证机制可以回调此接口的实现程序来确定是否应该允许此连接。
 */
class MyHostnameVerifier implements HostnameVerifier {
	@Override
	public boolean verify(String hostname, SSLSession session) {
		return true;
/*		if("localhost".equals(hostname)){
			return true;
		} else {
			return false;
		}*/
	}
}

public class HttpsPost {
	/**
	 * 获得KeyStore.
	 * @throws Exception
	 */
	public KeyStore getKeyStore(String password, String keyStorePath)
			throws Exception {
		// 实例化密钥库
		KeyStore ks = KeyStore.getInstance("JKS");
		// 获得密钥库文件流
		FileInputStream is = new FileInputStream(keyStorePath);
		// 加载密钥库
		ks.load(is, password.toCharArray());
		// 关闭密钥库文件流
		is.close();
		return ks;
	}

	/**
	 * 获得SSLSocketFactory.
	 * @throws Exception
	 */
	public SSLContext getSSLContext() throws Exception {				
		// 实例化SSL上下文
		SSLContext ctx = SSLContext.getInstance("TLS");
		// 初始化SSL上下文

		ctx.init(new KeyManager[0],
				new TrustManager[] { new MyTrustManager()}, new SecureRandom());
	
		// 获得SSLSocketFactory
		return ctx;
	}

	/**
	 * 初始化HttpsURLConnection.
	 * @throws Exception
	 */
	public void initHttpsURLConnection() throws Exception {
		// 声明SSL上下文
		SSLContext sslContext = null;

		// todo 这一句不清楚是否有用
		//System.setProperty("java.protocol.handler.pkgs", "javax.net.ssl");

		// 实例化主机名验证接口
		HostnameVerifier hnv = new MyHostnameVerifier();
		try {
			sslContext = getSSLContext();
		} catch (GeneralSecurityException e) {
			e.printStackTrace();
		}
		if (sslContext != null) {
			HttpsURLConnection.setDefaultSSLSocketFactory(sslContext
					.getSocketFactory());
		}
		HttpsURLConnection.setDefaultHostnameVerifier(hnv);
	}

	/**
	 * 发送请求.
	 * 
	 * @param httpsUrl
	 *            请求的地址
	 * @param xmlStr
	 *            请求的数据
	 *  返回对方返回的值 String
	 */
	public String post(String httpsUrl, String xmlStr) {
		HttpsURLConnection urlCon = null;
		URL url = null;
		String result = "false";  // 默认为false
		try {
			url = new URL(httpsUrl);
			urlCon = (HttpsURLConnection)url.openConnection();
			urlCon.setDoInput(true);
			urlCon.setDoOutput(true);
			urlCon.setRequestMethod("POST");
			urlCon.setRequestProperty("Content-Length",
					String.valueOf(xmlStr.getBytes().length));
			urlCon.setUseCaches(false);

			urlCon.addRequestProperty("appkey", "CloudCallApp");
			urlCon.addRequestProperty("devicetype", "SERVER"); // Android,
			urlCon.addRequestProperty("deviceId", "00000000111111");
			urlCon.addRequestProperty("devicename", "imserver");
			urlCon.addRequestProperty("mac", "B8FF61A44664");
			urlCon.addRequestProperty("marketid", "100");
			urlCon.addRequestProperty("appversion", "1.0.0");
			urlCon.addRequestProperty("language", "CN");

			// 设置为gbk可以解决服务器接收时读取的数据中文乱码问题
			urlCon.getOutputStream().write(xmlStr.getBytes()); // .getBytes("gbk"));
			urlCon.getOutputStream().flush();
			urlCon.getOutputStream().close();
			BufferedReader in = new BufferedReader(new InputStreamReader(
					urlCon.getInputStream()));
			String line;
			StringBuffer temp = new StringBuffer();
			while ((line = in.readLine()) != null) {
				temp.append(line);
				//System.out.println(line);
			}
			result = temp.toString();
			
			urlCon.getInputStream().close();
			in.close();
		} catch (Exception e) {
			e.printStackTrace();		
		}
		
		url = null;
		urlCon = null;	
		return result;
	}

	/**
	 * 测试方法.
	 * 
	 * @param args
	 * @throws Exception
	 */
	public void main22(String[] args) throws Exception {
		// 本地起的https服务
		String baseUrl = "https://cd.cloudcall.hk:8080/Application";
		//String subUrl = "/ad/getadplatformlist.do";
		// String subUrl = "/update/getlatestversion.do";
		// String subUrl = "/social/getrefer.do";
		//String subUrl = "/social/squareapp.do";
		String subUrl = "/card/getcardlist.do";
		
		String httpsUrl = baseUrl + subUrl;
		// 传输文本
		String xmlStr = "{\"oper_type\":\"query\",\"context\":[{\"user_number\":\"18670037740\"}]}";
		HttpsPost httpsPost = new HttpsPost();
		httpsPost.initHttpsURLConnection();
		// 发起请求
		httpsPost.post(httpsUrl, xmlStr);
	}
}
