package com.hdq.miaosha.dao;

import com.hdq.miaosha.domain.Goods;
import com.hdq.miaosha.domain.MiaoshaGoods;
import com.hdq.miaosha.vo.GoodsVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * @author hdq
 */
@Mapper
public interface GoodsDao {


    /**
     *
     * @return
     */
    @Select("select g.*,mg.stock_count, mg.start_date, mg.end_date,mg.miaosha_price from miaosha_goods mg left join goods g on mg.goods_id = g.id")
    public List<GoodsVo> listGoodsVo();

    @Select("select g.*,mg.stock_count, mg.start_date, mg.end_date,mg.miaosha_price from miaosha_goods mg left join goods g on mg.goods_id = g.id where g.id = #{goodsId}")
    GoodsVo getGoodsVoByGoodsId(@Param("goodsId") long goodsId);

    /**
     *@Description : 解决超卖的第一步，判断库存大于0才能执行成功
     *@Author : huangdaoquan
     *@Date : 2022/6/28 21:09
     *@Version : 1.0
     **/
    @Update("update miaosha_goods set stock_count = stock_count - 1 where goods_id = #{goodsId} and stock_count > 0")
    int reduceStock( MiaoshaGoods goods);
}
