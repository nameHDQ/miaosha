package com.hdq.miaosha.service;


import com.hdq.miaosha.dao.GoodsDao;
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

}
