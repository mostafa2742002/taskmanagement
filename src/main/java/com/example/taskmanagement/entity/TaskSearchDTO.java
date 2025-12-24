package com.example.taskmanagement.entity;


import java.time.LocalDateTime;

public class TaskSearchDTO {
    private String status;
    private String priority;
    private Long userId;
    private String keyword;
    private String tagName;
    private LocalDateTime createdAfter;
    private LocalDateTime createdBefore;
    
    // Constructors
    public TaskSearchDTO() {}
    
    // Getters and Setters
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getPriority() {
        return priority;
    }
    
    public void setPriority(String priority) {
        this.priority = priority;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getKeyword() {
        return keyword;
    }
    
    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }
    
    public String getTagName() {
        return tagName;
    }
    
    public void setTagName(String tagName) {
        this.tagName = tagName;
    }
    
    public LocalDateTime getCreatedAfter() {
        return createdAfter;
    }
    
    public void setCreatedAfter(LocalDateTime createdAfter) {
        this.createdAfter = createdAfter;
    }
    
    public LocalDateTime getCreatedBefore() {
        return createdBefore;
    }
    
    public void setCreatedBefore(LocalDateTime createdBefore) {
        this.createdBefore = createdBefore;
    }
}