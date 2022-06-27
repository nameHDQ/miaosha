package com.hdq.miaosha.service;


import com.hdq.miaosha.dao.MiaoshaUserDao;
import com.hdq.miaosha.domain.MiaoshaUser;
import com.hdq.miaosha.domain.User;
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
import java.security.Principal;

/**
 * 分布式session 即用户登录后，session会被存储到redis中，然后用redis来管理session
 * 自己的service一定要调用自己的，别人的service可能有缓存数据操作，不同service的缓存操作可能不一样
 * 在controller里一定要调用service，不要调用dao，service里有缓存操作，直接调用dao可能绕过了缓存操作，导致缓存和数据库不一致
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
      * @Description: *******重要*********对象级缓存，相比页面缓存，粒度更细，只要对象不变化，那么这个对象是永久有效的
      * @Author: huangdaoquan
      * @Date: 2022/6/27 16:42
      * @Param id:
      * @return: com.hdq.miaosha.domain.MiaoshaUser
      * @Version: 1.0
      **/
    public MiaoshaUser getById(long id){
        // 取缓存
        MiaoshaUser miaoshaUser = redisService.get(MiaoshaUserKey.getById, "" + id, MiaoshaUser.class);
        if (miaoshaUser != null){
            return miaoshaUser;
        }
        // 缓存为空，到数据库中查询，更新缓存
        miaoshaUser = miaoshaUserDao.getById(id);
        if (miaoshaUser != null){
            redisService.set(MiaoshaUserKey.getById, "" + id, miaoshaUser);
        }
        return miaoshaUser;
    }

    /**
     *@Description : 对于对象级缓存， 更改数据库的对象数据时，一定要将缓存里的数据也更改，保持一致
     *@Author : huangdaoquan
     *@Date : 2022/6/27 18:10
     *@Version : 1.0
     **/
    public boolean updatePassword(long id, String token, String passWordNew){
        // 取user
        MiaoshaUser miaoshaUser = getById(id);
        if (miaoshaUser == null){
            throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
        }
        MiaoshaUser toBeupdate = new MiaoshaUser();

        toBeupdate.setPassword(MD5Util.formPassToDBPass(passWordNew, miaoshaUser.getSalt()));
        toBeupdate.setId(id);

        miaoshaUserDao.update(toBeupdate);
        // 更新缓存

        // 删除缓存中的旧user
        redisService.delete(MiaoshaUserKey.getById, "" + id);
        // 更新缓存中的旧user的token，注意 不是删除，而是更新，如果是删除需要两步操作，而更新只需要一步就行
        miaoshaUser.setPassword(toBeupdate.getPassword());
        redisService.set(MiaoshaUserKey.token, "" + token, miaoshaUser);


        return true;
    }

    /**
     * 登录 将返回result封装类改成 boolean 体现是否真正登陆的逻辑
     * 采用统一异常处理  跑出异常
     * @param loginVo
     * @return
     */
    public String login(HttpServletResponse response, LoginVo loginVo) {
        if (loginVo == null) {

            throw new GlobalException(CodeMsg.SERVER_ERROR);
        }
        String mobile = loginVo.getMobile();
        String formPass = loginVo.getPassword();

        //判断手机号是否存在 暂时使用手机号作为ID
        MiaoshaUser user = getById(Long.parseLong(mobile));
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
        //TODO: 每次登陆都生成一个cookie，这个没必要，只要cookie没过期，其实可以不用生成
        // 解决方案：不设置cookie的有效期，让它永远有效，给redis中的token设置个有效期，如果cookie中的token过期 重新生成
        addCookie(response, token, user);
        return token;
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
            //TODO：解决方案：使用refreshToken和aceessToken 双token的方式，其中 refreshToken是用户刷新的token  accessToken是用户登录的token
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
