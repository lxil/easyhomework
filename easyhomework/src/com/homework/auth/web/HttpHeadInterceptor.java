package com.homework.auth.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.ModelMap;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.WebRequestInterceptor;

import com.homework.auth.util.CloudCallUtils;
import com.homework.auth.util.ExceptionMessageParser;

public class HttpHeadInterceptor implements WebRequestInterceptor {
	@Autowired
	CloudCallUtils ccUtils;
	public HttpHeadInterceptor() {
		// TODO Auto-generated constructor stub
		//System.out.println("interceptor init");
	}

	@Override
	public void afterCompletion(WebRequest arg0, Exception arg1)
			throws Exception {
		// TODO Auto-generated method stub
		//System.out.println("ac");
	}

	@Override
	public void postHandle(WebRequest arg0, ModelMap arg1) throws Exception {
		// TODO Auto-generated method stub
		//System.out.println("post");
	}

	@Override
	public void preHandle(WebRequest arg0) throws Exception {
		// url将是  uri=/Application/web/admins.do  如此形式
		if(arg0.isSecure()){
			System.out.println("[preHandle]走https头信息检查");
			String[] descKV = arg0.getDescription(false).split("=");		
			boolean isWebpage = descKV[1].contains("/web/");
			boolean isSmsPage = descKV[1].contains("sms.do");
			if(isWebpage || isSmsPage)
				return;
			boolean isLegalRequest=ccUtils.checkHeads(arg0);
			if(!isLegalRequest){
				Exception ex = new Exception(ExceptionMessageParser.ILLEGAL_HEAD);
				throw ex;
			}
		}else{
			System.out.println("[preHandle]http，直接返回");
			return ;
		}
	}
	public static void main(String[] args) {
		System.out.println("a\nb");
	}

}
