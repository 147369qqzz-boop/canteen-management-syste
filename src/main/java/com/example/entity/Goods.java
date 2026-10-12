package com.example.entity;

import java.math.BigDecimal;

public class Goods {

    /**主键ID */
    private Integer id;
    /**名称 */
    private String name;
    /**图片 */
    private String img;
    /**价格 */
    private BigDecimal price;
    /**热量(千卡) */
    private BigDecimal calories;
    /**蛋白质(g) */
    private BigDecimal protein;
    /**碳水化合物(g) */
    private BigDecimal carbs;
    /**脂肪(g) */
    private BigDecimal fat;
    /**简介 */
    private String description;
    /**详情 */
    private String content;
    /**库存 */
    private Integer store;
    /**分类ID */
    private Integer categoryId;
    /**菜系名称 */
    private String cuisineName;
    /**上架状态 */
    private String status;
    /**浏览量 */
    private Integer views;
    /**销量 */
    private Integer saleCount;
    /**创建时间 */
    private String time;

    private String recommend;

    private Integer businessId;

    // 必须生成getter/setter（如果用了@Data注解则自动生成，无需手动写）
    public Integer getBusinessId() {
            return businessId;
        }

    public void setBusinessId(Integer businessId) {
            this.businessId = businessId;
        }


    public String getRecommend() {
        return recommend;
    }

    public void setRecommend(String recommend) {
        this.recommend = recommend;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getStore() {
        return store;
    }

    public void setStore(Integer store) {
        this.store = store;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getViews() {
        return views;
    }

    public void setViews(Integer views) {
        this.views = views;
    }

    public Integer getSaleCount() {
        return saleCount;
    }

    public void setSaleCount(Integer saleCount) {
        this.saleCount = saleCount;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getCuisineName() {
        return cuisineName;
    }

    public void setCuisineName(String cuisineName) {
        this.cuisineName = cuisineName;
    }

    public BigDecimal getCalories() {
        return calories;
    }

    public void setCalories(BigDecimal calories) {
        this.calories = calories;
    }

    public BigDecimal getProtein() {
        return protein;
    }

    public void setProtein(BigDecimal protein) {
        this.protein = protein;
    }

    public BigDecimal getCarbs() {
        return carbs;
    }

    public void setCarbs(BigDecimal carbs) {
        this.carbs = carbs;
    }

    public BigDecimal getFat() {
        return fat;
    }

    public void setFat(BigDecimal fat) {
        this.fat = fat;
    }
}
