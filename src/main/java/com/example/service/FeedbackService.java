package com.example.service;

import cn.hutool.core.date.DateUtil;
import com.example.entity.Feedback;
import com.example.mapper.FeedbackMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FeedbackService {

    @Resource
    private FeedbackMapper feedbackMapper;

    public void add(Feedback feedback) {
        feedback.setStatus(0);
        feedback.setTime(DateUtil.now());
        feedbackMapper.insert(feedback);
    }

    public void deleteById(Integer id) {
        feedbackMapper.deleteById(id);
    }

    public void updateById(Feedback feedback) {
        feedbackMapper.updateById(feedback);
    }

    public Feedback selectById(Integer id) {
        return feedbackMapper.selectById(id);
    }

    public List<Feedback> selectAll(Feedback feedback) {
        return feedbackMapper.selectAll(feedback);
    }

    public PageInfo<Feedback> selectPage(Feedback feedback, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Feedback> list = feedbackMapper.selectAll(feedback);
        return PageInfo.of(list);
    }

    public void reply(Integer id, String reply) {
        Feedback feedback = feedbackMapper.selectById(id);
        if (feedback != null) {
            feedback.setReply(reply);
            feedback.setStatus(1);
            feedbackMapper.updateById(feedback);
        }
    }
}
