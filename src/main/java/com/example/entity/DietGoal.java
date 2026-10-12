package com.example.entity;

import java.math.BigDecimal;
import java.util.Date;

public class DietGoal {

    private Integer id;
    private Integer userId;
    private String goalType;
    private BigDecimal dailyCalories;
    private BigDecimal dailyProtein;
    private BigDecimal dailyCarbs;
    private BigDecimal dailyFat;
    private Date createTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getGoalType() {
        return goalType;
    }

    public void setGoalType(String goalType) {
        this.goalType = goalType;
    }

    public BigDecimal getDailyCalories() {
        return dailyCalories;
    }

    public void setDailyCalories(BigDecimal dailyCalories) {
        this.dailyCalories = dailyCalories;
    }

    public BigDecimal getDailyProtein() {
        return dailyProtein;
    }

    public void setDailyProtein(BigDecimal dailyProtein) {
        this.dailyProtein = dailyProtein;
    }

    public BigDecimal getDailyCarbs() {
        return dailyCarbs;
    }

    public void setDailyCarbs(BigDecimal dailyCarbs) {
        this.dailyCarbs = dailyCarbs;
    }

    public BigDecimal getDailyFat() {
        return dailyFat;
    }

    public void setDailyFat(BigDecimal dailyFat) {
        this.dailyFat = dailyFat;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
