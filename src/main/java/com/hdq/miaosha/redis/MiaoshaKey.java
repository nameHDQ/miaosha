package com.hdq.miaosha.redis;

/**
 * @Description : 秒杀key默认永久有效
 * @Author : huangdaoquan
 * @Date : 2022/7/2 21:34
 * @Version : 1.0
 **/
public class MiaoshaKey extends BasePrefix{



    public MiaoshaKey(String prefix) {
        super(prefix);
    }

    public MiaoshaKey(int expireSeconds, String prefix) {
        super(expireSeconds, prefix);
    }

    public static MiaoshaKey isGoodsOver = new MiaoshaKey("go");
    public static MiaoshaKey getMiaoshaPath = new MiaoshaKey(60,"mp");
    public static MiaoshaKey getMiaoshaVerifyCode = new MiaoshaKey(300,"mp");

}
