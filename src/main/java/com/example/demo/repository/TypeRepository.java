package com.example.demo.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.example.demo.entities.Type;
@Repository
public interface TypeRepository extends JpaRepository<Type, Long> {
    @Query("SELECT t FROM Type t LEFT JOIN FETCH t.formulaire f LEFT JOIN FETCH f.champs ")
    Set<Type> findAllWithFormulairesAndChamps();


    Optional<Type> findByName(String name);



    @Query("SELECT  t FROM Type t LEFT JOIN FETCH t.formulaire f LEFT JOIN FETCH f.champs  where  t.status = 'active'")
    Set<Type> findAllWithFormulairesAndChampsNotNormal();
}


