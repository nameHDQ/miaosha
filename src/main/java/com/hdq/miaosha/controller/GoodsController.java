package com.hdq.miaosha.controller;

import com.hdq.miaosha.domain.MiaoshaUser;
import com.hdq.miaosha.redis.GoodsKey;
import com.hdq.miaosha.redis.RedisService;
import com.hdq.miaosha.result.Result;
import com.hdq.miaosha.service.GoodsService;
import com.hdq.miaosha.service.MiaoshaUserServicce;
import com.hdq.miaosha.vo.GoodsDetailVo;
import com.hdq.miaosha.vo.GoodsVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.Thymeleaf;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring5.context.webflux.SpringWebFluxContext;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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

    @Autowired
    ThymeleafViewResolver thymeleafViewResolver;

    /**
     * 商品列表详情
     *
     * 重要：*******页面缓存技术(html和页面显示的数据都缓存起来)：页面缓存的redis-key有效期一般比较短（60s），主要是防止瞬间用户的访问量突然加大的情况，如果时间太长，导致数据
     * 的实时性比较低
     *
     * 重要：*******一般为了兼容游览器 会把token放在游览器的请求参数里面 所以也要判断请求参数里面是否有token
     * 游览器的请求参数里面的token优先级高于cookie里面的token
     *
     * QPS:1267(我的mysql在远程服务器上，所以qps会随着网络波动)
     * 5000 * 10
     * @param model model
     * @miaoshauser 通过配置参数处理器获取到的用户信息
     * cookieToken 存在cookie中的token
     * paramToken    存在请求参数中的token
     * @return
     */
    @RequestMapping(value = "/to_list", produces = "text/html")
    @ResponseBody
    public String toList(Model model,
//                         使用统一参数处理优化冗余代码 下面代码省去了
                           HttpServletResponse response,
                         HttpServletRequest request,
//                         @CookieValue(value = MiaoshaUserServicce.COOKIE_NAME_TOKEN, required = false) String cookieToken,
//                         @RequestParam(value = MiaoshaUserServicce.COOKIE_NAME_TOKEN, required = false) String paramToken,
                         MiaoshaUser miaoshauser) {

//        model.addAttribute("user", miaoshauser);
//        // 查询商品列表
//        List<GoodsVo> goodsVos = goodsService.listGoodsVo();
//        model.addAttribute("goodsList", goodsVos);


//        return "goods_list";

        // 先从缓存里取
        String html = redisService.get(GoodsKey.getGoodsList, "", String.class);
        // 非空， 直接取缓存里的内容
        if (!StringUtils.isEmpty(html)){
            return html;
        }
        // 空的，先获取数据，再使用thymeleafViewResolver手动渲染
        model.addAttribute("user", miaoshauser);
        // 查询商品列表
        List<GoodsVo> goodsVos = goodsService.listGoodsVo();
        model.addAttribute("goodsList", goodsVos);

        WebContext webContext = new WebContext(request, response, request.getServletContext(), request.getLocale(),model.asMap());
        html = thymeleafViewResolver.getTemplateEngine().process("goods_list", webContext);
        // 渲染的html页面保存到缓存中
        if (!StringUtils.isEmpty(html)){
            redisService.set(GoodsKey.getGoodsList, "", html);
        }
        return html;
    }

    /**
      * @Description: todo:一般如果数据库里goods ID自增，那么很容易别人就从1开始for循环遍历
      * 解决方案：**************snowflake算法（很重要，要去做的）***************
      * URL缓存：物品详情要针对不同物品的ID单独设置缓存的key：  classname + goodsID 粒度更细
      * 每一个物品的详情页面因ID不同 进而URL不同，所以把带ID的html存到缓存中，实现URL缓存
      * 与页面缓存的区别在于多了一个ID
      * @Author: huangdaoquan
      * @Date: 2022/6/20 11:15
      * @Param model:
      * @Param goodsId:
      * @Param miaoshaUser:
      * @return: java.lang.String
      * @Version: 1.0
      **/
    @RequestMapping(value = "/to_detail2/{goodsId}", produces = "text/html")
    @ResponseBody
    public String toDetail2( HttpServletResponse response,
                            HttpServletRequest request,Model model, MiaoshaUser miaoshaUser, @PathVariable("goodsId") long goodsId){

        // 先从缓存里取
        String html = redisService.get(GoodsKey.getGoodsDetail, "" + goodsId, String.class);

        // 非空， 直接取缓存里的内容
        if (!StringUtils.isEmpty(html)){

            return html;
        }
        // 空的，先获取数据，再使用thymeleafViewResolver手动渲染
        model.addAttribute("user", miaoshaUser);
        GoodsVo goodsVo = goodsService.getGoodsVoByGoodsId(goodsId);
        model.addAttribute("goods", goodsVo);

        // 秒杀开始和结束时间
        long startAt = goodsVo.getStartDate().getTime();
        long endAt = goodsVo.getEndDate().getTime();
        long now = System.currentTimeMillis();

        // 秒杀状态
        int miaoshaStatus = 0;
        int remainSeconds = 0;

        if (now < startAt){//秒杀没开始
            miaoshaStatus = 0;
            remainSeconds = (int) ((startAt - now) /1000);
        }else if (now > endAt){//秒杀结束
            miaoshaStatus = 2;
            remainSeconds = -1;
        }else {// 秒杀进行
            miaoshaStatus = 1;
            remainSeconds = 0;
        }
        model.addAttribute("miaoshaStatus", miaoshaStatus);
        model.addAttribute("remainSeconds", remainSeconds);
        WebContext webContext = new WebContext(request, response, request.getServletContext(), request.getLocale(),model.asMap());
        html = thymeleafViewResolver.getTemplateEngine().process("goods_detail", webContext);
        // 渲染的html页面保存到缓存中
        if (!StringUtils.isEmpty(html)){
            redisService.set(GoodsKey.getGoodsDetail, "" + goodsId, html);
        }
        return html;
    }


    /**
     *@Description : 页面静态化：页面只存储html，动态数据通过接口获取
     * 页面是静态的html，但是数据是动态ajax（或者其他vue，angular等框架）获取的
     *@Author : huangdaoquan
     *@Date : 2022/6/27 19:30
     *@Version : 1.0
     **/
    @RequestMapping(value = "/detail/{goodsId}")
    @ResponseBody
    public Result<GoodsDetailVo> toDetail(HttpServletResponse response,
                                           HttpServletRequest request, Model model, MiaoshaUser miaoshaUser, @PathVariable("goodsId") long goodsId){

        GoodsVo goodsVo = goodsService.getGoodsVoByGoodsId(goodsId);

        // 秒杀开始和结束时间
        long startAt = goodsVo.getStartDate().getTime();
        long endAt = goodsVo.getEndDate().getTime();
        long now = System.currentTimeMillis();

        // 秒杀状态
        int miaoshaStatus = 0;
        int remainSeconds = 0;

        if (now < startAt){//秒杀没开始
            miaoshaStatus = 0;
            remainSeconds = (int) ((startAt - now) /1000);
        }else if (now > endAt){//秒杀结束
            miaoshaStatus = 2;
            remainSeconds = -1;
        }else {// 秒杀进行
            miaoshaStatus = 1;
            remainSeconds = 0;
        }

        GoodsDetailVo goodsDetailVo = new GoodsDetailVo();
        goodsDetailVo.setGoodsVo(goodsVo);
        goodsDetailVo.setMiaoshaUser(miaoshaUser);
        goodsDetailVo.setMiaoshaStatus(miaoshaStatus);
        goodsDetailVo.setRemainSeconds(remainSeconds);
        return Result.success(goodsDetailVo);
    }

}
