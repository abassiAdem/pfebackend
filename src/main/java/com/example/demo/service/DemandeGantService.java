package com.example.demo.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.demo.dto.DemandeGanttDTO;
import com.example.demo.dto.StatusMetierCountDTO;
import com.example.demo.enmus.StatutDemande;
import com.example.demo.entities.Demande;
import com.example.demo.entities.Realisateur;
import com.example.demo.repository.DemandRepository;
import com.example.demo.repository.RealisateurRepository;
@Service
public class DemandeGantService {

	
	private final DemandRepository demandeRepository;
	private final RealisateurRepository realisateurRepository;
	public DemandeGantService(DemandRepository demandeRepository,RealisateurRepository realisateurRepository) {
		this.demandeRepository=demandeRepository;
		this.realisateurRepository=realisateurRepository;
	}
	
	
	

    
    
    public Map<String, List<Map<String, Object>>> getDemandesByStatusAndType() {
        List<StatusMetierCountDTO> rawData = demandeRepository.countDemandesByStatusAndType();


        Map<String, List<Map<String, Object>>> result = new TreeMap<>();

        for (StatusMetierCountDTO entry : rawData) {
            String statusKey = entry.getStatut().name();
            Map<String, Object> dataItem = new HashMap<>();
            dataItem.put("type", entry.getMetier());
            dataItem.put("count", entry.getCount());

            result.computeIfAbsent(statusKey, k -> new ArrayList<>()).add(dataItem);
        }
        return result;
    }
 
    
    
    public  Map<String, List<Map<String, Object>>> getDemandesByStatusAndMetier() {
        List<StatusMetierCountDTO> rawData = demandeRepository.countDemandesByStatusAndMetier();

        Map<String, List<Map<String, Object>>> result = new TreeMap<>();

        for (StatusMetierCountDTO entry : rawData) {
            String statusKey = entry.getStatut().name();
            Map<String, Object> dataItem = new HashMap<>();
            dataItem.put("metier", entry.getMetier());
            dataItem.put("count", entry.getCount());

            result.computeIfAbsent(statusKey, k -> new ArrayList<>()).add(dataItem);
        }
        return result;
    }
    
    
	
	
	
	
	
public Map<Long, List<DemandeGanttDTO>> getGanttData(Long responsableId) {
        // Get all demandes with relevant statuses
        List<Demande> demandes = demandeRepository.findForGanttInResponsableDepartement(
            List.of(StatutDemande.AFFECTEE, StatutDemande.EN_COURS, 
                    StatutDemande.EN_ATTENTE_DE_DEPENDENCE, StatutDemande.TERMINEE),responsableId
        );
        
        // Process demandes and group by realisateur ID
        Map<Long, List<DemandeGanttDTO>> demandesByRealisateur = demandes.stream()
            .filter(d -> d.getRealisateur() != null)
            .flatMap(d -> processDemande(d).stream())
            .collect(Collectors.groupingBy(
                DemandeGanttDTO::getRealisateurId, 
                HashMap::new,
                Collectors.toList()
            ));
        
        // Get all realisateurs
        List<Realisateur> allRealisateurs = realisateurRepository.findAllByResponsableDepartement(responsableId);
        
        // Create result map with all realisateurs (including those without demandes)
        Map<Long, List<DemandeGanttDTO>> result = new HashMap<>();
        for (Realisateur realisateur : allRealisateurs) {
            Long realisateurId = realisateur.getId();
            
            // Get demandes for this realisateur or create a placeholder if none
            List<DemandeGanttDTO> realisateurDemandes = demandesByRealisateur.getOrDefault(
                realisateurId, new ArrayList<>());
                
            // If there are no demandes for this realisateur, add a placeholder entry
            // that contains the realisateur's information but is not displayed on the chart
            if (realisateurDemandes.isEmpty()) {
                DemandeGanttDTO placeholderDTO = new DemandeGanttDTO();
                placeholderDTO.setId("placeholder-" + realisateurId);
                placeholderDTO.setRealisateurId(realisateurId);
                placeholderDTO.setRealisateurName(
                    realisateur.getAgent().getFirstName() + " " + 
                    realisateur.getAgent().getLastName()
                );
                placeholderDTO.setMetier(realisateur.getMetier());
                // Set other fields to null or appropriate default values
                // This placeholder won't be rendered in the Gantt chart but ensures
                // the realisateur's name is included in the data
                
                realisateurDemandes = List.of(placeholderDTO);
            }
            
            result.put(realisateurId, realisateurDemandes);
        }
        
        return result;
    }
       private List<DemandeGanttDTO> processDemande(Demande d) {
    	   List<Demande> dependenciesWithDate = demandeRepository.findDependenciesWithDateForParent(d.getId());

    	// Check if the demande has the appropriate status and at least one valid dependent demande
    	if (dependenciesWithDate != null && !dependenciesWithDate.isEmpty()) {
    	    return splitIntoPhases(d);
    	}
       else {
               return Collections.singletonList(createStandardTask(d));
           }
       }
       private List<Long> getDependencyIds(Demande d) {

           return d.getDependentDemandes().stream()
               .map(Demande::getId)
               .collect(Collectors.toList());
       }

       private DemandeGanttDTO createStandardTask(Demande d) {
     	    DemandeGanttDTO dto = new DemandeGanttDTO();
     	    dto.setId(d.getId().toString());
     	    dto.setTitle(d.getTitle());
     	    dto.setStatut(d.getStatut());
     	    dto.setStartDate(calculateStartDate(d));
     	    dto.setEndDate(calculateEndDate(d));
     	    dto.setColor(getColorByStatut(d.getStatut()));
     	    dto.setDependencies(getDependencyIds(d));
     	    
     	    dto.setRealisateurId(d.getRealisateur().getId());
     	    dto.setRealisateurName(
     	        d.getRealisateur().getAgent().getFirstName() + " " + 
     	        d.getRealisateur().getAgent().getLastName()
     	    );
     	    dto.setMetier(d.getRealisateur().getMetier());
     	    return dto;
     	}

       private List<DemandeGanttDTO> splitIntoPhases(Demande d) {
     	    List<DemandeGanttDTO> phases = new ArrayList<>();
     	    
     	    List<Demande> dependencies = demandeRepository.findDependenciesWithDateForParent(d.getId());
     	    Demande firstDependence = dependencies.stream()
     	    	    .filter(dep -> dep.getDateDependence() != null)
     	    	    .min(Comparator.comparing(Demande::getDateDependence))
     	    	    .get();
     	    
     	   Demande lastDependence = dependencies.stream()
       	        .filter(dep -> dep.getDateDependence() != null)
       	        .max(Comparator.comparing(Demande::getDateEstimee))
       	        .get();
     	    
     	    if (!dependencies.isEmpty()) {

     	    	
     	        DemandeGanttDTO waitingPhase = new DemandeGanttDTO();
     	        configurePhase(waitingPhase, d, "-WAIT", " (En Attente)", 
     	                      d.getDateEnCours() != null ? d.getDateEnCours() : d.getDateAffectation(),
     	                      firstDependence.getDateDependence(), 
     	                      "#FFA500");
     	        phases.add(waitingPhase);

     	       String color="";
    	    	if (d.getStatut() == StatutDemande.TERMINEE) {
    	    		color="#9E9E9E";
    	    	}else {
    	    		color="#2196F3";
    	    	}
     	        // Phase de travail
     	        LocalDate start = lastDependence.getDateTerminee() != null 
     	                            ? lastDependence.getDateTerminee() 
     	                            : lastDependence.getDateEstimee();
     	        DemandeGanttDTO workPhase = new DemandeGanttDTO();
     	        configurePhase(workPhase, d, "-WORK", " (Travail)", 
     	                      start.plusDays(1),
     	                      calculateEndDate(d), 
     	                      color);
     	        phases.add(workPhase);
     	    }
     	    
     	    return phases;
     	}

       private void configurePhase(DemandeGanttDTO dto, Demande d, String suffix, String titleSuffix,
                                  LocalDate start, LocalDate end, String color) {
           dto.setId(d.getId() + suffix);
           dto.setTitle(d.getTitle() + titleSuffix);
           dto.setStatut(d.getStatut());
           dto.setStartDate(start);
           dto.setEndDate(end);
           dto.setColor(color);
           dto.setDependencies(getDependencyIds(d));
           dto.setRealisateurId(d.getRealisateur().getId());
   	    dto.setRealisateurName(
   	        d.getRealisateur().getAgent().getFirstName() + " " + 
   	        d.getRealisateur().getAgent().getLastName()
   	       
   	    );
   	    
   	 dto.setMetier(d.getRealisateur().getMetier());
       }


       private LocalDate calculateEndDate(Demande d) {
           return switch (d.getStatut()) {
               case TERMINEE -> d.getDateTerminee();
               case EN_COURS -> d.getDateEstimee();
               case AFFECTEE -> (d.getDateEstimee() != null) 
                                ? d.getDateEstimee() 
                                : d.getDateAffectation().plusDays(d.getDureeEstimee());
               case EN_ATTENTE_DE_DEPENDENCE -> d.getDateEstimee() != null 
                                ? d.getDateEstimee()
                                : d.getDateCreation().plusDays(d.getDureeEstimee());
               default -> throw new IllegalStateException("Statut non géré: " + d.getStatut());
           };
       }

       private String getColorByStatut(StatutDemande statut) {
           return switch (statut) {
               case AFFECTEE -> "#4CAF50";
               case EN_COURS -> "#2196F3";
               case TERMINEE -> "#9E9E9E";
               case EN_ATTENTE_DE_DEPENDENCE -> "#FF9800";
               default -> "#000000";
           };
       }
       private LocalDate calculateStartDate(Demande d) {
           return switch (d.getStatut()) {
               case EN_COURS -> d.getDateEnCours();
               case TERMINEE -> d.getDateEnCours();
               case AFFECTEE -> (d.getDateAffectation() != null) ? d.getDateAffectation() : d.getDateCreation();
               case EN_ATTENTE_DE_DEPENDENCE -> d.getDateDependence() != null ? d.getDateDependence() : d.getDateCreation();
               default -> throw new IllegalStateException("Statut non géré: " + d.getStatut());
           };
       }
       
}



   

