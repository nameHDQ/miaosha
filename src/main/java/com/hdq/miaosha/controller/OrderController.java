package com.hdq.miaosha.controller;


import com.hdq.miaosha.domain.MiaoshaUser;
import com.hdq.miaosha.domain.OrderInfo;
import com.hdq.miaosha.domain.User;
import com.hdq.miaosha.redis.RedisService;
import com.hdq.miaosha.redis.UserKey;
import com.hdq.miaosha.result.CodeMsg;
import com.hdq.miaosha.result.Result;
import com.hdq.miaosha.service.GoodsService;
import com.hdq.miaosha.service.OrderService;
import com.hdq.miaosha.vo.GoodsVo;
import com.hdq.miaosha.vo.OrderDetailVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.websocket.server.PathParam;

@Controller
@RequestMapping("/order")
public class OrderController {


    @Autowired
    RedisService redisService;

    @Autowired
    OrderService orderService;


    @Autowired
    GoodsService goodsService;



    @ResponseBody
    @RequestMapping("/hello")
    public Result<String> thymeleaf(Model model){
        return Result.success("hello,imooc");
    }

    @ResponseBody
    @RequestMapping("/redis/get")
    public Result<User> redisGet(Model model){
        User test = redisService.get(UserKey.getById, "" + 1, User.class);
        return Result.success(test);
    }

    /**
     *@Description : TODO:添加一个拦截器就可以直接去判断miaoshauser是否为空，避免大量的重复验证
     *@Author : huangdaoquan
     *@Date : 2022/6/28 20:42
     *@Version : 1.0
     **/
    @ResponseBody
    @RequestMapping("/detail")
    public Result<OrderDetailVo> info(Model model, MiaoshaUser miaoshaUser, @RequestParam("orderId") long orderId){

        if (miaoshaUser == null){
            return Result.error(CodeMsg.SESSION_ERROR);
        }

        OrderInfo orderInfo = orderService.getOrderById(orderId);
        if (orderInfo == null){
            return Result.error(CodeMsg.ORDER_NOT_EXIST);
        }

        Long goodsId = orderInfo.getGoodsId();
        GoodsVo goodsVo = goodsService.getGoodsVoByGoodsId(goodsId);

        OrderDetailVo orderDetailVo = new OrderDetailVo();
        orderDetailVo.setOrderInfo(orderInfo);
        orderDetailVo.setGoodsVo(goodsVo);
        return Result.success(orderDetailVo);
    }
}
