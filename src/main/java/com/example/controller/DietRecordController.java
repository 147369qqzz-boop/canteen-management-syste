package com.example.controller;

import com.example.entity.DietRecord;
import com.example.service.DietRecordService;
import com.example.utils.Result;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/dietRecord")
public class DietRecordController {

    @Resource
    private DietRecordService dietRecordService;

    @PostMapping("/add")
    public Result add(@RequestBody DietRecord dietRecord) {
        dietRecordService.add(dietRecord);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    public Result deleteById(@PathVariable Integer id) {
        dietRecordService.deleteById(id);
        return Result.success();
    }

    @PutMapping("/update")
    public Result updateById(@RequestBody DietRecord dietRecord) {
        dietRecordService.updateById(dietRecord);
        return Result.success();
    }

    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        DietRecord record = dietRecordService.selectById(id);
        return Result.success(record);
    }

    @GetMapping("/selectAll")
    public Result selectAll(DietRecord dietRecord) {
        List<DietRecord> list = dietRecordService.selectAll(dietRecord);
        return Result.success(list);
    }

    @GetMapping("/selectPage")
    public Result selectPage(DietRecord dietRecord,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        PageInfo<DietRecord> page = dietRecordService.selectPage(dietRecord, pageNum, pageSize);
        return Result.success(page);
    }
}
