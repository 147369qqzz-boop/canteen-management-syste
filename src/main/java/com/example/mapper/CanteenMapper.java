package com.example.mapper;

import com.example.entity.Canteen;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface CanteenMapper {

    List<Canteen> selectAll(Canteen canteen);

    void insert(Canteen canteen);

    void deleteById(Integer id);

    void updateById(Canteen canteen);

    Canteen selectById(Integer id);
}
