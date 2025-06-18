package com.example.demo.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.entities.AppUser;
import com.example.demo.entities.Chef;
import com.example.demo.entities.Demande;



@Repository
public interface AgentRepository extends  JpaRepository<AppUser, Long> {
	List<AppUser> findByRole(String role);
   /* @Query(value = "SELECT a.* FROM agent a INNER JOIN responsable_fonctionnel r ON a.id = r.agent_id", 
            nativeQuery = true)
     List<AppUser> findAllResponsables();*/
	
    @Query("SELECT u FROM AppUser u JOIN Employee e ON u = e.agent WHERE e.chef.id = :chefId")
    List<AppUser> findEmployeesByChefId(@Param("chefId") Long chefId);
	
	
    Optional<AppUser> findByKeycloakId(String keycloakId);
    Optional<AppUser> findByEmail(String email);
    @Query("SELECT a FROM AppUser a WHERE a.chef IS NOT NULL")
    List<AppUser> findAllChefs();

    List<AppUser> findByIsActiveTrue();
    
    
    List<AppUser> findByChef(Chef chef);
    
    
    @Query("SELECT DATE(u.lastLogin), COUNT(u.id) " +
            "FROM AppUser u " +
            "WHERE u.lastLogin >= :startDate AND u.lastLogin < :endDate " +
            "GROUP BY DATE(u.lastLogin) " +
            "ORDER BY DATE(u.lastLogin)")
     List<Object[]> findUserLoginStats(@Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate);

     @Query("SELECT u.isActive, COUNT(u.id) FROM AppUser u GROUP BY u.isActive")
     List<Object[]> countUsersByActiveStatus();

	/*List<AppUser> findResponsableForDemande(Demande demande);*/

}