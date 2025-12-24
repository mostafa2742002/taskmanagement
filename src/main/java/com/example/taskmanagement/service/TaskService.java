package com.example.taskmanagement.service;


import com.example.taskmanagement.entity.Task;
import com.example.taskmanagement.entity.Tag;
import com.example.taskmanagement.entity.User;
import com.example.taskmanagement.repository.TaskRepository;
import com.example.taskmanagement.repository.TagRepository;
import com.example.taskmanagement.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;

@Service
public class TaskService {
    
    @Autowired
    private TaskRepository taskRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private TagRepository tagRepository;
    
    // Basic CRUD (same as before)
    public Task createTask(Long userId, Task task) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        task.setUser(user);
        return taskRepository.save(task);
    }
    
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }
    
    public Optional<Task> getTaskById(Long id) {
        return taskRepository.findById(id);
    }
    
    // ========================================
    // QUERY METHOD EXAMPLES
    // ========================================
    
    public List<Task> getTasksByStatus(String status) {
        return taskRepository.findByStatus(status);
    }
    
    public List<Task> searchTasks(String keyword) {
        return taskRepository.findByTitleContainingIgnoreCase(keyword);
    }
    
    public List<Task> getTasksCreatedAfter(LocalDateTime date) {
        return taskRepository.findByCreatedAtAfter(date);
    }
    
    public List<Task> getRecentTasks(int days) {
        LocalDateTime date = LocalDateTime.now().minusDays(days);
        return taskRepository.findByCreatedAtAfter(date);
    }
    
    public List<Task> getTasksInDateRange(LocalDateTime start, LocalDateTime end) {
        return taskRepository.findByCreatedAtBetween(start, end);
    }
    
    public Task getLatestTaskForUser(Long userId) {
        return taskRepository.findFirstByUserIdOrderByCreatedAtDesc(userId);
    }
    
    public List<Task> getTop5RecentTasksByStatus(String status) {
        return taskRepository.findTop5ByStatusOrderByCreatedAtDesc(status);
    }
    
    public long countTasksByStatus(String status) {
        return taskRepository.countByStatus(status);
    }
    
    public boolean taskExistsForUser(String title, Long userId) {
        return taskRepository.existsByTitleAndUserId(title, userId);
    }
    
    // ========================================
    // JPQL QUERY EXAMPLES
    // ========================================
    
    public List<Task> getTasksByUsername(String username) {
        return taskRepository.findTasksByUsername(username);
    }
    
    public List<Task> searchTasksByKeywordAndStatus(String keyword, String status) {
        return taskRepository.searchTasksByKeywordAndStatus(keyword, status);
    }
    
    public List<String> getAllDistinctStatuses() {
        return taskRepository.findAllDistinctStatuses();
    }
    
    public Map<String, Long> getTaskCountByStatus() {
        List<Object[]> results = taskRepository.countTasksGroupedByStatus();
        Map<String, Long> statusCounts = new HashMap<>();
        for (Object[] result : results) {
            String status = (String) result[0];
            Long count = (Long) result[1];
            statusCounts.put(status, count);
        }
        return statusCounts;
    }
    
    // ========================================
    // UPDATE/DELETE EXAMPLES
    // ========================================
    
    public int bulkUpdateTaskStatus(String oldStatus, String newStatus) {
        return taskRepository.bulkUpdateStatus(oldStatus, newStatus);
    }
    
    public int updateTaskStatus(Long taskId, String status) {
        return taskRepository.updateTaskStatus(taskId, status);
    }
    
    public int deleteOldCompletedTasks(int daysOld) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);
        return taskRepository.deleteOldTasksByStatus("DONE", cutoffDate);
    }
    
    @Transactional
    public void deleteTasksByStatus(String status) {
        taskRepository.deleteByStatus(status);
    }
    
    // ========================================
    // PAGINATION EXAMPLES
    // ========================================
    
    public Page<Task> getTasksWithPagination(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return taskRepository.findAll(pageable);
    }
    
    public Page<Task> getTasksByStatusWithPagination(String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return taskRepository.findByStatus(status, pageable);
    }
    
    public Page<Task> searchTasksWithPagination(String keyword, int page, int size, String sortBy) {
        Sort sort = Sort.by(Sort.Direction.DESC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        return taskRepository.searchTasks(keyword, pageable);
    }
    
    // Existing methods (same as before)
    public List<Task> getTasksByUserId(Long userId) {
        return taskRepository.findByUserId(userId);
    }
    
    public List<Task> getUserTasksByStatus(Long userId, String status) {
        return taskRepository.findByStatusAndUserId(status, userId);
    }
    
    public List<Task> getTasksByTagName(String tagName) {
        return taskRepository.findByTagName(tagName);
    }
    
    public Task updateTask(Long id, Task taskDetails) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + id));
        
        task.setTitle(taskDetails.getTitle());
        task.setDescription(taskDetails.getDescription());
        task.setStatus(taskDetails.getStatus());
        
        return taskRepository.save(task);
    }
    
    @Transactional
    public Task addTagToTask(Long taskId, Long tagId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new RuntimeException("Tag not found"));
        
        task.addTag(tag);
        return taskRepository.save(task);
    }
    
    @Transactional
    public Task removeTagFromTask(Long taskId, Long tagId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new RuntimeException("Tag not found"));
        
        task.removeTag(tag);
        return taskRepository.save(task);
    }
    
    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }
}