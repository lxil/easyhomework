package com.homework.auth.service;

import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.homework.auth.dao.CCAuthDao;
import com.homework.auth.dao.UserInfoDao;
import com.homework.auth.dao.UserShortLinkDao;
import com.homework.auth.util.Config;
import com.homework.auth.util.EasemobUtil;
import com.homework.auth.util.MD5Oper;

@Service
public class AuthService {
	private static Logger logger = Logger.getLogger("parameterlog");
	@Autowired
	CCAuthDao authDao;
	@Autowired
	UserInfoDao userInfoDao;
	@Autowired
	UserShortLinkDao userShortLinkDao;
	
	/**
	 * 通过手机号返回对应的用户ID，即userid
	 * 不存在则返回null
	 * @param userNum
	 * @return
	 */
	public String getUseridForTelnumber(String telnumber) throws Exception{
		return authDao.getUseridForTelnumber(telnumber);
	}
	
	
	
	/**
	 * 根据手机号和获取短信的原因，查询最后一次获取短信的间隔分钟以及今天内获取的总数
	 * @param aniid
	 * @param reason
	 * @return
	 */
	public Map<String, Object> getAuthCodeTimeAndDiff(String aniid, String reason){
		return authDao.getAuthCodeTimeAndDiff(aniid, reason);
	}
	
	/**
	 * 提供添加用户验证码的服务
	 * @param userNumber
	 * @param authCode
	 * @param reason
	 * @param operator
	 * @return
	 */
	public boolean addUserAuthCode(String userNumber,String authCode,String reason,String operator,String sendtype){
		return authDao.addAccountAuthCode(userNumber, authCode, reason, operator,sendtype);
	}
	
	/**
	 * 根据获取短信的内容、手机号以及原因   返回该条短信到现在的时间差
	 * 如果该条短信不存在  返回-1
	 * @param aniid
	 * @param authCode
	 * @param reason
	 * @return
	 */
	public int checkAuthCodeOverTime(String aniid, String authCode,String reason){
		return authDao.checkAuthCodeOverTime(aniid, authCode, reason);
	}
	
	/**
	 * 提供获取用户一天之内修改密码次数的服务
	 * @param userNumber
	 * @param reason
	 * @return
	 */
	public int getModifyPasswordTimes(String userNumber,String reason){
		return authDao.getPasswordModifyTimes(userNumber, reason);
	}
	
	/**
	 * 提供更新密码的服务
	 * @param userid
	 * @param newPassword
	 */
	public void updatePassword(String userid,String newPassword){
		authDao.authModifyPasswd(userid, newPassword);
	}
	/**
	 * 提供添加修改密码历史记录的服务
	 * @param userNumber
	 * @param reason
	 * @param password
	 * @return
	 */
	public boolean addModifyPasswdHistory(String userNumber,String reason,String password){
		return authDao.addModifyPasswdHistory(userNumber, reason,password);
	}
	
	/**
	 * 生成制定规则的用户id
	 * 规则：非0开头的10数字的随机数
	 * @return
	 */
	private  String  generateUid(){
		long maxId = 9999999999l;
		long minId = 1000000000l;
		String uid = "";
		boolean isContinue = true;
		while(isContinue){
			uid = Math.round(Math.random() * (maxId - minId) + minId) + "";
			if(! authDao.useridExist(uid)){
				isContinue = false;
			}
		}
		return uid;
	}
	/**
	 * 提供用户注册的服务
	 * @param userNumber
	 * @param password
	 * @param status
	 * @param operator
	 * @param acctid
	 * @param timeplanid
	 * @param chargeid
	 * @param serviceid
	 * @return  注册成功返回用户id   否则返回空
	 */
	public String doUserRegist(String userNumber,String password,String operator,String reason,String numberPrefix, String marketid,String userType,String recShortcode){
		boolean flag = false;
		String uid = generateUid();
		String passwordMd5="";
		if(!flag){
			logger.info("[doUserRegist]userid is " + uid);
			passwordMd5=MD5Oper.md5_encode(MD5Oper.md5_encode(password));
			flag = authDao.doAddUserAccount(uid,userNumber, passwordMd5, numberPrefix,userType,recShortcode);
		}
		if(flag){
			registImById(uid, password, passwordMd5);
			return uid;	
		}
		return null;
				
	}
	/**
	 * 注册IM
	 * @param userid
	 * @param pass
	 * @param passMd5
	 */
	public void registImById(final String userid,final String pass,final String passMd5){
		new Thread(){
			@Override
			public void run() {
				try {
					String md5Flag=Config.getString("IM_REGISTER_MD5FLAG");
					if(md5Flag!=null && "1".equals(md5Flag)){//加密
						//注册IM服务器账号
						EasemobUtil.createNewIMUserSingle(userid, passMd5);
					}else{
						EasemobUtil.createNewIMUserSingle(userid, pass);
					}
				} catch (Exception e) {
					logger.error("[registImById] userid："+userid+"  error:", e);
				}
			}
		}.start();
	}
	/**
	 * 提供用户校验密码的服务
	 * @param userid
	 * @param password
	 * @return
	 */
	public boolean checkPassword(String userid, String password) {
		return authDao.checkPasswd(userid, password);
	}
	
	
	/* 登录认证的逻辑
	 * 目前只支持账号登录
	 * 返回的是 jSon串
	 */
	
	public JSONObject verify(String userid, String pin, JSONObject header) {
		/*
		 * 拦截器已经验证头部信息，无需再次验证
		*/
		JSONObject jo = new JSONObject();
		logger.debug("userid:" + userid + ",pin:" + pin);
		
		try {
			boolean flag = !(userid == null || userid.isEmpty()); //判断用户是否存在
			if(flag==false){
				jo.put("result", "failed");
				jo.put("text", "账号不存在");
				jo.put("errno", -431);
				return jo;
			}
			Map<String, Object> userAccMap=authDao.getUserAccount(userid);
			if(userAccMap==null || userAccMap.size()==0){
				flag=false;
			}
			if (flag) {
				// 用户存在
				// 判断用户帐号密码是否正确
				String pwd=userAccMap.get("encryptpin").toString();
				if ((MD5Oper.md5_encode(userid+pin)).equals(pwd)) {//密码正确
					String userType=userAccMap.get("user_type").toString();
					logger.info("用户存在，检查密码...");
					Map<String, Object> userMap=userInfoDao.getUserInfByIdAndType(userid, userType);
					if (userMap != null && userMap.size()>0) {
						jo = (JSONObject)JSONObject.toJSON(userMap);		// 以用户信息为实体，重新 生成一个json对象
					}
					logger.info("登录成功...");
					jo.put("result", "success");
					jo.put("text", "");
					jo.put("text", "");
			        jo.put("userid", userid);	
			        
			      //邀请好友时发送的信息  invite_friend_info
					String shortcode = userShortLinkDao.genShareShortcode(userid);
					String shortUrl=Config.getString("APP_SERVER_HTTP");
					String weiboText="欢迎使用作业通，我的邀请码"+shortcode+"，下载地址是："+ "/" +shortUrl;
					jo.put("invite_url", shortUrl);
					jo.put("invite_friend_info", weiboText);
					
				} else {
					jo.put("result", "failed");
					jo.put("text", "您输入的密码有误");
					jo.put("errno", -1);
				}

			} else {
				jo.put("result", "failed");
				jo.put("text", "账号不存在");
				jo.put("errno", -431);
			}
		} catch (Exception e) {
			logger.error("[verify] error:", e);
		}
//		String Response = jo.toString();
//		logger.info("[verify]response:" +  jo.toString());
		return jo;
	}
}
