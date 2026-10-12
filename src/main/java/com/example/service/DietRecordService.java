package com.example.service;

import com.example.entity.DietRecord;
import com.example.mapper.DietRecordMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DietRecordService {

    @Resource
    private DietRecordMapper dietRecordMapper;

    public void add(DietRecord dietRecord) {
        dietRecordMapper.insert(dietRecord);
    }

    public void deleteById(Integer id) {
        dietRecordMapper.deleteById(id);
    }

    public void updateById(DietRecord dietRecord) {
        dietRecordMapper.updateById(dietRecord);
    }

    public DietRecord selectById(Integer id) {
        return dietRecordMapper.selectById(id);
    }

    public List<DietRecord> selectAll(DietRecord dietRecord) {
        return dietRecordMapper.selectAll(dietRecord);
    }

    public PageInfo<DietRecord> selectPage(DietRecord dietRecord, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<DietRecord> list = dietRecordMapper.selectAll(dietRecord);
        return PageInfo.of(list);
    }
}
