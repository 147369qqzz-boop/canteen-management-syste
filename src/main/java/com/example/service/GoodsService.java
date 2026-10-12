package com.example.service;

import cn.hutool.core.date.DateUtil;
import com.example.entity.Business;
import com.example.entity.Goods;
import com.example.mapper.CartMapper;
import com.example.mapper.GoodsMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GoodsService {

    @Resource
    private GoodsMapper goodsMapper;
    @Resource
    CartMapper cartMapper;
    @Resource
    private BusinessService businessService;

    public void add(Goods goods) {
        goods.setViews(0);
        goods.setSaleCount(0);
        String now = DateUtil.now();
        goods.setTime(now);
        goodsMapper.insert(goods);
    }

    @Transactional
    public void deleteById(Integer id) {
        goodsMapper.deleteById(id);
        cartMapper.deleteByGoodsId(id);
    }

    public void updateById(Goods goods) {
        goodsMapper.updateById(goods);
        if ("下架".equals(goods.getStatus())) {
            cartMapper.deleteByGoodsId(goods.getId());
        }
    }

    public Goods selectById(Integer id) {
        return goodsMapper.selectById(id);
    }

    public List<Goods> selectAll(Goods goods) {
        return goodsMapper.selectAll(goods);
    }

    public PageInfo<Goods> selectPage(Goods goods, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Goods> list = goodsMapper.selectAll(goods);
        return PageInfo.of(list);
    }

    // 商家查询菜品（Controller直接调用）
    public List<Goods> getByBusinessId(Integer userId) {
        Business business = businessService.selectByUserId(userId);
        if (business == null) {
            return new java.util.ArrayList<>();
        }
        return goodsMapper.selectByBusinessId(business.getId());
    }

}