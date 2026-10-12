package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface OrdersMapper extends BaseMapper<Orders> {

    int insert(Orders orders);
    int deleteById(Integer id);
    int updateById(Orders orders);
    Orders selectById(Integer id);
    List<Orders> selectAll(Orders orders);
    List<Orders> selectByBusinessId(@Param("businessId") Integer businessId, @Param("orderNo") String orderNo);

    List<Orders> getByBusinessId(@Param("businessId") Integer businessId);
}