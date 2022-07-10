package com.hdq.miaosha.rabbitmq;

import com.hdq.miaosha.domain.MiaoshaUser;

/**
 * @Description : TODO
 * @Author : huangdaoquan
 * @Date : 2022/7/2 18:24
 * @Version : 1.0
 **/
public class MiaoshaMessage {
    private MiaoshaUser miaoshaUser;
    private long goodsId;

    public MiaoshaUser getMiaoshaUser() {
        return miaoshaUser;
    }

    public void setMiaoshaUser(MiaoshaUser miaoshaUser) {
        this.miaoshaUser = miaoshaUser;
    }

    public long getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(long goodsId) {
        this.goodsId = goodsId;
    }
}
