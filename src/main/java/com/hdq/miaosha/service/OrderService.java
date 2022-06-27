package com.hdq.miaosha.service;

import com.hdq.miaosha.dao.OrderDao;
import com.hdq.miaosha.domain.MiaoshaOrder;
import com.hdq.miaosha.domain.MiaoshaUser;
import com.hdq.miaosha.domain.OrderInfo;
import com.hdq.miaosha.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * @Description : TODO
 * @Author : huangdaoquan
 * @Date : 2022/6/20 16:54
 * @Version : 1.0
 **/
@Service
public class OrderService {

    @Autowired
    OrderDao orderDao;

    public MiaoshaOrder getMiaoshaOrderByUserIdGoodsId(Long userId, long goodsId) {

        return orderDao.getMiaoshaOrderByUserIdGoodsId(userId, goodsId);
    }

    @Transactional
    public OrderInfo createOrder(MiaoshaUser miaoshaUser, GoodsVo goodsVo) {

        //写订单
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setUserId(miaoshaUser.getId());
        orderInfo.setCreateDate(new Date());
        orderInfo.setGoodsId(goodsVo.getId());
        orderInfo.setDeliveryAddrId(0L);
        orderInfo.setGoodsName(goodsVo.getGoodsName());
        orderInfo.setGoodsCount(1);
        orderInfo.setGoodsPrice(goodsVo.getMiaoshaPrice());
        orderInfo.setOrderChannel(1);
        // 0 新建未支付，1 已支付 2 已发货 3 已收货 最好用一个枚举类型来表示
        orderInfo.setStatus(0);
        long orderId = orderDao.insert(orderInfo);


        MiaoshaOrder miaoshaOrder = new MiaoshaOrder();
        miaoshaOrder.setOrderId(orderId);
        miaoshaOrder.setGoodsId(goodsVo.getId());
        miaoshaOrder.setUserId(miaoshaUser.getId());

        orderDao.insertMiaoshaOrder(miaoshaOrder);
        return orderInfo;
    }
}
