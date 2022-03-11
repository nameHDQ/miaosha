package com.hdq.miaosha.controller;

import com.hdq.miaosha.domain.MiaoshaUser;
import com.hdq.miaosha.redis.RedisService;
import com.hdq.miaosha.service.GoodsService;
import com.hdq.miaosha.service.MiaoshaUserServicce;
import com.hdq.miaosha.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * @author hdq
 */
@Controller
@RequestMapping("/goods")
public class GoodsController {

    @Autowired
    private MiaoshaUserServicce miaoshaUserServicce;

    @Autowired
    private RedisService redisService;

    @Autowired
    private GoodsService goodsService;

    /**
     * 商品列表详情 一般为了兼容游览器 会把token放在游览器的请求参数里面 所以也要判断请求参数里面是否有token
     * 游览器的请求参数里面的token优先级高于cookie里面的token
     * @param model model
     * @miaoshauser 通过配置参数处理器获取到的用户信息
     * cookieToken 存在cookie中的token
     * paramToken    存在请求参数中的token
     * @return
     */
    @RequestMapping("/to_list")
    public String toList(Model model,
//                         使用统一参数处理优化冗余代码 下面代码省去了
//                         HttpServletResponse response,
//                         @CookieValue(value = MiaoshaUserServicce.COOKIE_NAME_TOKEN, required = false) String cookieToken,
//                         @RequestParam(value = MiaoshaUserServicce.COOKIE_NAME_TOKEN, required = false) String paramToken,
                         MiaoshaUser miaoshauser) {

        model.addAttribute("user", miaoshauser);
        // 查询商品列表
        List<GoodsVo> goodsVos = goodsService.listGoodsVo();
        model.addAttribute("goodsList", goodsVos);
        return "goods_list";
    }

}
