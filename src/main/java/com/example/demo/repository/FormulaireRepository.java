package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entities.Formulaire;
import com.example.demo.entities.Type;

@Repository
public interface FormulaireRepository extends JpaRepository<Formulaire, Long> {
	List<Formulaire> findByTypeformulaire(Type type);
}

