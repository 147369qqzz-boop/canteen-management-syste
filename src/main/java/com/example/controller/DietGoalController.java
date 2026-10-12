package com.example.controller;

import com.example.entity.DietGoal;
import com.example.service.DietGoalService;
import com.example.utils.Result;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/dietGoal")
public class DietGoalController {

    @Resource
    private DietGoalService dietGoalService;

    @PostMapping("/add")
    public Result add(@RequestBody DietGoal dietGoal) {
        dietGoalService.add(dietGoal);
        return Result.success();
    }

    @PutMapping("/update")
    public Result updateById(@RequestBody DietGoal dietGoal) {
        dietGoalService.updateById(dietGoal);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    public Result deleteById(@PathVariable Integer id) {
        dietGoalService.deleteById(id);
        return Result.success();
    }

    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        DietGoal goal = dietGoalService.selectById(id);
        return Result.success(goal);
    }

    @GetMapping("/selectAll")
    public Result selectAll(DietGoal dietGoal) {
        List<DietGoal> list = dietGoalService.selectAll(dietGoal);
        return Result.success(list);
    }

    @GetMapping("/selectPage")
    public Result selectPage(DietGoal dietGoal,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        PageInfo<DietGoal> page = dietGoalService.selectPage(dietGoal, pageNum, pageSize);
        return Result.success(page);
    }
}
