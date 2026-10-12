package com.example.mapper;

import com.example.entity.Business;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface BusinessMapper {

    // 新增：和你项目里其他Mapper一致的selectAll方法
    List<Business> selectAll(Business business);



    // 其他原有方法（insert、deleteById、updateById、selectById）
    void insert(Business business);
    void deleteById(Integer id);
    void updateById(Business business);
    Business selectById(Integer id);
}