package com.example.taskmanagement.service;

import com.example.taskmanagement.entity.Tag;
import com.example.taskmanagement.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class TagService {
    
    @Autowired
    private TagRepository tagRepository;
    
    public Tag createTag(Tag tag) {
        if (tagRepository.existsByName(tag.getName())) {
            throw new RuntimeException("Tag already exists");
        }
        return tagRepository.save(tag);
    }
    
    public List<Tag> getAllTags() {
        return tagRepository.findAll();
    }
    
    public Optional<Tag> getTagById(Long id) {
        return tagRepository.findById(id);
    }
    
    public Optional<Tag> getTagByName(String name) {
        return tagRepository.findByName(name);
    }
    
    public List<Tag> getTagsByTaskId(Long taskId) {
        return tagRepository.findByTaskId(taskId);
    }
    
    public List<Tag> getTagsByUserId(Long userId) {
        return tagRepository.findByUserId(userId);
    }
    
    public Tag updateTag(Long id, Tag tagDetails) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tag not found"));
        
        tag.setName(tagDetails.getName());
        tag.setColor(tagDetails.getColor());
        
        return tagRepository.save(tag);
    }
    
    public void deleteTag(Long id) {
        tagRepository.deleteById(id);
    }
}

