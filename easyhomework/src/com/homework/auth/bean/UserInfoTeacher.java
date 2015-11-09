package com.homework.auth.bean;

import java.util.Date;

public class UserInfoTeacher {
	private String userid;			// 用户ID，唯一标识
	private String telnumber;		// 用户手机号码
	private String name;
	private String gender;		// 性别  male, female
	private String card_id;
	private String nature;
	private String grade;
	private String course;
	private float charge;
	private int booking_maxnum;
	private Date oper_time;
	private Double balance;
	
	public Double getBalance() {
		return balance;
	}
	public void setBalance(Double balance) {
		this.balance = balance;
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
	public String getNature() {
		return nature;
	}
	public void setNature(String nature) {
		this.nature = nature;
	}
	public String getGrade() {
		return grade;
	}
	public void setGrade(String grade) {
		this.grade = grade;
	}
	public String getCourse() {
		return course;
	}
	public void setCourse(String course) {
		this.course = course;
	}
	public float getCharge() {
		return charge;
	}
	public void setCharge(float charge) {
		this.charge = charge;
	}
	public int getBooking_maxnum() {
		return booking_maxnum;
	}
	public void setBooking_maxnum(int booking_maxnum) {
		this.booking_maxnum = booking_maxnum;
	}
	public Date getOper_time() {
		return oper_time;
	}
	public void setOper_time(Date oper_time) {
		this.oper_time = oper_time;
	}
	@Override
	public boolean equals(Object o) {
		if(o == null || !(o instanceof UserInfoTeacher)) return false;
		UserInfoTeacher u = (UserInfoTeacher) o;
		return u.getTelnumber().equals(getTelnumber()) || u.getUserid().equals(getUserid());
	}
	
}
