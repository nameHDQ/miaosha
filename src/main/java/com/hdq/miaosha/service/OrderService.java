package com.hdq.miaosha.service;

import com.hdq.miaosha.dao.OrderDao;
import com.hdq.miaosha.domain.MiaoshaOrder;
import com.hdq.miaosha.domain.MiaoshaUser;
import com.hdq.miaosha.domain.OrderInfo;
import com.hdq.miaosha.redis.OrderKey;
import com.hdq.miaosha.redis.RedisService;
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


    @Autowired
    RedisService redisService;

    /**
     *@Description : 优化方案：因为相同的秒杀产品都是在很短时间内，所以把秒杀信息存在缓存中，这时候先查缓存，如果缓存没有再查数据库
     *@Author : huangdaoquan
     *@Date : 2022/6/28 21:20
     *@Version : 1.0
     **/
    public MiaoshaOrder getMiaoshaOrderByUserIdGoodsId(Long userId, long goodsId) {
        return redisService.get(OrderKey.getMiaoshaOrderByUidGid, "" + userId + "_" + goodsId, MiaoshaOrder.class);

//        return orderDao.getMiaoshaOrderByUserIdGoodsId(userId, goodsId);
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
        orderDao.insert(orderInfo);
        MiaoshaOrder miaoshaOrder = new MiaoshaOrder();
        // 上面没有设置orderinfo的id，默认为0，怎么能直接获取呢？
        // 这里是因为，将orderinfo插入数据库后，mybatis会把数据库里赠改行的值重新封装到对象，数据库里id自增，所以可以返回
        miaoshaOrder.setOrderId(orderInfo.getId());
        miaoshaOrder.setGoodsId(goodsVo.getId());
        miaoshaOrder.setUserId(miaoshaUser.getId());

        orderDao.insertMiaoshaOrder(miaoshaOrder);
        // 写入缓存
        redisService.set(OrderKey.getMiaoshaOrderByUidGid, "" + miaoshaUser.getId() + "_" + goodsVo.getId(), miaoshaOrder);
        return orderInfo;
    }

    public OrderInfo getOrderById(long orderId) {


        return orderDao.getOrderById(orderId);

    }
}
