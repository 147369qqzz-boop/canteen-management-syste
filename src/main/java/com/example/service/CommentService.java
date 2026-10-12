package com.example.service;

import cn.hutool.core.date.DateUtil;
import com.example.entity.Business;
import com.example.entity.Comment;
import com.example.entity.Goods;
import com.example.mapper.CommentMapper;
import com.example.mapper.GoodsMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentService {

    @Resource
    private CommentMapper commentMapper;
    @Resource
    private BusinessService businessService;
    @Resource
    private GoodsMapper goodsMapper;

    public void add(Comment comment) {
        comment.setTime(DateUtil.now());
        commentMapper.insert(comment);
    }

    public void deleteById(Integer id) {
        commentMapper.deleteById(id);
    }

    public void updateById(Comment comment) {
        commentMapper.updateById(comment);
    }

    public Comment selectById(Integer id) {
        return commentMapper.selectById(id);
    }

    public List<Comment> selectAll(Comment comment) {
        return commentMapper.selectAll(comment);
    }

    public PageInfo<Comment> selectPage(Comment comment, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Comment> list = commentMapper.selectAll(comment);
        return PageInfo.of(list);
    }

    // 商家查询评价（Controller直接调用）
    public List<Comment> getByBusinessId(Integer userId) {
        Business business = businessService.selectByUserId(userId);
        if (business == null) {
            return new ArrayList<>();
        }
        List<Goods> goodsList = goodsMapper.selectByBusinessId(business.getId());
        if (goodsList.isEmpty()) {
            return new ArrayList<>();
        }
        List<Integer> goodsIds = goodsList.stream().map(Goods::getId).collect(Collectors.toList());
        return commentMapper.selectByGoodsIds(goodsIds);
    }
}