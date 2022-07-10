package com.hdq.miaosha.service;

import com.hdq.miaosha.dao.GoodsDao;
import com.hdq.miaosha.domain.Goods;
import com.hdq.miaosha.domain.MiaoshaOrder;
import com.hdq.miaosha.domain.MiaoshaUser;
import com.hdq.miaosha.domain.OrderInfo;
import com.hdq.miaosha.redis.MiaoshaKey;
import com.hdq.miaosha.redis.RedisService;
import com.hdq.miaosha.util.MD5Util;
import com.hdq.miaosha.util.UUIDUtil;
import com.hdq.miaosha.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

/**
 * @Description : TODO
 * @Author : huangdaoquan
 * @Date : 2022/6/21 13:27
 * @Version : 1.0
 **/
@Service
public class MiaoshaServicce {

    @Autowired
    GoodsService goodsService;

    @Autowired
    OrderService orderService;


    @Autowired
    RedisService redisService;


    /**
      * @Description: 只要订单没有在数据库生成成功，即使，缓存里的库存少了也没关系，不过是少少卖了几个
     * 后续可以调回来接着卖，但是多卖就很严重了
      * @Author: huangdaoquan
      * @Date: 2022/7/2 21:27
      * @Param miaoshaUser:
      * @Param goodsVo:
      * @return: com.hdq.miaosha.domain.OrderInfo
      * @Version: 1.0
      **/
    @Transactional
    public OrderInfo miaosha(MiaoshaUser miaoshaUser, GoodsVo goodsVo) {
        // 减库存，下订单，写入秒杀订单

        boolean successs = goodsService.reduceStock(goodsVo);
        if (successs){
            // order_info,miaosha_user 数据库减库存成功，才会下订单
            return orderService.createOrder(miaoshaUser, goodsVo);
        }else {

            // 减库存失败，缓存标记商品卖完了
            setGooodsOver(goodsVo.getId());
            return null;
        }


    }

    /**
      * @Description: 商品卖完缓存标记
      * @Author: huangdaoquan
      * @Date: 2022/7/2 21:32
      * @Param goodId:
      * @return: void
      * @Version: 1.0
      **/
    private void setGooodsOver(Long goodId) {
        redisService.set(MiaoshaKey.isGoodsOver, "" + goodId, true);
    }

    /**
      * @Description: 如果订单生成失败，但是缓存里的库存减了，也没关系，一样认为下单失败，
     * 但是若是缓存都减，订单生成全部失败，一个没卖出去如何解决
     * 可以发现卖完后，找一个时间点查询该秒杀商品的订单数量是否和总秒杀数一致，不一致重新更新库存缓存接着卖
      * @Author: huangdaoquan
      * @Date: 2022/7/2 21:19
      * @Param miaoshaUserId:
      * @Param goodsId:
      * @return: long
      * @Version: 1.0
      **/
    public long getMiaoshaResult(Long miaoshaUserId, long goodsId) {

        MiaoshaOrder miaoshaOrder = orderService.getMiaoshaOrderByUserIdGoodsId(miaoshaUserId, goodsId);
        if (miaoshaOrder != null){
            return miaoshaOrder.getOrderId();
        }else {
            // 获取订单失败，分析情况，卖完了：结束,没卖完：继续轮询状态，等待订单下单完成
            boolean isOver = getGoodsOver(goodsId);
            if (isOver){
                return -1;
            }else {
                return 0;
            }
        }
    }
    /**
      * @Description: 获取是否卖完标记
      * @Author: huangdaoquan
      * @Date: 2022/7/2 21:38
      * @Param goodsId:
      * @return: boolean
      * @Version: 1.0
      **/
    private boolean getGoodsOver(long goodsId) {
        return redisService.exits(MiaoshaKey.isGoodsOver, ""+ goodsId);
    }

    public String creatMiaoshaPath(MiaoshaUser miaoshaUser, long goodsId) {
        String path= MD5Util.md5(UUIDUtil.uuid() + "123456");
        redisService.set(MiaoshaKey.getMiaoshaPath, "" + miaoshaUser.getId()+"_" + goodsId, path);
        return path;
    }

    public boolean checkPath(MiaoshaUser miaoshaUser, long goodsId, String path) {
        if (miaoshaUser == null || path == null) return false;
        String pathOld = redisService.get(MiaoshaKey.getMiaoshaPath, "" + miaoshaUser.getId() + "_" + goodsId, String.class);
        return path.equals(pathOld);
    }

    public BufferedImage createVerifyCode(MiaoshaUser miaoshaUser, long goodsId) {
        if (miaoshaUser == null || goodsId < 0) return null;

        int width = 80;
        int height = 32;
        //create the image
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();
        // set the background color
        g.setColor(new Color(0xDCDCDC));
        g.fillRect(0, 0, width, height);
        // draw the border
        g.setColor(Color.black);
        g.drawRect(0, 0, width - 1, height - 1);
        // create a random instance to generate the codes
        Random rdm = new Random();
        // make some confusion
        for (int i = 0; i < 50; i++) {
            int x = rdm.nextInt(width);
            int y = rdm.nextInt(height);
            g.drawOval(x, y, 0, 0);
        }
        // generate a random code
        String verifyCode = generateVerifyCode(rdm);
        g.setColor(new Color(0, 100, 0));
        g.setFont(new Font("Candara", Font.BOLD, 24));
        g.drawString(verifyCode, 8, 24);
        g.dispose();
        //把验证码存到redis中
        int rnd = calc(verifyCode);
        redisService.set(MiaoshaKey.getMiaoshaVerifyCode, miaoshaUser.getId()+","+goodsId, rnd);
        //输出图片
        return image;
    }

    public boolean checkVerifyCode(MiaoshaUser user, long goodsId, int verifyCode) {
        if(user == null || goodsId <=0) {
            return false;
        }
        Integer codeOld = redisService.get(MiaoshaKey.getMiaoshaVerifyCode, user.getId()+","+goodsId, Integer.class);
        if(codeOld == null || codeOld - verifyCode != 0 ) {
            return false;
        }
        redisService.delete(MiaoshaKey.getMiaoshaVerifyCode, user.getId()+","+goodsId);
        return true;
    }

    /**
      * @Description: 计算数字验证码的结果
      * @Author: huangdaoquan
      * @Date: 2022/7/4 20:28
      * @Param exp:
      * @return: int
      * @Version: 1.0
      **/
    private static int calc(String exp) {
        try {
            ScriptEngineManager manager = new ScriptEngineManager();
            ScriptEngine engine = manager.getEngineByName("JavaScript");
            return (Integer)engine.eval(exp);
        }catch(Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private static char[] ops = new char[] {'+', '-', '*'};
    /**
     * + - *
     * */

    /**
      * @Description: 生成计算数字验证码
      * @Author: huangdaoquan
      * @Date: 2022/7/4 20:29
      * @Param rdm:
      * @return: java.lang.String
      * @Version: 1.0
      **/
    private String generateVerifyCode(Random rdm) {
        int num1 = rdm.nextInt(10);
        int num2 = rdm.nextInt(10);
        int num3 = rdm.nextInt(10);
        char op1 = ops[rdm.nextInt(3)];
        char op2 = ops[rdm.nextInt(3)];
        String exp = ""+ num1 + op1 + num2 + op2 + num3;
        return exp;
    }
}
