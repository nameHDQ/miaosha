package com.hdq.miaosha.service;


import com.hdq.miaosha.dao.GoodsDao;
import com.hdq.miaosha.domain.Goods;
import com.hdq.miaosha.domain.MiaoshaGoods;
import com.hdq.miaosha.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author hdq
 */
@Service
public class GoodsService {

    @Autowired
    private GoodsDao goodsDao;


    /**
     *
     * @return
     */
    public List<GoodsVo> listGoodsVo(){
        return goodsDao.listGoodsVo();
    }

    public GoodsVo getGoodsVoByGoodsId(long goodsId) {
        return goodsDao.getGoodsVoByGoodsId(goodsId);
    }


    public void reduceStock(GoodsVo goodsVo) {
        MiaoshaGoods goods = new MiaoshaGoods();
        goods.setGoodsId(goodsVo.getId());
        goodsDao.reduceStock(goods);
    }
}
