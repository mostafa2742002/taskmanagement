package com.example.taskmanagement.entity;


import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonBackReference;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tasks")
public class Task extends Auditable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String title;
    
    @Column(length = 500)
    private String description;
    
    @Column(nullable = false)
    private String status = "TODO";
    
    @Column(nullable = false)
    private String priority = "MEDIUM";
    
    // ⭐ OPTIMISTIC LOCKING ⭐
    // Version field for handling concurrent updates
    @Version
    @Column(name = "version")
    private Long version;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference
    private User user;
    
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "task_tags",
        joinColumns = @JoinColumn(name = "task_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();
    
    // Lifecycle callbacks
    @PrePersist
    protected void onCreate() {
        if (status == null) {
            status = "TODO";
        }
        if (priority == null) {
            priority = "MEDIUM";
        }
    }
    
    @PostPersist
    protected void afterCreate() {
        System.out.println("Task created: " + this.title);
    }
    
    @PreUpdate
    protected void onUpdate() {
        System.out.println("Task updated: " + this.title);
    }
    
    @PostUpdate
    protected void afterUpdate() {
        System.out.println("Task update completed: " + this.title);
    }
    
    @PreRemove
    protected void onDelete() {
        System.out.println("Task about to be deleted: " + this.title);
    }
    
    @PostRemove
    protected void afterDelete() {
        System.out.println("Task deleted: " + this.title);
    }
    
    @PostLoad
    protected void onLoad() {
        System.out.println("Task loaded from database: " + this.title);
    }
    
    // Constructors
    public Task() {
        this.status = "TODO";
        this.priority = "MEDIUM";
    }
    
    public Task(String title, String description, String status) {
        this.title = title;
        this.description = description;
        this.status = status;
        this.priority = "MEDIUM";
    }
    
    // Helper methods
    public void addTag(Tag tag) {
        this.tags.add(tag);
        tag.getTasks().add(this);
    }
    
    public void removeTag(Tag tag) {
        this.tags.remove(tag);
        tag.getTasks().remove(this);
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
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
    
    public Long getVersion() {
        return version;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public Set<Tag> getTags() {
        return tags;
    }
    
    public void setTags(Set<Tag> tags) {
        this.tags = tags;
    }
}