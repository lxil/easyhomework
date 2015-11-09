package com.homework.auth.web;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.ServletRequestBindingException;

import com.alibaba.fastjson.JSONObject;
import com.homework.auth.util.CloudCallUtils;

@Controller
public class BaseController {
	@Autowired
	CloudCallUtils ccUtils;
	
	final String DEFAULT_RESPONSE_TEXT = "{\"result\":\"failed\",\"text\":\"System Error, please contact our staff\"}";
	
	private static Logger logger = Logger.getLogger("parameterlog");
	
	public final JSONObject getReq2Json(HttpServletRequest req) throws IOException, ServletRequestBindingException {
		return ccUtils.getJSONObject(req);	
	}
	
	public void flushResponse(HttpServletResponse resp, String responseText)
			throws IOException {
		responseText = responseText == null ? DEFAULT_RESPONSE_TEXT
				: responseText;
		logger.debug("response:" + responseText);
		resp.setCharacterEncoding("utf-8");
		resp.getWriter().write(responseText);
	}
	


}
