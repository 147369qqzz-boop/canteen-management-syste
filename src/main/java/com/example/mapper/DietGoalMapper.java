package com.example.mapper;

import com.example.entity.DietGoal;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface DietGoalMapper {

    List<DietGoal> selectAll(DietGoal dietGoal);

    void insert(DietGoal dietGoal);

    void deleteById(Integer id);

    void updateById(DietGoal dietGoal);

    DietGoal selectById(Integer id);
}
