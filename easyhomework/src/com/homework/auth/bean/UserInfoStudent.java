package com.homework.auth.bean;

import java.util.Date;

public class UserInfoStudent {
	private String userid;			// 用户ID，唯一标识
	private String telnumber;		// 用户手机号码
	private String name;
	private String gender;		// 性别  male, female
	private String card_id;
	private String school;
	private String grade;
	private Date oper_time;
	private Double balance;
	
	public Double getBalance() {
		return balance;
	}
	public void setBalance(Double balance) {
		this.balance = balance;
	}
	
	public boolean equals(Object o) {
		if(o == null || !(o instanceof UserInfoStudent)) return false;
		UserInfoStudent u = (UserInfoStudent) o;
		return u.getTelnumber().equals(getTelnumber()) || u.getUserid().equals(getUserid());
	}


	public String getUserid() {
		return userid;
	}


	public void setUserid(String userid) {
		this.userid = userid;
	}


	public String getTelnumber() {
		return telnumber;
	}


	public void setTelnumber(String telnumber) {
		this.telnumber = telnumber;
	}


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public String getGender() {
		return gender;
	}


	public void setGender(String gender) {
		this.gender = gender;
	}


	public String getCard_id() {
		return card_id;
	}


	public void setCard_id(String card_id) {
		this.card_id = card_id;
	}


	public String getSchool() {
		return school;
	}


	public void setSchool(String school) {
		this.school = school;
	}


	public String getGrade() {
		return grade;
	}


	public void setGrade(String grade) {
		this.grade = grade;
	}


	public Date getOper_time() {
		return oper_time;
	}


	public void setOper_time(Date oper_time) {
		this.oper_time = oper_time;
	}
	
	
    	
	
}
