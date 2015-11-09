package com.homework.auth.util;

import hk.cloudcall.tools.EncryptTool;


/**
 * 加密解密工具
 * @author kevin.xie
 */
public class EncryptUtil {

	/**
	 * 对称性加解密:传入原文即返回密文；传入密文即返回原文
	 * @param original
	 * @return
	 */
	public static String encdec(String text) {
		return ByteString.bytesToString(EncryptTool.encdec(text.getBytes()));
	}
	

}
