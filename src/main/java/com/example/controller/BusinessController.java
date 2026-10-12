package com.example.controller;

import com.example.entity.Business;
import com.example.entity.Comment;
import com.example.entity.Goods;
import com.example.mapper.BusinessMapper;
import com.example.mapper.CommentMapper;
import com.example.mapper.GoodsMapper;
import com.example.utils.Result;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/business")
public class BusinessController {

    private final BusinessMapper businessMapper;
    private final GoodsMapper goodsMapper;
    private final CommentMapper commentMapper;

    public BusinessController(BusinessMapper businessMapper, GoodsMapper goodsMapper, CommentMapper commentMapper) {
        this.businessMapper = businessMapper;
        this.goodsMapper = goodsMapper;
        this.commentMapper = commentMapper;
    }

    @GetMapping("/info")
    public Result getInfo(@RequestParam Integer userId) {
        List<Business> list = businessMapper.selectAll(null);
        Business business = list.stream().filter(b -> b.getUserId().equals(userId)).findFirst().orElse(null);
        return Result.success(business);
    }

    @PostMapping("/add")
    public Result add(@RequestBody Business business) {
        businessMapper.insert(business);
        return Result.success();
    }

    @PutMapping("/update")
    public Result update(@RequestBody Business business) {
        businessMapper.updateById(business);
        return Result.success();
    }

    @PutMapping("/toggleStatus")
    public Result toggleStatus(@RequestBody Business business) {
        Business db = businessMapper.selectById(business.getId());
        if (db != null) {
            db.setOperateStatus(business.getOperateStatus());
            businessMapper.updateById(db);
        }
        return Result.success();
    }

    @PutMapping("/setCookingTime")
    public Result setCookingTime(@RequestBody Business business) {
        Business db = businessMapper.selectById(business.getId());
        if (db != null) {
            db.setEstimatedCookingTime(business.getEstimatedCookingTime());
            businessMapper.updateById(db);
        }
        return Result.success();
    }

    @PutMapping("/updateQueue")
    public Result updateQueue(@RequestBody Business business) {
        Business db = businessMapper.selectById(business.getId());
        if (db != null) {
            db.setQueueCount(business.getQueueCount());
            businessMapper.updateById(db);
        }
        return Result.success();
    }

    @GetMapping("/stalls")
    public Result getStalls(@RequestParam(required = false) Integer canteenId,
                            @RequestParam(required = false) String floor) {
        Business query = new Business();
        query.setCanteenId(canteenId);
        query.setFloor(floor);
        List<Business> list = businessMapper.selectAll(query);
        return Result.success(list);
    }

    // 聚合接口：一次返回档口+菜品+评分，避免N+1请求超时
    @GetMapping("/stallsDetail")
    public Result stallsDetail(@RequestParam(required = false) Integer canteenId,
                               @RequestParam(required = false) String floor,
                               @RequestParam(required = false, defaultValue = "all") String filterStatus) {
        Business query = new Business();
        query.setCanteenId(canteenId);
        query.setFloor(floor);
        List<Business> stalls = businessMapper.selectAll(query);

        // 过滤休息中的档口
        if ("营业中".equals(filterStatus)) {
            stalls = stalls.stream()
                .filter(s -> !"休息中".equals(s.getOperateStatus()))
                .collect(Collectors.toList());
        }

        // 收集所有档口ID，批量查菜品
        List<Integer> businessIds = stalls.stream().map(Business::getId).collect(Collectors.toList());
        List<Goods> allGoods = new ArrayList<>();
        if (!businessIds.isEmpty()) {
            // 查所有档口的菜品
            for (Integer bid : businessIds) {
                List<Goods> goods = goodsMapper.selectByBusinessId(bid);
                allGoods.addAll(goods);
            }
        }

        // 批量查所有菜品的评分
        List<Integer> goodsIds = allGoods.stream().map(Goods::getId).collect(Collectors.toList());
        Map<Integer, Double> scoreMap = new HashMap<>();
        if (!goodsIds.isEmpty()) {
            List<Comment> comments = commentMapper.selectByGoodsIds(goodsIds);
            Map<Integer, List<Comment>> grouped = comments.stream()
                .collect(Collectors.groupingBy(Comment::getGoodsId));
            for (Map.Entry<Integer, List<Comment>> e : grouped.entrySet()) {
                double avg = e.getValue().stream()
                    .mapToDouble(c -> c.getScore() != null ? c.getScore() : 0)
                    .average().orElse(0);
                scoreMap.put(e.getKey(), Math.round(avg * 10.0) / 10.0);
            }
        }

        // 组装结果：每个档口带菜品列表+评分
        Map<Integer, List<Goods>> goodsByBusiness = allGoods.stream()
            .collect(Collectors.groupingBy(Goods::getBusinessId));

        List<Map<String, Object>> result = new ArrayList<>();
        for (Business stall : stalls) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", stall.getId());
            item.put("businessName", stall.getBusinessName());
            item.put("address", stall.getAddress());
            item.put("phone", stall.getPhone());
            item.put("avatar", stall.getAvatar());
            item.put("status", stall.getStatus());
            item.put("canteenId", stall.getCanteenId());
            item.put("floor", stall.getFloor());
            item.put("operateStatus", stall.getOperateStatus());
            item.put("estimatedCookingTime", stall.getEstimatedCookingTime());
            item.put("queueCount", stall.getQueueCount());

            List<Map<String, Object>> goodsList = new ArrayList<>();
            List<Goods> stallGoods = goodsByBusiness.getOrDefault(stall.getId(), Collections.emptyList());
            for (Goods g : stallGoods) {
                Map<String, Object> gm = new LinkedHashMap<>();
                gm.put("id", g.getId());
                gm.put("name", g.getName());
                gm.put("img", g.getImg());
                gm.put("price", g.getPrice());
                gm.put("calories", g.getCalories());
                gm.put("protein", g.getProtein());
                gm.put("carbs", g.getCarbs());
                gm.put("fat", g.getFat());
                gm.put("saleCount", g.getSaleCount());
                gm.put("avgScore", scoreMap.getOrDefault(g.getId(), 0.0));
                goodsList.add(gm);
            }
            item.put("goodsList", goodsList);
            result.add(item);
        }

        return Result.success(result);
    }

    @GetMapping("/queueOverview")
    public Result queueOverview(@RequestParam(required = false) Integer canteenId) {
        Business query = new Business();
        query.setCanteenId(canteenId);
        query.setOperateStatus("营业中");
        List<Business> list = businessMapper.selectAll(query);
        return Result.success(list);
    }

    @GetMapping("/selectAll")
    public Result selectAll(Business business) {
        List<Business> list = businessMapper.selectAll(business);
        return Result.success(list);
    }

    @PutMapping("/review")
    public Result review(@RequestBody Business business) {
        Business db = businessMapper.selectById(business.getId());
        if (db != null) {
            db.setStatus(business.getStatus());
            businessMapper.updateById(db);
        }
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    public Result deleteById(@PathVariable Integer id) {
        businessMapper.deleteById(id);
        return Result.success();
    }

    @PutMapping("/assignCanteen")
    public Result assignCanteen(@RequestBody Business business) {
        Business db = businessMapper.selectById(business.getId());
        if (db != null) {
            db.setCanteenId(business.getCanteenId());
            db.setFloor(business.getFloor());
            businessMapper.updateById(db);
        }
        return Result.success();
    }
}
