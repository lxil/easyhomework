/**
 * 保存联系人，以RC4加密，加密后的数组值转换成字符串
 * 查询联系人，及云呼好友（isccuser=1）
 * 
 */

package com.homework.auth.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;

import com.homework.auth.bean.DownloadConf;
import com.homework.auth.bean.UserInfoStudent;
import com.homework.auth.bean.UserInfoTeacher;
import com.homework.auth.util.Config;

@Repository
public class UserInfoDao {
	@Autowired
	JdbcTemplate workJdbcTemplate;

	/**
	 * save userinfo
	 */
	public int saveUserInfoTeacher(UserInfoTeacher user) {
		int itmp = -1;
		String userid = user.getUserid();
		if (!(userid == null || userid.isEmpty())) {
			String sql = null;
			// 是否已存在该用户的记录
				sql = "UPDATE USER_INFO_TEACHER SET ";
				sql += "name=?,gender=?,card_id=?,nature=?,grade=?,course=?,charge=?,booking_maxnum=?,oper_time=CURRENT_TIMESTAMP()";
				sql += " WHERE userid='" + userid + "'";
				itmp = workJdbcTemplate.update(sql,user.getName(),user.getGender(),user.getCard_id(),user.getNature(),user.getGrade(),user.getCourse(),
						user.getCharge(),user.getBooking_maxnum(),user.getUserid());
			if(itmp<1){
				sql = "INSERT INTO USER_INFO_TEACHER(userid,telnumber,name,gender,card_id,nature,grade,course,charge,booking_maxnum,oper_time)";
				sql += "VALUES(?,?,?,?,?,?,?,?,?,?,CURRENT_TIMESTAMP())";
				itmp = workJdbcTemplate.update(sql,userid,user.getTelnumber(),user.getName(),user.getGender(),user.getCard_id(),user.getNature(),user.getGrade(),user.getCourse(),
						user.getCharge(),user.getBooking_maxnum());
			}
			
		}

		return itmp;
	}
	
	public int saveOrUpdUserStud(UserInfoStudent stuInfo){
		int itmp = -1;
		String userid = stuInfo.getUserid();
		if (!(userid == null || userid.isEmpty())) {
			String sql = null;
			// 是否已存在该用户的记录
				sql = "UPDATE USER_INFO_STUDENT SET ";
				sql += "name=?,gender=?,card_id=?,school=?,grade=?,oper_time=CURRENT_TIMESTAMP()";
				sql += " WHERE userid=?";
				itmp = workJdbcTemplate.update(sql,stuInfo.getName(),stuInfo.getGender(),stuInfo.getCard_id(),stuInfo.getSchool(),stuInfo.getGrade(),stuInfo.getUserid());
			if(itmp<1){
				sql = "INSERT INTO USER_INFO_STUDENT(userid,telnumber,name,gender,card_id,school,grade,oper_time)";
				sql += "VALUES(?,?,?,?,?,?,?,CURRENT_TIMESTAMP())";
				itmp = workJdbcTemplate.update(sql,userid,stuInfo.getTelnumber(),stuInfo.getName(),stuInfo.getGender(),stuInfo.getCard_id(),stuInfo.getSchool(),stuInfo.getGrade());
			}
			
		}

		return itmp;
	}
	/**
	 * 根据用户类型以及用户id，修改用户信息表中手机号
	 * @param userid
	 * @param telnumber
	 * @param user_type
	 * @return
	 */
	public int updTelnumberByID(String userid,String telnumber,String user_type) throws Exception{
		String sql="";
		if(StringUtils.isEmpty(userid) || StringUtils.isEmpty(user_type)){
			return -1;
		}
		if(user_type.equals("teacher")){
			sql="update USER_INFO_TEACHER set telnumber=? where userid=?";
		}else{
			sql="update USER_INFO_STUDENT set telnumber=? where userid=?";
		}
		return  workJdbcTemplate.update(sql,telnumber,userid);
	}
	/**
	 * 根据用户id和类型  获取用户信息
	 * @param userid
	 * @param user_type
	 * @return
	 */
	public Map<String, Object> getUserInfByIdAndType(String userid,String user_type){
		try {
			String sql="";
			if(StringUtils.isEmpty(userid) || StringUtils.isEmpty(user_type)){
				return null;
			}
			if(user_type.equals("teacher")){
				sql="select * from  USER_INFO_TEACHER  where userid=?";
			}else{
				sql="select * from  USER_INFO_STUDENT where userid=?";
			}
			return workJdbcTemplate.queryForMap(sql,userid);
		} catch (DataAccessException e) {
			return null;
		}
	}
	
	/**
	 * 修改用户某一属性的值
	 * @param fileName
	 * @param fileValue
	 * @param userid
	 * @return
	 * @throws Exception
	 */
	public int updUserInfoStudentByCon(String fileName,Object fileValue,String userid)throws Exception{
		String	sql = "UPDATE USER_INFO_STUDENT SET "+fileName+"=?,oper_time=CURRENT_TIMESTAMP()";
		sql += " WHERE userid='" + userid + "'";
		int res= workJdbcTemplate.update(sql,fileValue);
		return res;
	}
	
	public int updUserInfoTeacherByCon(String fileName,Object fileValue,String userid)throws Exception{
		String	sql = "UPDATE USER_INFO_TEACHER SET "+fileName+"=?,oper_time=CURRENT_TIMESTAMP()";
		sql += " WHERE userid='" + userid + "'";
		int res= workJdbcTemplate.update(sql,fileValue);
		return res;
	}
	
	/**
	 * 获取用户头像
	 * 
	 * @param userid
	 * @return
	 */
	public String[] getUserIcon(String userid) {

		Map<String, Object> userIcon;
		String[] iconUrl = new String[2];
		String sql = "select * from  USER_ICON where userid =?";
		SqlRowSet rs = workJdbcTemplate.queryForRowSet(sql,
				new Object[] { userid });
		if (rs.next()) {
			userIcon = workJdbcTemplate.queryForMap(sql,
					new Object[] { userid });
			if (userIcon != null) {
				String serUrl = Config.getString("file_server_url");
				iconUrl[0] = serUrl + userIcon.get("iconurl");
				iconUrl[1] = serUrl + userIcon.get("smalliconurl");
			}
		}
		return iconUrl;
	}
	
	
	public int setUserIcon(String userid, String iconUrl) {
		int itmp = -1;
		if (userid == null)
			return itmp;

		String sql = "delete from USER_ICON where userid =?";
		workJdbcTemplate.update(sql, new Object[] { userid });
		sql = "INSERT INTO USER_ICON (userid, iconurl, smalliconurl) VALUES (?,?,?)";
		itmp = workJdbcTemplate.update(sql, new Object[] { userid, iconUrl,
				iconUrl + "_small" });

		return itmp;
	}
	
	public int addAlbumphotoById(String photoId,String photoUrl,String smallPhoto,String userid,String createTime,String description, String photoType){
		try {
			String sql="insert into USER_ALBUM_PHOTO(photo_id,photourl,smallphotourl,userid,create_time,description,photo_type) values(?,?,?,?,?,?,?)";
			int res=workJdbcTemplate.update(sql, photoId,photoUrl,smallPhoto,userid,createTime,description,photoType);
			return res;
		} catch (Exception e) {
			return -1;
		}
	}
	
	
	/**
	 * 根据照片ID，删除照片（可多张一起删除，ID用“##”隔开）
	 * @param photo_id_list
	 * @return
	 */
	public int deleteAlbumPhoto(String photo_id_list){
		try {
			String sql="delete from USER_ALBUM_PHOTO where photo_id ";
			if(photo_id_list!=null && photo_id_list.contains("##")){
				if( photo_id_list.contains("'##'")){
					photo_id_list=photo_id_list.replace("##", ",");
				}else{
					photo_id_list=("'"+photo_id_list.replaceAll("##", "\',\'")+"'").replace(",''", "");
				}
				sql+=" in ("+photo_id_list+")";
			}else{
				sql+="= '"+photo_id_list+"'";
			}
			return workJdbcTemplate.update(sql);
		} catch (Exception e) {
			return -1;
		}
	}
	
	/**
	 * 查版本
	 * @param appkey
	 * @param platform
	 * @return
	 */
	public List<DownloadConf> getDownloadConf(String appkey, String platform){
		String sql="select * from DOWNLOAD_URL where appkey=? and platform=?";	
		return workJdbcTemplate.query(sql, new DownloadConfRowMapper(), appkey, platform);
	}
	

class DownloadConfRowMapper implements RowMapper<DownloadConf>{

	@Override
	public DownloadConf mapRow(ResultSet rs, int rowNum) throws SQLException {
		// TODO Auto-generated method stub
		DownloadConf conf = new DownloadConf();
		conf.setFileSize(rs.getInt("file_size"));
		conf.setLatestVersion(rs.getString("latest_version"));
		conf.setMarket(rs.getString("market"));
		conf.setMd5(rs.getString("md5"));
		conf.setNote(rs.getString("note"));
		conf.setPlatform(rs.getString("platform"));
		conf.setUrl(rs.getString("url"));
		conf.setNoteCn(rs.getString("note_cn"));
		conf.setNoteCnTr(rs.getString("note_cn_tr"));
		conf.setNoteEn(rs.getString("note_en"));
		conf.setNoteEs(rs.getString("note_es"));
		return conf;
	}}
}
