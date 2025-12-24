package com.example.taskmanagement.service;

import com.example.taskmanagement.entity.Task;
import com.example.taskmanagement.entity.Tag;
import com.example.taskmanagement.entity.User;
import com.example.taskmanagement.entity.TaskSearchDTO;
import com.example.taskmanagement.repository.TaskRepository;
import com.example.taskmanagement.repository.TagRepository;
import com.example.taskmanagement.repository.UserRepository;
import com.example.taskmanagement.specification.TaskSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import java.util.List;
import java.util.Optional;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TagRepository tagRepository;

    // ========================================
    // TRANSACTION EXAMPLES
    // ========================================

    // Default transaction (REQUIRED propagation, READ_COMMITTED isolation)
    @Transactional
    public Task createTask(Long userId, Task task) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        task.setUser(user);
        return taskRepository.save(task);
    }

    // Read-only transaction (optimized for reads, no dirty checking)
    @Transactional(readOnly = true)
    public Optional<Task> getTaskById(Long id) {
        return taskRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    // Transaction with rollback on specific exception
    @Transactional(rollbackFor = Exception.class)
    public Task createTaskWithValidation(Long userId, Task task) {
        if (task.getTitle() == null || task.getTitle().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be empty");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        task.setUser(user);
        return taskRepository.save(task);
    }

    // Transaction that creates a new transaction (REQUIRES_NEW)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Task createTaskInNewTransaction(Long userId, Task task) {
        // This runs in its own transaction, independent of the caller
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        task.setUser(user);
        return taskRepository.save(task);
    }

    // Multiple operations in one transaction
    @Transactional
    public void createMultipleTasks(Long userId, List<Task> tasks) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        for (Task task : tasks) {
            task.setUser(user);
            taskRepository.save(task);
        }
        // If any save fails, ALL are rolled back
    }

    // ========================================
    // OPTIMISTIC LOCKING EXAMPLE
    // ========================================

    @Transactional
    public Task updateTaskWithOptimisticLock(Long id, Task taskDetails) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        // Update fields
        task.setTitle(taskDetails.getTitle());
        task.setDescription(taskDetails.getDescription());
        task.setStatus(taskDetails.getStatus());
        task.setPriority(taskDetails.getPriority());

        try {
            return taskRepository.save(task);
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new RuntimeException("Task was updated by another user. Please refresh and try again.");
        }
    }

    // Retry logic for optimistic locking failures
    @Transactional
    public Task updateTaskWithRetry(Long id, Task taskDetails, int maxRetries) {
        int attempts = 0;

        while (attempts < maxRetries) {
            try {
                Task task = taskRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("Task not found"));

                task.setTitle(taskDetails.getTitle());
                task.setDescription(taskDetails.getDescription());
                task.setStatus(taskDetails.getStatus());

                return taskRepository.save(task);

            } catch (ObjectOptimisticLockingFailureException e) {
                attempts++;
                if (attempts >= maxRetries) {
                    throw new RuntimeException("Failed to update task after " + maxRetries + " attempts");
                }
                // Wait a bit before retrying
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        throw new RuntimeException("Update failed");
    }

    // ========================================
    // SPECIFICATIONS (DYNAMIC QUERIES)
    // ========================================

    @Transactional(readOnly = true)
    public List<Task> searchTasks(TaskSearchDTO searchDTO) {
        Specification<Task> spec = null;

        if (searchDTO.getStatus() != null) {
            spec = Specification.where(TaskSpecification.hasStatus(searchDTO.getStatus()));
        }

        if (searchDTO.getPriority() != null) {
            spec = (spec == null) ? Specification.where(TaskSpecification.hasPriority(searchDTO.getPriority()))
                    : spec.and(TaskSpecification.hasPriority(searchDTO.getPriority()));
        }

        if (searchDTO.getUserId() != null) {
            spec = (spec == null) ? Specification.where(TaskSpecification.belongsToUser(searchDTO.getUserId()))
                    : spec.and(TaskSpecification.belongsToUser(searchDTO.getUserId()));
        }

        if (searchDTO.getKeyword() != null) {
            Specification<Task> titleSpec = TaskSpecification.titleContains(searchDTO.getKeyword());
            Specification<Task> descSpec = TaskSpecification.descriptionContains(searchDTO.getKeyword());
            Specification<Task> keywordSpec = Specification.where(titleSpec).or(descSpec);
            spec = (spec == null) ? keywordSpec : spec.and(keywordSpec);
        }

        if (searchDTO.getTagName() != null) {
            spec = (spec == null) ? Specification.where(TaskSpecification.hasTag(searchDTO.getTagName()))
                    : spec.and(TaskSpecification.hasTag(searchDTO.getTagName()));
        }

        if (searchDTO.getCreatedAfter() != null || searchDTO.getCreatedBefore() != null) {
            spec = (spec == null) ? Specification.where(TaskSpecification.createdBetween(
                    searchDTO.getCreatedAfter(),
                    searchDTO.getCreatedBefore()))
                    : spec.and(TaskSpecification.createdBetween(
                            searchDTO.getCreatedAfter(),
                            searchDTO.getCreatedBefore()));
        }

        return taskRepository.findAll(spec != null ? spec : Specification.where((root, query, cb) -> cb.conjunction()));
    }

    @Transactional(readOnly = true)
    public Page<Task> searchTasksWithPagination(TaskSearchDTO searchDTO, Pageable pageable) {
        Specification<Task> spec = null;

        if (searchDTO.getStatus() != null) {
            spec = Specification.where(TaskSpecification.hasStatus(searchDTO.getStatus()));
        }

        if (searchDTO.getPriority() != null) {
            spec = (spec == null) ? Specification.where(TaskSpecification.hasPriority(searchDTO.getPriority()))
                    : spec.and(TaskSpecification.hasPriority(searchDTO.getPriority()));
        }

        if (searchDTO.getUserId() != null) {
            spec = (spec == null) ? Specification.where(TaskSpecification.belongsToUser(searchDTO.getUserId()))
                    : spec.and(TaskSpecification.belongsToUser(searchDTO.getUserId()));
        }

        if (searchDTO.getKeyword() != null) {
            Specification<Task> titleSpec = TaskSpecification.titleContains(searchDTO.getKeyword());
            Specification<Task> descSpec = TaskSpecification.descriptionContains(searchDTO.getKeyword());
            Specification<Task> keywordSpec = Specification.where(titleSpec).or(descSpec);
            spec = (spec == null) ? keywordSpec : spec.and(keywordSpec);
        }

        return taskRepository.findAll(spec != null ? spec : Specification.where((root, query, cb) -> cb.conjunction()),
                pageable);
    }

    // ========================================
    // OTHER OPERATIONS
    // ========================================

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
    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }

    // Batch operation
    @Transactional
    public void deleteMultipleTasks(List<Long> taskIds) {
        taskIds.forEach(id -> taskRepository.deleteById(id));
    }
}