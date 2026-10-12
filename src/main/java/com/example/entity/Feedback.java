package com.example.entity;

public class Feedback {

    /**主键ID */
    private Integer id;
    /**反馈用户ID */
    private Integer userId;
    /**反馈用户名 */
    private String userName;
    /**反馈内容 */
    private String content;
    /**管理员回复 */
    private String reply;
    /**状态：0=待回复 1=已回复 */
    private Integer status;
    /**创建时间 */
    private String time;

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

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
