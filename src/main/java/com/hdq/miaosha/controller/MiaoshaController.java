package com.hdq.miaosha.controller;

import com.hdq.miaosha.domain.MiaoshaOrder;
import com.hdq.miaosha.domain.MiaoshaUser;
import com.hdq.miaosha.domain.OrderInfo;
import com.hdq.miaosha.domain.User;
import com.hdq.miaosha.redis.RedisService;
import com.hdq.miaosha.result.CodeMsg;
import com.hdq.miaosha.service.GoodsService;
import com.hdq.miaosha.service.MiaoshaServicce;
import com.hdq.miaosha.service.MiaoshaUserServicce;
import com.hdq.miaosha.service.OrderService;
import com.hdq.miaosha.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.websocket.server.PathParam;

/**
 * @Description : TODO
 * @Author : huangdaoquan
 * @Date : 2022/6/20 16:40
 * @Version : 1.0
 **/
@Controller
@RequestMapping("/miaosha")
public class MiaoshaController {
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

    @RequestMapping("/do_miaosha")
    public String miaosha(Model model, MiaoshaUser miaoshaUser, @PathParam("goodsId") long goodsId){

        if (miaoshaUser == null){
            return "login";
        }
        model.addAttribute("user", miaoshaUser);
        GoodsVo goodsVo = goodsService.getGoodsVoByGoodsId(goodsId);
        Integer stockCount = goodsVo.getStockCount();
        // 1.判断库存
        if (stockCount <= 0){
            model.addAttribute("errmsg", CodeMsg.MIAO_SHA_OVER.getMsg());
            return "miaosha_fail";
        }
        // 2.判断自己是否已经秒杀过此产品（这里假定一人只能秒杀一次 一个产品）
        MiaoshaOrder miaoshaOrder = orderService.getMiaoshaOrderByUserIdGoodsId(miaoshaUser.getId(), goodsId);
        if (miaoshaOrder != null){
            // 不为空 已经别秒杀了
            model.addAttribute("errmsg", CodeMsg.REPEATE_MIAOSHA.getMsg());
            return "miaosha_fail";
        }
        // 还有库存且能秒杀 1 减库存 2 下订单 3 写入秒杀订单  3部为一个不可分割的事物单元
        // 3.秒杀成功后直接跳转到订单详情页面
        OrderInfo orderInfo = miaoshaServicce.miaosha(miaoshaUser, goodsVo);
        model.addAttribute("orderInfo", orderInfo);
        model.addAttribute("goods", goodsVo);
        return "order_detail";
    }

}
