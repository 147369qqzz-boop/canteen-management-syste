package com.example.mapper;

import com.example.entity.User;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface UserMapper {
    List<User> selectAll(String name);
    User selectByUsername(String username);
    User selectByUsernameAndRole(String username, String role);
    // 新增：根据ID查询用户
    User selectById(Integer id);
    int deleteById(Integer id);
    int insert(User user);
    int updateById(User user);
}