package com.hdq.miaosha.rabbitmq;

import com.hdq.miaosha.domain.MiaoshaOrder;
import com.hdq.miaosha.domain.MiaoshaUser;
import com.hdq.miaosha.domain.OrderInfo;
import com.hdq.miaosha.redis.RedisService;
import com.hdq.miaosha.result.CodeMsg;
import com.hdq.miaosha.result.Result;
import com.hdq.miaosha.service.GoodsService;
import com.hdq.miaosha.service.MiaoshaServicce;
import com.hdq.miaosha.service.MiaoshaUserServicce;
import com.hdq.miaosha.service.OrderService;
import com.hdq.miaosha.vo.GoodsVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Description : TODO
 * @Author : huangdaoquan
 * @Date : 2022/6/29 21:52
 * @Version : 1.0
 **/
@Service
public class MQReceiver {


    private static Logger log = LoggerFactory.getLogger(MQReceiver.class);


    @Autowired
    private MiaoshaUserServicce miaoshaUserServicce;

    @Autowired
    private RedisService redisService;

    @Autowired
    private GoodsService goodsService;

    @Autowired
    OrderService orderService;

    @Autowired
    MiaoshaServicce miaoshaServicce;

    @RabbitListener(queues = MQConfig.QUEUE)
    public void receive(String message){
        log.info("receive message:" + message);
    }

    @RabbitListener(queues = MQConfig.TOPIC_QUEUE1)
    public void receiveTopic1(String message){
        log.info("receive topic queue1 message:" + message);
    }

    @RabbitListener(queues = MQConfig.TOPIC_QUEUE2)
    public void receiveTopic2(String message){
        log.info("receive topic queue2 message:" + message);
    }

    @RabbitListener(queues = MQConfig.HEADERS_QUEUE)
    public void receiveHeader(byte[] message){
        log.info("receive header queue message:" + new String(message));
    }


    /**
      * @Description: 秒杀排队消息
      * @Author: huangdaoquan
      * @Date: 2022/7/2 19:47
      * @Param message:
      * @return: void
      * @Version: 1.0
      **/
    @RabbitListener(queues = MQConfig.MIAOSHA_QUEUE)
    public void receiveMiaosha(String message){
        log.info("receive miaosha queue message:" + new String(message));
        MiaoshaMessage miaoshaMessage = RedisService.stringToBean(message, MiaoshaMessage.class);
        MiaoshaUser miaoshaUser = miaoshaMessage.getMiaoshaUser();
        long goodsId = miaoshaMessage.getGoodsId();
        // 收到消息后  开始处理订单

        GoodsVo goodsVo = goodsService.getGoodsVoByGoodsId(goodsId);
        Integer stockCount = goodsVo.getStockCount();
        // 1.判断库存
        if (stockCount <= 0){
            return ;
        }
        // 2.判断自己是否已经秒杀过此产品（这里假定一人只能秒杀一次 一个产品）
        // 优化方案：因为相同的秒杀产品都是在很短时间内，所以把秒杀信息存在缓存中，这时候先查缓存，如果缓存有再查数据库
        MiaoshaOrder miaoshaOrder = orderService.getMiaoshaOrderByUserIdGoodsId(miaoshaUser.getId(), goodsId);
        if (miaoshaOrder != null){
            // 不为空 已经秒杀了过了
            return ;
        }
        // 还有库存且能秒杀 1 减库存 2 下订单 3 写入秒杀订单  3部为一个不可分割的事物单元
        // 3.数据库减库存，下订单，写入秒杀订单
       miaoshaServicce.miaosha(miaoshaUser, goodsVo);
    }
}
