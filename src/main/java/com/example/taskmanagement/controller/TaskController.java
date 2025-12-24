package com.example.taskmanagement.controller;

import com.example.taskmanagement.entity.Task;
import com.example.taskmanagement.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    
    @Autowired
    private TaskService taskService;
    
    // Basic CRUD (same as before)
    @PostMapping("/user/{userId}")
    public ResponseEntity<Task> createTask(@PathVariable Long userId, @RequestBody Task task) {
        Task createdTask = taskService.createTask(userId, task);
        return new ResponseEntity<>(createdTask, HttpStatus.CREATED);
    }
    
    @GetMapping
    public ResponseEntity<List<Task>> getAllTasks() {
        List<Task> tasks = taskService.getAllTasks();
        return ResponseEntity.ok(tasks);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskById(@PathVariable Long id) {
        return taskService.getTaskById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    // ========================================
    // QUERY METHOD ENDPOINTS
    // ========================================
    
    @GetMapping("/search")
    public ResponseEntity<List<Task>> searchTasks(@RequestParam String keyword) {
        List<Task> tasks = taskService.searchTasks(keyword);
        return ResponseEntity.ok(tasks);
    }
    
    @GetMapping("/recent")
    public ResponseEntity<List<Task>> getRecentTasks(@RequestParam(defaultValue = "7") int days) {
        List<Task> tasks = taskService.getRecentTasks(days);
        return ResponseEntity.ok(tasks);
    }
    
    @GetMapping("/date-range")
    public ResponseEntity<List<Task>> getTasksInDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        List<Task> tasks = taskService.getTasksInDateRange(start, end);
        return ResponseEntity.ok(tasks);
    }
    
    @GetMapping("/user/{userId}/latest")
    public ResponseEntity<Task> getLatestTaskForUser(@PathVariable Long userId) {
        Task task = taskService.getLatestTaskForUser(userId);
        return task != null ? ResponseEntity.ok(task) : ResponseEntity.notFound().build();
    }
    
    @GetMapping("/status/{status}/top5")
    public ResponseEntity<List<Task>> getTop5RecentByStatus(@PathVariable String status) {
        List<Task> tasks = taskService.getTop5RecentTasksByStatus(status);
        return ResponseEntity.ok(tasks);
    }
    
    @GetMapping("/status/{status}/count")
    public ResponseEntity<Long> countTasksByStatus(@PathVariable String status) {
        long count = taskService.countTasksByStatus(status);
        return ResponseEntity.ok(count);
    }
    
    @GetMapping("/exists")
    public ResponseEntity<Boolean> taskExists(
            @RequestParam String title,
            @RequestParam Long userId) {
        boolean exists = taskService.taskExistsForUser(title, userId);
        return ResponseEntity.ok(exists);
    }
    
    // ========================================
    // JPQL QUERY ENDPOINTS
    // ========================================
    
    @GetMapping("/username/{username}")
    public ResponseEntity<List<Task>> getTasksByUsername(@PathVariable String username) {
        List<Task> tasks = taskService.getTasksByUsername(username);
        return ResponseEntity.ok(tasks);
    }
    
    @GetMapping("/search/advanced")
    public ResponseEntity<List<Task>> searchWithStatusFilter(
            @RequestParam String keyword,
            @RequestParam String status) {
        List<Task> tasks = taskService.searchTasksByKeywordAndStatus(keyword, status);
        return ResponseEntity.ok(tasks);
    }
    
    @GetMapping("/statuses")
    public ResponseEntity<List<String>> getAllStatuses() {
        List<String> statuses = taskService.getAllDistinctStatuses();
        return ResponseEntity.ok(statuses);
    }
    
    @GetMapping("/statistics/by-status")
    public ResponseEntity<Map<String, Long>> getTaskStatistics() {
        Map<String, Long> stats = taskService.getTaskCountByStatus();
        return ResponseEntity.ok(stats);
    }
    
    // ========================================
    // UPDATE/DELETE ENDPOINTS
    // ========================================
    
    @PutMapping("/bulk-update-status")
    public ResponseEntity<String> bulkUpdateStatus(
            @RequestParam String oldStatus,
            @RequestParam String newStatus) {
        int updated = taskService.bulkUpdateTaskStatus(oldStatus, newStatus);
        return ResponseEntity.ok(updated + " tasks updated");
    }
    
    @DeleteMapping("/cleanup")
    public ResponseEntity<String> cleanupOldTasks(@RequestParam(defaultValue = "30") int daysOld) {
        int deleted = taskService.deleteOldCompletedTasks(daysOld);
        return ResponseEntity.ok(deleted + " old tasks deleted");
    }
    
    // ========================================
    // PAGINATION ENDPOINTS
    // ========================================
    
    @GetMapping("/paginated")
    public ResponseEntity<Page<Task>> getTasksPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Task> tasks = taskService.getTasksWithPagination(page, size);
        return ResponseEntity.ok(tasks);
    }
    
    @GetMapping("/status/{status}/paginated")
    public ResponseEntity<Page<Task>> getTasksByStatusPaginated(
            @PathVariable String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Task> tasks = taskService.getTasksByStatusWithPagination(status, page, size);
        return ResponseEntity.ok(tasks);
    }
    
    @GetMapping("/search/paginated")
    public ResponseEntity<Page<Task>> searchTasksPaginated(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy) {
        Page<Task> tasks = taskService.searchTasksWithPagination(keyword, page, size, sortBy);
        return ResponseEntity.ok(tasks);
    }
    
    // Existing endpoints (same as before)
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Task>> getUserTasks(@PathVariable Long userId) {
        List<Task> tasks = taskService.getTasksByUserId(userId);
        return ResponseEntity.ok(tasks);
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Task>> getTasksByStatus(@PathVariable String status) {
        List<Task> tasks = taskService.getTasksByStatus(status);
        return ResponseEntity.ok(tasks);
    }
    
    @GetMapping("/tag/{tagName}")
    public ResponseEntity<List<Task>> getTasksByTag(@PathVariable String tagName) {
        List<Task> tasks = taskService.getTasksByTagName(tagName);
        return ResponseEntity.ok(tasks);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(@PathVariable Long id, @RequestBody Task task) {
        Task updatedTask = taskService.updateTask(id, task);
        return ResponseEntity.ok(updatedTask);
    }
    
    @PostMapping("/{taskId}/tags/{tagId}")
    public ResponseEntity<Task> addTagToTask(
            @PathVariable Long taskId,
            @PathVariable Long tagId) {
        Task task = taskService.addTagToTask(taskId, tagId);
        return ResponseEntity.ok(task);
    }
    
    @DeleteMapping("/{taskId}/tags/{tagId}")
    public ResponseEntity<Task> removeTagFromTask(
            @PathVariable Long taskId,
            @PathVariable Long tagId) {
        Task task = taskService.removeTagFromTask(taskId, tagId);
        return ResponseEntity.ok(task);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }
}