package com.hdq.miaosha.rabbitmq;

import com.hdq.miaosha.redis.RedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

/**
 * @Description : TODO
 * @Author : huangdaoquan
 * @Date : 2022/6/29 21:52
 * @Version : 1.0
 **/
@Service
public class MQSender {

    private static Logger log = LoggerFactory.getLogger(MQReceiver.class);
    @Autowired
    AmqpTemplate amqpTemplate;


    public void send(Object message){
        String msg = RedisService.beanToValue(message);
        log.info("send message:" + msg);
        amqpTemplate.convertAndSend(MQConfig.QUEUE, msg);
    }


    public void sendTopic(Object message){
        String msg = RedisService.beanToValue(message);
        log.info("send topic message:" + msg);
        amqpTemplate.convertAndSend(MQConfig.TOPIC_EXCHANGE, "topic.key1", msg + "1");
        amqpTemplate.convertAndSend(MQConfig.TOPIC_EXCHANGE, "topic.key2", msg + "2");
    }

    public void sendFanout(Object message){
        String msg = RedisService.beanToValue(message);
        log.info("send fanout message:" + msg);
        amqpTemplate.convertAndSend(MQConfig.FANOUT_EXCHANGE, "",msg + "1");
    }

    public void sendHeader(Object message){
        String msg = RedisService.beanToValue(message);
        log.info("send header message:" + msg);
        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setHeader("header1", "value1");
        messageProperties.setHeader("header2", "value2");
        Message obj = new Message(msg.getBytes(), messageProperties);
        amqpTemplate.convertAndSend(MQConfig.HEADERS_EXCHANGE, "", obj);
    }


    /**
      * @Description: 使用direct交换机
      * @Author: huangdaoquan
      * @Date: 2022/7/2 19:40
      * @Param miaoshaMessage:
      * @return: void
      * @Version: 1.0
      **/
    public void sendMiaoshaMessage(MiaoshaMessage miaoshaMessage) {


        String msg = RedisService.beanToValue(miaoshaMessage);
        log.info("send message:" + msg);
        amqpTemplate.convertAndSend(MQConfig.MIAOSHA_QUEUE, msg);


    }
}
