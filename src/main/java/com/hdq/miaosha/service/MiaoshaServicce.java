package com.hdq.miaosha.service;

import com.hdq.miaosha.dao.GoodsDao;
import com.hdq.miaosha.domain.Goods;
import com.hdq.miaosha.domain.MiaoshaUser;
import com.hdq.miaosha.domain.OrderInfo;
import com.hdq.miaosha.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @Description : TODO
 * @Author : huangdaoquan
 * @Date : 2022/6/21 13:27
 * @Version : 1.0
 **/
@Service
public class MiaoshaServicce {

    @Autowired
    GoodsService goodsService;

    @Autowired
    OrderService orderService;


    @Transactional
    public OrderInfo miaosha(MiaoshaUser miaoshaUser, GoodsVo goodsVo) {
        // 减库存，下订单，写入秒杀订单

        goodsService.reduceStock(goodsVo);
        // order_info,miaosha_user
        return orderService.createOrder(miaoshaUser, goodsVo);

    }
}
