package com.hdq.miaosha.access;

import com.hdq.miaosha.domain.MiaoshaUser;
import com.hdq.miaosha.redis.AccessKey;
import com.hdq.miaosha.redis.RedisService;
import com.hdq.miaosha.result.CodeMsg;
import com.hdq.miaosha.result.Result;
import com.hdq.miaosha.service.MiaoshaUserServicce;
import com.hdq.miaosha.util.WebUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Description : TODO
 * @Author : huangdaoquan
 * @Date : 2022/7/6 19:46
 * @Version : 1.0
 **/
@Service
public class AccessInterceptor extends HandlerInterceptorAdapter {

    @Autowired
    private MiaoshaUserServicce userService;

    @Autowired
    private RedisService redisService;

    public AccessInterceptor() {
        super();
    }

    /**
     *@Description : 防止刷接口的通用拦截器
     *@Author : huangdaoquan
     *@Date : 2022/7/6 19:52
     *@Version : 1.0
     **/
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod){
            MiaoshaUser user = getUser(request, response);
            /**
             *@Description : 保存当前线程的user用户
             *@Author : huangdaoquan
             *@Date : 2022/7/6 20:22
             *@Version : 1.0
             **/
            UserContext.setUser(user);

            HandlerMethod handlerMethod = (HandlerMethod) handler;
            AccessLimit accesslimit = handlerMethod.getMethodAnnotation(AccessLimit.class);
            if (accesslimit == null){
                return true;
            }

            int seconds = accesslimit.seconds();
            int maxCount = accesslimit.maxCount();
            boolean needLogin = accesslimit.needLogin();

            String key = request.getRequestURI();


            if (needLogin){
                // 需要登录
                if (user == null){
                    WebUtil.render(response, CodeMsg.SESSION_ERROR);
                    return false;
                }
                key += "_" + user.getId();
            }else {
                // 不需要登录则不需要限制流量， 啥都不做。
            }
            AccessKey accessKey = AccessKey.withExpire(seconds);
            Integer count = redisService.get(accessKey, key, Integer.class);
            if(count == null){
                redisService.set(accessKey, key, 1);
            }else if (count < maxCount){
                redisService.incr(accessKey, key);
            }else {
                WebUtil.render(response, CodeMsg.ACCESS_LIMIT_REACHED);
                return false;
            }
        }
        return true;
    }

    private MiaoshaUser getUser(HttpServletRequest request, HttpServletResponse response){


        // 一般为了兼容游览器 会把token放在游览器的请求参数里面 所以也要判断请求参数里面是否有token
        // 游览器的请求参数里面的token优先级高于cookie里面的token
        String paraToken = request.getParameter(MiaoshaUserServicce.COOKIE_NAME_TOKEN);
        String cookieToken = getCookieValue(request, MiaoshaUserServicce.COOKIE_NAME_TOKEN);
        if (StringUtils.isEmpty(paraToken) && StringUtils.isEmpty(cookieToken)) {
            return null;
        }
        String token = StringUtils.isEmpty(paraToken) ? cookieToken : paraToken;
        return userService.getByToken(response, token);
    }
    private String getCookieValue(HttpServletRequest request, String cookieName) {
        // 获取所有的cookie
        Cookie[] cookies = request.getCookies();
        if (cookies == null || cookies.length <= 0){
            return null;
        }
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(cookieName)) {
                return cookie.getValue();
            }
        }
        return null;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        super.postHandle(request, response, handler, modelAndView);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        super.afterCompletion(request, response, handler, ex);
    }

    @Override
    public void afterConcurrentHandlingStarted(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        super.afterConcurrentHandlingStarted(request, response, handler);
    }
}
