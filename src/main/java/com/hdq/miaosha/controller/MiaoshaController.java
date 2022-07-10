package com.hdq.miaosha.controller;

import com.hdq.miaosha.access.AccessLimit;
import com.hdq.miaosha.domain.MiaoshaOrder;
import com.hdq.miaosha.domain.MiaoshaUser;
import com.hdq.miaosha.domain.OrderInfo;
import com.hdq.miaosha.domain.User;
import com.hdq.miaosha.rabbitmq.MQSender;
import com.hdq.miaosha.rabbitmq.MiaoshaMessage;
import com.hdq.miaosha.redis.AccessKey;
import com.hdq.miaosha.redis.GoodsKey;
import com.hdq.miaosha.redis.MiaoshaKey;
import com.hdq.miaosha.redis.RedisService;
import com.hdq.miaosha.result.CodeMsg;
import com.hdq.miaosha.result.Result;
import com.hdq.miaosha.service.GoodsService;
import com.hdq.miaosha.service.MiaoshaServicce;
import com.hdq.miaosha.service.MiaoshaUserServicce;
import com.hdq.miaosha.service.OrderService;
import com.hdq.miaosha.util.MD5Util;
import com.hdq.miaosha.util.UUIDUtil;
import com.hdq.miaosha.vo.GoodsVo;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.websocket.server.PathParam;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description : TODO
 * @Author : huangdaoquan
 * @Date : 2022/6/20 16:40
 * @Version : 1.0
 **/
@Controller
@RequestMapping("/miaosha")
public class MiaoshaController implements InitializingBean {
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

    @Autowired
    MQSender mqSender;

    // 商品是否卖完的本地化比较，这样在卖完后，可以减少一次数据库查询
    private Map<Long, Boolean> localOverMap = new HashMap<>();



    /**
      * @Description: 动态秒杀地址生成，防止刷IP，这里只是简单的一种生成方式
     * 更好的是返回302跳转到新的页面，新页面和旧的秒杀页面完全不同，秒杀地址也不同
     * 类似京东抢茅台，跳转到一个新的结算界面
     *最常用限流算法的就是使用令牌桶或者漏斗桶算法
     * // 秒杀接口限流：
     * 使用
      * @Author: huangdaoquan
      * @Date: 2022/7/4 19:18
      * @Param model:
  * @Param miaoshaUser:
  * @Param goodsId:
      * @return: com.hdq.miaosha.result.Result<java.lang.String>
      * @Version: 1.0
      **/
    @AccessLimit(seconds = 5, maxCount= 10,needLogin = true)
    @RequestMapping(value = "/path", method = RequestMethod.GET)
    @ResponseBody
    public Result<String> getMiaoshaPath (HttpServletRequest httpServletRequest,Model model, MiaoshaUser miaoshaUser
            , @RequestParam("goodsId") long goodsId, @RequestParam(value = "verifyCode",defaultValue = "0") int verifyCode){
        if (miaoshaUser == null){
            return Result.error(CodeMsg.SESSION_ERROR);
        }

        /**
         *@Description :限制访问次数，对于动态生成地址，如果访问这个生成动态地址的行为很频繁，次数大于五，说明有人在恶意刷动态生成地址的秒杀接口
         * 限制5秒钟5次
         *
         * 优化：
         * 通过注解拦截器优化
         *@Author : huangdaoquan
         *@Date : 2022/7/6 19:14
         *@Version : 1.0
         **/
//        String requestURI = httpServletRequest.getRequestURI();
//        String key = requestURI + "_" + miaoshaUser.getId();
//        Integer count = redisService.get(AccessKey.accessKey, key, Integer.class);
//
//        if(count == null){
//            redisService.set(AccessKey.accessKey, key, 1);
//        }else if (count < 5){
//            redisService.incr(AccessKey.accessKey, key);
//        }else {
//            return Result.error(CodeMsg.ACCESS_LIMIT_REACHED);
//        }

        boolean check = miaoshaServicce.checkVerifyCode(miaoshaUser, goodsId, verifyCode);
        if (!check){
            return Result.error(CodeMsg.REQUEST_ILLEGAL);
        }
        model.addAttribute("user", miaoshaUser);
        String path  = miaoshaServicce.creatMiaoshaPath(miaoshaUser, goodsId);
        return Result.success(path);

    }


    @RequestMapping(value = "/verifyCode", method = RequestMethod.GET)
    @ResponseBody
    public Result<String> getMiaoshaVerifyCode (Model model, MiaoshaUser miaoshaUser
            , @PathParam("goodsId") long goodsId, HttpServletResponse httpServletResponse){
        if (miaoshaUser == null){
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        model.addAttribute("user", miaoshaUser);
        BufferedImage image = miaoshaServicce.createVerifyCode(miaoshaUser, goodsId);
        try {
            ServletOutputStream outputStream = httpServletResponse.getOutputStream();
            ImageIO.write(image, "JPEG", outputStream);
            outputStream.flush();
            outputStream.close();
            // 数据已经通过HttpServletResponse返回到页面，打印到页面了
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error(CodeMsg.MIAOSHA_FAIL);
        }
    }

    /**
      * @Description: 系统初始化时所做的一些事情
     * 系统初始化加载秒杀库存
      * @Author: huangdaoquan
      * @Date: 2022/7/2 18:07

      * @return: void
      * @Version: 1.0
      **/
    @Override
    public void afterPropertiesSet() throws Exception {

        List<GoodsVo> goodsList = goodsService.listGoodsVo();
        if (goodsList == null){
            return;
        }
        // 预加载商品库存
        for (GoodsVo goodsVo : goodsList) {
            redisService.set(GoodsKey.getMiaoshaGoodsStock, "" + goodsVo.getId(), goodsVo.getGoodsStock());
            localOverMap.put(goodsVo.getId(), false);
        }



    }
    /**
     *@Description : 第一版
     *@Author : huangdaoquan
     *@Date : 2022/6/28 16:27
     *@Version : 1.0
     **/
//    @RequestMapping("/do_miaosha")
//    public String miaosha(Model model, MiaoshaUser miaoshaUser, @PathParam("goodsId") long goodsId){
//
//        if (miaoshaUser == null){
//            return "login";
//        }
//        model.addAttribute("user", miaoshaUser);
//        GoodsVo goodsVo = goodsService.getGoodsVoByGoodsId(goodsId);
//        Integer stockCount = goodsVo.getStockCount();
//        // 1.判断库存
//        if (stockCount <= 0){
//            model.addAttribute("errmsg", CodeMsg.MIAO_SHA_OVER.getMsg());
//            return "miaosha_fail";
//        }
//        // 2.判断自己是否已经秒杀过此产品（这里假定一人只能秒杀一次 一个产品）
//        MiaoshaOrder miaoshaOrder = orderService.getMiaoshaOrderByUserIdGoodsId(miaoshaUser.getId(), goodsId);
//        if (miaoshaOrder != null){
//            // 不为空 已经别秒杀了
//            model.addAttribute("errmsg", CodeMsg.REPEATE_MIAOSHA.getMsg());
//            return "miaosha_fail";
//        }
//        // 还有库存且能秒杀 1 减库存 2 下订单 3 写入秒杀订单  3部为一个不可分割的事物单元
//        // 3.秒杀成功后直接跳转到订单详情页面
//        OrderInfo orderInfo = miaoshaServicce.miaosha(miaoshaUser, goodsVo);
//        model.addAttribute("orderInfo", orderInfo);
//        model.addAttribute("goods", goodsVo);
//        return "order_detail";
//    }
    /**
     *@Description : 第二版，页面静态化
     * GET POST 有什么区别：
     * GET：向服务端获取数据，具有幂等性，无论调用多少次，结果都是一样的
     * POST：向服务端提交数据，不具有幂等性，使得服务端数据发生了变化
     *
     *
     * 超卖解决方案第二步：
     * 如果一个用户在一瞬间发出两个相同的秒杀请求 req1 req2 产生重复秒杀
     * 解决方案：在数据库miaoshaorder中建立唯一索引，避免重复插入
     *
     * 第三版：系统初始化把商品库存数量加载redis
     * 收到秒杀请求，redis预减库存
     * 库存不足，直接返回
     * 库存足，请求进入redis队列，返回排队中。异步请求出队，减少库存，生成订单
     *
     * 客户端一直轮询，是否秒杀成功
     *@Author : huangdaoquan
     *@Date : 2022/6/28 16:27
     *@Version : 1.0
     **/
    @RequestMapping(value = "/{path}/do_miaosha", method = RequestMethod.POST)
    @ResponseBody
    public Result<Integer> miaosha ( Model model, MiaoshaUser miaoshaUser
            , @PathParam("goodsId") long goodsId, @PathVariable("path") String path){

        if (miaoshaUser == null){
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        model.addAttribute("user", miaoshaUser);





        // 验证动态生成的path
        boolean check = miaoshaServicce.checkPath(miaoshaUser, goodsId, path);
        if (!check){
            return Result.error(CodeMsg.REQUEST_ILLEGAL);
        }


//        V3版本：redis预减库存，异步下单


        // 访问本地化状态，是否卖完，减少一次数据库访问
        Boolean isOver = localOverMap.get(goodsId);
        if (isOver) {
            return Result.error(CodeMsg.MIAO_SHA_OVER);
        }
        // 预减缓存库存，如果缓存里的库存没了，说明，秒杀完了，下面的请求不必进来了，减少了数据库的访问
        // 注意：redis的decr操作是原子的，所以这里不存在并发问题
        Long stock = redisService.decr(GoodsKey.getMiaoshaGoodsStock, "" + goodsId);

        if (stock < 0){
            localOverMap.put(goodsId, true);
            return Result.error(CodeMsg.MIAO_SHA_OVER);
        }

        // 判断是否已经秒杀到了
        // 判断自己是否已经秒杀过此产品（这里假定一人只能秒杀一次 一个产品）
        // 优化方案：因为相同的秒杀产品都是在很短时间内，所以把秒杀信息存在缓存中，这时候先查缓存，如果缓存有再查数据库
        MiaoshaOrder miaoshaOrder = orderService.getMiaoshaOrderByUserIdGoodsId(miaoshaUser.getId(), goodsId);
        if (miaoshaOrder != null){
            // 不为空 已经秒杀了过了
            return Result.error(CodeMsg.REPEATE_MIAOSHA);
        }
        // 入队
        MiaoshaMessage miaoshaMessage = new MiaoshaMessage();
        miaoshaMessage.setMiaoshaUser(miaoshaUser);
        miaoshaMessage.setGoodsId(goodsId);
        mqSender.sendMiaoshaMessage(miaoshaMessage);
        return Result.success(0);// 排队中
//        V2版本：页面静态化
//        GoodsVo goodsVo = goodsService.getGoodsVoByGoodsId(goodsId);
//        Integer stockCount = goodsVo.getStockCount();
//        // 1.判断库存
//        if (stockCount <= 0){
//            return Result.error(CodeMsg.MIAO_SHA_OVER);
//        }
//        // 2.判断自己是否已经秒杀过此产品（这里假定一人只能秒杀一次 一个产品）
//        // 优化方案：因为相同的秒杀产品都是在很短时间内，所以把秒杀信息存在缓存中，这时候先查缓存，如果缓存有再查数据库
//        MiaoshaOrder miaoshaOrder = orderService.getMiaoshaOrderByUserIdGoodsId(miaoshaUser.getId(), goodsId);
//        if (miaoshaOrder != null){
//            // 不为空 已经秒杀了过了
//            return Result.error(CodeMsg.REPEATE_MIAOSHA);
//        }
//        // 还有库存且能秒杀 1 减库存 2 下订单 3 写入秒杀订单  3部为一个不可分割的事物单元
//        // 3.秒杀成功后直接跳转到订单详情页面
//        OrderInfo orderInfo = miaoshaServicce.miaosha(miaoshaUser, goodsVo);
//        return Result.success(orderInfo);
    }



    /**
      * @Description:   返回状态
     * 成功：orderid:（数据库自增 大于0 的）
     * 失败：-1
     * 排队中：0
      * @Author: huangdaoquan
      * @Date: 2022/7/2 21:13
      * @Param model:
      * @Param miaoshaUser:
      * @Param goodsId:
      * @return: com.hdq.miaosha.result.Result<java.lang.Integer>
      * @Version: 1.0
      **/
    @RequestMapping(value = "/result", method = RequestMethod.GET)
    @ResponseBody
    public Result<Long> miaoshaResult (Model model, MiaoshaUser miaoshaUser, @PathParam("goodsId") long goodsId) {
        if (miaoshaUser == null){
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        model.addAttribute("user", miaoshaUser);

        long orderId = miaoshaServicce.getMiaoshaResult(miaoshaUser.getId(), goodsId);


        return Result.success(orderId);
    }

}
