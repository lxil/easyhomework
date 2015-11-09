package com.homework.auth.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 时间类处理工具
 */
public class DateUtil {
	
	public final static String YYYYMM = "yyyyMM";

	/**
	 * 计算时间是yyyy年的ww周
	 * @param date
	 * @return
	 */
	public static String getWeekOfYear(Date date) {
		Calendar cal = Calendar.getInstance();
		if(date != null) cal.setTime(date);
		int year = cal.get(Calendar.YEAR);
		int week = cal.get(Calendar.WEEK_OF_YEAR);
		if(week==1) {
			int m = cal.get(Calendar.MONTH);
			if(m==11) {//临时解决方案
				year = year+1;
			}
		}
		return new StringBuilder().append(year).append(week).toString();
	}

	/**
	 * 按给出的格式化时间
	 * @param date
	 * @param pattern
	 * @return
	 */
	public static String format(Date date, String pattern) {
		SimpleDateFormat  df = new SimpleDateFormat (pattern);
		return df.format(date);
	}
	
	public static void main(String[] args) {
		String s = getWeekOfYear(null);
		System.out.println(s);
	}
	
}
