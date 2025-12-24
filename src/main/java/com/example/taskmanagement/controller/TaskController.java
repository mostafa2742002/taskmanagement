package com.example.taskmanagement.controller;


import com.example.taskmanagement.entity.Task;
import com.example.taskmanagement.entity.TaskSearchDTO;
import com.example.taskmanagement.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    
    @Autowired
    private TaskService taskService;
    
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
    
    // Advanced search with Specifications
    @PostMapping("/search")
    public ResponseEntity<List<Task>> searchTasks(@RequestBody TaskSearchDTO searchDTO) {
        List<Task> tasks = taskService.searchTasks(searchDTO);
        return ResponseEntity.ok(tasks);
    }
    
    // Advanced search with pagination
    @PostMapping("/search/paginated")
    public ResponseEntity<Page<Task>> searchTasksPaginated(
            @RequestBody TaskSearchDTO searchDTO,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction) {
        
        Sort.Direction sortDirection = direction.equalsIgnoreCase("ASC") 
            ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        
        Page<Task> tasks = taskService.searchTasksWithPagination(searchDTO, pageable);
        return ResponseEntity.ok(tasks);
    }
    
    // Update with optimistic locking
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTask(@PathVariable Long id, @RequestBody Task task) {
        try {
            Task updatedTask = taskService.updateTaskWithOptimisticLock(id, task);
            return ResponseEntity.ok(updatedTask);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Error: " + e.getMessage());
        }
    }
    
    // Update with retry
    @PutMapping("/{id}/retry")
    public ResponseEntity<?> updateTaskWithRetry(@PathVariable Long id, @RequestBody Task task) {
        try {
            Task updatedTask = taskService.updateTaskWithRetry(id, task, 3);
            return ResponseEntity.ok(updatedTask);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Error: " + e.getMessage());
        }
    }
    
    @PostMapping("/{taskId}/tags/{tagId}")
    public ResponseEntity<Task> addTagToTask(
            @PathVariable Long taskId,
            @PathVariable Long tagId) {
        Task task = taskService.addTagToTask(taskId, tagId);
        return ResponseEntity.ok(task);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }
    
    // Batch delete
    @DeleteMapping("/batch")
    public ResponseEntity<Void> deleteMultipleTasks(@RequestBody List<Long> taskIds) {
        taskService.deleteMultipleTasks(taskIds);
        return ResponseEntity.noContent().build();
    }
}