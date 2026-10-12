package com.example.entity;

import java.util.Date;

public class Business {

    /**商家ID */
    private Integer id;
    /**关联用户表ID */
    private Integer userId;
    /**商家名称/食堂窗口名 */
    private String businessName;
    /**窗口位置 */
    private String address;
    /**联系电话 */
    private String phone;
    /**商家头像 */
    private String avatar;
    /**状态：1正常 0禁用 */
    private Boolean status;
    /**所属食堂ID */
    private Integer canteenId;
    /**所在楼层 */
    private String floor;
    /**营业状态：营业中/休息中 */
    private String operateStatus;
    /**预计出餐时间(分钟) */
    private Integer estimatedCookingTime;
    /**当前排队人数 */
    private Integer queueCount;
    /**创建时间 */
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

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public Integer getCanteenId() {
        return canteenId;
    }

    public void setCanteenId(Integer canteenId) {
        this.canteenId = canteenId;
    }

    public String getFloor() {
        return floor;
    }

    public void setFloor(String floor) {
        this.floor = floor;
    }

    public String getOperateStatus() {
        return operateStatus;
    }

    public void setOperateStatus(String operateStatus) {
        this.operateStatus = operateStatus;
    }

    public Integer getEstimatedCookingTime() {
        return estimatedCookingTime;
    }

    public void setEstimatedCookingTime(Integer estimatedCookingTime) {
        this.estimatedCookingTime = estimatedCookingTime;
    }

    public Integer getQueueCount() {
        return queueCount;
    }

    public void setQueueCount(Integer queueCount) {
        this.queueCount = queueCount;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
