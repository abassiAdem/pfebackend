package com.example.demo.repository;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entities.AppUser;
import com.example.demo.entities.ResponsableFonctionnel;
@Repository
public interface ResponsableRepository extends JpaRepository<ResponsableFonctionnel, Long> {
    Optional<ResponsableFonctionnel> findByAgent(AppUser agent);
}
