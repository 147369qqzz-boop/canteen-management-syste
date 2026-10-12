package com.example.controller;

import com.example.common.Result;
import com.example.entity.Orders;
import com.example.service.OrdersService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrdersController {

    // 注意：变量名要和注入的一致（ordersService）
    @Resource
    private OrdersService ordersService;

    /**
     * 管理员查询所有订单（支持分页和订单号模糊搜索）
     */
//    @GetMapping("/admin/list")
//    public Result listOrders(
//            @RequestParam(defaultValue = "1") Integer pageNum,
//            @RequestParam(defaultValue = "5") Integer pageSize,
//            @RequestParam(required = false) String orderNo) {
//
//        // 1. 构建MyBatis-Plus分页对象
//        Page<Orders> page = new Page<>(pageNum, pageSize);
//
//        // 2. 构建查询条件
//        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();
//        if (StringUtils.isNotBlank(orderNo)) {
//            wrapper.like(Orders::getOrderNo, orderNo);
//        }
//        wrapper.orderByDesc(Orders::getTime); // 按下单时间倒序（你的实体类字段是time）
//
//        // 3. 调用Service的page方法（现在可以正常识别了）
//        Page<Orders> orderPage = ordersService.page(page, wrapper);
//
//        // 4. 返回结果
//        return Result.success(orderPage);
//    }
    @PutMapping("/business/orders/status")
    public Result updateOrderStatus(@RequestBody Orders orders) {
        ordersService.updateById(orders);
        return Result.success();
    }

    @PostMapping("/add")
    public Result add(@RequestBody Orders orders) {
        ordersService.add(orders);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id) {
        ordersService.deleteById(id);
        return Result.success();
    }

    @PutMapping("/update")
    public Result update(@RequestBody Orders orders) {
        ordersService.updateById(orders);
        return Result.success();
    }

    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        Orders orders = ordersService.selectById(id);
        return Result.success(orders);
    }

    @GetMapping("/selectAll")
    public Result selectAll(Orders orders) {
        List<Orders> list = ordersService.selectAll(orders);
        return Result.success(list);
    }

    @GetMapping("/selectPage")
    public Result selectPage(Orders orders,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        com.github.pagehelper.PageInfo<Orders> page = ordersService.selectPage(orders, pageNum, pageSize);
        return Result.success(page);
    }

    /**
     * 商家查询自己的所有订单，支持按订单号模糊搜索
     */
    @GetMapping("/business/orders")
    public Result getBusinessOrders(@RequestParam(required = false) Integer userId,
                                    @RequestParam(required = false) String orderNo) {
        List<Orders> list = ordersService.getByBusinessId(userId, orderNo);
        return Result.success(list);
    }

    // 更新订单状态（通用）
    @PutMapping("/updateStatus")
    public Result updateStatus(@RequestBody Orders orders) {
        ordersService.updateById(orders);
        return Result.success();
    }
}