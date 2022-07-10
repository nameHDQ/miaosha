package com.hdq.miaosha.redis;

/**
 *@Description : orderkey先永久不过期
 *@Author : huangdaoquan
 *@Date : 2022/6/28 21:23
 *@Version : 1.0
 **/
public class OrderKey extends BasePrefix{
    private OrderKey(String getPrefix) {
        super(getPrefix);
    }


    public static OrderKey getMiaoshaOrderByUidGid = new OrderKey("moug");



}
