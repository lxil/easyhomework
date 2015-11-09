package com.homework.auth.util;
  
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.log4j.Logger;
  

  
public class HttpPost {     
	private static Logger logger = Logger.getLogger("parameterlog"); 
  
    /** 
     * 
     *  
     * @param urlStr 
     * @param fileMap 
     * @return 
     */  
    public static String formUpload(String urlStr, Map<String, String> textMap,  
            Map<String, InputStream> fileMap) {  
        String res = "";  
        HttpURLConnection conn = null;  
        String BOUNDARY = "---------------------------123821742118716"; //boundary就是request头和上传文件内容的分隔符  
        try {  
            URL url = new URL(urlStr);  
            conn = (HttpURLConnection) url.openConnection();  
            conn.setConnectTimeout(5000);  
            conn.setReadTimeout(30000);  
            conn.setDoOutput(true);  
            conn.setDoInput(true);  
            conn.setUseCaches(false);  
            conn.setRequestMethod("POST");  
            conn.setRequestProperty("Connection", "Keep-Alive");  
            conn  
                    .setRequestProperty("User-Agent",  
                            "Mozilla/5.0 (Windows; U; Windows NT 6.1; zh-CN; rv:1.9.2.6)");  
            conn.setRequestProperty("Content-Type",  
                    "multipart/form-data; boundary=" + BOUNDARY);  
  
            OutputStream out = new DataOutputStream(conn.getOutputStream());  
            // text  
            if (textMap != null) {  
                StringBuffer strBuf = new StringBuffer();  
                Iterator iter = textMap.entrySet().iterator();  
                while (iter.hasNext()) {  
                    Map.Entry entry = (Map.Entry) iter.next();  
                    String inputName = (String) entry.getKey();  
                    String inputValue = (String) entry.getValue();  
                    if (inputValue == null) {  
                        continue;  
                    }  
                    strBuf.append("\r\n").append("--").append(BOUNDARY).append(  
                            "\r\n");  
                    strBuf.append("Content-Disposition: form-data; name=\""  
                            + inputName + "\"\r\n\r\n");  
                    strBuf.append(inputValue);  
                }  
                out.write(strBuf.toString().getBytes());  
            }  
  
            // file  
            if (fileMap != null) {  
                Iterator iter = fileMap.entrySet().iterator();  
                while (iter.hasNext()) {  
                    Map.Entry entry = (Map.Entry) iter.next();  
                    String inputName = (String) entry.getKey();  
                    InputStream is = (InputStream) entry.getValue(); 
                    String contentType = "application/octet-stream";  
       
                    StringBuffer strBuf = new StringBuffer();  
                    strBuf.append("\r\n").append("--").append(BOUNDARY).append(  
                            "\r\n");  
                    strBuf.append("Content-Disposition: form-data; name=\""  
                            + inputName + "\"; filename=\"" + UUID.randomUUID()  
                            + "\"\r\n");  
                    strBuf.append("Content-Type:" + contentType + "\r\n\r\n");  
  
                    out.write(strBuf.toString().getBytes());  
                    int bytes = 0;  
                    byte[] bufferOut = new byte[1024];  
                    while ((bytes = is.read(bufferOut)) != -1) {  
                        out.write(bufferOut, 0, bytes);  
                    }  
                    is.close();  
                }  
            }  
  
            byte[] endData = ("\r\n--" + BOUNDARY + "--\r\n").getBytes();  
            out.write(endData);  
            out.flush();  
            out.close();  
  
            // 读取返回数据  
            StringBuffer strBuf = new StringBuffer();  
            BufferedReader reader = new BufferedReader(new InputStreamReader(  
                    conn.getInputStream()));  
            String line = null;  
            while ((line = reader.readLine()) != null) {  
                strBuf.append(line).append("\n");  
            }  
            res = strBuf.toString();  
            reader.close();  
            reader = null;  
        } catch (Exception e) {  
//            System.out.println("发送POST请求出错。" + urlStr);  
//            e.printStackTrace();  
            logger.error("[HttpPost] formUpload error:",e);
        } finally {  
            if (conn != null) {  
                conn.disconnect();  
                conn = null;  
            }  
        }  
        return res;  
    }  
  
    
    /*
     * 按数据原样post到 http url
     * 原样返回结果
     */
	 public static String postHttpReq(String json,String url) {
		 logger.debug("[postHttpReq]reqest:" + json);
		 logger.debug("[postHttpReq]url:" + url);
		 
	        HttpClient httpClient = new HttpClient();
	        
	        byte b[] = null;
	        try {
				b=json.getBytes("UTF-8");//把字符串转换为二进制数据			
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("[postHttpReq] exception!", e);
			}
	        
	        RequestEntity requestEntity = new ByteArrayRequestEntity(b);

	        EntityEnclosingMethod postMethod = new PostMethod();
	      
	        postMethod.setRequestEntity(requestEntity);// 设置数据
	        postMethod.setPath(url);// 设置服务的url
	        postMethod.setRequestHeader("Content-Type", "text/xml;charset=utf-8");// 设置请求头编码

	        // 设置连接超时
	        httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(
	                5 * 1000);
	        // 设置读取超时
	        httpClient.getHttpConnectionManager().getParams().setSoTimeout(20 * 1000);

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
	        	logger.error("[postHttpReq]HttpException", e);
	            e.printStackTrace();
	        } catch (IOException e) {
	            e.printStackTrace();
	            logger.error("[postHttpReq]IOException", e);
	        } finally {
	            postMethod.releaseConnection();// 释放连接
	        }

	        if (statusCode != HttpStatus.SC_OK) {
	            logger.error("[postHttpReq]HTTP服务异常" + statusCode);
	        }
	        return responseMsg;
	    }
}  