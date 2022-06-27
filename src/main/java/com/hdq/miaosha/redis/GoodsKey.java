package com.hdq.miaosha.redis;

/**
  * @Description: 页面缓存的key有效期一般比较短
  * @Author: huangdaoquan
  * @Date: 2022/6/27 15:20
  * @Param null:
  * @return: null
  * @Version: 1.0
  **/
public class GoodsKey extends BasePrefix{
    private GoodsKey(int expireSeconds, String getPrefix) {
        super(expireSeconds, getPrefix);
    }

    public static GoodsKey getGoodsList = new GoodsKey(60,"gl");
    public static GoodsKey getGoodsDetail = new GoodsKey(60,"gd");
}
