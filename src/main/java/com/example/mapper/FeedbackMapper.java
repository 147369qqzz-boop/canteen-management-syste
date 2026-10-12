package com.example.mapper;

import com.example.entity.Feedback;
import java.util.List;

public interface FeedbackMapper {

    int insert(Feedback feedback);

    int deleteById(Integer id);

    int updateById(Feedback feedback);

    Feedback selectById(Integer id);

    List<Feedback> selectAll(Feedback feedback);
}
