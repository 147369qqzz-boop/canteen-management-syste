package com.example.service;

import com.example.entity.Canteen;
import com.example.mapper.CanteenMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CanteenService {

    @Resource
    private CanteenMapper canteenMapper;

    public void add(Canteen canteen) {
        canteenMapper.insert(canteen);
    }

    public void deleteById(Integer id) {
        canteenMapper.deleteById(id);
    }

    public void updateById(Canteen canteen) {
        canteenMapper.updateById(canteen);
    }

    public Canteen selectById(Integer id) {
        return canteenMapper.selectById(id);
    }

    public List<Canteen> selectAll(Canteen canteen) {
        return canteenMapper.selectAll(canteen);
    }

    public PageInfo<Canteen> selectPage(Canteen canteen, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Canteen> list = canteenMapper.selectAll(canteen);
        return PageInfo.of(list);
    }
}
