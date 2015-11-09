package com.homework.auth.web;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSONObject;
import com.homework.auth.util.ExceptionMessageParser;

public class WebExceptionHandler implements HandlerExceptionResolver {
@Autowired
ExceptionMessageParser exMessageParser;
	@Override
	public ModelAndView resolveException(HttpServletRequest arg0,
			HttpServletResponse arg1, Object arg2, Exception arg3) {
		arg1.setCharacterEncoding("utf-8");
		arg3.printStackTrace();
		
		// TODO Auto-generated method stub
		JSONObject jsonObject =null;
		arg1.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		try{
			jsonObject =exMessageParser.getExceptionResponse(arg3.getMessage());
		}catch(Exception e){
			System.out.println("not a custom exception");
			jsonObject = new JSONObject();
			jsonObject.put("result", "failed");
			jsonObject.put("text", "unknown exception");
		}
				
			try {
				arg1.getWriter().write(jsonObject.toJSONString());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		return new ModelAndView();
	}

}
