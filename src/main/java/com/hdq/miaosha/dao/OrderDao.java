package com.hdq.miaosha.dao;

import com.hdq.miaosha.domain.MiaoshaOrder;
import com.hdq.miaosha.domain.OrderInfo;
import org.apache.ibatis.annotations.*;

/**
 * @Description : TODO
 * @Author : huangdaoquan
 * @Date : 2022/6/21 13:33
 * @Version : 1.0
 **/
@Mapper
public interface OrderDao {

    @Select("select * from miaosha_order where user_id = #{userId} and goods_id = #{goodsId}")
    public MiaoshaOrder getMiaoshaOrderByUserIdGoodsId(@Param("userId") Long userId, @Param("goodsId") long goodsId);

    @Insert("insert into order_info(user_id, goods_id, goods_name, goods_count, goods_price, order_channel, status, create_date) " +
            "values(#{userId}, #{goodsId}, #{goodsName}, #{goodsCount}, #{goodsPrice}, #{orderChannel}, #{status}, #{createDate})")
    @SelectKey(keyColumn = "id", keyProperty = "id", resultType = long.class, before = false, statement = "select last_insert_id()")
    long insert(OrderInfo orderInfo);


    @Insert("insert into miaosha_order(user_id, order_id, goods_id) values(#{userId}, #{orderId}, #{goodsId})")
    int insertMiaoshaOrder(MiaoshaOrder miaoshaOrder);
}
