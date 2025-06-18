package com.example.demo.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.example.demo.dto.AffectationRequest;
import com.example.demo.dto.DemandeDTO;
import com.example.demo.dto.DemandeGanttDTO;

import com.example.demo.dto.DemandeResponseDTO;
import com.example.demo.dto.RealisateurDTO;
import com.example.demo.dto.ResponseDem;
import com.example.demo.enmus.NotificationType;
import com.example.demo.enmus.StatutDemande;
import com.example.demo.entities.AppUser;
import com.example.demo.entities.Demande;
import com.example.demo.entities.Notification;
import com.example.demo.exception.EntityNotFoundException;
import com.example.demo.repository.AgentRepository;
import com.example.demo.repository.DemandRepository;
import com.example.demo.repository.NotificatonRepository;
import com.example.demo.service.DemandService;
import com.example.demo.service.DemandeGantService;
import com.example.demo.service.NotificationService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
@RestController
@RequestMapping("/api/demandes")
public class demandController {
    private DemandService demandeServicee;
    private DemandeGantService  demandeGantService;
    private final ObjectMapper objectMapper;
    private NotificationService nts;
    private final AgentRepository appUserRepository;
    private final NotificatonRepository notificationRepository;
    private final DemandRepository demandeRepository;
    public demandController(ObjectMapper objectMapper,DemandService demandeServicee,DemandeGantService demandeGantService,NotificationService nts,AgentRepository appUserRepository,NotificatonRepository notificationRepository,DemandRepository demandeRepository) {
    	this.demandeServicee = demandeServicee;
    	this.demandeGantService=demandeGantService;
    	this.nts=nts;
    	this.appUserRepository=appUserRepository;
    	this.notificationRepository=notificationRepository;
    	this.objectMapper = objectMapper;
    	this.demandeRepository=demandeRepository;
    }
    
    
    //NEW
    @GetMapping("/ALL")

    public ResponseEntity<List<DemandeResponseDTO> > getAllDemande() {
        List<DemandeResponseDTO>  response = demandeServicee.getAllDemandesDetaillees();
        return ResponseEntity.ok(response);
    }

    //NEW
    @GetMapping("/responsable/{responsableId}")
    public ResponseEntity<List<DemandeResponseDTO>> getDemandesByResponsable(
            @PathVariable Long responsableId) {
        
        List<DemandeResponseDTO> response = demandeServicee.getDemandesByResponsableAndStatuts(responsableId, Arrays.asList(
                StatutDemande.EN_ATTENTE_DE_DEPENDENCE,
                StatutDemande.AFFECTEE,
                StatutDemande.ACCEPTEE,
                StatutDemande.EN_ATTENTE_DE_CHEF,
                StatutDemande.EN_COURS
            ));
        return ResponseEntity.ok(response);
    }
    
    //NEW
    @GetMapping("/en-attente-responsable/{responsableId}")
    public ResponseEntity<List<DemandeResponseDTO>> getDemandesEnAttenteResponsable(@PathVariable Long responsableId) {
        List<DemandeResponseDTO> response = demandeServicee.getAllDemandesEnAttenteResponsable(responsableId);
        return ResponseEntity.ok(response);
    }

    //NEW
    @GetMapping("/finalise/{responsableId}")
    public ResponseEntity<List<DemandeResponseDTO>> getDemandesFinalisees2(@PathVariable Long responsableId) {
        List<DemandeResponseDTO> response = demandeServicee.getDemandesFinalisees(responsableId);
        return ResponseEntity.ok(response);
    }

    //NEW
    @GetMapping("/count-by-statusres/{responsableId}")
    public ResponseEntity<Map<StatutDemande, Long>> getAllDemandesCountByStatus(@PathVariable Long responsableId) {
        Map<StatutDemande, Long> data = demandeServicee.getAllDemandesCountByStatus(responsableId);
        return ResponseEntity.ok(data);
    }

    @GetMapping("/count-by-status/{agentId}")
    public ResponseEntity<Map<StatutDemande, Long>> getDemandesCountByStatus(@PathVariable Long agentId) {
        Map<StatutDemande, Long> data = demandeServicee.getDemandesCountByStatusForAgent(agentId);
        return ResponseEntity.ok(data);
    }
 
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDemande(@PathVariable Long id) {
    	demandeServicee.deleteDemandeById(id);
        return ResponseEntity.noContent().build();
    }
    
    //NEW
    @GetMapping("/gantt/{responsableId}")
    public  ResponseEntity<Map<Long, List<DemandeGanttDTO>>> getGanttData(@PathVariable Long responsableId) {
        Map<Long,List<DemandeGanttDTO>> data = demandeGantService.getGanttData(responsableId);
        return ResponseEntity.ok(data);
    }
    @PutMapping("/{id}/status/anulle")
    @Transactional
    public ResponseEntity<Demande> setStatusAnule(@PathVariable Long id) {
    	demandeServicee.updateStatus(id, StatutDemande.ANNULEE);
    	Demande res = demandeServicee.getdemandeForNotification(id);

        return ResponseEntity.ok(res);
    }
    
    @PutMapping("/{id}/status/Reject")
    @Transactional
    public ResponseEntity<Demande> setStatusReject(@PathVariable Long id) {
    	Demande res =demandeServicee.updateStatus(id, StatutDemande.REJECTEE);
    	
    	return ResponseEntity.ok(res);
    }
 
    @PutMapping("/{id}/status/Reject/responsable/{idr}")
    @Transactional
    public ResponseEntity<Demande> setStatusRejectResponable(@PathVariable Long id,
            @RequestBody String message,
            @PathVariable Long  idr) {
        Demande demande = demandeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Demande not found"));
	
			demande.setStatut(StatutDemande.REJECTEE);
			Demande res = demandeRepository.save(demande);
			if (message == null || message.trim().isEmpty()) {
			message = "Aucune raison fournie";
			}
			
		    AppUser rs = appUserRepository.findById(idr)
		            .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + idr));
		    
        String Subject = "demande " + res.getTitle() + " rejectée";
        String msg = "La demande " + res.getTitle() + " a été rejetée pour la raison suivante : " + message;
        nts.SendNotificationAndEmail(msg, res, rs, Subject);

        return ResponseEntity.ok(res);
    }
   
    @PutMapping("/update-dependence/{dependenceId}")
    public ResponseEntity<DemandeResponseDTO> updateDataDependence(@PathVariable Long dependenceId) {
        DemandeResponseDTO response = demandeServicee.updateDataDependence(dependenceId);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{id}/status/Accept/{idr}")
    @Transactional
    public ResponseEntity<?> setStatusAccept(
            @PathVariable Long id,
            @PathVariable Long idr) {
        
        try {
            if (idr == null || idr <= 0) {
                return ResponseEntity.badRequest()
                    .body(Map.of(
                        "error", "Invalid responsable ID",
                        "message", "user ID null"
                    ));
            }

            Optional<AppUser> responsableOpt = appUserRepository.findById(idr);
            if (responsableOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                        "error", "Responsable not found",
                        "message", "Aucun utilisateur trouvé avec l'ID :: " + idr
                    ));
            }



            Demande res = demandeServicee.updateStatusByAgent(id, StatutDemande.ACCEPTEE, idr);
            
            return ResponseEntity.ok(res);


        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "error", "Processing error",
                    "message", "Échec de la mise à jour du statut de la demande : " + e.getMessage()
                ));
        }
    } 
    @PutMapping("/{id}/status/Affectee")
    @Transactional
    public ResponseEntity<Demande> setStatusAffectee(@PathVariable Long id) {
    	Demande res=demandeServicee.updateStatus(id, StatutDemande.AFFECTEE);
 
    	return ResponseEntity.ok(res);
    }   
    



    @PutMapping("/{demandeId}/affecter-realisateur/{realisateurId}")
    public ResponseEntity<DemandeResponseDTO> affecterRealisateur2(
            @PathVariable Long demandeId,
            @PathVariable Long realisateurId,
            @RequestBody AffectationRequest request) {
        DemandeResponseDTO updated = demandeServicee.affecterRealisateur(
            demandeId, 
            realisateurId, 
            request.getDateAffectation(),
            request.getDureEstimee()
        );
      
        return ResponseEntity.ok(updated);
    }
 
    /*
    @GetMapping("/en-cour")
    public ResponseEntity<List<DemandeResponseDTO>> getAllDemandesEnCour() {
        List<DemandeResponseDTO> response = demandeServicee.getAllDemandesEnAttenteResponsable();
        return ResponseEntity.ok(response);
    }*/
    

    
    @GetMapping("/finalisees/{realisateurId}")
    public ResponseEntity<List<DemandeResponseDTO>> getDemandesFinalisees(@PathVariable Long realisateurId) {
        List<DemandeResponseDTO> response = demandeServicee.getDemandesRealisateur(realisateurId);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/finalises/{agentId}")
    public ResponseEntity<List<DemandeResponseDTO>> getDemandesFinaliseesByAgent(@PathVariable("agentId") Long agentId) {
    	List<DemandeResponseDTO> response = demandeServicee.getDemandesFinaliseesByAgent(agentId);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/chef/{chefId}")
    public ResponseEntity<List<DemandeResponseDTO>> getAllDemandesForChef(@PathVariable Long chefId) {
        List<DemandeResponseDTO> response = demandeServicee.getAllDemandesForChef(chefId);
        return ResponseEntity.ok(response);
    }
    @PutMapping("/adjust-date-estimee/{demandeId}")
    public ResponseEntity<DemandeResponseDTO> adjustDateEstimee(
            @PathVariable Long demandeId,
            @RequestParam  LocalDate newEstimated) {
        DemandeResponseDTO response = demandeServicee.adjustDateEstimee(demandeId, newEstimated);
        return ResponseEntity.ok(response);
    }
    @PutMapping("/urgence/{demandeId}")
    public ResponseEntity<DemandeResponseDTO> UpdateUrgence(
            @PathVariable Long demandeId, 
            @RequestBody Map<String, String> requestBody) {
  
        String newUrgence = requestBody.get("urgence");
        
        if (demandeId == null) {
            throw new IllegalArgumentException("Demande ID ne peut pas être nul");
        }
        if (newUrgence == null) {
            throw new IllegalArgumentException("Urgence ne peut pas être nul");
        }
        
        DemandeResponseDTO response = demandeServicee.UpdateUrgence(demandeId, newUrgence);
        return ResponseEntity.ok(response);
    }

    
    @PutMapping("/InfoSup/{demandeId}")
    public ResponseEntity<DemandeResponseDTO> UpdateInfoSup(
            @PathVariable Long demandeId, 
            @RequestBody Map<String, String> requestBody) {  

        if(requestBody == null || !requestBody.containsKey("infoSup")) {
            throw new IllegalArgumentException("Information Supplimentaire ne peut pas être nul");
        }
        
        String infoSup = requestBody.get("infoSup");
        
        if (demandeId == null) {
            throw new IllegalArgumentException("Demande ID ne peut pas être nul");
        }

        
        DemandeResponseDTO response = demandeServicee.UpdateInfoSup(demandeId, infoSup);
        return ResponseEntity.ok(response);
    }

    
    @GetMapping("/agent/{agentId}")
    public ResponseEntity<List<DemandeResponseDTO>> getAllDemandesForAgent(@PathVariable Long agentId) {
        List<DemandeResponseDTO> response = demandeServicee.getAllDemandesForAgent(agentId);
        return ResponseEntity.ok(response);
    }

@PostMapping(
            value = "/create", 
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
        )
        @Order(1)
        public ResponseEntity<DemandeResponseDTO> createDemande(
            @RequestPart("demande") String demandeJson,
            MultipartHttpServletRequest request
        ) throws IOException {
            DemandeDTO dto = objectMapper.readValue(demandeJson, DemandeDTO.class);

            Map<String, MultipartFile> fileMap = request.getFileMap();
            for ( var entry : fileMap.entrySet() ) {
            	  String         name       = entry.getKey();
            	  MultipartFile  file       = entry.getValue();
            	  byte[]         bytes      = file.getBytes();
            	  String         base64     = Base64.getEncoder().encodeToString(bytes);
            	  String         contentType = file.getContentType();         
            	  String         originalName = file.getOriginalFilename();   

            	  Map<String,Object> fileDto = Map.of(
            	    "fileName",    originalName,
            	    "contentType", contentType,
            	    "data",        base64
            	  );
            	  dto.getData().put(name, fileDto);
            	}


            DemandeResponseDTO created = demandeServicee.createDemande(dto);

            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        }
    
    @PutMapping(
            value = "/{id}", 
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
        )
    public ResponseEntity<DemandeResponseDTO> updateDemande(
            @PathVariable Long id, 
            @RequestPart("demande") String demandeJson,
            MultipartHttpServletRequest request) throws IOException {
        DemandeDTO dto = objectMapper.readValue(demandeJson, DemandeDTO.class);
        Map<String, MultipartFile> fileMap = request.getFileMap();
        for ( var entry : fileMap.entrySet() ) {
      	  String         name       = entry.getKey();
      	  MultipartFile  file       = entry.getValue();
      	  byte[]         bytes      = file.getBytes();
      	  String         base64     = Base64.getEncoder().encodeToString(bytes);
      	  String         contentType = file.getContentType();          
      	  String         originalName = file.getOriginalFilename();   

      	  Map<String,Object> fileDto = Map.of(
      	    "fileName",    originalName,
      	    "contentType", contentType,
      	    "data",        base64
      	  );
      	  dto.getData().put(name, fileDto);
      	}
        DemandeResponseDTO response = demandeServicee.updateDemande(id, dto);
        return ResponseEntity.ok(response);
    }
    

    

    //NEW
    @GetMapping("/availability/{responsableId}")
    public ResponseEntity<List<RealisateurDTO>> getRealisateurWithAvailability(@PathVariable Long responsableId) {
        List<RealisateurDTO> dto = demandeServicee.getAllRealisateursWithAvailability(responsableId);
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/{id}/status/Attenteres")
    public ResponseEntity<Demande> setStatusRespons(@PathVariable Long id) {
    	
        return ResponseEntity.ok(demandeServicee.updateStatus(id, StatutDemande.EN_ATTENTE_DE_RESPONSABLE));
    }
    
    
    @PutMapping("/{id}/terminee")
    public ResponseEntity<DemandeResponseDTO> completeDemande(@PathVariable Long id) {
        DemandeResponseDTO updated = demandeServicee.completeDemande(id);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{id}")
    @Order(2)
    public ResponseEntity<DemandeResponseDTO> getDemandeDetaille(@PathVariable Long id) {
        DemandeResponseDTO response = demandeServicee.getDemandeDetaille(id);
        return ResponseEntity.ok(response);
    }
    @PutMapping("/{id}/status/Cours")
    public ResponseEntity<Demande> setStatusCours(@PathVariable Long id) {
        
        return ResponseEntity.ok(demandeServicee.updateStatusCour(id, StatutDemande.EN_COURS));
    }
    
    
    @GetMapping("/chef/{chefId}/users")
    public ResponseEntity<?> getAllByAgentChefId(@PathVariable Long chefId) {
        try {
            List<DemandeResponseDTO> demandes = demandeServicee.getAllDemandeTeamsChefId(chefId);
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "All demandes retrieved for chef's team",
                "data", demandes,
                "count", demandes.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "status", "error",
                    "message", "Failed to retrieve demandes",
                    "error", e.getMessage()
                ));
        }
    }
    
    
 

}