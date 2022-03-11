package com.hdq.miaosha.service;


import com.hdq.miaosha.dao.MiaoshaUserDao;
import com.hdq.miaosha.domain.MiaoshaUser;
import com.hdq.miaosha.exception.GlobalException;
import com.hdq.miaosha.redis.MiaoshaUserKey;
import com.hdq.miaosha.redis.RedisService;
import com.hdq.miaosha.result.CodeMsg;
import com.hdq.miaosha.util.MD5Util;
import com.hdq.miaosha.util.UUIDUtil;
import com.hdq.miaosha.vo.LoginVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

/**
 * 分布式session 即用户登录后，session会被存储到redis中，然后用redis来管理session
 * @author hdq
 */
@Service
public class MiaoshaUserServicce {


    public static final String COOKIE_NAME_TOKEN = "token";


    @Autowired
    private MiaoshaUserDao miaoshaUserDao;

    @Autowired
    private RedisService redisService;
    /**
     * 登录 将返回result封装类改成 boolean 体现是否真正登陆的逻辑
     * 采用统一异常处理  跑出异常
     * @param loginVo
     * @return
     */
    public boolean login(HttpServletResponse response, LoginVo loginVo) {
        if (loginVo == null) {
            throw new GlobalException(CodeMsg.SERVER_ERROR);
        }
        String mobile = loginVo.getMobile();
        String formPass = loginVo.getPassword();

        //判断手机号是否存在 暂时使用手机号作为ID
        MiaoshaUser user = miaoshaUserDao.getById(Long.parseLong(mobile));
        if (user == null) {
            throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
        }
        //验证密码
        String dbPass = user.getPassword();
        String saltDB = user.getSalt();
        String DBPass = MD5Util.formPassToDBPass(formPass, saltDB);
        if (!dbPass.equals(DBPass)) {
            throw new GlobalException(CodeMsg.PASSWORD_ERROR);
        }
        // 登录成功 生成token
        String token = UUIDUtil.uuid();
        // 添加cookie
        addCookie(response, token, user);
        return true;
    }

    /**
     * 根据token从redis内存缓存获取用户信息
     * //TODO: 2022/2/9 如果客户端禁用cookie 使用JWT(Json Web Token)来代替cookie
     * //TODO: 2022/2/9  更新cookie 延长有效期 但是会频繁访问DB 导致DB压力过大、解决方案：使用refreshToken和aceessToken的方式、其中 refreshToken是用户刷新的token  accessToken是用户登录的token
     * @param token
     * @return
     */
    public MiaoshaUser getByToken(HttpServletResponse response, String token) {
        if (StringUtils.isEmpty(token)) {
            return null;
        }
        MiaoshaUser miaoshaUser = redisService.get(MiaoshaUserKey.token, token, MiaoshaUser.class);
        if (miaoshaUser != null) {
            // 更新cookie 延长有效期 但是会频繁访问DB 导致DB压力过大
            // 解决方案：使用refreshToken和aceessToken的方式
            // 其中 refreshToken是用户刷新的token  accessToken是用户登录的token
            addCookie(response, token, miaoshaUser);
        }
        return miaoshaUser;
    }

    /**
     * 更新cookie 延长有效期
     * @param response
     * @param token 先用老得token 后期优化使用refreshToken
     * @param miaoshaUser
     */
    private void addCookie(HttpServletResponse response,String token, MiaoshaUser miaoshaUser) {
        // 将用户的token存入redis
        redisService.set(MiaoshaUserKey.token, token, miaoshaUser);
        // 将token写入cookie 里面存的token不包含redis中的token的前缀
        Cookie cookie = new Cookie(COOKIE_NAME_TOKEN, token);
        // 设置cookie的有效期 和redis的有效期保持一致
        cookie.setMaxAge(MiaoshaUserKey.token.expireSeconds());
        cookie.setPath("/");
        response.addCookie(cookie);
    }

}
