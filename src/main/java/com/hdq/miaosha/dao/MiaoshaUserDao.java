package com.hdq.miaosha.dao;

import com.hdq.miaosha.domain.MiaoshaUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * miaosha_user表的Dao层
 * @author hdq
 */
@Mapper
public interface MiaoshaUserDao {

	/**
	 * 根据id查询用户
	 * @param id 用户id
	 * @return
	 */
	@Select("select * from miaosha_user where id = #{id}")
	public MiaoshaUser getById(@Param("id")long id);

	/**
	 * 根据用户名查询用户
	 * @param toBeUpdate 用户名
	 */
	@Update("update miaosha_user set password = #{password} where id = #{id}")
	public void update(MiaoshaUser toBeUpdate);
}
