package com.example.taskmanagement.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import java.util.Optional;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JpaConfig {
    
    @Bean
    public AuditorAware<String> auditorProvider() {
        // In a real app, get the current user from SecurityContext
        // For now, we'll return a fixed user
        return () -> Optional.of("system");
        
        // In production with Spring Security:
        // return () -> Optional.ofNullable(SecurityContextHolder.getContext())
        //     .map(SecurityContext::getAuthentication)
        //     .filter(Authentication::isAuthenticated)
        //     .map(Authentication::getName);
    }
}