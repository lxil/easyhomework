package com.homework.auth.util;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

@Service
public class ExceptionMessageParser {
	public static final String ILLEGAL_HEAD = "illegalhead";

	public JSONObject getExceptionResponse(String exMessage) {

		JSONObject jsonObject = null;
		if (exMessage.equals(ILLEGAL_HEAD)) {
			jsonObject = new JSONObject();
			jsonObject.put("result", "failed");
			jsonObject.put("text", "错误的http头部");
		} else {

		}
		return jsonObject;
	}

	public static String getExJSonString(String msg) {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("result", "failed");
		jsonObject.put("text", msg);

		return jsonObject.toJSONString();
	}
}
