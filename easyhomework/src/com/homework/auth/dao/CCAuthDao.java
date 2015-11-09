/**
 * 保存联系人，以RC4加密，加密后的数组值转换成字符串
 * 查询联系人，及云呼好友（isccuser=1）
 * 
 */

package com.homework.auth.dao;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;

import com.homework.auth.util.MD5Oper;

@Repository
public class CCAuthDao {
	@Autowired
	JdbcTemplate workJdbcTemplate;

	/**
	 * 通过手机号返回对应的用户ID，即userid
	 * 不存在则返回null
	 * @param userNum
	 * @return
	 */
	public String getUseridForTelnumber(String telnumber) throws Exception{
		if (telnumber==null || telnumber.isEmpty()) {
			return null;
		}
		SqlRowSet rowSet = workJdbcTemplate
				.queryForRowSet(
						"select userid from USER_ACCOUNT where telnumber=?",
						telnumber);
		
		rowSet.last();
		if (rowSet.getRow() > 0) {
			return rowSet.getString("userid");
		}else {
			return null;
		}
		
	}
	/**
	 * 根据手机号和获取短信的原因，查询最后一次获取短信的间隔分钟以及今天内获取的总数
	 * @param aniid
	 * @param reason
	 * @return
	 */
	public Map<String, Object> getAuthCodeTimeAndDiff(String aniid, String reason) {
		try {
			Map<String, Object> rowMap = workJdbcTemplate.queryForMap(
							"select count(*) as times ,MIN(TIMESTAMPDIFF(SECOND,operate_time,CURRENT_TIMESTAMP()))  as timediff from  ACCOUNT_AUTHCODE " +
							"where aniid = ? and reason =? and date(operate_time) = CURDATE()",
							aniid, reason);
			return rowMap;
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * 添加帐号验证码
	 * 
	 * @param userNumber
	 * @param authCode
	 * @param reason
	 * @param operator
	 * @return
	 */
	public boolean addAccountAuthCode(String userNumber, String authCode,
			String reason, String operator,String sendtype) {
		String sql = "insert into ACCOUNT_AUTHCODE(aniid,authcode,reason,operate_time,operator,send_type) values(?,?,?,current_timestamp(),?,?)";
		int affected = workJdbcTemplate.update(sql, userNumber, authCode, reason,
				operator,sendtype);
		return (affected > 0);
	}
	
	/**
	 * 根据获取短信的内容、手机号以及原因   返回该条短信到现在的时间差
	 * 如果该条短信不存在  返回-1
	 * @param aniid
	 * @param authCode
	 * @param reason
	 * @return
	 */
	public int checkAuthCodeOverTime(String aniid, String authCode,String reason) {
		try {
			SqlRowSet rowSet = workJdbcTemplate
					.queryForRowSet(
							"SELECT TIMESTAMPDIFF(SECOND,operate_time,CURRENT_TIMESTAMP()) as timediff FROM ACCOUNT_AUTHCODE where aniid = ? and reason = ? and authcode = ?",
							aniid, reason, authCode);
			rowSet.first();
			return rowSet.getInt("timediff");
		} catch (Exception e) {
			return -1;
		} 
	}
	
	/**
	 * 根据用户帐号和原因获取当天修改次数
	 * 
	 * @param aniid
	 * @param reason
	 * @return
	 */
	public int getPasswordModifyTimes(String aniid, String reason) {
		SqlRowSet rowSet = workJdbcTemplate
				.queryForRowSet(
						"select count(1) as times from MODIFY_PASSWORD_HISTORY where aniid=? and reason = ? and date(last_operate_time) = CURDATE() ;",
						aniid, reason);
		rowSet.first();
		return rowSet.getInt("times");
	}
	
	/**
	 * 修改用户密码
	 * 
	 * @param userid
	 * @param passwd
	 */
	public void authModifyPasswd(String userid, String passwd) {
		//对密码进行加密
		String encryptpin="";	
		encryptpin = MD5Oper.md5_encode(userid+passwd);
		String sql = "UPDATE USER_ACCOUNT set encryptpin=? where userid = ? ";
		workJdbcTemplate.update(sql,encryptpin, userid);
	}
	
	/**
	 * 添加修改密码历史记录
	 * 
	 * @param aniid
	 * @param reason
	 * @return
	 */
	public boolean addModifyPasswdHistory(String aniid, String reason,
			String password) {
		String sql = "insert into MODIFY_PASSWORD_HISTORY(aniid,reason,last_operate_time,new_password) values(?,?,current_timestamp(),?)";
		int affected = workJdbcTemplate.update(sql, aniid, reason, password);
		return (affected > 0);
	}
	
	/**
	 * 检查用户是否存在，根据userid来判断
	 * 
	 * @param userNumber
	 * @param authCode
	 * @param reason
	 * @return
	 */
	public boolean useridExist(String userid) {
		if (userid == null || userid.isEmpty()) {
			return false;
		}
		
		SqlRowSet rowSet = workJdbcTemplate
				.queryForRowSet(
						"select userid from USER_ACCOUNT where userid=?", userid);
		rowSet.last();
		return rowSet.getRow() > 0;
	}
	/**
	 * 根据用户id  获取账号信息
	 * @param userid
	 * @return
	 */
	public Map<String, Object> getUserAccount(String userid) {
		Map<String, Object> userAccount=null;
		String sql = "select * from  USER_ACCOUNT where userid =?";
		SqlRowSet rs = workJdbcTemplate.queryForRowSet(sql,
				new Object[] { userid });
		if (rs.next()) {
			userAccount = workJdbcTemplate.queryForMap(sql,
					new Object[] { userid });
		}
		return userAccount;
	}
	/**
	 * 用户注册，增加一个账号，Auth系统里的，USER_ACCOUNT表
	 * 
	 * @param aniid
	 * @param accid
	 * @param pin
	 * @param status
	 * @param operator
	 * @return
	 */
	public boolean doAddUserAccount(String userid,String aniid, String pin,String region,String userType,String recShortcode) {
		String sql = "insert INTO USER_ACCOUNT(userid,telnumber,region,createtime,encryptpin,user_type,recom_shortcode) VALUES(?,?,?,current_timestamp(),?,?,?)";
		int affected = -1;
		try {
			String enctrypt ="";
			enctrypt = MD5Oper.md5_encode(userid+pin);
			affected = workJdbcTemplate.update(sql, userid, aniid, region,enctrypt,userType,recShortcode);
		} catch (Exception e) {
			affected = -1;
			e.printStackTrace();
		}
		return (affected > 0);
	}
	
	/*
	 * 检查该用户的密码
	 * telnumber 手机号
	 * passwd 密码两次MD5后的值
	 */
	public boolean checkPasswd(String userid, String passwd) {
		String enctrypt ="";
		enctrypt = MD5Oper.md5_encode(userid+passwd);
		SqlRowSet rowSet = workJdbcTemplate
				.queryForRowSet(
						"select userid from USER_ACCOUNT where userid=? and encryptpin=?",
						userid, enctrypt);
		rowSet.last();

		boolean result = rowSet.getRow() > 0;
		return result;
	}
	
	
	/**
	 * 修改用户手机号
	 * 
	 * @param userid
	 * @param telnumber
	 */
	public int authModifyTelnumber(String userid, String telnumber) {
		try {
			String sql = "UPDATE USER_ACCOUNT set telnumber=? where userid = ? ";
			return workJdbcTemplate.update(sql,telnumber, userid);
		} catch (DataAccessException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
			return -1;
		}
	}
}
