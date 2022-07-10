package com.hdq.miaosha.redis;

/**
 *@Description : orderkey先永久不过期
 *@Author : huangdaoquan
 *@Date : 2022/6/28 21:23
 *@Version : 1.0
 **/
public class AccessKey extends BasePrefix{


    public AccessKey(String prefix) {
        super(prefix);
    }

    public AccessKey(int expireSeconds, String prefix) {
        super(expireSeconds, prefix);
    }

    public static AccessKey accessKey = new AccessKey(5 ,"access");
    public static AccessKey withExpire(int expireSeconds){
        return new AccessKey(expireSeconds,"access");
    }



}
