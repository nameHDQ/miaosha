package com.hdq.miaosha.rabbitmq;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * @Description : TODO
 * @Author : huangdaoquan
 * @Date : 2022/6/29 21:53
 * @Version : 1.0
 **/
@Configuration
public class MQConfig {

    public static final String QUEUE = "queue";
    public static final String MIAOSHA_QUEUE = "miaosha.queue";
    public static final String TOPIC_QUEUE1 = "topic.queue1";
    public static final String TOPIC_QUEUE2 = "topic.queue2";
    public static final String HEADERS_QUEUE = "header.queue";

    public static final String TOPIC_EXCHANGE = "topicExchange";
    public static final String FANOUT_EXCHANGE = "fanoutExchange";
    public static final String HEADERS_EXCHANGE = "headersExchange";

    public static final String ROUTING_KEY1 = "topic.key1";
    public static final String ROUTING_KEY2 = "topic.#";

    @Bean
    public Queue miaoshaQueue() {
        return new Queue("miaosha.queue", true);
    }


    //下面为 rebbitmq的四种交换机模式例子



    /**
      * @Description: Direct 模式  交换机 exchange
      * @Author: huangdaoquan
      * @Date: 2022/6/30 17:01

      * @return: org.springframework.amqp.core.Queue
      * @Version: 1.0
      **/
    @Bean
    public Queue queue(){
        return new Queue(QUEUE, true);
    }


    /**
      * @Description: topic模式
      * @Author: huangdaoquan
      * @Date: 2022/6/30 17:04

      * @return: org.springframework.amqp.core.Queue
      * @Version: 1.0
      **/
    @Bean
    public Queue topicQueue1(){
        return new Queue(TOPIC_QUEUE1, true);
    }
    @Bean
    public Queue topicQueue2(){
        return new Queue(TOPIC_QUEUE2, true);
    }


    /**
      * @Description:  topic交换机
      * @Author: huangdaoquan
      * @Date: 2022/6/30 17:06

      * @return: org.springframework.amqp.core.TopicExchange
      * @Version: 1.0
      **/
    @Bean
    public TopicExchange topicExchange(){
        return new TopicExchange(TOPIC_EXCHANGE);
    }

    @Bean
    public Binding topicBind1(){
        return BindingBuilder.bind(topicQueue1()).to(topicExchange()).with(ROUTING_KEY1);
    }


    @Bean
    public Binding topicBind2(){
        return BindingBuilder.bind(topicQueue2()).to(topicExchange()).with(ROUTING_KEY2);
    }




    /**
      * @Description: 广播模式   广播交换机
      * @Author: huangdaoquan
      * @Date: 2022/6/30 19:23

      * @return: org.springframework.amqp.core.FanoutExchange
      * @Version: 1.0
      **/
    @Bean
    public FanoutExchange fanOutExchange(){
        return new FanoutExchange(FANOUT_EXCHANGE);
    }

    @Bean
    public Binding fanoutBind1(){
        return BindingBuilder.bind(topicQueue1()).to(fanOutExchange());
    }


    @Bean
    public Binding fanoutBind2(){
        return BindingBuilder.bind(topicQueue2()).to(fanOutExchange());
    }

    /**
      * @Description: headers模式
      * @Author: huangdaoquan
      * @Date: 2022/6/30 19:54

      * @return: org.springframework.amqp.core.HeadersExchange
      * @Version: 1.0
      **/
    @Bean
    public HeadersExchange headersExchange(){
        return new HeadersExchange(HEADERS_EXCHANGE);
    }

    @Bean
    public Queue headerQueue(){
        return new Queue(HEADERS_QUEUE, true);
    }

    @Bean
    public Binding headersBind1(){
        Map<String, Object> map = new HashMap<>();
        map.put("header1", "value1");
        map.put("header2", "value2");
        return BindingBuilder.bind(headerQueue()).to(headersExchange()).whereAll(map).match();
    }






}
