package com.example.mapper;

import com.example.entity.Comment;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface CommentMapper {
    int insert(Comment comment);
    int deleteById(Integer id);
    int updateById(Comment comment);
    Comment selectById(Integer id);
    List<Comment> selectAll(Comment comment);


    List<Comment> selectByGoodsIds(@Param("goodsIds") List<Integer> goodsIds);
}