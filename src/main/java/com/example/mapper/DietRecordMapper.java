package com.example.mapper;

import com.example.entity.DietRecord;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface DietRecordMapper {

    List<DietRecord> selectAll(DietRecord dietRecord);

    void insert(DietRecord dietRecord);

    void deleteById(Integer id);

    void updateById(DietRecord dietRecord);

    DietRecord selectById(Integer id);
}
