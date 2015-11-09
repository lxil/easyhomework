package com.homework.auth.web;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSONObject;
import com.homework.auth.service.AuthService;
import com.homework.auth.service.UserInfoService;
import com.homework.auth.util.CloudCallUtils;
import com.homework.auth.util.Config;
import com.homework.auth.util.MD5Oper;

@Controller
public class AccountController {

	private static Logger logger = Logger.getLogger("parameterlog");

	final String DEFAULT_RESPONSE_TEXT = "{\"result\":\"failed\",\"text\":\"操作失败\"}";
	@Autowired
	CloudCallUtils ccUtils;
	@Autowired
	AuthService authService;
	@Autowired
	UserInfoService userInfoService;

	public void flushResponse(HttpServletResponse resp, String responseText)
			throws IOException {
		responseText = responseText == null ? DEFAULT_RESPONSE_TEXT
				: responseText;
		logger.debug("response:" + responseText);
		resp.setCharacterEncoding("utf-8");
		resp.getWriter().write(responseText);
	}
	
	/**
	 * 获取验证码
	 * 
	 * @param req
	 * @param resp
	 */
	@RequestMapping(value = "/account/getAuthCode.do")
	public void getAuthCode(HttpServletRequest req, HttpServletResponse resp) {
		logger.debug("call /account/getAuthCode.do");

		String responseText = "{\"result\":\"failed\",\"text\":\"获取验证码错误\"}";
		JSONObject responseJson = new JSONObject();
//		String numberPrefix = "86" ;
		String userNumber = "" ;
		try {
			JSONObject jo = ccUtils.getJSONObject(req);
//			JSONObject header = jo.getJSONObject("http_headers") ;
			userNumber = jo.getString("telnumber");
			String reason = jo.getString("reason");
			
			if (null == userNumber || "".equals(userNumber) || null == reason) {
				responseJson.put("result", "failed");   
				responseJson.put("errno", -412);
				responseJson.put("text", "您提供的信息不足，请确认后重新提交");
			} else {
				
				//先判断用户是否存在
				boolean userExist = false ;
				String userid = authService.getUseridForTelnumber(userNumber);
				userExist = !(userid==null || userid.isEmpty());				
				
				//如果用户不存在，通过发送验证码修改密码，不允许操作，提示先注册
				if (!userExist && "renew".equalsIgnoreCase(reason)) {
					responseJson.put("result", "failed");
					responseJson.put("errno", -431);
					responseJson.put("text", "帐号不存在，请先注册");
				} else if (userExist && "register".equalsIgnoreCase(reason)) {
					//如果用户存在，再次使用发送验证码进行注册，不允许操作，提示找回密码登录
					responseJson.put("result", "failed");
					responseJson.put("errno", -430);
					responseJson.put("text","该用户已注册，请通过<忘记密码>登录");
				} else {
					
					boolean isInInterval =false;int cur_auth_times = 0;
					Map<String, Object> timeMap=authService.getAuthCodeTimeAndDiff(userNumber, reason);
					//查到今天内最后获取的时间间隔
					if(timeMap!=null && timeMap.containsKey("timediff") && timeMap.get("timediff")!=null  && !"".equals(timeMap.get("timediff"))){
						int timediff=Integer.parseInt(timeMap.get("timediff").toString());
						if(timediff>Config.getInt("GET_AUTH_CODE_INTERVAL")){//时间间隔大于要求的时间
							isInInterval=true;
						}
						//今天内获取的验证码次数
						cur_auth_times=Integer.parseInt(timeMap.get("times").toString());
					}else{
						isInInterval=true;
					}
					if (isInInterval) {
						// 再次获取验证码的时间间隔已经过了

						// 判断今天获取了多少次验证码
						if (cur_auth_times >= Config.getInt("GET_AUTH_CODE_TIMES")) {
							responseJson.put("result", "failed");
							responseJson.put("errno", -406);
							responseJson.put("text","您已超过获取验证码上限次数，请明天再来");
						} else {
							// 生成5为随机数作为验证码
							int code = (int) (Math.random() * 100000);
							//不足5位，在前面以0补足
							DecimalFormat df = new DecimalFormat("00000");
							String authCode = df.format(code);
							logger.debug("[getAuthCode]生成的验证码是：" + authCode);
							String operator = req.getRemoteAddr();
							boolean flag = authService.addUserAuthCode(userNumber, authCode, reason, operator,null);
							if (flag) {
								//发送短信
								ccUtils.sendSms(userNumber, "本次操作验证码是："+authCode+",3分钟内有效。如非本人操作，请忽略。");
								responseJson.put("result", "success");
								responseJson.put("errno", 0);
								responseJson.put("text", "");
							} else {
								responseJson.put("result", "failed");
								responseJson.put("errno", -500);
								responseJson.put("text", "获取验证码失败");
							}
						}
					} else {
						responseJson.put("result", "failed");
						responseJson.put("errno", -501);
						responseJson.put("text","获取验证码操作过于频繁，请稍后再试！");
					}
				}
			}
		} catch (NullPointerException e) {
			responseJson.put("result", "failed");
			responseJson.put("errno", -412);
			responseJson.put("text", "您提供的信息不足，请确认后重新提交");
		} catch (Exception ex) {
			ex.printStackTrace();
			responseJson.put("result", "failed");
			responseJson.put("errno", -500);
			responseJson.put("text", "获取验证码失败");
			logger.error("Exception:" + ex.getMessage());
		} finally {
			responseText = responseJson.toString();
			logger.info("[getAuthCode]responseText:" + responseText);
			resp.setCharacterEncoding("utf-8");
			try {
				resp.getWriter().write(responseText);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		logger.info("[getAuthCode.do]获取短信验证码返回"+ responseText);
	}
	
	
	
	
	/**
	 * 校对验证码
	 * 
	 * @param req
	 * @param resp
	 */
	@RequestMapping(value = "/account/verifyAuthCode.do")
	public void verifyUserAuthCode(HttpServletRequest req,
			HttpServletResponse resp) {
		String responseText = "{\"result\":\"failed\",\"text\":\"操作失败\"}";
		JSONObject responseJson = new JSONObject();
		try {
			JSONObject jo = ccUtils.getJSONObject(req);
			String userNumber = jo.getString("telnumber");
			String authCode = jo.getString("authcode");
			String reason = jo.getString("reason");
			if (null == userNumber || "".equals(userNumber) || null == authCode
					|| "".equals(authCode) || null == reason
					|| "".equals(reason)) {
				responseJson.put("result", "failed");
				responseJson.put("text", "您提供的信息不足，请确认后重新提交");
			} else {
				// 首先判断是否存在验证码
				logger.debug("userNumber是："+userNumber);
				logger.debug("验证码是："+authCode);
				int timediff = authService.checkAuthCodeOverTime(userNumber, authCode, reason);
				if (timediff>0) {
					// 先判断是否超时
					if (timediff>Config.getInt("AUTH_CODE_OVER_TIME")) {
						// 超时
						responseJson.put("result", "failed");
						responseJson.put("text", "您输入的验证码已过期");
					} else {
						responseJson.put("result", "success");
						responseJson.put("text", "");
					}
				} else {
					responseJson.put("result", "failed");
					responseJson.put("text", "您输入的验证码有误");
				}
			}

		} catch (NullPointerException e) {
			responseJson.put("result", "failed");
			responseJson.put("text","您提供的信息不足，请确认后重新提交");
		} catch (Exception ex) {
			ex.printStackTrace();
			responseJson.put("result", "failed");
			responseJson.put("text","操作失败");
			logger.error("Exception:" + ex.getMessage());
		} finally {
			responseText = responseJson.toString();
			resp.setCharacterEncoding("utf-8");
			try {
				logger.debug("[verifyUserAuthCode] result:"+responseText);
				resp.getWriter().write(responseText);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	/**
	 * 密码非法检查
	 * 
	 * @param password
	 * @return
	 */
	private boolean checkPasswordValid(String password) {
		boolean flag = true;
		password = password.trim();
		// 密码长度不小于5位，不大于15位
		if (password.length() < 5 || password.length() > 15) {
			flag = false;
		} else {
			// 密码中的每个字符ascii码必须在33~126之间
			char[] charArr = password.toCharArray();
			for (int i = 0; i < charArr.length; i++) {
				int ascii = (int) charArr[i];
				if (ascii < 33 || ascii > 126) {
					flag = false;
					break;
				}
			}
		}
		return flag;
	}
	/**
	 * 设置密码，分两种情况：
	 * 1. 忘记密码的情况下，获取验证码之后，设置密码 
	 * 2. 注册的时候，获取验证码之后设置密码，注册会写入用户表
	 * 
	 * @param req
	 * @param resp
	 */
	@RequestMapping(value = "/account/setPasswd.do")
	public void setUserPassword(HttpServletRequest req, HttpServletResponse resp) {
		logger.debug("call /account/setPasswd.do");
		String responseText = "{\"result\":\"failed\",\"text\":\"操作失败\"}";
		JSONObject responseJson = new JSONObject();
		//号码前缀
//		String numberPrefix = "86" ;
		String telNumber = "" ;
		String userid =null;
		try {
			JSONObject jo = ccUtils.getJSONObject(req);
			JSONObject header = jo.getJSONObject("http_headers") ;
			String marketid = header.getString("marketid");
			
			String reason = jo.getString("reason");
			String password = jo.getString("pwd");
		    telNumber = jo.getString("telnumber");
			// 下面两个参数不是必须的
			String gender = jo.getString("gender");
			String name = jo.getString("name");
			String userType=jo.getString("user_type");
			if (null == reason || "".equals(reason) || null == password
					|| "".equals(password) || null == telNumber
					|| "".equals(telNumber)) {
				// 非空判断
				responseJson.put("result", "failed");
				responseJson.put("text","您提供的信息不足，请确认后重新提交");
			} else if (!this.checkPasswordValid(password)) {
				// 密码非法判断
				responseJson.put("result", "failed");
				responseJson.put("text", "密码含有非法字符，请重新输入");
			} else {
				logger.debug("[setUserPassword]提交过来的号码是："+telNumber);
				logger.info("[setPasswd.do]检查用户是否存在...");
				 userid = authService.getUseridForTelnumber(telNumber);
				boolean userExist = !(userid==null || userid.isEmpty());
				// 如果是忘记密码，重新设置密码
				if ("renew".equals(reason)) {
					logger.info("[setPasswd.do]重设密码...");
					if (userExist) {		
						// 设置密码之前查询今天重置密码的次数
						int curent_day_m_times = authService
								.getModifyPasswordTimes(telNumber, reason);
						if (curent_day_m_times >= Config.getInt("RESET_PASSWORD_TIMES")) {
							responseJson.put("result", "failed");
							responseJson.put("text","您今天已经超过修改密码次数上限，请明天再来");
						} else {
							password=MD5Oper.md5_encode(MD5Oper.md5_encode(password));
							// 若没有超过次数限制，则进行密码重置
							authService.updatePassword(userid, password);
							// 同时插入历史记录表
							authService.addModifyPasswdHistory(userid, reason, password);

							// 这里可以去同步SIP服务器
							// 在这里调用
							logger.debug("userNumber:" + telNumber
									+ "password has been changed to "
									+ password + ";");
							responseJson.put("result", "success");
							responseJson.put("text","密码修改成功");
						}
					} else {
						responseJson.put("result", "false");
						responseJson.put("text", "帐号不存在，请先注册");
					}
				} else if ("register".equals(reason)) {
					// 如果是新注册的，首先要判断帐号唯一性，判断是否已经存在
                    logger.info("[setPasswd.do]注册账号...");
					if (userExist) {
						// 用户已经注册
						responseJson.put("result", "failed");
						responseJson.put("text","该用户已注册");
					} else {
						userType=(userType==null || "".equals(userType))?"student":userType;
						String recShortcode=jo.getString("rec_shortcode");
						// 用户未注册
						String operator = "http:" + req.getRemoteAddr();
						// 向account表和ani表中增加用户信息
						 userid = authService.doUserRegist(telNumber, password, operator,reason,"86",marketid,userType,recShortcode);
						 logger.debug("[getUseridForTelnumber]userid is " + userid);
						if (userid!=null && !"".equals(userid)) {
							// 同时插入历史记录表
							authService.addModifyPasswdHistory(telNumber,reason, password);
							// 如果注册时用户传递了性别和姓名，则初始化用户信息表记录，不会影响整个注册过程
							try {
								// 向info表中添加用户信息
								userInfoService.initUserInfo(userid, telNumber, name, gender, userType);
							} catch (Exception e) {
								logger.error("[setUserPassword] initUserInfo exception:", e);
							}
							
							responseJson.put("userid", userid);
							responseJson.put("result", "success");
							responseJson.put("text", "注册成功"+ userid);
						} else {
							logger.debug("userNumber:" + telNumber
									+ " failed to interact!");
							responseJson.put("result", "failed");
							responseJson.put("text", "注册失败");
						}
					}
				}
			}
		}catch (Exception ex) {
			ex.printStackTrace();
			responseJson.put("result", "failed");
			responseJson.put("text","操作失败");
			logger.error("[setUserPassword]Exception:", ex);
		} finally {
			responseText = responseJson.toString();
			resp.setCharacterEncoding("utf-8");
			try {
				resp.getWriter().write(responseText);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
	//修改手机号
	@RequestMapping(value = "/account/updAuthTelnumber.do")
	public void updAuthTelnumber(HttpServletRequest req, HttpServletResponse resp) {
		logger.debug("call /account/updAuthTelnumber.do");
		String responseText = "{\"result\":\"failed\",\"text\":\"操作失败\"}";
		JSONObject responseJson = new JSONObject();
		try {
			JSONObject jo = ccUtils.getJSONObject(req);
//			JSONObject header = jo.getJSONObject("http_headers") ;
			String curpwd = jo.getString("curpwd");
			String userid = jo.getString("userid");
			String telNumber = jo.getString("new_telnumber");
			String userType=jo.getString("user_type");
			//判断要更换的手机号是否已注册过
			String puserid=authService.getUseridForTelnumber(telNumber);
			if(puserid!=null && !"".equals(puserid)){
				responseJson.put("result", "failed");
				responseJson.put("text", "该手机号已被绑定了其他账号");
			}else{
				//验证密码是否正确
				boolean flag=authService.checkPassword(userid, curpwd);
				if(flag){
					int i=userInfoService.updTelnumberByID(userid, telNumber, userType);
					if(i>0){
						responseJson.put("result", "success");
						responseJson.put("text", "修改成功");
					}else{
						responseJson.put("result", "failed");
						responseJson.put("text","操作失败");
					}
				}else{
					responseJson.put("result", "failed");
					responseJson.put("text", "密码不正确，请重新输入");
				}
			}
		}catch (Exception ex) {
			ex.printStackTrace();
			responseJson.put("result", "failed");
			responseJson.put("text","操作失败");
			logger.error("[updAuthTelnumber]Exception:", ex);
		} finally {
			responseText = responseJson.toString();
			resp.setCharacterEncoding("utf-8");
			try {
				resp.getWriter().write(responseText);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
	

	/**
	 * 
	 * 密码重新设置，在已民登录状态下，用户提供旧密码和新密码
	 * 
	 * @param req
	 * @param resp
	 */
	@RequestMapping(value = "/account/resetPasswd.do")
	public void resetUserPassword(HttpServletRequest req,
			HttpServletResponse resp) {
		String responseText = "{\"result\":\"failed\",\"text\":\"操作失败\"}";
		JSONObject responseJson = new JSONObject();
		try {
			JSONObject jo = ccUtils.getJSONObject(req);
			String curpwd = jo.getString("curpwd");
			String newpwd = jo.getString("newpwd");
			String userid = jo.getString("userid");		// 
			String telNumber = jo.getString("telnumber");		// 用户提交的手机号码，带国际区号的，也就是ani计费账号
			if (userid == null || userid.isEmpty()) {
				userid = authService.getUseridForTelnumber(telNumber);
			}
			
			logger.debug("[resetUserPassword]登录状态下修改密码提交的userid是:"+userid + "手机号是:" +telNumber);

			if (null == curpwd || "".equals(curpwd) || null == newpwd
					|| "".equals(newpwd) || null == telNumber
					|| "".equals(telNumber)) {
				responseJson.put("result", "failed");
				responseJson.put("text","您输入的参数有误");
			} else {
				curpwd=MD5Oper.md5_encode(MD5Oper.md5_encode(curpwd));
				boolean flag = authService.checkPassword(userid, curpwd);
				// 旧密码验证成功
				if (flag) {
					logger.debug("userNumber:" + telNumber
							+ "has confirmed password successfully!");
					// 修改密码之前先查询当天已经修改密码的次数
					String reason = "in_reset";
					int curent_day_m_times = authService.getModifyPasswordTimes(telNumber, reason);
					if (curent_day_m_times >= Config.getInt("IN_RESET_PASSWORD_TIMES")) {
						logger.debug("userNumber:"
								+ telNumber
								+ "has no chance to reset his/her password today!");
						responseJson.put("result", "failed");
						responseJson.put("text","您今天已经超过修改密码次数上限，请明天再来");
					} else {
						// 密码合法校验
						if (this.checkPasswordValid(newpwd)) {
							// 修改新密码
							newpwd=MD5Oper.md5_encode(MD5Oper.md5_encode(newpwd));
							authService.updatePassword(userid, newpwd);

							// 同时插入历史记录表
							authService.addModifyPasswdHistory(userid,
									reason, newpwd);

							// 这里可以去同步SIP服务器
							// 在这里调用

							logger.debug("telNumber:" + telNumber
									+ "password has been changed to " + newpwd
									+ ";");
							responseJson.put("result", "success");
							responseJson.put("text","密码重置成功");
						} else {
							responseJson.put("result", "failed");
							responseJson.put("text","密码含有非法字符，请重新输入");
						}

					}
				} else {
					logger.debug("userNumber:" + telNumber + " password:"
							+ curpwd + " confirmed failed !");
					responseJson.put("result", "failed");
					responseJson.put("text", "原始密码不正确，请重新输入");
				}
			}

		} catch (NullPointerException e) {
			responseJson.put("result", "failed");
			responseJson.put("text", "您提供的信息不足，请确认后重新提交");
		} catch (Exception ex) {
			ex.printStackTrace();
			responseJson.put("result", "failed");
			responseJson.put("text","操作失败");
			logger.error("Exception:" + ex.getMessage());
		} finally {
			responseText = responseJson.toString();
			resp.setCharacterEncoding("utf-8");
			try {
				resp.getWriter().write(responseText);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	// 上传个人信息  学生
	@RequestMapping(value = "/social/studentUserinfo.do")
	public void studentUserinfo(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		logger.debug("call studentUserinfo.do...");
		String responseText = DEFAULT_RESPONSE_TEXT;
		try {
			JSONObject jo = ccUtils.getJSONObject(req);
			responseText = userInfoService.setStudentUserInfo(jo);
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error("studentUserinfo.do Exception:" + ex.toString());
		}
		flushResponse(resp, responseText);
	}
		
	// 上传个人信息  教师
	@RequestMapping(value = "/social/teacherUserinfo.do")
	public void teacherUserinfo(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		logger.debug("call teacherUserinfo.do...");
		String responseText = DEFAULT_RESPONSE_TEXT;
		try {
			JSONObject jo = ccUtils.getJSONObject(req);
			responseText = userInfoService.setTeacherUserInfo(jo);
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error("teacherUserinfo.do Exception:" + ex.toString());
		}
		flushResponse(resp, responseText);
	}
		
	// 编辑个人信息
	@RequestMapping(value = "/social/updStudentUserinfo.do")
	public void updStudentUserinfo(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		logger.debug("call updUserinfo.do...");
		String responseText = DEFAULT_RESPONSE_TEXT;
		try {
			JSONObject jo = ccUtils.getJSONObject(req);
			responseText = userInfoService.updUserinfo(jo,"student");
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error("userinfo.do Exception:" + ex.toString());
		}
		flushResponse(resp, responseText);
	}
		// 编辑个人信息
	@RequestMapping(value = "/social/updTeacherUserinfo.do")
	public void updTeacherUserinfo(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		logger.debug("call updTeacherUserinfo.do...");
		String responseText = DEFAULT_RESPONSE_TEXT;
		try {
			JSONObject jo = ccUtils.getJSONObject(req);
			responseText = userInfoService.updUserinfo(jo,"teacher");
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error("updTeacherUserinfo.do Exception:" + ex.toString());
		}
		flushResponse(resp, responseText);
	}
	
	
	/**
	 * 4.3.5.	上传照片（包含头像、海报图片、详情图片）
	 * @param req
	 * @param resp
	 * @param file
	 * @throws IOException
	 */
	@RequestMapping(value = "/social/uploadPhoto.do")
	public void uploadPhoto(HttpServletRequest req, HttpServletResponse resp,
			@RequestParam("icon") MultipartFile file) throws IOException {
		logger.debug("call /social/uploadPhoto.do...");
		String url = null, smallUrl = null;
		String responseText = DEFAULT_RESPONSE_TEXT;
		logger.debug("[uploadPhoto]icon file: " + file);
		try {
			JSONObject jo = ccUtils.getJSONObject(req);
			String ip = req.getServerName();
			String userid = jo.getString("userid");
			String upload_type=jo.getString("upload_type");
			logger.info("[uploadPhoto]userid:" + userid + "upload_type:" + upload_type);
			// 如果userid为空
			if (userid==null || userid.isEmpty() || upload_type==null || "".equals(upload_type)) {
				responseText="{\"result\":\"failed\",\"text\":\"您提供的信息不足，操作失败\"}";
			}
			if ("0".equals(upload_type)) {//头像
				url = userInfoService.setUserIconFile(file, ip, userid);
				if (url != null) {
					smallUrl = url + "_small";
					responseText = "{\"result\":\"success\",\"iconurl\":\""
							+ url + "\" ,\"smalliconurl\":\"" + smallUrl
							+ "\"}";
					
				}else {
					logger.error("[uploadPhoto] setUserIconFile failed");
				}
			}else {
				String description=jo.getString("description");
				responseText =userInfoService.uploadPhoto(file, ip, userid, upload_type, description);
			}			
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error("uploadPhoto Exception:" + ex.toString());
		}
				
		flushResponse(resp, responseText);
	}
	
	// 删除照片
	@RequestMapping(value = "/social/delPhoto.do")
	public void delalbumphoto(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		logger.debug("call /social/delPhoto.do...");
		String responseText = DEFAULT_RESPONSE_TEXT;
		 try {
		    JSONObject jo = ccUtils.getJSONObject(req);
			responseText = userInfoService.deleteAlbumPhotoList(jo);
			logger.debug("/social/delPhoto.do call end!!");
		 } catch (Exception ex) {
			 ex.printStackTrace();
			 logger.error("[/social/delPhoto.do] Exception:", ex);
		 }
		flushResponse(resp, responseText);
	}
	
	// 升级接口
	// 本接口已兼容http请求
	@RequestMapping(value = "/account/getlatestversion.do")
	public void getlatestversion(HttpServletRequest req, HttpServletResponse resp) throws Exception {
		logger.info("getlatestversion");
		// 云通修改为http兼容接口，service层处理头信息判断
		try {
			String responseText = userInfoService.getUpdateInfo(req);
			resp.setCharacterEncoding("utf-8");
			resp.getWriter().write(responseText);
		} catch (Exception e) {
			flushResponse(resp, DEFAULT_RESPONSE_TEXT);
		}

	}
	
	
	/**
	 * 登录认证接口
	 * 目前只支持用户手机号登录
	 * @param req
	 * @param resp
	 */
	@RequestMapping(value = "/account/confirmInfo.do")
	public void accountVerify(HttpServletRequest req, HttpServletResponse resp) {
		logger.info("confirmInfo.do...");
		String responseText = "{\"result\":\"failed\",\"text\":\"Login failed\"}";
		JSONObject responseJson = new JSONObject();
		try {
			JSONObject jo = ccUtils.getJSONObject(req);
			JSONObject header = jo.getJSONObject("http_headers") ;
			
			String userid = jo.getString("userid");
			String userNumber = jo.getString("telnumber");
			String pwd = jo.getString("pwd");
			if((userNumber == null || userNumber.equals("")) && (userid == null || userid.equals(""))){
				responseJson.put("result", "failed");
				responseJson.put("text", "您提供的信息不足，操作失败");
			}
			if(userid == null || userid.equals("")) {
				userid = authService.getUseridForTelnumber(userNumber);
				logger.info("getTelnumberByUserId..." + userid);
			}
			
			if (null == pwd || "".equals(pwd)) {
				responseJson.put("result", "failed");
				responseJson.put("text", "您输入的密码为空，请重新输入");
				responseJson.put("errno", -412);
				responseText = responseJson.toString();
			} else {
				responseJson = authService.verify(userid, pwd, header);
				responseText = responseJson.toString();
			}
		} catch (NullPointerException ex) {
			logger.debug("[confirmInfo]NullPointException:" + ex.getMessage());
			responseJson.put("result", "failed");
			responseJson.put("text", "您提供的信息不足，操作失败");
			responseJson.put("errno", -412);
			responseText = responseJson.toString();
		} catch (Exception e) {
			e.printStackTrace();
			responseJson.put("result", "failed");
			responseJson.put("text", "操作失败");
			responseJson.put("errno", -500);
		} finally {
			resp.setCharacterEncoding("utf-8");
			logger.info("[confirmInfo]responseText:" + responseText);
			try {
				resp.getWriter().write(responseText);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}



