package com.example.demo.repository;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entities.AppUser;
import com.example.demo.entities.Chef;
@Repository
public interface ChefRepository extends JpaRepository<Chef, Long> {
    Optional<Chef> findByAgent(AppUser agent);
}