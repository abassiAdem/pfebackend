package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entities.Champ;

@Repository
public interface ChampRepository extends JpaRepository<Champ, Long> {
	List<Champ> findByFormulaireId(Long formulaireId);
}