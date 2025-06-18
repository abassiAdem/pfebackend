package com.example.demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.demo.entities.EmailConfiguration;
@Repository
public interface EmailConfigurationRepository extends JpaRepository<EmailConfiguration, Long> {
    
    @Query("SELECT e FROM EmailConfiguration e ORDER BY e.lastUpdated DESC")
    Optional<EmailConfiguration> findLatestConfiguration();
    
    @Query("SELECT e FROM EmailConfiguration e ORDER BY e.id ASC LIMIT 1")
    Optional<EmailConfiguration> findFirstConfiguration();
    
    Optional<EmailConfiguration> findFirstByOrderById();

    Optional<EmailConfiguration> findFirstByOrderByIdDesc();
}