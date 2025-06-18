package com.example.demo.repository;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.dto.AgentDemandesCountDTO;
import com.example.demo.dto.StatusMetierCountDTO;
import com.example.demo.enmus.StatutDemande;
import com.example.demo.entities.AppUser;
import com.example.demo.entities.Demande;
import com.example.demo.entities.Realisateur;


@Repository
public interface DemandRepository extends JpaRepository<Demande, Long> {
	/*
	@Query("SELECT d FROM Demande d WHERE d.realisateur.responsable.id = :responsableId AND d.statut IN :statuts")
	List<Demande> findByResponsableIdAndStatutIn(
	    @Param("responsableId") Long responsableId, 
	    @Param("statuts") List<StatutDemande> statuts
	);
	@Query("SELECT d.statut as statut, COUNT(d) as count " +
            "FROM Demande d " +
            "WHERE d.statut IN (:statuses) " +
            "GROUP BY d.statut")
     List<AgentDemandesCountDTO> countDemandesByStatus(
         @Param("statuses") List<StatutDemande> statuses
     );*/
	
	@Query("SELECT d FROM Demande d WHERE d.agent = :agent ")
    List<Demande> findDemandesByAgent(@Param("agent") AppUser agent);
    @Query("SELECT d FROM Demande d WHERE d.realisateur = :realisateur")
    List<Demande> findDemandesByRealisateur(@Param("realisateur") Realisateur realisateur);
    
    @Query("SELECT d.realisateur FROM Demande d WHERE d.id = :demandeId")
    Optional<Realisateur> findRealisateurByDemandeId(@Param("demandeId") Long demandeId);
    /*
    @Query(value = "SELECT t.name, COUNT(d.id) FROM demande d " +
 	       "JOIN type t ON t.id = d.type_id " +
 	       "WHERE (:statuses IS NULL OR d.statut IN (:statuses)) "+
 	       "AND (:startDateStr IS NULL OR d.date_creation >= TO_DATE(:startDateStr, 'YYYY-MM-DD')) " +
 	       "AND (:endDateStr IS NULL OR d.date_creation <= TO_DATE(:endDateStr, 'YYYY-MM-DD')) " +
 	       "GROUP BY t.name", nativeQuery = true)
 	List<Object[]> countDemandesByTypeAndStatusAndDateRange(
 	    @Param("statuses") List<String> statuses,
 	    @Param("startDateStr") String startDateStr,
 	    @Param("endDateStr") String endDateStr);
 	
 	


 @Query(value = "SELECT r.metier, COUNT(d.id) FROM demande d " +
         "JOIN realisateur r ON r.id = d.realisateur_id " +
         "WHERE (:statuses IS NULL OR d.statut IN (:statuses)) " +
         "AND (:startDate IS NULL OR d.date_creation >= TO_DATE(:startDate, 'YYYY-MM-DD')) " +
         "AND (:endDate IS NULL OR d.date_creation <= TO_DATE(:endDate, 'YYYY-MM-DD')) " +
         "GROUP BY r.metier", nativeQuery = true)
  List<Object[]> countByMetierStatusAndDateRange(
      @Param("statuses") List<String> statuses,
      @Param("startDate") String startDate,
      @Param("endDate") String endDate);
  */
  
  
  @Query("SELECT d FROM Demande d " +
          "LEFT JOIN FETCH d.dependentDemandes " +
          "WHERE d.realisateur.id = :realisateurId " +
          "AND d.statut IN :statuts")
   List<Demande> findByRealisateurIdAndStatutIn2(
       @Param("realisateurId") Long realisateurId, 
       @Param("statuts") List<StatutDemande> statuts
   );
  
     
     @Query("SELECT d FROM Demande d " +
             "WHERE d.demandeParent.id = :parentId " +
             "AND d.dateDependence IS NOT NULL")
      List<Demande> findDependenciesWithDateForParent(@Param("parentId") Long parentId);
     
     
     
 	
	@Query("SELECT d FROM Demande d WHERE d.statut IN :statuts")
	List<Demande> findByStatutIn(@Param("statuts") List<StatutDemande> statuts);

	List<Demande> findByStatut(StatutDemande statut);
 @Query("SELECT d.type.name, COUNT(d) FROM Demande d GROUP BY d.type.name")
 List<Object[]> countDemandesByType();
 

 	
     @Query("SELECT d.statut as statut, r.metier as metier, COUNT(d) as count " +
             "FROM Demande d JOIN d.realisateur r " +
             "GROUP BY d.statut, r.metier")
      List<StatusMetierCountDTO> countDemandesByStatusAndMetier();
 	
 	
     
     
     @Query("SELECT d FROM Demande d JOIN Employee e ON d.agent = e.agent WHERE e.chef.id = :chefId")
     List<Demande> findTeamDemandesByChefId(@Param("chefId") Long chefId);

     @Query("SELECT d FROM Demande d JOIN Employee e ON d.agent = e.agent WHERE e.chef.id = :chefId AND d.statut IN :statuses")
     List<Demande> findTeamDemandesByChefIdAndStatuses(
         @Param("chefId") Long chefId,
         @Param("statuses") List<StatutDemande> statuses);
     List<Demande> findByAgentIdAndStatutIn(Long agentId, List<StatutDemande> statuts);

     List<Demande> findByAgentChefId(Long chefId);

     List<Demande> findByAgentChefIdAndStatut(Long chefId, StatutDemande statut);

     @Query("SELECT d FROM Demande d WHERE d.agent.chef.id = :chefId AND d.statut IN :statusList")
     List<Demande> findByAgentChefIdAndStatutIn(@Param("chefId") Long chefId, @Param("statusList") List<StatutDemande> statusList);
     @Query("SELECT d FROM Demande d JOIN Employee e ON d.agent.id = e.agent.id WHERE d.statut = :statut AND e.chef.id = :chefId")
     List<Demande> findByStatutAndEmployeeChefId(@Param("statut") StatutDemande statut, @Param("chefId") Long chefId);

     List<Demande> findByStatutIn(Collection<StatutDemande> statuts);
     
     List<Demande> findByStatutAndDateAffectationBefore(StatutDemande statut, LocalDate date);
     
     /*
     @Query("SELECT DISTINCT d FROM Demande d " +
             "LEFT JOIN FETCH d.dependentDemandes " + 
    		 "WHERE d.statut IN :statuts")
      List<Demande> findForGantt(@Param("statuts") List<StatutDemande> statuts);*/
     
     @Query("SELECT d FROM Demande d WHERE d.realisateur.id = :realisateurId AND d.statut IN :statuts")
     List<Demande> findByRealisateurIdAndStatutIn(
         @Param("realisateurId") Long realisateurId, 
         @Param("statuts") List<StatutDemande> statuts
     );
      
     @Query("SELECT d.statut as statut, t.name as metier, COUNT(d) as count " +
             "FROM Demande d JOIN d.type t " +
             "GROUP BY d.statut, t.name")
      List<StatusMetierCountDTO> countDemandesByStatusAndType();
     
     @Query("SELECT d.statut as statut, COUNT(d) as count " +
             "FROM Demande d " +
             "WHERE d.agent.id = :agentId " +
             "AND d.statut IN (:statuses) " +
             "GROUP BY d.statut")
      List<AgentDemandesCountDTO> countDemandesByStatusForAgent(
          @Param("agentId") Long agentId,
          @Param("statuses") List<StatutDemande> statuses
      );
     
     
     
     //ADDED
     @Query("""
   	      SELECT d
   	      FROM Demande d
   	      WHERE d.statut    IN :statuts
   	        AND d.agent.departement.id = (
   	            SELECT u.departement.id
   	            FROM AppUser u
   	            WHERE u.id = :responsableId
   	        )
   	      """)
   	    List<Demande> findByStatutInAndResponsableDepartement(
   	      @Param("statuts") List<StatutDemande> statuts,
   	      @Param("responsableId") Long responsableId
   	    );
     
     @Query(value = """
 	        SELECT 
 	          r.metier          AS metier, 
 	          COUNT(d.id)       AS cnt
 	        FROM demande d
 	        JOIN realisateur r 
 	          ON r.id = d.realisateur_id
 	        JOIN agent a
 	          ON a.id = d.agent_id
 	        WHERE (:statuses    IS NULL OR d.statut IN (:statuses))
 	          AND (:startDate   IS NULL OR d.date_creation >= TO_DATE(:startDate, 'YYYY-MM-DD'))
 	          AND (:endDate     IS NULL OR d.date_creation <= TO_DATE(:endDate,   'YYYY-MM-DD'))
 	          -- only demandes whose agent.departement matches the responsable’s
 	          AND a.departement_id = (
 	              SELECT departement_id 
 	              FROM agent 
 	              WHERE id = :responsableId
 	          )
 	        GROUP BY r.metier
 	        """, nativeQuery = true)
 	    List<Object[]> countByMetierStatusDateRangeAndResponsableDept(
 	        @Param("statuses")      List<String> statuses,
 	        @Param("startDate")     String startDate,
 	        @Param("endDate")       String endDate,
 	        @Param("responsableId") Long responsableId
 	    );
 	    
 	   @Query(value = """
	    	    SELECT 
	    	        t.name, COUNT(d.id)
	    	    FROM demande d
	    	    JOIN type t ON t.id = d.type_id
	    	    JOIN agent a ON a.id = d.agent_id
	    	    WHERE (:statuses IS NULL OR d.statut IN (:statuses))
	    	      AND (:startDateStr IS NULL OR d.date_creation >= TO_DATE(:startDateStr, 'YYYY-MM-DD'))
	    	      AND (:endDateStr   IS NULL OR d.date_creation <= TO_DATE(:endDateStr, 'YYYY-MM-DD'))
	    	      AND a.departement_id = (
	    	          SELECT departement_id
	    	          FROM agent
	    	          WHERE id = :responsableId
	    	      )
	    	    GROUP BY t.name
	    	    """, nativeQuery = true)
	    	List<Object[]> countDemandesByTypeStatusDateRangeAndResponsableDepartement(
	    	    @Param("statuses")      List<String> statuses,
	    	    @Param("startDateStr")  String startDateStr,
	    	    @Param("endDateStr")    String endDateStr,
	    	    @Param("responsableId") Long responsableId
	    	);
	    	
	    	
	    	//gantt
	    	
	    	@Query("""
	    		    SELECT DISTINCT d FROM Demande d
	    		    LEFT JOIN FETCH d.dependentDemandes
	    		    JOIN d.agent a
	    		    WHERE d.statut IN :statuts
	    		      AND a.departement.id = (
	    		          SELECT u.departement.id FROM AppUser u WHERE u.id = :responsableId
	    		      )
	    		""")
	    		List<Demande> findForGanttInResponsableDepartement(
	    		    @Param("statuts") List<StatutDemande> statuts,
	    		    @Param("responsableId") Long responsableId
	    		);
     
	    	
	    	//card
	    	 @Query("""
	    			    SELECT d.statut     AS statut,
	    			           COUNT(d)     AS count
	    			    FROM   Demande d
	    			    WHERE  d.statut    IN :statuses
	    			      AND  d.agent.departement.id = (
	    			             SELECT u.departement.id
	    			             FROM   AppUser u
	    			             WHERE  u.id = :responsableId
	    			           )
	    			    GROUP  BY d.statut
	    			    """)
	    			  List<AgentDemandesCountDTO> countByStatusAndResponsableDepartment(
	    			      @Param("statuses")      List<StatutDemande> statuses,
	    			      @Param("responsableId") Long responsableId
	    			  );
}
