package com.example.controller;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.example.common.Result;
import com.example.dto.LoginResponse;
import com.example.entity.*;
import com.example.mapper.OrderDetailMapper;
import com.example.service.*;
import com.example.utils.JwtUtils;
import jakarta.annotation.Resource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

@RestController
public class WebController {

    @Resource
    private AdminService adminService;
    @Resource
    private UserService userService;
    @Resource
    OrdersService ordersService;
    @Resource
    GoodsService goodsService;
    @Resource
    CategoryService categoryService;
    @Resource
    OrderDetailMapper orderDetailMapper;

    /**
     * 默认请求接口
     */
    @GetMapping("/")
    public Result hello() {
        return Result.success();
    }

    /**
     * 登录
     */
    @PostMapping("/login")
    public Result login(@RequestBody Account account) {
        Account ac = null;
        if ("管理员".equals(account.getRole())) {
            ac = adminService.login(account);
        }
        if ("普通用户".equals(account.getRole())) {
            ac = userService.login(account);
        }
        if ("商家".equals(account.getRole())) {
            ac = userService.login(account);
        }
        if (ac == null) {
            return Result.error("登录失败，用户不存在");
        }
        String token = JwtUtils.generateToken(ac.getId(), ac.getUsername(), ac.getRole());
        LoginResponse resp = LoginResponse.of(
                token, ac.getId(), ac.getUsername(), ac.getName(), ac.getRole(), ac.getAvatar());
        return Result.success(resp);
    }

    /**
     * 注册
     */
    @PostMapping("/register")
    public Result register(@RequestBody User user) {
        if (!user.getPassword().equals(user.getNewPassword())) {
            return Result.error("两次输入密码不一致");
        }
        userService.add(user);
        return Result.success();
    }



    /**
     * 修改密码
     */
    @PutMapping("/updatePassword")
    public Result updatePassword(@RequestBody Account account) {
        if ("管理员".equals(account.getRole())) {
            adminService.updatePassword(account);
        }
        if ("普通用户".equals(account.getRole())) {
            userService.updatePassword(account);
        }
        return Result.success();
    }

    // ==================== 原有统计接口 ====================

    @GetMapping("/count")
    public Result count() {
        List<Orders> ordersList = ordersService.selectAll(null).stream()
                .filter(orders -> !orders.getStatus().equals("已取消")).toList();
        BigDecimal total = ordersList.stream().map(Orders::getTotal)
                .reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        String todayDate = DateUtil.today();
        BigDecimal today = ordersList.stream()
                .filter(orders -> orders.getTime().contains(todayDate))
                .map(Orders::getTotal).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        Integer goods = goodsService.selectAll(null).size();
        Integer user = userService.selectByAll(null).size();
        Map<String, Object> map = new HashMap<>();
        map.put("total", total);
        map.put("today", today);
        map.put("goods", goods);
        map.put("user", user);
        return Result.success(map);
    }

    @GetMapping("/selectLine")
    public Result selectLine() {
        Date date = new Date();
        DateTime start = DateUtil.offsetDay(date, -6);
        List<DateTime> dateTimes = DateUtil.rangeToList(start, date, DateField.DAY_OF_YEAR);
        List<String> dateStrList = dateTimes.stream()
                .map(dateTime -> DateUtil.format(dateTime, "MM-dd")).sorted().toList();
        List<Orders> ordersList = ordersService.selectAll(null).stream()
                .filter(orders -> !orders.getStatus().equals("已取消")).toList();
        int year = DateUtil.year(date);
        ArrayList<BigDecimal> countList = new ArrayList<>();
        for (String day : dateStrList) {
            BigDecimal total = ordersList.stream()
                    .filter(o -> o.getTime().contains(String.valueOf(year)) && o.getTime().contains(day))
                    .map(Orders::getTotal).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
            countList.add(total);
        }
        Map<String, Object> map = new HashMap<>();
        map.put("date", dateStrList);
        map.put("count", countList);
        return Result.success(map);
    }

    @GetMapping("/selectPie")
    public Result selectPie() {
        List<Map<String, Object>> list = new ArrayList<>();
        List<Category> categoryList = categoryService.selectAll(null);
        Map<String, Object> map;
        for (Category category : categoryList) {
            map = new HashMap<>();
            map.put("name", category.getName());
            BigDecimal total = BigDecimal.ZERO;
            List<OrderDetail> orderDetailList = orderDetailMapper.selectAll(null);
            for (OrderDetail orderDetail : orderDetailList) {
                Integer orderId = orderDetail.getOrderId();
                Orders orders = ordersService.selectById(orderId);
                if (!orders.getStatus().equals("已取消")) {
                    Integer goodsId = orderDetail.getGoodsId();
                    Goods goods = goodsService.selectById(goodsId);
                    if (goods.getCategoryId().equals(category.getId())) {
                        total = total.add(orders.getTotal());
                    }
                }
            }
            map.put("value", total);
            if (total.compareTo(BigDecimal.ZERO) > 0) {
                list.add(map);
            }
        }
        return Result.success(list);
    }

    // ==================== 新增图表接口 ====================

    /**
     * 菜品销量排行（柱状图数据）
     */
    @GetMapping("/selectGoodsRank")
    public Result selectGoodsRank(@RequestParam(defaultValue = "10") Integer limit) {
        // 1. 获取所有有效订单
        List<Orders> validOrders = ordersService.selectAll(null).stream()
                .filter(o -> !"已取消".equals(o.getStatus()))
                .toList();

        // 2. 统计每个菜品的总销量
        Map<Integer, Integer> goodsCountMap = new HashMap<>();
        List<OrderDetail> allDetails = orderDetailMapper.selectAll(null);

        for (Orders order : validOrders) {
            List<OrderDetail> details = allDetails.stream()
                    .filter(d -> d.getOrderId().equals(order.getId()))
                    .toList();
            for (OrderDetail detail : details) {
                Integer goodsId = detail.getGoodsId();
                // 没有 count 字段，每条明细视为 1 份
                goodsCountMap.merge(goodsId, 1, Integer::sum);
            }
        }

        // 3. 转换为前端格式
        List<Map<String, Object>> list = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : goodsCountMap.entrySet()) {
            Goods goods = goodsService.selectById(entry.getKey());
            if (goods != null) {
                Map<String, Object> map = new HashMap<>();
                map.put("name", goods.getName());
                map.put("value", entry.getValue());
                list.add(map);
            }
        }

        // 4. 排序并截取
        list.sort((a, b) -> ((Integer) b.get("value")).compareTo((Integer) a.get("value")));
        if (list.size() > limit) {
            list = list.subList(0, limit);
        }
        return Result.success(list);
    }

    /**
     * 各时段订单量分布（按小时统计）
     */
    @GetMapping("/selectHourDistribution")
    public Result selectHourDistribution() {
        List<Orders> validOrders = ordersService.selectAll(null).stream()
                .filter(o -> !"已取消".equals(o.getStatus()))
                .toList();

        int[] hourCounts = new int[24];

        for (Orders order : validOrders) {
            String timeStr = order.getTime();
            if (timeStr != null && timeStr.length() >= 13) {
                try {
                    String hourStr = timeStr.substring(11, 13);
                    int hour = Integer.parseInt(hourStr);
                    if (hour >= 0 && hour < 24) {
                        hourCounts[hour]++;
                    }
                } catch (Exception ignored) {}
            }
        }

        List<String> hours = new ArrayList<>();
        List<Integer> counts = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            hours.add(String.format("%02d:00", i));
            counts.add(hourCounts[i]);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("hours", hours);
        result.put("counts", counts);
        return Result.success(result);
    }
}