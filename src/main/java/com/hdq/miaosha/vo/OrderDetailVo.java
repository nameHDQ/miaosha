package com.hdq.miaosha.vo;

import com.hdq.miaosha.domain.OrderInfo;

/**
 * @Description : TODO
 * @Author : huangdaoquan
 * @Date : 2022/6/28 18:59
 * @Version : 1.0
 **/
public class OrderDetailVo {

    private GoodsVo goodsVo;
    private OrderInfo orderInfo;

    public GoodsVo getGoodsVo() {
        return goodsVo;
    }

    public void setGoodsVo(GoodsVo goodsVo) {
        this.goodsVo = goodsVo;
    }

    public OrderInfo getOrderInfo() {
        return orderInfo;
    }

    public void setOrderInfo(OrderInfo orderInfo) {
        this.orderInfo = orderInfo;
    }
}
