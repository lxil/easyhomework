package com.homework.auth.exception;

/**
 * 接口请求验证异常
 * @author Kevin.Xie
 */
public class ValifyException extends Exception {

	private static final long serialVersionUID = 1L;
	
	public ValifyException() {
		System.out.println(123);
	}
	
	public ValifyException(String message) {
		super(message);
	}
	
	
	public ValifyException(String message, Throwable e) {
		super(message, e);
	}
	
}
