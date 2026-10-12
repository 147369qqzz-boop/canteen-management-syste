package com.example.entity;

public class Notice {

    /**主键ID */
    private Integer id;
    /**公告标题 */
    private String title;
    /**公告内容 */
    private String content;
    /**状态：1=发布 0=草稿 */
    private Integer status;
    /**创建时间 */
    private String time;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
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
