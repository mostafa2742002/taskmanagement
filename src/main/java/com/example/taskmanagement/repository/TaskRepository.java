package com.example.taskmanagement.repository;


import com.example.taskmanagement.entity.Task;
import com.example.taskmanagement.entity.TaskSummaryDTO;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Repository
public interface TaskRepository extends JpaRepository<Task, Long>, 
                                        JpaSpecificationExecutor<Task> {
    
    // ========================================
    // 1. QUERY METHODS (Derived Queries)
    // ========================================
    // JPA creates the query automatically from the method name!
    
    // Find by single field
    List<Task> findByStatus(String status);
    
    // Find by multiple fields (AND)
    List<Task> findByStatusAndUserId(String status, Long userId);
    
    // Find with OR condition
    List<Task> findByStatusOrTitle(String status, String title);
    
    // Find with LIKE (contains)
    List<Task> findByTitleContaining(String keyword);
    
    // Find with LIKE (starts with)
    List<Task> findByTitleStartingWith(String prefix);
    
    // Find with LIKE (ends with)
    List<Task> findByTitleEndingWith(String suffix);
    
    // Find with case-insensitive search
    List<Task> findByTitleContainingIgnoreCase(String keyword);
    
    // Find with comparison operators
    List<Task> findByCreatedAtAfter(LocalDateTime date);
    List<Task> findByCreatedAtBefore(LocalDateTime date);
    List<Task> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    
    // Find with NULL checks
    List<Task> findByDescriptionIsNull();
    List<Task> findByDescriptionIsNotNull();
    
    // Find with ordering
    List<Task> findByStatusOrderByCreatedAtDesc(String status);
    List<Task> findByUserIdOrderByTitleAsc(Long userId);
    
    // Find first/top N results
    Task findFirstByUserIdOrderByCreatedAtDesc(Long userId);
    List<Task> findTop5ByStatusOrderByCreatedAtDesc(String status);
    
    // Count queries
    long countByStatus(String status);
    long countByUserId(Long userId);
    
    // Exists queries
    boolean existsByTitleAndUserId(String title, Long userId);
    
    // Delete queries
    @Transactional
    void deleteByStatus(String status);
    
    // ========================================
    // 2. JPQL QUERIES (@Query)
    // ========================================
    // JPQL works with ENTITIES (not tables!)
    // Use class names and field names, not table/column names
    
    // Basic JPQL query
    @Query("SELECT t FROM Task t WHERE t.status = :status")
    List<Task> findTasksByStatus(@Param("status") String status);
    
    // JPQL with multiple parameters
    @Query("SELECT t FROM Task t WHERE t.status = :status AND t.user.id = :userId")
    List<Task> findTasksByStatusAndUser(
        @Param("status") String status,
        @Param("userId") Long userId
    );
    
    // JPQL with JOIN
    @Query("SELECT t FROM Task t JOIN t.user u WHERE u.username = :username")
    List<Task> findTasksByUsername(@Param("username") String username);
    
    // JPQL with multiple JOINs
    @Query("SELECT t FROM Task t JOIN t.tags tag WHERE tag.name = :tagName")
    List<Task> findByTagName(@Param("tagName") String tagName);
    
    // JPQL with complex WHERE conditions
    @Query("SELECT t FROM Task t WHERE " +
           "(t.title LIKE %:keyword% OR t.description LIKE %:keyword%) " +
           "AND t.status = :status")
    List<Task> searchTasksByKeywordAndStatus(
        @Param("keyword") String keyword,
        @Param("status") String status
    );
    
    // JPQL with date range
    @Query("SELECT t FROM Task t WHERE t.createdAt BETWEEN :startDate AND :endDate")
    List<Task> findTasksCreatedBetween(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    // JPQL with aggregation functions
    @Query("SELECT COUNT(t) FROM Task t WHERE t.status = :status")
    long countTasksByStatus(@Param("status") String status);
    
    @Query("SELECT t.status, COUNT(t) FROM Task t GROUP BY t.status")
    List<Object[]> countTasksGroupedByStatus();
    
    // JPQL with DISTINCT
    @Query("SELECT DISTINCT t.status FROM Task t")
    List<String> findAllDistinctStatuses();
    
    // JPQL returning specific fields (DTO projection)
    @Query("SELECT t.id, t.title, t.status FROM Task t WHERE t.user.id = :userId")
    List<Object[]> findTaskSummaryByUserId(@Param("userId") Long userId);
    
    // ========================================
    // 3. NATIVE SQL QUERIES
    // ========================================
    // Raw SQL queries (use table and column names!)
    // Use when JPQL is not enough or for complex DB-specific operations
    
    @Query(value = "SELECT * FROM tasks WHERE status = :status", nativeQuery = true)
    List<Task> findTasksByStatusNative(@Param("status") String status);
    
    @Query(value = "SELECT * FROM tasks WHERE user_id = :userId AND " +
                   "created_at > DATE_SUB(NOW(), INTERVAL 7 DAY)", 
           nativeQuery = true)
    List<Task> findRecentTasksByUser(@Param("userId") Long userId);
    
    // Native query with pagination
    @Query(value = "SELECT * FROM tasks WHERE status = :status ORDER BY created_at DESC",
           countQuery = "SELECT count(*) FROM tasks WHERE status = :status",
           nativeQuery = true)
    Page<Task> findTasksByStatusNativeWithPagination(@Param("status") String status, Pageable pageable);
    
    // ========================================
    // 4. UPDATE/DELETE QUERIES
    // ========================================
    // These modify data, so need @Modifying and @Transactional
    
    @Modifying
    @Transactional
    @Query("UPDATE Task t SET t.status = :newStatus WHERE t.status = :oldStatus")
    int bulkUpdateStatus(
        @Param("oldStatus") String oldStatus,
        @Param("newStatus") String newStatus
    );
    
    @Modifying
    @Transactional
    @Query("UPDATE Task t SET t.status = :status WHERE t.id = :taskId")
    int updateTaskStatus(@Param("taskId") Long taskId, @Param("status") String status);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM Task t WHERE t.status = :status AND t.createdAt < :date")
    int deleteOldTasksByStatus(@Param("status") String status, @Param("date") LocalDateTime date);
    
    // ========================================
    // 5. PAGINATION AND SORTING
    // ========================================
    // Using Pageable parameter
    
    Page<Task> findByStatus(String status, Pageable pageable);
    Page<Task> findByUserId(Long userId, Pageable pageable);
    
    @Query("SELECT t FROM Task t WHERE t.title LIKE %:keyword%")
    Page<Task> searchTasks(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT new com.example.taskmanagement.entity.TaskSummaryDTO(" +
           "t.id, t.title, t.status, u.username) " +
           "FROM Task t JOIN t.user u WHERE u.id = :userId")
    List<TaskSummaryDTO> findTaskSummariesByUserId(@Param("userId") Long userId);

    List<Task> findByUserId(Long userId);


    // Query with optimistic lock
    @Query("SELECT t FROM Task t WHERE t.id = :id")
    Optional<Task> findByIdWithLock(@Param("id") Long id);
    
}