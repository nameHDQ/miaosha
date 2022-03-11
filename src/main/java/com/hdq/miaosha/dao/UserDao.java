package com.hdq.miaosha.dao;

import com.hdq.miaosha.domain.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * @author hdq
 */
@Mapper
public interface UserDao {

    /**
     *根据id查询用户
     * @param id 用户id
     * @return
     */
    @Select("select * from miaosha_user where id = #{id}")
    public User getById(@Param("id") long id);
}
