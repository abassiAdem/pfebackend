package com.example.demo.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.AgentDemandesCountDTO;
import com.example.demo.dto.DemandeDTO;

import com.example.demo.dto.DemandeResponseDTO;
import com.example.demo.dto.NotificationDto;
import com.example.demo.dto.RealisateurDTO;
import com.example.demo.dto.ResponseDem;
import com.example.demo.dto.StatusMetierCountDTO;
import com.example.demo.enmus.NotificationStatus;
import com.example.demo.enmus.NotificationType;
import com.example.demo.enmus.StatutDemande;
import com.example.demo.entities.AppUser;
import com.example.demo.entities.Demande;
import com.example.demo.entities.Notification;
import com.example.demo.entities.Realisateur;
import com.example.demo.entities.ResponsableFonctionnel;
import com.example.demo.entities.Type;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.AgentRepository;
import com.example.demo.repository.DemandRepository;
import com.example.demo.repository.NotificatonRepository;
import com.example.demo.repository.RealisateurRepository;
import com.example.demo.repository.ResponsableRepository;
import com.example.demo.repository.TypeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class DemandService {
	private final RealisateurRepository realisateurRepository;
	private final TypeRepository typeRepository;
    private final DemandRepository demandeRepository;
    private final AgentRepository agentRepository;  
    private final NotificatonRepository notificationRepository ;
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;
    private NotificationService nts;
    private final ResponsableRepository responsableRepository;
       @Autowired
       public DemandService(RealisateurRepository realisateurRepository,TypeRepository typeRepository,
    		   DemandRepository demandeRepository,
               AgentRepository agentRepository,NotificatonRepository notificationRepository,ResponsableRepository responsableRepository
              ,SimpMessagingTemplate messagingTemplate ,ObjectMapper objjectMapper,NotificationService nts) {
    	   this.realisateurRepository=realisateurRepository;
           this.typeRepository = typeRepository;
           this.demandeRepository = demandeRepository;
           this.agentRepository = agentRepository;
           this.notificationRepository=notificationRepository;
           this.responsableRepository=responsableRepository;
           this.messagingTemplate=messagingTemplate;
           this.objectMapper = objjectMapper;
           this.nts=nts;
       }

       public List<DemandeResponseDTO> getDemandesByResponsableAndStatuts(Long responsableId, List<StatutDemande> statuts) {
    	   List<Demande> demandes = demandeRepository.findByStatutInAndResponsableDepartement(
            	   statuts,
                   responsableId
               );
               
               return demandes.stream()
                             .map(this::mapToResponseDTO)
                             .collect(Collectors.toList());
           }
       
       
       public Map<StatutDemande, Long> getDemandesCountByStatusForAgent(Long agentId) {
           List<StatutDemande> statuses = Arrays.asList(
               StatutDemande.EN_COURS,
               StatutDemande.AFFECTEE,
               StatutDemande.ACCEPTEE,
               StatutDemande.EN_ATTENTE_DE_CHEF,
               StatutDemande.EN_ATTENTE_DE_RESPONSABLE
           );

           List<AgentDemandesCountDTO> rawData = demandeRepository.countDemandesByStatusForAgent(agentId, statuses);
           Map<StatutDemande, Long> result = new HashMap<>();

           for (AgentDemandesCountDTO entry : rawData) {
               result.put(entry.getStatut(), entry.getCount());
           }

           return result;
       }


       public Map<StatutDemande, Long> getAllDemandesCountByStatus(Long responsableId) {
           List<StatutDemande> statuses = Arrays.asList(
               StatutDemande.EN_COURS,
               StatutDemande.AFFECTEE,
               StatutDemande.ACCEPTEE,
               StatutDemande.EN_ATTENTE_DE_CHEF,
               StatutDemande.EN_ATTENTE_DE_DEPENDENCE
           );

           List<AgentDemandesCountDTO> rawData = demandeRepository.countByStatusAndResponsableDepartment(statuses,responsableId);
           Map<StatutDemande, Long> result = new HashMap<>();

           for (AgentDemandesCountDTO entry : rawData) {
               result.put(entry.getStatut(), entry.getCount());
           }

           return result;
       }
       @Transactional
       public void deleteDemandeById(Long id) {
           Demande demande = demandeRepository.findById(id)
                   .orElseThrow(() -> new RuntimeException("Demande not found with id: " + id));
           demande.setStatut(StatutDemande.ANNULEE); 
           demandeRepository.save(demande);
       }
       
       
       
       
       
       
       
       

       private void createNotification(
    		    Demande demande,
    		    AppUser requester,
    		    AppUser responder,
    		    String message,
    		    NotificationType type
    		) {
    		    Notification notification = new Notification();
    		    notification.setDemande(demande);
    		    notification.setRequester(requester);
    		    notification.setResponder(responder);
    		    notification.setType(type);
    		    notification.setMessage(message);
    		    notification.setStatut(NotificationStatus.UNREAD);
    		    notification.setActionable(false);
    		    notificationRepository.save(notification);

    		    NotificationDto notificationDto = NotificationDto.fromEntity(notification);

    		    messagingTemplate.convertAndSend(
    		        "/topic/notifications/" + responder.getId(), 
    		        notificationDto
    		    );

    		   
    		}
       
       
       
       
       @Transactional(readOnly = true)
       public List<Demande> getFinalizedDemandes() {
           List<StatutDemande> statuts = List.of(
               StatutDemande.ANNULEE,
               StatutDemande.REJECTEE,
               StatutDemande.TERMINEE
           );
           List<Demande> result = demandeRepository.findByStatutIn(statuts);
          
           return result;
       }

       @Transactional(readOnly = true)
       public List<Demande> getRecentDemandes() {
           List<StatutDemande> statuts = List.of(
               StatutDemande.EN_ATTENTE_DE_CHEF,
               StatutDemande.EN_ATTENTE_DE_RESPONSABLE, 
               StatutDemande.EN_COURS
           );
           List<Demande> result = demandeRepository.findByStatutIn(statuts);
          
           return result;
       }

       public ResponseEntity<List<Demande>> getDemandesByStatus(StatutDemande status) {
           List<Demande> demandes = demandeRepository.findByStatut(status);
           if (demandes.isEmpty()) {
               return ResponseEntity.noContent().build();
           }
           return ResponseEntity.ok(demandes);
       }
       
       public DemandeResponseDTO createDemande(DemandeDTO dto) {
           Demande demande = new Demande();
           demande.setUrgence(dto.getUrgence());
           demande.setData(dto.getData());
           demande.setJustification(dto.getJustification());
           demande.setDureeEstimee(dto.getDureeEstimee());
           demande.setTitle(dto.getTitle());
           if((dto.getInfoSup()==null) ||(!(dto.getInfoSup().isEmpty()))){
        	   demande.setInfoSup(dto.getInfoSup());}
           Type type = typeRepository.findByName(dto.getType())
                   .orElseThrow(() -> new RuntimeException("type non trouvé"));
           demande.setType(type);
           

           var agent = agentRepository.findById(dto.getAgentId())
                   .orElseThrow(() -> new RuntimeException("Agent non trouvé"));
           demande.setAgent(agent);
           if (type.getId().equals(2L)) {
               demande.setStatut(StatutDemande.AFFECTEE);
           } else if (agent.getRealisateur() != null || agent.getChef() != null) {
               demande.setStatut(StatutDemande.EN_ATTENTE_DE_RESPONSABLE);
           }else if (agent.getResponsable()!= null) {
               demande.setStatut(StatutDemande.ACCEPTEE);
           }else {
               demande.setStatut(StatutDemande.EN_ATTENTE_DE_CHEF);
           }
 
           if (dto.getParentId() == null) {
        	   demande.setDateEstimee(calculateDateWithWeekends(LocalDate.now(), dto.getDureeEstimee()-1));
           } else {

               Demande parent = demandeRepository.findById(dto.getParentId())
                       .orElseThrow(() -> new RuntimeException("Demande parent non trouvée"));
               demande.setDemandeParent(parent);
               parent.getDependentDemandes().add(demande);
               demande.setDateEstimee(calculateDateWithWeekends(LocalDate.now(), dto.getDureeEstimee()-1));
               
               
           }
           
           Demande saved = demandeRepository.save(demande);
           if(agent.getResponsable()==null) {
        	   triggerNotificationsBasedOnStatus(saved, null, null,null);  
           }
           if (type.getId().equals(2L)) {
               try {
                   Map<String, Object> data = dto.getData();
                   if (data == null) {
                       return mapToResponseDTO(saved);
                   }
                   
                   Object dateObj = data.get("dateDebut");
                   if (dateObj == null) {
                       return mapToResponseDTO(saved);
                   }
                   
                   LocalDate dateDebut;
                   if (dateObj instanceof LocalDate) {
                       dateDebut = (LocalDate) dateObj;
                   }  else if (dateObj instanceof String) {
                	    String s = (String) dateObj;
                	    try { 
                	        dateDebut = LocalDate.parse(s, DateTimeFormatter.ISO_LOCAL_DATE);
                	    } catch (DateTimeParseException ex1) {
                	        try {
                	            dateDebut = OffsetDateTime.parse(s, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                	                                       .toLocalDate();
                	        } catch (DateTimeParseException ex2) {
                	            return mapToResponseDTO(saved);
                	        }
                	    }
                	} else {
                       return mapToResponseDTO(saved);
                   }

                   Object dureeObj = data.get("dureeConge");
                   if (dureeObj == null) {
                       return mapToResponseDTO(saved);
                   }
                   
                   Long dureeConge;
                   if (dureeObj instanceof Number) {
                       dureeConge = ((Number) dureeObj).longValue();
                   } else if (dureeObj instanceof String) {
                       try {
                           dureeConge = Long.valueOf((String) dureeObj);
                       } catch (Exception e) {
                           return mapToResponseDTO(saved);
                       }
                   } else {
                       return mapToResponseDTO(saved);
                   }

                   DemandeResponseDTO result = affecterRealisateur(
                       saved.getId(),
                       dto.getAgentId(),
                       dateDebut,
                       dureeConge
                   );
                   
                   return result;
               } catch (Exception e) {
                   e.printStackTrace();
                   return mapToResponseDTO(saved);
               }
           }
           return mapToResponseDTO(saved);
       }

   //          triggerNotificationsBasedOnStatus(saved, null, null,null);    

	public DemandeResponseDTO updateDataDependence(Long dependenceId) {
	    Demande dependence = demandeRepository.findById(dependenceId)
	            .orElseThrow(() -> new RuntimeException("Dépendance non trouvée"));
	    LocalDate now = LocalDate.now();
	    dependence.setDateDependence(now);
	    //dependence.setDepend(true);
	    
	    demandeRepository.save(dependence);
	    //du date creation => need time for dependence
	
	    //long joursEcoules = ChronoUnit.DAYS.between(dependence.getDateCreation(), now);
	    //long delta = dependence.getDureeDependence() - joursEcoules;
	    //if (dependence.getDemandeParent() != null) {
	        Demande parent = dependence.getDemandeParent();
	        recalcEstimatedDate(parent);
	        parent.setStatut(StatutDemande.EN_ATTENTE_DE_DEPENDENCE);
	        propagateRecalcEstimatedDate(parent.getDemandeParent());
	        demandeRepository.save(parent);
	
	    return mapToResponseDTO(dependence);
	    }
       
public DemandeResponseDTO adjustDateEstimee(Long demandeId, LocalDate newEstimated) {
           Demande demande = demandeRepository.findById(demandeId)
                   .orElseThrow(() -> new RuntimeException("Demande non trouvée"));
           // compute signed business‐day delta
        // Compute the signed work-day delta in one go:
           int delta = businessDaysDelta(demande.getDateEstimee(), newEstimated);

           demande.setDureeEstimee(demande.getDureeEstimee() + delta);
           demande.setDateEstimee(newEstimated);
           demandeRepository.save(demande);

           if (demande.getDemandeParent() != null && demande.getDateDependence() != null ) {
               recalcEstimatedDate(demande.getDemandeParent());
               propagateRecalcEstimatedDate(demande.getDemandeParent().getDemandeParent());
           }
           Demande saved = demandeRepository.save(demande);
           return mapToResponseDTO(saved);
       }

       
       public Demande getdemandeForNotification(Long demandeId) {
           Demande demande = demandeRepository.findById(demandeId)
                   .orElseThrow(() -> new RuntimeException("Demande non trouvée"));
		return demande;
       }

       public DemandeResponseDTO getDemande(Long demandeId) {
           Demande demande = demandeRepository.findById(demandeId)
                   .orElseThrow(() -> new RuntimeException("Demande non trouvée"));
           DemandeResponseDTO response = mapToResponseDTO(demande);
           if ("TERMINEE".equals(demande.getStatut())
                   && demande.getDateEnCours() != null
                   && demande.getDateTerminee() != null) {
               long totalDays = ChronoUnit.DAYS.between(demande.getDateEnCours(), demande.getDateTerminee());
               List<Interval> intervals = new ArrayList<>();
               for (Demande dep : demande.getDependentDemandes()) {
                   if (dep.getDateDependence() != null && dep.getDateTerminee() != null) {
                       intervals.add(new Interval(dep.getDateDependence(), dep.getDateTerminee()));
                   }
               }
               long unionDays = countBusinessDaysInIntervals(intervals);
               long dureeTravail = totalDays - unionDays;
               response.setDureeTravailRealisateur(dureeTravail);
               response.setDureeRetardDependence(unionDays);
           }
           return response;
       }

private DemandeResponseDTO mapToResponseDTO(Demande demande) {
           DemandeResponseDTO dto = new DemandeResponseDTO();
           dto.setId(demande.getId());
           dto.setStatut(demande.getStatut());
           dto.setUrgence(demande.getUrgence());
           dto.setDureEstimee(demande.getDureeEstimee());
           dto.setTitle(demande.getTitle());
           //dto.setDepend(demande.getDepend());
           dto.setApprobationAnnulation(demande.getApprobationAnnulation());
           dto.setApprobationModification(demande.getApprobationModification());
           dto.setData(demande.getData());
           dto.setInfoSup(demande.getInfoSup());
           if (demande.getAgent() != null) {
               dto.setDemandeurName(demande.getAgent().getFirstName() + " " + demande.getAgent().getLastName());
           } else {
               dto.setDemandeurName("inconnue");
           }
           if (demande.getRealisateur() != null) {
               dto.setRealisateurName(demande.getRealisateur().getAgent().getFirstName() + " " + demande.getRealisateur().getAgent().getLastName());
           } else {
               dto.setRealisateurName("inconnue");
           }
           dto.setJustification(demande.getJustification());
           dto.setDateCreation(demande.getDateCreation());
           dto.setDateAcceptation(demande.getDateAcceptation());
           dto.setDateEnCours(demande.getDateEnCours());
           dto.setDateTerminee(demande.getDateTerminee());
           dto.setDateEstime(demande.getDateEstimee());
           dto.setDateAffectation(demande.getDateAffectation());
           dto.setDateDependence(demande.getDateDependence());
           List<DemandeResponseDTO> deps = demande.getDependentDemandes().stream()
                   .map(this::mapToResponseDTO)
                   .collect(Collectors.toList());
           dto.setDependentDemandes(deps);
           Type type = typeRepository.findById(demande.getType().getId())
                   .orElseThrow(() -> new RuntimeException("Type non trouvé"));
           dto.setType(type.getName());
           return dto;
       }


private static class Interval {
           private LocalDate start;
           private LocalDate end;

           public Interval(LocalDate start, LocalDate end) {
               this.start = start;
               this.end = end;
           }
           public LocalDate getStart() {
               return start;
           }
           public LocalDate getEnd() {
               return end;
           }
       }

private long countBusinessDaysInIntervals(List<Interval> intervals) {
            if (intervals.isEmpty()) return 0;

            intervals.sort(Comparator.comparing(Interval::getStart));

            LocalDate currentStart = intervals.get(0).getStart();
            LocalDate currentEnd = intervals.get(0).getEnd();
            long totalBusinessDays = 0;

            for (int i = 1; i < intervals.size(); i++) {
                Interval interval = intervals.get(i);
                if (interval.getStart().isAfter(currentEnd)) {
                 
                    totalBusinessDays += calculateWorkdaysBetween(currentStart, currentEnd);
                    currentStart = interval.getStart();
                    currentEnd = interval.getEnd();
                } else {
                    if (interval.getEnd().isAfter(currentEnd)) {
                        currentEnd = interval.getEnd();
                    }
                }
            }

            totalBusinessDays += calculateWorkdaysBetween(currentStart, currentEnd);

            return totalBusinessDays;
        }
public Demande updateStatusCour(Long id, StatutDemande newStatus) {
    Demande demande = demandeRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Demande not found"));
    
    if (StatutDemande.EN_COURS == newStatus){
    	if(demande.getDateEnCours() == null){
        demande.setDateEstimee(calculateDateWithWeekends(LocalDate.now(), demande.getDureeEstimee()-1));
        System.out.print("hello");
    	}
        if (demande.getDemandeParent() != null && demande.getDateDependence() != null ) {
            recalcEstimatedDate(demande.getDemandeParent());
            propagateRecalcEstimatedDate(demande.getDemandeParent().getDemandeParent());
        }
       
    }
    StatutDemande previousStatus = demande.getStatut();
    demande.setStatut(newStatus);
    Demande savedDemande=demandeRepository.save(demande);
    triggerNotificationsBasedOnStatus(savedDemande, previousStatus,"",null);
    return savedDemande;
}       


public Demande updateStatusByAgent(Long id, StatutDemande newStatus,Long idr) {
    Demande demande = demandeRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Demande not found"));
    demande.setStatut(newStatus);
    if (StatutDemande.EN_COURS == newStatus){
        demande.setDateEstimee(calculateDateWithWeekends(LocalDate.now(), demande.getDureeEstimee()-1));
        if (demande.getDemandeParent() != null && demande.getDateDependence() != null ) {
            recalcEstimatedDate(demande.getDemandeParent());
            propagateRecalcEstimatedDate(demande.getDemandeParent().getDemandeParent());
        }
    }
    StatutDemande previousStatus = demande.getStatut();
    Demande savedDemande = demandeRepository.save(demande);
    Long ida=null;
    if(idr!=null) {
    	ida=idr;
    }
    triggerNotificationsBasedOnStatus(savedDemande, previousStatus,"",ida);
    return savedDemande;
}

       public Demande updateStatus(Long id, StatutDemande newStatus) {
           Demande demande = demandeRepository.findById(id)
                   .orElseThrow(() -> new RuntimeException("Demande not found"));
           ;
           if (StatutDemande.EN_COURS == newStatus){
               demande.setDateEstimee(calculateDateWithWeekends(LocalDate.now(), demande.getDureeEstimee()-1));
               if (demande.getDemandeParent() != null && demande.getDateDependence() != null ) {
                   recalcEstimatedDate(demande.getDemandeParent());
                   propagateRecalcEstimatedDate(demande.getDemandeParent().getDemandeParent());
               }
           }
           StatutDemande previousStatus = demande.getStatut();
           demande.setStatut(newStatus);
           Demande savedDemande = demandeRepository.save(demande);
           triggerNotificationsBasedOnStatus(savedDemande, previousStatus,"",null);
           return savedDemande;
       }
       private void triggerNotificationsBasedOnStatus(Demande demande, StatutDemande previousStatus, String motifRejet,Long idr) {
           StatutDemande newStatus = demande.getStatut();
           
           /*if (previousStatus == newStatus) {
               return;
           }*/
           System.out.println("im in triggerNotificationsBasedOnStatus ");
           switch (newStatus) {
               case EN_ATTENTE_DE_CHEF:
            	   nts.handleDemandeEnAttenteDuChef(demande,"Une nouvelle demande requiert votre validation. Demande: ");
                   break;
               case EN_ATTENTE_DE_RESPONSABLE:
            	   nts.handleDemandeEnAttenteDeResponsable(demande,"Une nouvelle demande requiert votre validation en tant que responsable. " );
                   break;
               case REJECTEE:
            	   nts.handleDemandeRejected(demande, motifRejet);
                   break;
               case ACCEPTEE:
            	   nts.handleDemandeAccepte(demande, motifRejet,idr);
                   break;
               case EN_COURS:
                   nts.handleDemandeEnCour(demande);
                   break;
               case TERMINEE:
                   nts.handleDemandeTerminee(demande);
               default:
                  
                   break;
           }
       }
       
       private void recalcEstimatedDate(Demande parent) {
           if (parent == null) return;
           List<Interval> intervals = new ArrayList<>();
           for (Demande dep : parent.getDependentDemandes()) {
               if (dep.getDateDependence() != null && dep.getDateEstimee() != null) {
                   LocalDate end = (dep.getDateTerminee() != null)
                           ? dep.getDateTerminee()
                           : dep.getDateEstimee();
                   
                   intervals.add(new Interval(dep.getDateDependence(), end));
                   
               }
           }
           long unionDays = countBusinessDaysInIntervals(intervals);
           parent.setDateEstimee(calculateDateWithWeekends(parent.getDateEnCours(), parent.getDureeEstimee()+unionDays -1));
           //LocalDate newEstimated = base.plusDays(unionDays);
           //parent.setDateEstimee(newEstimated);
           //demandeRepository.save(parent);
       }

       private void propagateRecalcEstimatedDate(Demande parent) {
           if (parent != null) {
               recalcEstimatedDate(parent);
               propagateRecalcEstimatedDate(parent.getDemandeParent());
           }
       }
       
		@Transactional
		public DemandeResponseDTO updateDemande(Long demandeId, DemandeDTO demandeDTO) {
		    Demande demande = demandeRepository.findById(demandeId)
		            .orElseThrow(() -> new RuntimeException("Demande non trouvée"));
		    StatutDemande previousStatus=demande.getStatut();
		    if(previousStatus == StatutDemande.EN_ATTENTE_DE_DEPENDENCE || 
		    		previousStatus == StatutDemande.EN_COURS || previousStatus == StatutDemande.AFFECTEE|| 
		    				previousStatus == StatutDemande.ACCEPTEE) {
		        
		        ///sendUpdateNotifications(demande, demandeId);
		    }
		    demande.setTitle(demandeDTO.getTitle());
		    demande.setUrgence(demandeDTO.getUrgence());
		    demande.setJustification(demandeDTO.getJustification());
		    
		   demande.setInfoSup(demandeDTO.getInfoSup());
		    
		    Map<String, Object> existingData = demande.getData();
		    if (existingData == null) {
		        existingData = new HashMap<>();
		    }
		    if (demandeDTO.getData() != null) {
		        existingData.putAll(demandeDTO.getData());
		    }
		    demande.setData(existingData);
		    
		    if (demandeDTO.getType() != null && !demandeDTO.getType().equals(demande.getType().getName())) {
		        Type type = typeRepository.findByName(demandeDTO.getType())
		                .orElseThrow(() -> new RuntimeException("Type non trouvé"));
		        demande.setType(type);
		        demande.setDureeEstimee(type.getDureeEstimee());
		    }
		    if (demandeDTO.getParentId() != null && 
		        (demande.getDemandeParent() == null || 
		         !demandeDTO.getParentId().equals(demande.getDemandeParent().getId()))) {
		        
		        updateParentDemande(demande, demandeDTO);
		    } 
		    if (demandeDTO.getDureeEstimee() != null && 
		        !demandeDTO.getDureeEstimee().equals(demande.getDureeEstimee())) {
		        
		        demande.setDureeEstimee(demandeDTO.getDureeEstimee());
		        demande.setDateEstimee(LocalDate.now().plusDays(demandeDTO.getDureeEstimee()));
		        
		        /*if (demande.getDemandeParent() != null) {
		            recalcEstimatedDate(demande.getDemandeParent());
		        }*/
		    }
		    AppUser user=demande.getAgent();
		    if(user.getRole().equals("employee")) {
		    if(demande.getStatut()==StatutDemande.EN_ATTENTE_DE_RESPONSABLE || demande.getStatut()==StatutDemande.ACCEPTEE || demande.getStatut()==StatutDemande.AFFECTEE ) {
		    	 demande.setStatut(StatutDemande.EN_ATTENTE_DE_CHEF);
		    	 nts.handleDemandeEnAttenteDuChef(demande,  "Une  demande modifiée requiert votre validation en tant que chef.");
		    }
		    }else {
		    	demande.setStatut(StatutDemande.EN_ATTENTE_DE_RESPONSABLE);
		    	 nts.handleDemandeEnAttenteDeResponsable(demande,  "Une  demande modifiée requiert votre validation en tant que chef.");
		    }
		    
		   
		    demande.setApprobationModification(false);
		    
		    Demande updated = demandeRepository.save(demande);
		 
		    return mapToResponseDTO(updated);
		}
		
		private void sendUpdateNotifications(Demande demande, Long demandeId) {
		    if (demande.getRealisateur() != null) {
		        createNotification(
		            demande,
		            demande.getAgent(), 
		            demande.getRealisateur().getAgent(),
		            "Demande REQ---" + demandeId + " a été modifiée",
		            NotificationType.DEMANDE_MODIFICATION
		        );
		    }
		
		    if (demande.getRealisateur() != null && demande.getRealisateur().getResponsable() != null) {
		        ResponsableFonctionnel responsable = demande.getRealisateur().getResponsable();
		        createNotification(
		            demande,
		            demande.getAgent(), 
		            responsable.getAgent(),
		            "Demande REQ--" + demandeId + " a été modifiée",
		            NotificationType.DEMANDE_MODIFICATION
		        );
		    }
		}

private void updateParentDemande(Demande demande, DemandeDTO demandeDTO) {
    Demande newParent = demandeRepository.findById(demandeDTO.getParentId())
            .orElseThrow(() -> new RuntimeException("Demande parent non trouvée"));
    
    if (demande.getDemandeParent() != null) {
        demande.getDemandeParent().getDependentDemandes().remove(demande);
    }
    demande.setDemandeParent(newParent);
    newParent.getDependentDemandes().add(demande);
    demande.setIsAttached(true);

   
}
 
       
      public List<DemandeResponseDTO> getAllDemandesForChef(Long chefId) {
           List<Demande> demandes = demandeRepository.findByStatutAndEmployeeChefId(StatutDemande.EN_ATTENTE_DE_CHEF, chefId);
           return demandes.stream().map(this::mapToResponseDTO).collect(Collectors.toList());
       }


       public List<DemandeResponseDTO> getDemandesFinaliseesByAgent(Long agentId) {
     	  List<Demande> demandes= demandeRepository.findByAgentIdAndStatutIn(agentId, 
     			 Arrays.asList(StatutDemande.TERMINEE,StatutDemande.ANNULEE,StatutDemande.REJECTEE));

           return demandes.stream().map(this::mapToResponseDTO).collect(Collectors.toList());
       }
       
       
       public List<DemandeResponseDTO> getAllDemandesForAgent(Long agentId) {
           List<Demande> demandes= demandeRepository.findByAgentIdAndStatutIn(agentId, 
                   Arrays.asList(StatutDemande.AFFECTEE,StatutDemande.EN_COURS,StatutDemande.ACCEPTEE,StatutDemande.EN_ATTENTE_DE_CHEF,StatutDemande.EN_ATTENTE_DE_RESPONSABLE,StatutDemande.EN_ATTENTE_DE_DEPENDENCE));

           return demandes.stream().map(this::mapToResponseDTO).collect(Collectors.toList());
       }
       
      
       
       public List<DemandeResponseDTO> getDemandesRealisateur(Long realisateurId) {
   	    List<Demande> demandes = demandeRepository.findByRealisateurIdAndStatutIn(
   	        realisateurId, 
   	        Arrays.asList(StatutDemande.AFFECTEE,StatutDemande.EN_ATTENTE_DE_DEPENDENCE,StatutDemande.EN_COURS, StatutDemande.TERMINEE, StatutDemande.ANNULEE)
   	    );
   	    return demandes.stream().map(this::mapToResponseDTO).collect(Collectors.toList());
   	}
       
       public List<DemandeResponseDTO> getAllDemandesEnAttenteResponsable(Long responsableId) {
    	   List<StatutDemande> statuts = List.of(
    		        StatutDemande.ACCEPTEE,
    		        StatutDemande.EN_ATTENTE_DE_RESPONSABLE
    		    );

    		    List<Demande> demandes = demandeRepository
    		        .findByStatutInAndResponsableDepartement(statuts, responsableId);

    		    return demandes.stream()
    		                   .map(this::mapToResponseDTO)
    		                   .collect(Collectors.toList());
       }
       public List<DemandeResponseDTO> getDemandesFinalisees(Long responsableId) {
           List<StatutDemande> statuts = Arrays.asList(StatutDemande.TERMINEE,StatutDemande.ANNULEE,StatutDemande.REJECTEE);
           List<Demande> demandes = demandeRepository.findByStatutInAndResponsableDepartement(statuts,responsableId);
           return demandes.stream().map(this::mapToResponseDTO).collect(Collectors.toList());
       }

      public List<DemandeResponseDTO> getDemandesRealisateur() {
          List<StatutDemande> statuts = Arrays.asList(StatutDemande.TERMINEE,StatutDemande.ANNULEE,StatutDemande.EN_COURS);
          List<Demande> demandes = demandeRepository.findByStatutIn(statuts);
          return demandes.stream().map(this::mapToResponseDTO).collect(Collectors.toList());
      }

      public DemandeResponseDTO completeDemande(Long demandeId) {
          Demande demande = demandeRepository.findById(demandeId)
                  .orElseThrow(() -> new RuntimeException("Demande non trouvée"));
          demande.setStatut(StatutDemande.TERMINEE);
          demande.setDateTerminee(LocalDate.now());
          Demande saved = demandeRepository.save(demande);

          triggerNotificationsBasedOnStatus(saved,StatutDemande.EN_COURS,"",null);
          if (demande.getDemandeParent() != null && demande.getDateDependence() != null) {
              Demande parent = demande.getDemandeParent();
            
                  recalcEstimatedDate(parent);
                  propagateRecalcEstimatedDate(parent.getDemandeParent());
              
              boolean otherDependencesHaveDataDependence = parent.getDependentDemandes().stream()
                      .filter(dep -> !dep.getId().equals(demande.getId()))
                      .anyMatch(dep -> dep.getDateDependence() != null);
              if (!otherDependencesHaveDataDependence) {
            	  String msg="Voulez-vous recommencer le travail de cette demande  ?";
            	  AppUser requester=parent.getRealisateur().getResponsable().getAgent();
            	  AppUser responder=parent.getRealisateur().getAgent()  ;
            	  Notification notification=nts.createBaseNotification(requester,responder,parent,NotificationType.DEMANDE_START,msg,false);
            	  nts.deliverNotification(notification);
            	  
                   notificationRepository.save(notification);
              }
          }
          return mapToResponseDTO(saved);
      }
      
      

      

      private ResponseDem mapToResponseDem(Demande demande) {
          ResponseDem resp = new ResponseDem();
          resp.setId(demande.getId());
          resp.setStatut(demande.getStatut());
          resp.setTitle(demande.getTitle());
          if (demande.getAgent() != null) {
              resp.setDemandeurName(demande.getAgent().getFirstName() + " " + demande.getAgent().getLastName());
          } else {
              resp.setDemandeurName("Unknown");  
              
          }
          //resp.setDepend(demande.getDepend());
          resp.setInfoSup(demande.getInfoSup());
          resp.setApprobationAnnulation(demande.getApprobationAnnulation());
          resp.setApprobationModification(demande.getApprobationModification());
          resp.setUrgence(demande.getUrgence());
          resp.setJustification(demande.getJustification());
          resp.setDateCreation(demande.getDateCreation());
          resp.setDateEnCours(demande.getDateEnCours());
          resp.setTitle(demande.getTitle());

          Type type = typeRepository.findById(demande.getType().getId())
                  .orElseThrow(() -> new RuntimeException("Type non trouvé"));
          resp.setType(type.getName());
  
          return resp;
      }



public DemandeResponseDTO affecterRealisateur(Long demandeId, Long realisateurId, LocalDate dateAffectation
        ,Long dureEstimee) {
      Demande demande = demandeRepository.findById(demandeId)
          .orElseThrow(() -> new RuntimeException("Demande non trouvée"));
      Realisateur realisateur = realisateurRepository.findById(realisateurId)
          .orElseThrow(() -> new RuntimeException("Réalisateur non trouvé"));


      if (dateAffectation == null) {
          throw new IllegalArgumentException("Date d'affectation ne peut pas être nulle");
      }
      if(dureEstimee != null) {
          demande.setDureeEstimee(dureEstimee);
      }

      //cas depend non affecté
      demande.setDateEstimee(calculateDateWithWeekends(dateAffectation, demande.getDureeEstimee()-1));
      if (demande.getDemandeParent() != null && demande.getDateDependence() != null) {
          recalcEstimatedDate(demande.getDemandeParent());
          propagateRecalcEstimatedDate(demande.getDemandeParent().getDemandeParent());
      }
      demande.setRealisateur(realisateur);
      demande.setDateAffectation(dateAffectation);
demande.setStatut(StatutDemande.AFFECTEE);
      demandeRepository.save(demande);
      realisateurRepository.save(realisateur); 
      
      try {
    	  	 System.out.println("realisateur = "+realisateur.getId() );
    	  	 System.out.println("realisateur.getResponsable() "+realisateur.getResponsable().getId());
    	     ResponsableFonctionnel responsableFonctionnel = realisateur.getResponsable();
    	      if (responsableFonctionnel == null) {
    	    	  System.out.println("responsableFonctionnel = "+responsableFonctionnel.getId());
    	    	  
    	    	  throw new IllegalStateException("Le réalisateur n'a pas de responsable fonctionnel associé");
    	      }
    	      
    	      AppUser responsable = responsableFonctionnel.getAgent();
    	      if (responsable == null) {
    	          throw new IllegalStateException("Le responsable fonctionnel n'a pas d'utilisateur associé");
    	      }
    	      nts.sendAffectationNotifications(demande, realisateur, responsable);
      }catch(Exception notificationError) {
    	  
      }

      return mapToResponseDTO(demande);
  }



      public DemandeResponseDTO getDemandeDetaille(Long demandeId) {
          Demande demande = demandeRepository.findById(demandeId)
                  .orElseThrow(() -> new RuntimeException("Demande non trouvÃ©e"));
          DemandeResponseDTO response = mapToResponseDTO(demande);
          long delayCreationAccept = 0;
          long delayAcceptEnCours = 0;
          long unionBusinessDays=0;
          
          
          if (demande.getDateCreation() != null && demande.getDateAcceptation() != null) {
              delayCreationAccept = ChronoUnit.DAYS.between(demande.getDateCreation(), demande.getDateAcceptation());
              response.setDelayCreationAccept(delayCreationAccept);
          }

          
          if (demande.getDateAcceptation() != null && demande.getDateEnCours() != null) {
              delayAcceptEnCours = ChronoUnit.DAYS.between(demande.getDateAcceptation(), demande.getDateEnCours());
              response.setDelayAcceptEnCours(delayAcceptEnCours);
          }           
          if (demande.getStatut() == StatutDemande.TERMINEE
                  && demande.getDateEnCours() != null
                  && demande.getDateTerminee() != null) {
             
              List<Interval> intervals = new ArrayList<>();
              for (Demande dep : demande.getDependentDemandes()) {
                  if (dep.getDateDependence() != null && dep.getDateTerminee() != null) {
                      intervals.add(new Interval(dep.getDateDependence(), dep.getDateTerminee()));
                  }
              } 
              long totalBusinessDays = calculateWorkdaysBetween(demande.getDateEnCours(), demande.getDateTerminee());
               
              unionBusinessDays = countBusinessDaysInIntervals(intervals);
              
              long dureeTravail = totalBusinessDays - Math.max(unionBusinessDays, 0);
              response.setDureeTravailRealisateur(dureeTravail);
              response.setDureeRetardDependence(unionBusinessDays);
              
          }
          long totalMainDelay =  Math.max(delayCreationAccept,0) + Math.max(delayAcceptEnCours, 0) + Math.max(unionBusinessDays, 0);
          response.setTotalMainDelay(totalMainDelay);
          
          
          
          return response;
      }
      
      
      public DemandeResponseDTO affecterRealisateur2(Long demandeId, Long realisateurId, LocalDate dateAffectation,Long dureEstimee) {
          Demande demande = demandeRepository.findById(demandeId)
                  .orElseThrow(() -> new RuntimeException("Demande non trouvée"));
          var realisateur = realisateurRepository.findById(realisateurId)
                  .orElseThrow(() -> new RuntimeException("Réalisateur non trouvé"));
          demande.setDureeEstimee(dureEstimee);
          demande.setRealisateur(realisateur);
          demande.setDateAffectation(dateAffectation);
          demande.setStatut(StatutDemande.AFFECTEE);
          demande.setDateEstimee(dateAffectation.plusDays(

                  demande.getDureeEstimee()));

          if (demande.getDemandeParent() != null) {
              recalcEstimatedDate(demande.getDemandeParent());
              propagateRecalcEstimatedDate(demande.getDemandeParent().getDemandeParent());
          }

          demandeRepository.save(demande);
          return mapToResponseDTO(demande);
      }
      
      public List<DemandeResponseDTO> getDemandesForChefByStatuses(Long chefId, List<StatutDemande> statuses) {
          List<Demande> demandes = demandeRepository.findTeamDemandesByChefIdAndStatuses(chefId, statuses);
          return demandes.stream()
                  .map(this::mapToResponseDTO)
                  .collect(Collectors.toList());
      }
  
      
      public List<DemandeResponseDTO> getAllDemandeTeamsChefId(Long chefId) {
          List<Demande> demandes = demandeRepository.findTeamDemandesByChefId(chefId);
          return demandes.stream()
                  .map(this::mapToResponseDTO)
                  .collect(Collectors.toList());
      }
      
      private LocalDate calculateDateWithWeekends(LocalDate startDate, long durationInBusinessDays) {
          LocalDate result = startDate;
          System.out.println(startDate);
          System.out.println(startDate);
          System.out.println(startDate);
          System.out.println(startDate);
          System.out.println(startDate);
          
          if (result.getDayOfWeek() == DayOfWeek.SATURDAY) {
              result = result.plusDays(2); 
          } else if (result.getDayOfWeek() == DayOfWeek.SUNDAY) {
              result = result.plusDays(1); 
          }
          int addedBusinessDays = 0;

          while (addedBusinessDays < durationInBusinessDays) {
              result = result.plusDays(1);
              if (!(result.getDayOfWeek() == DayOfWeek.SATURDAY || result.getDayOfWeek() == DayOfWeek.SUNDAY)) {
                  addedBusinessDays++;
              }
          }

          return result;
      } 
      public RealisateurDTO getRealisateurWithAvailability(Long realisateurId) {
          Realisateur realisateur = realisateurRepository.findById(realisateurId)
              .orElseThrow(() ->  new RuntimeException("realisateur non trouvée"));
          
          List<Demande> activeDemandes = demandeRepository.findByRealisateurIdAndStatutIn(
              realisateurId,
              List.of(StatutDemande.AFFECTEE, StatutDemande.EN_COURS, StatutDemande.EN_ATTENTE_DE_DEPENDENCE)
          );
          
        
          RealisateurDTO dto = new RealisateurDTO();
          dto.setId(realisateur.getId());
          dto.setFirstName(realisateur.getFirstName() != null ? 
                           realisateur.getFirstName() : 
                           realisateur.getAgent().getFirstName());
          dto.setLastName(realisateur.getLastName() != null ? 
                          realisateur.getLastName() : 
                          realisateur.getAgent().getLastName());
          dto.setEmail(realisateur.getEmail() != null ? 
                       realisateur.getEmail() : 
                       realisateur.getAgent().getEmail());
          dto.setCompetences(realisateur.getCompetences());
          dto.setMetier(realisateur.getMetier());
          boolean hasEnCoursTask = activeDemandes.stream()
        	        .anyMatch(d -> d.getStatut() == StatutDemande.EN_COURS);

        	    dto.setDisponibilites(hasEnCoursTask ? "occupé" : "disponible");
          
          // Calculate occupancy dates and workload
          calculateAvailabilityDates(dto, activeDemandes);
          calculateWorkload(dto, activeDemandes);
          
          return dto;
      }

      public List<RealisateurDTO> getAllRealisateursWithAvailability(Long responsableId) {
          List<Realisateur> realisateurs = realisateurRepository.findAllByResponsableDepartement(responsableId);
          return realisateurs.stream()
              .map(r -> getRealisateurWithAvailability(r.getId()))
              .collect(Collectors.toList());
      }
      
      private void calculateAvailabilityDates(RealisateurDTO dto, List<Demande> activeDemandes) {
          LocalDate today = LocalDate.now();
          
          // Find the first occupied date (earliest start date of active tasks)
          Optional<LocalDate> earliestStartDate = activeDemandes.stream()
              .map(d -> getEffectiveStartDate(d))
              .filter(date -> date != null && !date.isBefore(today))
              .min(LocalDate::compareTo);
              
          // Find the latest end date (to determine when the realisateur will be available again)
          Optional<LocalDate> latestEndDate = activeDemandes.stream()
              .map(d -> getEffectiveEndDate(d))
              .filter(Objects::nonNull)
              .max(LocalDate::compareTo);
          
          dto.setFirstOccupiedDate(earliestStartDate.orElse(null));
          dto.setFirstAvailableDate(latestEndDate.map(date -> date.plusDays(1)).orElse(today));
      }
      
      private void calculateWorkload(RealisateurDTO dto, List<Demande> activeDemandes) {
          LocalDate today = LocalDate.now();
          LocalDate in7Days = today.plusDays(6);
          LocalDate in30Days = today.plusDays(29);
          
          // Calculate total workdays in the next 7 and 30 days period (excluding weekends)
          int totalDaysIn7Days = calculateWorkdaysBetween(today, in7Days);
          int totalDaysIn30Days = calculateWorkdaysBetween(today, in30Days);
          
          
          
          // Create a map to track occupation per day, accounting for multiple demandes on the same day
          Set<LocalDate> occupiedDays7 = new HashSet<>();
          Set<LocalDate> occupiedDays30 = new HashSet<>();
          
          for (Demande demande : activeDemandes) {
              LocalDate startDate = getEffectiveStartDate(demande);
              LocalDate endDate = getEffectiveEndDate(demande);
              
              if (startDate == null || endDate == null) continue;
              
              
              LocalDate effectiveStart = startDate.isBefore(today) ? today : startDate;
              
             
              if (!endDate.isBefore(today) && !startDate.isAfter(in7Days)) {
                  LocalDate effectiveEnd = endDate.isAfter(in7Days) ? in7Days : endDate;
                  
                  
                  LocalDate currentDate = effectiveStart;
                  while (!currentDate.isAfter(effectiveEnd)) {
                      if (isWorkday(currentDate)) {
                          //dailyOccupation7Days.put(currentDate, 
                              //dailyOccupation7Days.getOrDefault(currentDate, 0) + 1);
                    	  occupiedDays7.add(currentDate);
                      }
                      currentDate = currentDate.plusDays(1);
                  }
              }
              
              // Calculate overlap for 30 days period
              if (!endDate.isBefore(today) && !startDate.isAfter(in30Days)) {
                  LocalDate effectiveEnd = endDate.isAfter(in30Days) ? in30Days : endDate;
                  
                  // Mark each day in this range as occupied
                  LocalDate currentDate = effectiveStart;
                  while (!currentDate.isAfter(effectiveEnd)) {
                      if (isWorkday(currentDate)) {
                    	  occupiedDays30.add(currentDate);
                      }
                      currentDate = currentDate.plusDays(1);
                  }
              }
          }
          
          // Count occupied days (any day with at least one task is considered occupied)
          double occupiedDaysIn7Days = occupiedDays7.size();
          double occupiedDaysIn30Days = occupiedDays30.size();
          
          // Calculate workload as percentage (occupied days / total workdays)
          dto.setWorkloadNext7Days(totalDaysIn7Days > 0 ? 
                                  (occupiedDaysIn7Days / totalDaysIn7Days) * 100 : 0);
          dto.setWorkloadNext30Days(totalDaysIn30Days > 0 ? 
                                   (occupiedDaysIn30Days / totalDaysIn30Days) * 100 : 0);
      }
      
      private boolean isWorkday(LocalDate date) {
          DayOfWeek dayOfWeek = date.getDayOfWeek();
          return dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY;
      }
      
      private LocalDate getEffectiveStartDate(Demande demande) {
          LocalDate today = LocalDate.now();
          
          switch (demande.getStatut()) {
              case EN_COURS:
                  return demande.getDateEnCours();
              case AFFECTEE:
                  return demande.getDateAffectation() != null ? demande.getDateAffectation() : today;
              case EN_ATTENTE_DE_DEPENDENCE:
            	    List<Demande> dependencies = demandeRepository.findDependenciesWithDateForParent(demande.getId());
            	    Demande lastDependence = dependencies.stream()
            	        .filter(dep -> dep.getDateDependence() != null)
            	        .max(Comparator.comparing(Demande::getDateEstimee))
            	        .get();
            	    return lastDependence.getDateTerminee() != null ? 
            	           lastDependence.getDateTerminee() : 
            	           lastDependence.getDateEstimee();
                  
              default:
                  return today;
          }
      }
      
      
      private LocalDate getEffectiveEndDate(Demande demande) {
          LocalDate startDate = getEffectiveStartDate(demande);
          
          if (demande.getDateEstimee() != null) {
              return demande.getDateEstimee();
          } else if (startDate != null && demande.getDureeEstimee() != null) {
              return startDate.plusDays(demande.getDureeEstimee()-1);
          } else {
              return startDate; // Fallback if no duration information is available
          }
      }
      
      
      /*
      private int calculateWorkdaysBetween(LocalDate startDate, LocalDate endDate) {
          if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
              return 0;
          }
          
          int days = 0;
          LocalDate current = startDate;
          
          while (!current.isAfter(endDate)) {
              if (isWorkday(current)) {
                  days++;
              }
              current = current.plusDays(1);
          }
          
          return days;
      }
  */
      private int calculateWorkdaysBetween(LocalDate startDate, LocalDate endDate) {
    	    if (startDate == null || endDate == null) {
    	        return 0;
    	    }

    	    boolean forward = !startDate.isAfter(endDate);
    	    LocalDate a = forward ? startDate : endDate;
    	    LocalDate b = forward ? endDate   : startDate;

    	    int days = 0;
    	    LocalDate cursor = a;
    	    while (!cursor.isAfter(b)) {           
    	        if (isWorkday(cursor)) {
    	            days++;
    	        }
    	        cursor = cursor.plusDays(1);
    	    }

    	    return forward ? days : -days;
    	}
      
      
      private int businessDaysDelta(LocalDate start, LocalDate end) {
    	    if (start == null || end == null || start.equals(end)) {
    	        return 0;
    	    }

    	    int sign = start.isBefore(end) ? 1 : -1;
    	    LocalDate cursor = start.plusDays(sign);
    	    int days = 0;

    	    // Loop until we've passed end in the sign direction
    	    while ( (sign > 0 && !cursor.isAfter(end))
    	         || (sign < 0 && !cursor.isBefore(end)) ) {
    	        if (isWorkday(cursor)) {
    	            days++;
    	        }
    	        cursor = cursor.plusDays(sign);
    	    }

    	    return days * sign;
    	}
      public DemandeResponseDTO UpdateUrgence(Long demandeId,String  newUrgence) {
    	  
    	  
    	    Demande demande = demandeRepository.findById(demandeId)
    	            .orElseThrow(() -> new ResourceNotFoundException("Demande not found with id: " + demandeId));
     
      
    	    demande.setUrgence(newUrgence);
    	    demande.setDateModification(LocalDate.now());
    	
    	    Demande saved = demandeRepository.save(demande);
    	    
    	    return mapToResponseDTO(saved);
    	    
      }
      public DemandeResponseDTO UpdateInfoSup(Long demandeId,String  infoSup) {
    	  
    	  
  	    Demande demande = demandeRepository.findById(demandeId)
  	            .orElseThrow(() -> new ResourceNotFoundException("Demande not found with id: " + demandeId));
   
    
  	    demande.setInfoSup(infoSup);
  	    demande.setDateModification(LocalDate.now());
  	
  	    Demande saved = demandeRepository.save(demande);
  	    
  	    return mapToResponseDTO(saved);
  	    
    }
    
   
      public List<DemandeResponseDTO> getAllDemandesDetaillees() {
    	    List<Demande> demandes = demandeRepository.findAll();
    	    List<DemandeResponseDTO> responses = new ArrayList<>();
    	    
    	    for (Demande demande : demandes) {
    	        DemandeResponseDTO response = mapToResponseDTO(demande);
    	        long delayCreationAccept = 0;
    	        long delayAcceptEnCours = 0;
    	        long unionBusinessDays = 0;
    	         
    	        if (demande.getDateCreation() != null && demande.getDateAcceptation() != null) {
    	            delayCreationAccept = ChronoUnit.DAYS.between(demande.getDateCreation(), demande.getDateAcceptation());
    	            response.setDelayCreationAccept(delayCreationAccept);
    	        } 
    	        if (demande.getDateAcceptation() != null && demande.getDateEnCours() != null) {
    	            delayAcceptEnCours = ChronoUnit.DAYS.between(demande.getDateAcceptation(), demande.getDateEnCours());
    	            response.setDelayAcceptEnCours(delayAcceptEnCours);
    	        }           
    	         
    	        if (demande.getStatut() == StatutDemande.TERMINEE
    	                && demande.getDateEnCours() != null
    	                && demande.getDateTerminee() != null) {
    	            
    	            List<Interval> intervals = new ArrayList<>();
    	            for (Demande dep : demande.getDependentDemandes()) {
    	                if (dep.getDateDependence() != null && dep.getDateTerminee() != null) {
    	                    intervals.add(new Interval(dep.getDateDependence(), dep.getDateTerminee()));
    	                }
    	            } 
    	            
    	            long totalBusinessDays = calculateWorkdaysBetween(demande.getDateEnCours(), demande.getDateTerminee());
    	            unionBusinessDays = countBusinessDaysInIntervals(intervals);
    	            
    	            long dureeTravail = totalBusinessDays - Math.max(unionBusinessDays, 0);
    	            response.setDureeTravailRealisateur(dureeTravail);
    	            response.setDureeRetardDependence(unionBusinessDays);
    	        }
    	        
    	        long totalMainDelay = Math.max(delayCreationAccept, 0) + Math.max(delayAcceptEnCours, 0) + Math.max(unionBusinessDays, 0);
    	        response.setTotalMainDelay(totalMainDelay);
    	        
    	        responses.add(response);
    	    }
    	    
    	    return responses;
    	}

      
}
