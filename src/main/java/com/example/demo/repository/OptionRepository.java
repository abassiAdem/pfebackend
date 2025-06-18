package com.example.demo.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.entities.Option;

import jakarta.transaction.Transactional;

@Repository
public interface OptionRepository extends JpaRepository<Option, Long> {
	@Transactional
    @Modifying
   @Query("DELETE FROM Option o WHERE o.champ.id = :champId")
   void deleteByChampId(@Param("champId") Long champId);
}