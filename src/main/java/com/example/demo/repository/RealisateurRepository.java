package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.enmus.StatutDemande;
import com.example.demo.entities.AppUser;
import com.example.demo.entities.Demande;
import com.example.demo.entities.Realisateur;


import jakarta.transaction.Transactional;

@Repository
public interface RealisateurRepository extends JpaRepository<Realisateur, Long> {
    Optional<Realisateur> findByAgent(AppUser agent);
  

    @Query("SELECT r FROM Realisateur r WHERE r.responsable.id = :responsableId")
        List<Realisateur> findAllByResponsableDepartement(
            @Param("responsableId") Long responsableId
        );
}
