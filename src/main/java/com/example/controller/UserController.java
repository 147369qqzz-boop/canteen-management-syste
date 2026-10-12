package com.example.controller;

import com.example.common.Result;
import com.example.entity.User;
import com.example.service.UserService;
import com.github.pagehelper.PageInfo;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    // 直接注入你现有的Service
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // 查询用户信息
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        User user = userService.selectById(id);
        return Result.success(user);
    }

    // 分页查询接口（匹配前端 /user/selectPage）
    @GetMapping("/selectPage")
    public PageInfo<User> selectPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "5") Integer pageSize,
            @RequestParam(required = false) String name) {
        // 直接调用你Service里的方法
        return userService.selectPage(pageNum, pageSize, name);
    }

    // 新增用户
    @PostMapping("/add")
    public void add(@RequestBody User user) {
        userService.add(user);
    }

    // 修改用户
    @PutMapping("/update")
    public void update(@RequestBody User user) {
        userService.update(user);
    }

    // 删除用户
    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable Integer id) {
        userService.deleteById(id);
    }
}