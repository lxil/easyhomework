package com.homework.auth.service;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.servlet.http.HttpServletRequest;

import mediautil.image.jpeg.Entry;
import mediautil.image.jpeg.Exif;
import mediautil.image.jpeg.LLJTran;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.drew.imaging.jpeg.JpegMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifDirectory;
import com.homework.auth.bean.DownloadConf;
import com.homework.auth.bean.UserInfoStudent;
import com.homework.auth.bean.UserInfoTeacher;
import com.homework.auth.dao.CCAuthDao;
import com.homework.auth.dao.UserInfoDao;
import com.homework.auth.util.CloudCallUtils;
import com.homework.auth.util.Config;
import com.homework.auth.util.HttpCommonHeader;
import com.homework.auth.util.ResizeImage;
@Service
public class UserInfoService {
	
	private static Logger logger = Logger.getLogger("parameterlog");
	@Autowired
	UserInfoDao userInfoDao;
	@Autowired
	CCAuthDao ccAuthDao;
	@Autowired
	CloudCallUtils ccUtils;
	/*
	 * 刚注册时，初始化一个用户信息
	 */
	public boolean initUserInfo(String userid, String telnumber, String name, String gender, String userType) {
		int ret=0;
		if(userType!=null && "teacher".equals(userType)){//教师
			UserInfoTeacher user=new UserInfoTeacher();
			user.setUserid(userid);
			user.setTelnumber(telnumber);
			user.setName(name);
			user.setGender(gender);
			ret=userInfoDao.saveUserInfoTeacher(user);
		}else{
			UserInfoStudent user=new UserInfoStudent();
			user.setUserid(userid);
			user.setTelnumber(telnumber);
			user.setName(name);
			user.setGender(gender);
			ret=userInfoDao.saveOrUpdUserStud(user);
		}
		return ret > 0;
	}
	
	/**
	 * 根据用户类型以及用户id，修改用户信息表中手机号
	 * @param userid
	 * @param telnumber
	 * @param user_type
	 * @return
	 */
	public int updTelnumberByID(String userid,String telnumber,String user_type){
		try {
			int i=ccAuthDao.authModifyTelnumber(userid, telnumber);
			if(i>0){
				return userInfoDao.updTelnumberByID(userid, telnumber, user_type);
			}
			return i;
		} catch (Exception e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
			return -1;
		}
	}
	/**
	 * 上传用户信息  学生
	 * @param json
	 * @return
	 * @throws Exception
	 */
	public String setStudentUserInfo(JSONObject json)throws Exception {
		JSONObject jo = new JSONObject();  // 用来返回结果的json对象
		String result = null;
		String text = null;
		// 目前兼容上传userid或者telnumber来定位一个用户
		String userid = json.getString("userid");
		String telnumber = json.getString("telnumber");
		String name = json.getString("name");
		String gender = json.getString("gender");
		String card_id = json.getString("card_id");
		String school = json.getString("school");
		String grade = json.getString("grade");
		
		boolean useridExist =false;
		if (userid==null||userid.isEmpty()) {
			userid = ccAuthDao.getUseridForTelnumber(telnumber);
			useridExist=true;
		}else{
			useridExist =ccAuthDao.useridExist(userid);
		}
		if (useridExist) {
			UserInfoStudent user = new UserInfoStudent();
			user.setUserid(userid);
			user.setTelnumber(telnumber);
			user.setName(name);
			user.setGender(gender);
			user.setCard_id(card_id);
			user.setSchool(school);
			user.setGrade(grade);
			user.setBalance(0.0);
			
			int tmp = userInfoDao.saveOrUpdUserStud(user);
			if (tmp != -1) {
				result = "success";
				text ="操作成功";

			} else {
				result = "failed";
				text = "操作失败";
			}
			
			
			
		}else {
			logger.error("[setUserInfo]用户不存在");
			result= "failed";
			text = "该用户还未注册";
		}
		
		jo.put("result", result);
		jo.put("text", text);

		return jo.toString();
	}
	
	/**
	 * 上传用户信息  教师
	 * @param json
	 * @return
	 * @throws Exception
	 */
	public String setTeacherUserInfo(JSONObject json)throws Exception {
		JSONObject jo = new JSONObject();  // 用来返回结果的json对象
		String result = null;
		String text = null;
		// 目前兼容上传userid或者telnumber来定位一个用户
		String userid = json.getString("userid");
		String telnumber = json.getString("telnumber");
		String name = json.getString("name");
		String gender = json.getString("gender");
		String card_id = json.getString("card_id");
		String nature = json.getString("nature");
		String grade = json.getString("grade");
		String course = json.getString("course");
		float charge = json.getFloatValue("charge");
		int booking_maxnum=json.getIntValue("booking_maxnum");
		boolean useridExist =false;
		if (userid==null||userid.isEmpty()) {
			userid = ccAuthDao.getUseridForTelnumber(telnumber);
			useridExist=true;
		}else{
			useridExist =ccAuthDao.useridExist(userid);
		}
		logger.debug("[setUserInfo]useid:"+ userid + ",telnumber:"+telnumber);
		
		if (useridExist) {
			UserInfoTeacher user = new UserInfoTeacher();
			user.setUserid(userid);
			user.setTelnumber(telnumber);
			user.setName(name);
			user.setGender(gender);
			user.setCard_id(card_id);
			user.setNature(nature);
			user.setGrade(grade);
			user.setBalance(0.0);
			user.setCourse(course);
			user.setCharge(charge);
			user.setBooking_maxnum(booking_maxnum);
			int tmp = userInfoDao.saveUserInfoTeacher(user);
			if (tmp != -1) {
				result = "success";
				text ="操作成功";

			} else {
				result = "failed";
				text = "操作失败";
			}
			
			
			
		}else {
			logger.error("[setUserInfo]用户不存在");
			result= "failed";
			text = "该用户还未注册";
		}
		
		jo.put("result", result);
		jo.put("text", text);

		return jo.toString();
	}
	
	/**
	 * 编辑用户信息
	 * @param json
	 * @param userType
	 * @return
	 * @throws Exception
	 */
	public String updUserinfo(JSONObject json, String userType)throws Exception{
		logger.info("当前用户类型是：" + userType);
		JSONObject jo = new JSONObject();  // 用来返回结果的json对象
		try {
			String userid = json.getString("userid");
			String fileName = json.getString("filed_name");
			String fileValue = json.getString("filed_value");
			//验证账号密码是否正确
			if(userid==null || userid.isEmpty()){
				jo.put("result", "failed");
				jo.put("text", "提供的信息不足，操作失败");
				return jo.toString();
			}
			if(fileName==null || "".equals(fileName)){
				jo.put("result", "failed");
				jo.put("text", "提供的信息不足，操作失败");
				return jo.toString();
			}
			if(fileValue==null){
				jo.put("result", "failed");
				jo.put("text", "提供的信息不足，操作失败");
				return jo.toString();
			}
			int i=0;
			if(userType!=null && "teacher".equals(userType)){
				i=userInfoDao.updUserInfoTeacherByCon(fileName, fileValue, userid);
			}else{
				i=userInfoDao.updUserInfoStudentByCon(fileName, fileValue, userid);
			}
			if(i>0){
				jo.put("result", "success");
				jo.put("text", "操作成功");
			}else{
				jo.put("result", "failed");
				jo.put("text","操作失败");
			}
		} catch (Exception e) {
			logger.error("[updUserinfo] exception ", e);
			jo.put("result", "failed");
			jo.put("text","操作失败");
			return jo.toString();
		}
		
		return jo.toString();
	}
	
	
	
	/*
	 * file 请求里的头像文件 
	 * serverName 从请求里获得的服务器名字，用来组建最终的头像URL
	 * telnumber 要设置头像的用户手机号
	 * 返回操作完后，图像的URL。错误时返回null
	 */

	public String setUserIconFile(MultipartFile file, String serverName,String userid) throws Exception {
		logger.debug("[setUserIconFile]userid:" + userid);
		String url = null;
		if (file == null || file.isEmpty() || serverName == null
				|| serverName.isEmpty() || userid == null
				|| userid.isEmpty()) {
			return null;
		}

//		String port = "80";
		String fileName = UUID.randomUUID().toString();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		String date = dateFormat.format(new Date());
		String userIconPrefix = Config.getString("USER_ICON_PREFIX"); // URL里的虚拟目录
		logger.debug("[setUserIconFile] userIconPrefix:" + userIconPrefix);
		url =  userIconPrefix + date + "/"
				+ fileName;
		String userIconDirectory = Config.getString("USER_ICON_DIRECTORY"); // 硬盘里保存头像的根目录
		logger.debug("[setUserIcon] userIconDirectory:" + userIconDirectory);

		String path = userIconDirectory + date;
		File filePath = new File(path);
		if (!filePath.exists()) {
			filePath.mkdir();
		}
		File imageFile = new File(path + "/" + fileName);
		file.transferTo(imageFile);
		ResizeImage r = new ResizeImage();
		BufferedImage image = javax.imageio.ImageIO.read(imageFile);
		r.writeHighQuality(r.resizeImage(image, 100), path + "/", fileName
				+ "_small", 0.6f);
		deleteUserIconFile(path, userid);
		try {
			if (userInfoDao.setUserIcon(userid, url) == -1) {
				return null;
			}

		} catch (Exception ex) {
			logger.error("setusericon.do ERROR:", ex);
			return null;

		}
		String serUrl=Config.getString("file_server_url");
		return serUrl+url;
	}
	
	public void deleteUserIconFile(String path, String userid) {
		String[] iconUrl = getUserIcon(userid);
		if (iconUrl[0] != null) {
			String icon = iconUrl[0].substring(iconUrl[0].lastIndexOf("/"));
			logger.debug("deleteing file : " + path + icon);
			new File(path + icon).delete();
			new File(path + icon + "_small").delete();
		}
	}
	
	public String[] getUserIcon(String userid) {
		return userInfoDao.getUserIcon(userid);
	}
	
	/*
	 * file 请求里的头像文件 
	 * serverName 从请求里获得的服务器名字，用来组建最终的头像URL
	 * telnumber 要设置头像的用户手机号
	 * 返回操作完后，图像的URL。错误时返回null
	 */

	public String uploadPhoto(MultipartFile file, String serverName,String userid,String photoType,String description) throws Exception {
		logger.debug("[uploadPhoto]userid:" + userid);
		if (file == null || file.isEmpty() || serverName == null
				|| serverName.isEmpty() || userid == null
				|| userid.isEmpty()) {
			return null;
		}
		JSONObject jo = new JSONObject();  // 用来返回结果的json对象
		// 最终存入的图像名
		String fileName = UUID.randomUUID().toString();
		String albumPhotoPrefix = Config.getString("ALBUM_PHOTO_PREFIX"); // URL里的虚拟目录
		String albumPhotoDirectory = Config.getString("ALBUM_PHOTO_DIRECTORY"); // 硬盘里保存头像的根目录
		String 	url =albumPhotoPrefix + userid + "/"+ fileName;
		
		String path = albumPhotoDirectory + userid;
		File filePath = new File(path);
		if (!filePath.exists()) {
			filePath.mkdir();
		}
		File des = new File(path + "/" + fileName);
		FileCopyUtils.copy(file.getBytes(), des);
		
		//压缩图
 		ResizeImage r = new ResizeImage(); // 缩图工具
 		BufferedImage image = javax.imageio.ImageIO.read(des);
 		r.writeHighQuality(r.resizeMinImage(image, 265), path+"/", fileName+ "_small", 0.6f);
		
		//裁剪图
		String smallUrl=path + "/" + fileName+ "_small";
		int w=265,h=265;
		File f=new File(smallUrl);
        Iterator<ImageReader> iterator = ImageIO.getImageReadersByFormatName("jpg");   
        ImageReader reader = (ImageReader)iterator.next();   
        InputStream in=new FileInputStream(f);  
        ImageInputStream iis = ImageIO.createImageInputStream(in);   
        reader.setInput(iis, true);   
        ImageReadParam param = reader.getDefaultReadParam();   
        int imageIndex = 0;   
        Rectangle rect = new Rectangle((reader.getWidth(imageIndex)-w)/2, (reader.getHeight(imageIndex)-h)/2, w, h);    
        param.setSourceRegion(rect);   
        BufferedImage bi = reader.read(0,param);     
        ImageIO.write(bi, "jpg", f);    
        iis.flush();iis.close();
        in.close();
        try {
        	Metadata metadata = JpegMetadataReader.readMetadata(des);  
        	Directory directory = metadata.getDirectory(ExifDirectory.class);
        	if(directory.containsTag(ExifDirectory.TAG_ORIENTATION)){ // Exif信息中有保存方向,把信息复制到缩略图
        		int orientation = directory.getInt(ExifDirectory.TAG_ORIENTATION);
        		
        		LLJTran llj = new LLJTran(f);
        		
        		llj.read(LLJTran.READ_INFO, true);
        		
        		if(!(llj.getImageInfo() instanceof Exif)){
        			llj.addAppx(LLJTran.dummyExifHeader, 0, LLJTran.dummyExifHeader.length, true);
        		}
        		
        		Exif exif = ((Exif) llj.getImageInfo());      
        		
        		//写入方向的Exif
        		exif.setTagValue(Exif.ORIENTATION, -1, null, true); //先创建属性
        		
        		Entry entry = exif.getTagValue(Exif.ORIENTATION, true);
        		entry.setValue(0, orientation);
        		exif.setTagValue(Exif.ORIENTATION, -1, entry, true);
        		
        		llj.refreshAppx(); // Recreate Marker Data for changes done
        		//改写后的文件，文件必须存在
        		
        		InputStream inSmall = new BufferedInputStream(new FileInputStream(f));
				
				File exifFile = new File(f.getPath() + "_exif_temp");
				
        		OutputStream out = new BufferedOutputStream(new FileOutputStream(exifFile));
        		// Transfer remaining of image to output with new header.
        		llj.xferInfo(inSmall, out, LLJTran.REPLACE, LLJTran.REPLACE);
        		in.close();
        		out.close();
        		llj.freeMemory();
        		
        		//为解决自复制时产生文件数据丢失的bug,利用临时文件的解决办法
				if(f.delete()){
					exifFile.renameTo(f);
				}
        	}
		} catch (Exception e) {
			e.printStackTrace();
		}
		String uuid = UUID.randomUUID().toString().replaceAll("-", "");
		Long creatTime=System.currentTimeMillis();
		userInfoDao.addAlbumphotoById(uuid, url, url+"_small",  userid,creatTime+"",description,photoType);
		jo.put("result", "success");
		jo.put("text", "");
		jo.put(" photo_id", uuid);
		jo.put("create_time", creatTime);
		String serUrl=Config.getString("file_server_url");//获取文件服务器地址
		jo.put("photo_url",serUrl+ url);
		jo.put("small_photo_url", serUrl+url+"_small");
		jo.put("description", description);
		jo.put("upload_type", photoType);
		String res = JSON.toJSONString(jo);
		logger.info("[verify]Response:" + res);
		return res;
	}
	
	
	/**
	 * 删除用户相片，可以多张同时删除
	 * @param request
	 * @return
	 * @throws IOException
	 */
	public String deleteAlbumPhotoList(JSONObject jsonObj) throws Exception {
		JSONObject jo = new JSONObject();
		JSONObject header = jsonObj.getJSONObject("http_headers");
		if(null == header){
			jo.put("result", "failed");
			jo.put("text", "Error http common header");
			String res = JSON.toJSONString(jo);
			logger.info("[verify]Response:" + res);
			return res;
		}
		String userid = jsonObj.getString("userid");
		String photoidList = jsonObj.getString("photo_id_list");
		int resint=userInfoDao.deleteAlbumPhoto(photoidList);
		logger.info("用户："+userid+" 删除相片："+photoidList);
		if(resint>0){
			jo.put("result", "success");
			jo.put("text", "");
			String res = JSON.toJSONString(jo);
			logger.info("[verify]Response:" + res);
			return res;
		}else{
			jo.put("result", "failed");
			jo.put("text", "操作失败");
			String res = JSON.toJSONString(jo);
			logger.info("[verify]Response:" + res);
			return res;
		}
		
	}
	
	
	public String getUpdateInfo(HttpServletRequest req) throws Exception {	
		
		JSONObject jo = new JSONObject();
		HttpCommonHeader hdr = new HttpCommonHeader();
		String scheme = req.getScheme() ;
		if("http".equals(scheme)){
			JSONObject respJo = ccUtils.getJSONObject(req);
			JSONObject header = respJo == null ? null : respJo.getJSONObject("http_headers") ;
			// 必填字段
			try {
				hdr.setAppkey(header.getString("appkey"));
				hdr.setDevicetype(header.getString("devicetype"));
				hdr.setDeviceid(header.getString("deviceid"));
				hdr.setMac(header.getString("mac"));
				hdr.setLanguage(header.getString("language"));
			} catch (Exception ex) {
				logger.info("Common header error");
				ex.printStackTrace();
			}

			// 可选字段
			try {
				hdr.setDevicename(header.getString("devicename")==null?"":header.getString("devicename")); // 这个在文档里应该要有的
				hdr.setOsversion(header.getString("osversion")==null?"":header.getString("osversion"));
				hdr.setAppversion(header.getString("appversion")==null?"":header.getString("appversion"));
				hdr.setProtocol(header.getInteger("protocol")==null?0:header.getInteger("protocol"));
				hdr.setMarketid(header.getString("marketid")==null?"":header.getString("marketid"));
			} catch (Exception ex) {
			}
		}else{
			hdr.getHeaders(req);
		}
		List<DownloadConf> dlInfoList;
		// 目前 IOS只区分App Store版本和越狱版本，100为App Store，101为cloudcall
		String market = hdr.getMarketid();
		dlInfoList= userInfoDao.getDownloadConf(hdr.getAppkey(), hdr.getDevicetype());
		int index = getMatchPlatform(market,dlInfoList);		
		if(index > -1){
			jo.put("result", "success");
			jo.put("text", "");
			
			DownloadConf dlInfo = dlInfoList.get(index);
			jo.put("version", dlInfo.getLatestVersion());
			jo.put("fileSize", dlInfo.getFileSize());
			jo.put("md5", dlInfo.getMd5());
			jo.put("url", dlInfo.getUrl());
			jo.put("notes", dlInfo.getNoteCn() == null ? dlInfo.getNote() : dlInfo.getNoteCn());
		}
		else{
			jo.put("result", "failed");
			jo.put("text", "No configuration found");
		}
		
		String Response = jo.toString();
		return Response;
	}
	private int getMatchPlatform(String marketId,List<DownloadConf> dlInfoList) {
		
		final int MATCH_LEVEL_NOTHING = 0;
		final int MATCH_LEVEL_DEV_TYPE = 1;
		final int MATCH_LEVEL_MARKET = 2;
		 
		
		// 0是匹配了DevType, 1是匹配了Market, 2是匹配了AppVerison
		int matchLevel = MATCH_LEVEL_NOTHING; //因为获取adpList时用到devType，默认匹配到 devType
		int mostMatchIndex = -1; // 在列表中，最匹配的配置的下标
		for(int    i=0;    i < dlInfoList.size();    i++)    {   
			DownloadConf config = dlInfoList.get(i); 			
			
			String market = config.getMarket();
						
			int level = MATCH_LEVEL_DEV_TYPE;		// 当前的level
			if((market != null) && (market.length() > 0)){
				if(market.equalsIgnoreCase(marketId)){					
					level = MATCH_LEVEL_MARKET;							
				}
				else{
					continue;
				}
					
			}			

			if (level > matchLevel) {
				matchLevel = level;
				mostMatchIndex = i;
			}
					
		}

		return mostMatchIndex;
	}

}
