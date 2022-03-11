package com.hdq.miaosha.controller;

import com.hdq.miaosha.redis.RedisService;
import com.hdq.miaosha.result.Result;
import com.hdq.miaosha.service.MiaoshaUserServicce;
import com.hdq.miaosha.service.UserService;
import com.hdq.miaosha.vo.LoginVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/login")
public class LoginController {



    /**
     * 日志记录器 sf4j logger接口  可以自己实现日志
     */
    private static Logger log = LoggerFactory.getLogger(LoginController.class);
    @Autowired
    private RedisService redisService;

    @Autowired
    private MiaoshaUserServicce miaoshaUserServicce;

    @Autowired
    private UserService userService;
    /**
     * 跳转login页面
     * @param model
     * @return
     */
    @RequestMapping("/to_login")
    public String thymeleaf(Model model){
        return "login";
    }

    /**
     * 登录
     * 出现异常 直接被全局异常拦截器捕获 并返回错误信息
     * @param loginVo
     * @return
     */
    @ResponseBody
    @RequestMapping("/do_login")
    public Result<Boolean> doLogin(HttpServletResponse response, @Validated LoginVo loginVo){
        log.info(loginVo.toString());
//        String mobile = loginVo.getMobile();
//        String password = loginVo.getPassword();
        //参数校验
//        if (StringUtils.isEmpty(mobile)){
//            return Result.error(CodeMsg.MOBILE_EMPTY);
//        }
//        if (!ValidatorUtil.isMobile(mobile)){
//            return Result.error(CodeMsg.MOBILE_ERROR);
//        }
//        if (StringUtils.isEmpty(password)){
//            return Result.error(CodeMsg.PASSWORD_EMPTY);
//        }
        //登陆
        boolean login = miaoshaUserServicce.login(response, loginVo);
        return Result.success(true);
    }

}
