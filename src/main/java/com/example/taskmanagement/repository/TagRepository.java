package com.example.taskmanagement.repository;


import com.example.taskmanagement.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;


@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findByName(String name);
    boolean existsByName(String name);
    
    // Find all tags used by a specific task
    @Query("SELECT t FROM Tag t JOIN t.tasks task WHERE task.id = :taskId")
    List<Tag> findByTaskId(Long taskId);
    
    // Find all tags used by a specific user's tasks
    @Query("SELECT DISTINCT t FROM Tag t JOIN t.tasks task WHERE task.user.id = :userId")
    List<Tag> findByUserId(Long userId);
}