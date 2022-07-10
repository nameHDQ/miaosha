package com.hdq.miaosha.access;

import com.hdq.miaosha.domain.MiaoshaUser;
import com.hdq.miaosha.domain.User;

/**
 * @Description : ThreadLocal当前线程的状态变量保存地点，每个线程各自的threadlocal独立，各不影响
 * @Author : huangdaoquan
 * @Date : 2022/7/6 20:15
 * @Version : 1.0
 **/
public class UserContext {
    private static  ThreadLocal<MiaoshaUser> userHolder = new ThreadLocal<>();

    public static void setUser(MiaoshaUser miaoshaUser){
        userHolder.set(miaoshaUser);
    }
    public static MiaoshaUser getUser(){
        return userHolder.get();
    }
}
