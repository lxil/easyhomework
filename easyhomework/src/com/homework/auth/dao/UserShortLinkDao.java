/**
 * 保存联系人，以RC4加密，加密后的数组值转换成字符串
 * 查询联系人，及云呼好友（isccuser=1）
 * 
 */

package com.homework.auth.dao;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;
import org.springframework.stereotype.Repository;

import com.homework.auth.util.ShortUrlGenerator;

@Repository
public class UserShortLinkDao {
	@Autowired
	JdbcTemplate workJdbcTemplate;

	public String genShareShortcode(String userid){
		String shortcode="";
		Map<String, Object> shortcodeObj = getShortLinkByUserid(userid, "1");
		if(null != shortcodeObj && shortcodeObj.size()>0){
			shortcode = shortcodeObj.get("shortcode").toString();
		}else{
			shortcode = "ST" + ShortUrlGenerator.shortUrl(userid)[1];
			updateShortLink(userid,shortcode, "1");
		}
		return shortcode;
	}
	public boolean updateShortLink(String userid,String shortcode,String type){
		String sql = "insert into USER_SHORTLINK(userid,shortcode,type) values (?,?,?)";
		int row = workJdbcTemplate.update(sql, userid,shortcode,type);
		return row>0;
	}
	/**
	 * 根据shortcode查询用户账号
	 * @param shortcode
	 * @param type
	 * @return
	 */
	public Map<String, Object> getShortLinkByCode(String shortcode,String type){
		String sql = "select shortcode,userid from USER_SHORTLINK where shortcode=? and type=?";
		List<Map<String, Object>> results =  workJdbcTemplate.query(sql, new Object[]{shortcode,type},
				new RowMapperResultSetExtractor<Map<String, Object>>(new ColumnMapRowMapper(), 1));
		if(results!=null && results.size()>0){
			return results.get(0);  
		}else{
			return null;
		}
	}
	
	public Map<String, Object> getShortLinkByUserid(String userid,String type){
		try {
			String sql = "select shortcode,userid from USER_SHORTLINK where userid=? and type=?";
			return  workJdbcTemplate.queryForMap(sql,userid,type);
		} catch (DataAccessException e) {
			return null;
		}
	}
	
	
}
