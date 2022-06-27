package com.hdq.miaosha.controller;

import com.hdq.miaosha.domain.MiaoshaUser;
import com.hdq.miaosha.redis.RedisService;
import com.hdq.miaosha.result.Result;
import com.hdq.miaosha.service.GoodsService;
import com.hdq.miaosha.service.MiaoshaUserServicce;
import com.hdq.miaosha.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * @author hdq
 */
@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private MiaoshaUserServicce miaoshaUserServicce;

    @Autowired
    private RedisService redisService;

    @Autowired
    private GoodsService goodsService;
    
    
    /**
      * @Description: todo
      * @Author: huangdaoquan
      * @Date: 2022/6/23 12:29
      * @Param model: 
      * @Param miaoshauser: 
      * @return: java.lang.String
      * @Version: 1.0        
      **/
    @RequestMapping("/info")
    @ResponseBody
    public Result<MiaoshaUser> toList(Model model,
                                      MiaoshaUser miaoshauser) {

        return Result.success(miaoshauser);
    }

}
