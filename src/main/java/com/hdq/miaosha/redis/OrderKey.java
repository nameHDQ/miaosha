package com.hdq.miaosha.redis;

public class OrderKey extends BasePrefix{
    private OrderKey(int expireSecond, String getPrefix) {
        super(expireSecond, getPrefix);
    }



}
