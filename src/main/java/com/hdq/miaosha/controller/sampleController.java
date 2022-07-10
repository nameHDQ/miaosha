package com.hdq.miaosha.controller;


import com.hdq.miaosha.domain.User;
import com.hdq.miaosha.rabbitmq.MQSender;
import com.hdq.miaosha.redis.RedisService;
import com.hdq.miaosha.redis.UserKey;
import com.hdq.miaosha.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/demo")
public class sampleController {


    @Autowired
    RedisService redisService;

    @Autowired
    MQSender mqSender;

    @ResponseBody
    @RequestMapping("/hello")
    public Result<String> thymeleaf(Model model){
        return Result.success("hello,imooc");
    }

    @ResponseBody
    @RequestMapping("/mq")
    public Result<String> mq(Model model){
        mqSender.send("hello word");
        return Result.success("hello,imooc");
    }

    @ResponseBody
    @RequestMapping("/mq/topic")
    public Result<String> topic(Model model){
        mqSender.sendTopic("hello word");
        return Result.success("hello,imooc");
    }


    @ResponseBody
    @RequestMapping("/mq/header")
    public Result<String> header(Model model){
        mqSender.sendHeader("hello word");
        return Result.success("hello,imooc");
    }

    @ResponseBody
    @RequestMapping("/mq/fanout")
    public Result<String> fanout(Model model){
        mqSender.sendFanout("hello word");
        return Result.success("hello,imooc");
    }


    @ResponseBody
    @RequestMapping("/redis/get")
    public Result<User> redisGet(Model model){
        User test = redisService.get(UserKey.getById, "" + 1, User.class);
        return Result.success(test);
    }

    @ResponseBody
    @RequestMapping("/redis/set")
    public Result<Boolean> redisSet(Model model){
        User temp = new User(1, "11111");
        boolean test = redisService.set(UserKey.getById, "" + 1, temp);
        return Result.success(test);
    }
}
