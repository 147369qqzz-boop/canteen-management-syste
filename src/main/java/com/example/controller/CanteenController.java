package com.example.controller;

import com.example.entity.Canteen;
import com.example.service.CanteenService;
import com.example.utils.Result;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/canteen")
public class CanteenController {

    @Resource
    private CanteenService canteenService;

    @PostMapping("/add")
    public Result add(@RequestBody Canteen canteen) {
        canteenService.add(canteen);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    public Result deleteById(@PathVariable Integer id) {
        canteenService.deleteById(id);
        return Result.success();
    }

    @PutMapping("/update")
    public Result updateById(@RequestBody Canteen canteen) {
        canteenService.updateById(canteen);
        return Result.success();
    }

    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        Canteen canteen = canteenService.selectById(id);
        return Result.success(canteen);
    }

    @GetMapping("/selectAll")
    public Result selectAll(Canteen canteen) {
        List<Canteen> list = canteenService.selectAll(canteen);
        return Result.success(list);
    }

    @GetMapping("/selectPage")
    public Result selectPage(Canteen canteen,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        PageInfo<Canteen> page = canteenService.selectPage(canteen, pageNum, pageSize);
        return Result.success(page);
    }
}
