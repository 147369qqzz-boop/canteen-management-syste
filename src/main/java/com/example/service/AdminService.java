package com.example.service;

import cn.hutool.core.util.ObjectUtil;
import com.example.entity.Account;
import com.example.entity.Admin;
import com.example.exception.CustomException;
import com.example.mapper.AdminMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 管理员业务处理
 **/
@Service
public class AdminService {

    @Resource
    private AdminMapper adminMapper;

    /**
     * 新增
     */
    public void add(Admin admin) {
        Admin dbAdmin = adminMapper.selectByUsername(admin.getUsername());
        if (ObjectUtil.isNotNull(dbAdmin)) {
            throw new CustomException("用户名已存在");
        }
        if (ObjectUtil.isEmpty(admin.getPassword())) {
            admin.setPassword("admin");
        }
        admin.setPassword(passwordEncoder.encode(admin.getPassword()));
        if (ObjectUtil.isEmpty(admin.getName())) {
            admin.setName(admin.getUsername());
        }
        admin.setRole("管理员");
        adminMapper.insert(admin);
    }

    /**
     * 删除
     */
    public void deleteById(Integer id) {
        adminMapper.deleteById(id);
    }

    /**
     * 修改
     */
    public void updateById(Admin admin) {
        adminMapper.updateById(admin);
    }

    /**
     * 根据ID查询
     */
    public Admin selectById(Integer id) {
        Admin admin = adminMapper.selectById(id);
        if (admin != null) {
            admin.setPassword(null);
        }
        return admin;
    }

    /**
     * 查询所有
     */
    public List<Admin> selectAll(Admin admin) {
        List<Admin> list = adminMapper.selectAll(admin);
        list.forEach(a -> a.setPassword(null));
        return list;
    }

    /**
     * 分页查询
     */
    public PageInfo<Admin> selectPage(Admin admin, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Admin> list = adminMapper.selectAll(admin);
        list.forEach(a -> a.setPassword(null));
        return PageInfo.of(list);
    }

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 登录
     */
    public Account login(Account account) {
        Admin dbAdmin = adminMapper.selectByUsername(account.getUsername());
        if (ObjectUtil.isNull(dbAdmin)) {
            throw new CustomException("用户不存在");
        }
        String inputPassword = account.getPassword();
        String dbPassword = dbAdmin.getPassword();

        // BCrypt密文匹配
        if (passwordEncoder.matches(inputPassword, dbPassword)) {
            dbAdmin.setPassword(null);
            return dbAdmin;
        }
        // 兼容旧明文密码
        if (inputPassword.equals(dbPassword)) {
            dbAdmin.setPassword(passwordEncoder.encode(inputPassword));
            adminMapper.updateById(dbAdmin);
            dbAdmin.setPassword(null);
            return dbAdmin;
        }
        throw new CustomException("账号或密码错误");
    }

    /**
     * 修改密码
     */
    public void updatePassword(Account account) {
        Admin dbAdmin = adminMapper.selectByUsername(account.getUsername());
        if (ObjectUtil.isNull(dbAdmin)) {
            throw new CustomException("用户不存在");
        }
        String inputPassword = account.getPassword();
        String dbPassword = dbAdmin.getPassword();

        boolean verified = passwordEncoder.matches(inputPassword, dbPassword)
                || inputPassword.equals(dbPassword);

        if (!verified) {
            throw new CustomException("原密码错误");
        }
        dbAdmin.setPassword(passwordEncoder.encode(account.getNewPassword()));
        adminMapper.updateById(dbAdmin);
    }

}