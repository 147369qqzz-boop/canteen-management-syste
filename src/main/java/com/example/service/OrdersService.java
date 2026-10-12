package com.example.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import com.example.entity.*;
import com.example.exception.CustomException;
import com.example.mapper.*;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Service
public class OrdersService {

    @Resource
    private OrdersMapper ordersMapper;
    @Resource
    GoodsMapper goodsMapper;
    @Resource
    UserMapper userMapper;
    @Resource
    OrderDetailMapper orderDetailMapper;
    @Resource
    CartMapper cartMapper;
    @Resource
    private BusinessService businessService;

    @Transactional
    public void add(Orders orders) {
        orders.setStatus("待接单");
        orders.setTime(DateUtil.now());
        String orderNo = DateUtil.format(new Date(), "yyyyMMdd") + System.currentTimeMillis() + RandomUtil.randomNumbers(4);
        orders.setOrderNo(orderNo);
        List<Cart> cartList = orders.getCartList();
        // 从商品获取商家ID并设置到订单
        if (cartList != null && !cartList.isEmpty()) {
            Goods firstGoods = goodsMapper.selectById(cartList.get(0).getGoodsId());
            if (firstGoods != null) {
                orders.setBusinessId(firstGoods.getBusinessId());
            }
        }
        ordersMapper.insert(orders);
        Integer orderId = orders.getId();
        User user = userMapper.selectById(orders.getUserId());
        BigDecimal totalPrice = BigDecimal.ZERO;
        for (Cart cart : cartList) {
            Integer goodsId = cart.getGoodsId();
            Goods goods = goodsMapper.selectById(goodsId);
            if (goods.getStore() < cart.getNum()) {
                throw new CustomException(goods.getName() + "菜品库存不足");
            }
            goods.setStore(goods.getStore() - cart.getNum());
            goods.setSaleCount(goods.getSaleCount() + cart.getNum());
            goodsMapper.updateById(goods);
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setNum(cart.getNum());
            orderDetail.setGoodsId(goodsId);
            orderDetail.setGoodsImg(goods.getImg());
            orderDetail.setGoodsName(goods.getName());
            orderDetail.setGoodsPrice(goods.getPrice());
            orderDetail.setOrderId(orderId);
            orderDetailMapper.insert(orderDetail);
            if (cart.getId() != null) {
                cartMapper.deleteById(cart.getId());
            }
            totalPrice = totalPrice.add(goods.getPrice().multiply(BigDecimal.valueOf(cart.getNum())));
        }
        if (user.getAccount().compareTo(totalPrice) < 0) {
            throw new CustomException("对不起，您的账户余额不足，请充值！");
        }
        user.setAccount(user.getAccount().subtract(totalPrice));
        userMapper.updateById(user);
        orders.setTotal(totalPrice);
        ordersMapper.updateById(orders);
    }

    @Transactional
    public void deleteById(Integer id) {
        ordersMapper.deleteById(id);
        orderDetailMapper.deleteByOrderId(id);
    }

    @Transactional
    public void updateById(Orders orders) {
        if ("已取消".equals(orders.getStatus())) {
            Integer userId = orders.getUserId();
            User user = userMapper.selectById(userId);
            user.setAccount(user.getAccount().add(orders.getTotal()));
            userMapper.updateById(user);
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(orders.getId());
            List<OrderDetail> orderDetailList = orderDetailMapper.selectAll(orderDetail);
            for (OrderDetail detail : orderDetailList) {
                Integer goodsId = detail.getGoodsId();
                Goods goods = goodsMapper.selectById(goodsId);
                if (goods != null) {
                    goods.setStore(goods.getStore() + detail.getNum());
                    goods.setSaleCount(goods.getSaleCount() - detail.getNum());
                    goodsMapper.updateById(goods);
                }
            }
        }
        ordersMapper.updateById(orders);
    }

    public Orders selectById(Integer id) {
        return ordersMapper.selectById(id);
    }

    public List<Orders> selectAll(Orders orders) {
        return ordersMapper.selectAll(orders);
    }

    public PageInfo<Orders> selectPage(Orders orders, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Orders> list = ordersMapper.selectAll(orders);
        for (Orders o : list) {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(o.getId());
            List<OrderDetail> orderDetailList = orderDetailMapper.selectAll(orderDetail);
            o.setOrderDetailList(orderDetailList);
        }
        return PageInfo.of(list);
    }

    // 商家查询订单（Controller直接调用）
    public List<Orders> getByBusinessId(Integer userId, String orderNo) {
        Business business = businessService.selectByUserId(userId);
        if (business == null) {
            return new java.util.ArrayList<>();
        }
        return ordersMapper.selectByBusinessId(business.getId(), orderNo);
    }

}