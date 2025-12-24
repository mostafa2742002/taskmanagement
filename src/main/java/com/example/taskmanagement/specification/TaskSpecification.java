package com.example.taskmanagement.specification;


import com.example.taskmanagement.entity.Task;
import org.springframework.data.jpa.domain.Specification;
import java.time.LocalDateTime;


public class TaskSpecification {
    
    // Specification for status
    public static Specification<Task> hasStatus(String status) {
        return (root, query, cb) -> 
            status == null ? null : cb.equal(root.get("status"), status);
    }
    
    // Specification for priority
    public static Specification<Task> hasPriority(String priority) {
        return (root, query, cb) -> 
            priority == null ? null : cb.equal(root.get("priority"), priority);
    }
    
    // Specification for user
    public static Specification<Task> belongsToUser(Long userId) {
        return (root, query, cb) -> 
            userId == null ? null : cb.equal(root.get("user").get("id"), userId);
    }
    
    // Specification for title search
    public static Specification<Task> titleContains(String keyword) {
        return (root, query, cb) -> 
            keyword == null ? null : cb.like(cb.lower(root.get("title")), 
                "%" + keyword.toLowerCase() + "%");
    }
    
    // Specification for description search
    public static Specification<Task> descriptionContains(String keyword) {
        return (root, query, cb) -> 
            keyword == null ? null : cb.like(cb.lower(root.get("description")), 
                "%" + keyword.toLowerCase() + "%");
    }
    
    // Specification for date range
    public static Specification<Task> createdBetween(LocalDateTime start, LocalDateTime end) {
        return (root, query, cb) -> {
            if (start == null && end == null) return null;
            if (start == null) return cb.lessThanOrEqualTo(root.get("createdAt"), end);
            if (end == null) return cb.greaterThanOrEqualTo(root.get("createdAt"), start);
            return cb.between(root.get("createdAt"), start, end);
        };
    }
    
    // Specification for tag
    public static Specification<Task> hasTag(String tagName) {
        return (root, query, cb) -> {
            if (tagName == null) return null;
            return cb.equal(root.join("tags").get("name"), tagName);
        };
    }
}
