package com.example.service;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.example.entity.Account;
import com.example.entity.Admin;
import com.example.entity.User;
import com.example.exception.CustomException;
import com.example.mapper.UserMapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

@Service
public class UserService {

    @Resource
    private UserMapper userMapper;

    public List<User> selectByAll(String name) {
        List<User> users = userMapper.selectAll(name);
        users.forEach(u -> u.setPassword(null));
        return users;
    }

    /**
     * 登录查询（根据用户名+角色）
     */
    public User login(String username, String role) {
        User user = userMapper.selectByUsernameAndRole(username, role);
        if (user != null) {
            user.setPassword(null);
        }
        return user;
    }

    /**
     * 分页查询的方法
     * @param pageNum 当前页码
     * @param pageSize 每页展示的个数
     * @return 分页数据
     */

    public PageInfo<User> selectPage(Integer pageNum, Integer pageSize,String name){
        PageHelper.startPage(pageNum,pageSize);
        List<User> list =userMapper.selectAll(name);
        list.forEach(u -> u.setPassword(null));
        return PageInfo.of(list);
    }

    public void deleteById(Integer id) {
        userMapper.deleteById(id);

    }

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public void add(User user) {
        String username = user.getUsername();
        //校验账户是否重复
        User dbuser = userMapper.selectByUsername(username);
        if (dbuser != null){
            throw new CustomException("新增失败！账号重复");
        }
        if (StrUtil.isBlank(user.getPassword())){
            //默认密码
            user.setPassword("123");
        }
        if (StrUtil.isBlank(user.getName())){
            //默认姓名
            user.setName(user.getUsername());
        }
        user.setRole("普通用户");//默认用户的角色
        user.setAccount(BigDecimal.ZERO);//默认账户余额为0
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userMapper.insert(user);
    }

    public void update(User user) {
        //user对象里面必须包含ID，否则无法更新
        userMapper.updateById(user);
    }

    public Account login(Account account) {
        User dbUser = userMapper.selectByUsername(account.getUsername());
        if (ObjectUtil.isNull(dbUser)) {
            throw new CustomException("用户不存在");
        }
        String inputPassword = account.getPassword();
        String dbPassword = dbUser.getPassword();

        // BCrypt密文匹配
        if (passwordEncoder.matches(inputPassword, dbPassword)) {
            dbUser.setPassword(null);
            return dbUser;
        }
        // 兼容旧明文密码
        if (inputPassword.equals(dbPassword)) {
            dbUser.setPassword(passwordEncoder.encode(inputPassword));
            userMapper.updateById(dbUser);
            dbUser.setPassword(null);
            return dbUser;
        }
        throw new CustomException("账号或密码错误");
    }

    public User selectById(Integer id) {
        User user = userMapper.selectById(id);
        if (user != null) {
            user.setPassword(null);
        }
        return user;
    }

    /**
     * 修改密码
     */
    public void updatePassword(Account account) {
        User dbUser = userMapper.selectByUsername(account.getUsername());
        if (ObjectUtil.isNull(dbUser)) {
            throw new CustomException("用户不存在");
        }
        String inputPassword = account.getPassword();
        String dbPassword = dbUser.getPassword();

        boolean verified = passwordEncoder.matches(inputPassword, dbPassword)
                || inputPassword.equals(dbPassword);

        if (!verified) {
            throw new CustomException("原密码错误");
        }
        dbUser.setPassword(passwordEncoder.encode(account.getNewPassword()));
        userMapper.updateById(dbUser);
    }



}
