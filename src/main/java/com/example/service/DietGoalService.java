package com.example.service;

import com.example.entity.DietGoal;
import com.example.mapper.DietGoalMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DietGoalService {

    @Resource
    private DietGoalMapper dietGoalMapper;

    public void add(DietGoal dietGoal) {
        dietGoalMapper.insert(dietGoal);
    }

    public void deleteById(Integer id) {
        dietGoalMapper.deleteById(id);
    }

    public void updateById(DietGoal dietGoal) {
        dietGoalMapper.updateById(dietGoal);
    }

    public DietGoal selectById(Integer id) {
        return dietGoalMapper.selectById(id);
    }

    public List<DietGoal> selectAll(DietGoal dietGoal) {
        return dietGoalMapper.selectAll(dietGoal);
    }

    public PageInfo<DietGoal> selectPage(DietGoal dietGoal, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<DietGoal> list = dietGoalMapper.selectAll(dietGoal);
        return PageInfo.of(list);
    }
}
