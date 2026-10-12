package com.example.service;

import com.example.entity.Business;
import com.example.mapper.BusinessMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BusinessService {

    @Resource
    private BusinessMapper businessMapper;


    // 根据userId查询商家
    public Business selectByUserId(Integer userId) {
        List<Business> list = businessMapper.selectAll(null);
        return list.stream().filter(b -> b.getUserId().equals(userId)).findFirst().orElse(null);
    }
}