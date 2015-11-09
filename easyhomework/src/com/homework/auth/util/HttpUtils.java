package com.homework.auth.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

public class HttpUtils {

	private static Logger log = Logger.getLogger(HttpUtils.class);
	private static Logger timelog = Logger.getLogger("time");
	private static PoolingClientConnectionManager cm;

	private static HttpClient httpclient;

	static {
		cm = new PoolingClientConnectionManager();
		cm.setMaxTotal(100);
		httpclient = new DefaultHttpClient(cm);
	}

	public static String post(String url, Map<String, String> params) {

		try {
			// 构造一个post对象
			HttpPost httpPost = new HttpPost(url);
			// 添加所需要的post内容
			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
			Iterator<String> keys = params.keySet().iterator();
			String sparams = "";
			while (keys.hasNext()) {
				String key = keys.next();
				nvps.add(new BasicNameValuePair(key, params.get(key)));
				sparams += key + ":" + params.get(key);
			}
			// nvps.add(new BasicNameValuePair("postData",
			// "<Request><Head><MethodName>IMS_QUERY</MethodName><Spid>399368</Spid><Appid>176</Appid><Passwd>2b9ffbad07157c61acea16c607f3fece7c182595</Passwd></Head><Body></Body></Request>"));

			httpPost.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));
			long start = System.currentTimeMillis();
			HttpResponse response = httpclient.execute(httpPost);
			long end = System.currentTimeMillis();
			timelog.info("||time cost=" + (end - start) + "ms||url=" + url + "||params=" + sparams);
			HttpEntity entity = response.getEntity();
			// EntityUtils.consume(entity);
			return EntityUtils.toString(entity);

		} catch (Exception e) {
			e.printStackTrace();
			log.error("post error,url=" + url, e);
			return "";
		}

	}

	public static String post(String url, byte[] body) {

		try {
			// 构造一个post对象
			HttpPost httpPost = new HttpPost(url);
			// 添加所需要的post内容
			httpPost.setEntity(new ByteArrayEntity(body));
			HttpResponse response = httpclient.execute(httpPost);
			HttpEntity entity = response.getEntity();
			// EntityUtils.consume(entity);
			return EntityUtils.toString(entity);

		} catch (Exception e) {
			log.error("post error,url=" + url, e);
			return "";
		}

	}

	public static String get(String url) {

		try {
			HttpGet httpGet = new HttpGet(url);
			HttpResponse response = httpclient.execute(httpGet);
			HttpEntity entity = response.getEntity();
			return EntityUtils.toString(entity);
		} catch (Exception e) {
			return "";
		} finally {

		}

	}

	static class Runner implements Runnable {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				HttpGet httpGet = new HttpGet("http://www.baidu.com");
				HttpResponse response = httpclient.execute(httpGet);
				HttpEntity entity = response.getEntity();
			} catch (Exception e) {
				// TODO: handle exception
			}

		}

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		// System.out.println(HttpUtils.get("http://www.baidu.com"));
		// System.out.println(HttpUtils.post("http://open.fjii.com:8088/httpIntf/dealIntf",null));

		// ThreadGroup thGroup = new ThreadGroup("MyTest");
		//
		// long start = System.currentTimeMillis();
		// for (int i = 0; i < 100; i++) {
		// new Thread(thGroup,new Runner(), "trhead-" + i).start();
		// }
		// while(thGroup.activeCount()>0){
		// try {
		// Thread.sleep(20);
		// } catch (InterruptedException e) {
		// }
		// }
		// long end = System.currentTimeMillis();
		// System.out.println("time cost: " + (end - start));
	}

}
