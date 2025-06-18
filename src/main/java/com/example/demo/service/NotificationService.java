package com.example.demo.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import java.util.Optional;

import org.springframework.mail.MailException;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.ApprobationDto;
import com.example.demo.dto.InformationSupplementaireDto;
import com.example.demo.dto.NotificationDto;
import com.example.demo.entities.AppUser;
import com.example.demo.entities.Chef;
import com.example.demo.entities.Demande;
import com.example.demo.entities.Employee;
import com.example.demo.entities.Notification;
import com.example.demo.entities.Realisateur;
import com.example.demo.repository.AgentRepository;
import com.example.demo.repository.ChefRepository;
import com.example.demo.repository.DemandRepository;
import com.example.demo.repository.EmpolyeRepository;
import com.example.demo.repository.NotificatonRepository;
import com.example.demo.repository.RealisateurRepository;
import com.example.demo.enmus.NotificationType;
import com.example.demo.enmus.StatutDemande;
import com.example.demo.enmus.NotificationMethod;
import com.example.demo.enmus.NotificationStatus; 
import jakarta.persistence.Embeddable;
import jakarta.persistence.EntityNotFoundException;

import org.keycloak.email.EmailException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@Service
@Transactional
public class NotificationService {

	    private final NotificatonRepository notificationRepository;
	    private final DemandRepository demandRepository;
	    private final ChefRepository chefRepository;
	    private final EmpolyeRepository employerep;
	  
	    private final SimpMessagingTemplate messagingTemplate;
	    private final  AgentRepository appUserRepository;
	    private final EmailConfigurationService emailService;
	    private final RealisateurRepository rep;
	    public NotificationService(NotificatonRepository notificationRepository, DemandRepository demandRepository,
	                              ChefRepository chefRepository,EmpolyeRepository employerep,SimpMessagingTemplate messagingTemplate, AgentRepository appUserRepository,EmailConfigurationService emailService,RealisateurRepository rep) {
	        this.notificationRepository = notificationRepository;
	        this.demandRepository = demandRepository;
	        this.chefRepository = chefRepository;
	        this.employerep=employerep;
	        this.messagingTemplate = messagingTemplate;
	        this.appUserRepository=appUserRepository;
	        this.emailService=emailService;
	        this.rep=rep;
	    }
	    
	    private boolean isUserOnline(AppUser user) {
	        return user.getIsActive() != null && user.getIsActive();
	    }
	    public Notification createBaseNotification(AppUser requester, AppUser responder, Demande demande, 
                NotificationType type, String message, boolean isActionable) {
						Notification notification = new Notification();
						notification.setRequester(requester);
						notification.setResponder(responder);
						notification.setDemande(demande);
						if (demande != null) {
						notification.setDemandeTitle(demande.getTitle());
						}
						notification.setType(type);
						notification.setMessage(message);
						notification.setStatut(NotificationStatus.PENDING);
						notification.setIsRead(false);
						notification.setCreatedAt(LocalDateTime.now());
						notification.setActionable(isActionable);
					    if (!isActionable) {
					  
					        notification.setMethod(NotificationMethod.BOTH);
					    } else {
							if (isUserOnline(responder)) {
								notification.setMethod(NotificationMethod.NOTIFICATION);
								} else {
								notification.setMethod(NotificationMethod.EMAIL);
								}
					    }

			
						
						return notification;
						}
	    

	    private String getSubjectForNotificationType(NotificationType type) {
	        switch (type) {
	            case REQUEST_NOTIFICATION:
	                return "Nouvelle demande à traiter";
	            case REQUEST_REJECTED:
	                return "Demande rejetée";
	            case ADDITIONAL_INFO_REQUEST:
	                return "Demande d'informations supplémentaires";
	            case MODIFICATION_REQUEST:
	                return "Demande de modification";
	            case CANCELLATION_REQUEST:
	                return "Demande d'annulation";
	            case MODIFICATION_APPROVED:
	                return "Modification approuvée";
	            case MODIFICATION_REJECTED:
	                return "Modification rejetée";
	            case CANCELLATION_APPROVED:
	                return "Annulation approuvée";
	            case CANCELLATION_REJECTED:
	                return "Annulation rejetée";
	            case DEMANDE_START:
	                return "Demande en cours de traitement";
	            default:
	                return "Notification";
	        }
	    }
	    

	    @Transactional
	    public void handleDemandeEnAttenteDuChef(Demande demande,String msg) {
	        try {
	            AppUser agent = demande.getAgent();
	            if (agent == null) {
	               
	                return;
	            }

	            Optional<Employee> empOpt = employerep.findById(agent.getId());
	            if (empOpt.isEmpty() || empOpt.get().getChef() == null) {
	              
	                return;
	            }
	            AppUser chef = appUserRepository.findById(empOpt.get().getChef().getId())
	                .orElseThrow(() -> new IllegalArgumentException("Chef not found"));

	            String message = "Une nouvelle demande requiert votre validation. Demande: " + 
	                demande.getTitle() + " (ID: " + demande.getId() + ")";
	                
	            Notification notification = createBaseNotification(
	                agent, chef, demande, NotificationType.REQUEST_NOTIFICATION, message, false);
	                
	            deliverNotification(notification);
	            
	        } catch (Exception e) {
	    
	        }
	    }
	    

@Transactional
		public void handleDemandeEnAttenteDeResponsable(Demande demande,String msg ) {
    try {
    	AppUser rel=demande.getAgent();
        if(rel.getRole().equals("realisateur")) {
        	Optional<Realisateur> real=rep.findById(rel.getId());
            Optional<AppUser> responsableOpt = appUserRepository.findById(real.get().getResponsable().getId());
            if(responsableOpt.isPresent()) { 
                AppUser responsable = responsableOpt.get();  
                System.out.println("Agent ID: " + rel.getId());
                
                String message = msg +
                    "Demande: " + demande.getTitle() + " (ID: " + demande.getId() + ")";
                System.out.println("Sending notification to Responsable ID: " + responsable.getId());
                
                Notification notification = createBaseNotification(
                    rel, 
                    responsable,  
                    demande, 
                    NotificationType.REQUEST_NOTIFICATION, 
                    message, 
                    false
                );
                deliverNotification(notification);
            }
        }else {
            List<AppUser> responsables = appUserRepository.findByRole("responsable");
            
            if (responsables.isEmpty()) {
                throw new RuntimeException("No responsables found!");
            }

            AppUser agent = demande.getAgent();
            System.out.println("Agent ID: " + agent.getId());
            
            String message = msg+
                "Demande: " + demande.getTitle() + " (ID: " + demande.getId() + ")";
            
            for (AppUser responsable : responsables) {
            	
                System.out.println("Sending notification to Responsable ID: " + responsable.getId());
                
                Notification notification = createBaseNotification(
                    agent, 
                    responsable,  
                    demande, 
                    NotificationType.REQUEST_NOTIFICATION, 
                    message, 
                    false
                );
                
                deliverNotification(notification);
            }
        }

    } catch (Exception e) {

        e.printStackTrace();
       
        throw new RuntimeException("Failed to send notifications to responsables", e);
    }
}
	    

	    
	    @Transactional
	    public void handleDemandeRejected(Demande demande, String motifRejet) {
	        try {
	            AppUser agent = demande.getAgent();
	            if (agent == null) {
	         
	                return;
	            }


	            Optional<Employee> empOpt = employerep.findById(agent.getId());
	            if (empOpt.isEmpty() || empOpt.get().getChef() == null) {
	              
	                return;
	            }

	            AppUser requester = appUserRepository.findById(empOpt.get().getChef().getId())
	                .orElseThrow(() -> new IllegalArgumentException("Chef not found"));

	       
	            
	            String message = "Votre demande " + demande.getTitle() + " (ID: " + demande.getId() + 
	                ") a été rejetée. par votre chef" + (motifRejet != null ? "\n\nMotif: " + motifRejet : "");
	                
	            Notification notification = createBaseNotification(
	                requester, agent, demande, NotificationType.REQUEST_REJECTED, message, false);
	            
	            if (motifRejet != null) {
	                notification.setMotif(motifRejet);
	            }
	                
	            deliverNotification(notification);
	            
	        } catch (Exception e) {
	
	        }
	    }


	    @Transactional
	    public void handleDemandeAccepte(Demande demande, String motifAcceptation, Long idr) {
	        try {
	            AppUser approver = appUserRepository.findById(idr)
	                .orElseThrow(() -> new IllegalArgumentException("Responsable not found with ID: " + idr));
	            
	            AppUser demandeur = demande.getAgent();
	            if (demandeur == null) {
	                throw new IllegalArgumentException("Demande has no associated agent");
	            }
	            String message = "Votre demande " + demande.getTitle() + " (ID: " + demande.getId() + 
	                ") a été approuvée par " + approver.getFirstName() + " " + approver.getLastName() +
	                (motifAcceptation != null ? "\n\nCommentaire: " + motifAcceptation : "");

	            Notification notification = createBaseNotification(
	                approver,       
	                demandeur,     
	                demande,
	                NotificationType.REQUEST_APPROVED,
	                message,
	                false
	            );
	            
	            if (motifAcceptation != null) {
	                notification.setMotif(motifAcceptation);
	            }
	            
	            deliverNotification(notification);


	        } catch (Exception e) {
	            System.err.println("Error handling demande approval: " + e.getMessage());
	            e.printStackTrace();
	            throw new RuntimeException("Failed to process demande approval", e);
	        }
	    }
		

	    
	    

	    
	    public void sendAffectationNotifications(Demande demande, Realisateur realisateur, AppUser responsable) {
	        try {
	        	 System.out.println("sendAffectationNotifications  & deliverNotification" );
	            AppUser realisateurUser = realisateur.getAgent();
	            System.out.println("realisateurUser.getEmail()"+realisateurUser.getEmail());
	            System.out.println("realisateurUser.getEmail()"+realisateurUser.getEmail());
	            if (realisateurUser != null) {
	                String messageToRealisateur = "Vous avez été assigné à la demande \"" + demande.getTitle() + "\" (ID: " +
	                    demande.getId() + ") par " + responsable.getFirstName() + " " + responsable.getLastName() +
	                    ". Date d'affectation: " + demande.getDateAffectation();
	                System.out.println("realisateurUser.getEmail()"+realisateurUser.getEmail());
	                System.out.println("responsable.getEmail()"+responsable.getEmail());
	                Notification notificationToRealisateur = createBaseNotification(
	                    responsable,
	                    realisateurUser,
	                    demande,
	                    NotificationType.REQUEST_ASSIGNED,
	                    messageToRealisateur,
	                    false
	                );
	                deliverNotification(notificationToRealisateur);
	            }
	        } catch (Exception e) {
	       
	        }
	    }

	    public void deliverNotification(Notification notification) {
	        AppUser responder = notification.getResponder();
	        Notification saved = notificationRepository.save(notification);
	        
	        try {
	            switch (notification.getMethod()) {
	                case BOTH:
	                    sendNotificationViaWebSocket(saved, responder);
	                    sendNotificationViaEmail(saved, responder);
	                    break;
	                case NOTIFICATION:
	                    sendNotificationViaWebSocket(saved, responder);
	                    break;
	                case EMAIL:
	                    sendNotificationViaEmail(saved, responder);
	                    break;
	            }
   
	        } catch (MailException e) {
	           
	        } catch (Exception e) {
	          
	        }
	    }

	    private void sendNotificationViaWebSocket(Notification notification, AppUser recipient) {
	        try {
	            messagingTemplate.convertAndSend(
	                "/topic/notifications/" + recipient.getId(),
	                NotificationDto.fromEntity(notification)
	            );
	        } catch (MessagingException e) {
	           
	        }
	    }

	    private void sendNotificationViaEmail(Notification notification, AppUser recipient) {
	        try {
	            String subject = getSubjectForNotificationType(notification.getType());
	            emailService.sendNotificationEmail(
	                recipient,
	                notification.getRequester(),
	                subject,
	                notification.getMessage(),
	                notification.getType(),
	                notification
	            );
	        } catch (MailException e) {
	          
	        }
	    }
	    
	  
  public void SendNotificationAndEmail(String message,Demande res,AppUser ues,String subject) {
	    	
	    	Notification notification = new Notification();
	    	notification.setType(NotificationType.REQUEST_REJECTED);
	    	notification.setMotif(message);
	    	notification.setActionable(false);
	    	notification.setDemande(res);
	    	notification.setIsRead(false);
	    	notification.setRequester(ues);
	    	notification.setResponder(res.getAgent());

	        boolean isUserActive = isUserOnline(res.getAgent());
	        
	        if (notification.isActionable()) {
	      
	            if (isUserActive) {
	                System.out.println("Sending actionable notification only (user is online)");
	                notification.setMethod(NotificationMethod.NOTIFICATION);
	                messagingTemplate.convertAndSend("/topic/notifications/" + res.getAgent().getId(), 
	                                               NotificationDto.fromEntity(notification));
	            } else {
	                System.out.println("Sending actionable notification via email (user is offline)");
	                notification.setMethod(NotificationMethod.EMAIL);
	                emailService.sendNotificationEmail(
	                    res.getAgent(),
	                    ues,
	                    subject,
	                    message,
	                    NotificationType.REQUEST_REJECTED,
	                    notification
	                );
	            }
	        } else { 
	            notification.setMethod(NotificationMethod.BOTH);
	            
	            messagingTemplate.convertAndSend("/topic/notifications/" + res.getAgent().getId(), 
	                                           NotificationDto.fromEntity(notification));
	            

	            emailService.sendNotificationEmail(
	                res.getAgent(),
	                ues,
	                subject,
	                message,
	                NotificationType.REQUEST_REJECTED,
	                notification
	            );
	        }
	        
	        notificationRepository.save(notification);
	    }
  
  
  
  public void sendNotification(AppUser app, String message, Notification notification) {
	        notification.setResponder(app);
	        notification.setMessage(message);
	        AppUser chef = appUserRepository.findById(app.getId())
	                .orElseThrow(() -> new IllegalArgumentException("Chef not found."));
	        notification.setRequester(chef);
	        notificationRepository.save(notification);

	        messagingTemplate.convertAndSend("/topic/notifications/" + app.getId(), notification);
	    }
	    
	    @Transactional
	    public NotificationDto createAdditionalInformationRequest(InformationSupplementaireDto dto) {
	        Demande demande = demandRepository.findById(dto.getDemandeId())
	            .orElseThrow(() -> new IllegalArgumentException("Demande not found."));
	        AppUser req = appUserRepository.findById(dto.getRequesterId())
	            .orElseThrow(() -> new IllegalArgumentException("Requester not found."));
	        AppUser responder=demande.getAgent();
	        Notification nt = new Notification();
	        nt.setRequester(req);
	        nt.setResponder(responder);
	        nt.setMessage(dto.getMessage());
	        nt.setDemande(demande);
	        nt.setType(NotificationType.ADDITIONAL_INFO_REQUEST);
	        nt.setStatut(NotificationStatus.PENDING);
	        nt.setActionable(false); 
	        nt.setDemandeTitle(demande.getTitle());
	        
	        boolean isUserActive = isUserOnline(responder);
	        
	        if (nt.isActionable()) {

	            if (isUserActive) {
	            	nt.setMethod(NotificationMethod.NOTIFICATION);
	            } else {
	            	nt.setMethod(NotificationMethod.EMAIL);
	            }
	        } else {
	        	nt.setMethod(NotificationMethod.BOTH);
	        }
	        
	        Notification saved = notificationRepository.save(nt);
	        NotificationDto notificationDto = NotificationDto.fromEntity(saved);
	        
	        try {
	            if (!nt.isActionable() || isUserActive) {

	                messagingTemplate.convertAndSend(
	                    "/topic/notifications/" + responder.getId(), 
	                    notificationDto
	                );
	            }
	            
	            if (!nt.isActionable() || !isUserActive) {

	                String subject = "Demande d'informations supplémentaires";
	                String emailMessage = "Une demande d'informations supplémentaires a été soumise pour la demande " + 
	                    demande.getTitle() + ".\n\n" + dto.getMessage();
	                
	                emailService.sendNotificationEmail(
	                    responder,
	                    req,
	                    subject,
	                    emailMessage,
	                    NotificationType.ADDITIONAL_INFO_REQUEST,
	                    saved
	                );
	            }
	        } catch (Exception e) {
	            System.err.println("Failed to send notification: " + e.getMessage());
	            e.printStackTrace();
	          
	        }

	        return notificationDto;
	    }
	    @Transactional
	    public NotificationDto requestModificationApproval(ApprobationDto dto) {
	        try {
	            Demande demande = demandRepository.findById(dto.getDemandeId())
	                    .orElseThrow(() -> new IllegalArgumentException("Demande not found."));
	            AppUser user = appUserRepository.findById(dto.getUserId())
	                    .orElseThrow(() -> new IllegalArgumentException("User not found."));
	            
	            Optional<Employee> emp = employerep.findById(user.getId());
	            System.out.println("Demande title: " + demande.getTitle());
	            if (emp.isEmpty() || emp.get().getChef() == null) {
	                throw new IllegalStateException("User does not have an assigned chef.");
	            }

	            AppUser chef = appUserRepository.findById(emp.get().getChef().getId())
	                    .orElseThrow(() -> new IllegalArgumentException("Chef not found."));

	            Notification notification = new Notification();
	            notification.setRequester(user);
	            notification.setResponder(chef);
	            notification.setDemande(demande);
	            notification.setDemandeTitle(demande.getTitle());
	            notification.setMotif(dto.getMotifRejet());
	            notification.setType(NotificationType.MODIFICATION_REQUEST);
	            notification.setMessage(dto.getMessage());
	            notification.setStatut(NotificationStatus.PENDING);
	            notification.setIsRead(false);
	            notification.setActionable(true);
	            notification.setMethod(NotificationMethod.BOTH); 
	            
	            Notification saved = notificationRepository.save(notification);
	            
	            messagingTemplate.convertAndSend("/topic/notifications/" + chef.getId(), NotificationDto.fromEntity(saved));
	            
	            String subject = "Demande d'approbation de modification";
	            //String siteUrl = "https://votresite.com/demandes/" + demande.getId();
	            String emailMessage = "Une demande d'approbation de modification a été soumise pour la demande " + 
	                    demande.getTitle() + ".\n\n" + dto.getMessage() + 
	                    "\n\nVeuillez vous connecter à l'application pour traiter cette demande: ";
	            
	            emailService.sendNotificationEmail(
	                chef, 
	                user, 
	                subject, 
	                emailMessage, 
	                NotificationType.MODIFICATION_REQUEST, 
	                saved
	            );
	            
	            return NotificationDto.fromEntity(saved);
	       
	       
	        } catch (Exception e) {

	            throw new RuntimeException("Failed to process modification request.", e);
	        }
	    }

	    
	    @Transactional
	    public NotificationDto requestCancellationApproval(ApprobationDto dto) {
	        Demande demande = demandRepository.findById(dto.getDemandeId())
	                .orElseThrow(() -> new IllegalArgumentException("Demande not found."));

	        AppUser user = appUserRepository.findById(dto.getUserId())
	                .orElseThrow(() -> new IllegalArgumentException("User not found."));
	        
	        Optional<Employee> emp = employerep.findById(user.getId());
	        if (emp.isEmpty() || emp.get().getChef() == null) {
	            throw new IllegalStateException("User does not have an assigned chef.");
	        }
	        
	        AppUser chef = appUserRepository.findById(emp.get().getChef().getId())
	                .orElseThrow(() -> new IllegalArgumentException("Chef not found."));

	        Notification notification = new Notification();
	        notification.setRequester(user);
	        notification.setResponder(chef);
	        notification.setDemande(demande);
	        notification.setDemandeTitle(demande.getTitle());
	        notification.setMotif(dto.getMotifRejet());
	        notification.setType(NotificationType.CANCELLATION_REQUEST);
	        notification.setMessage(dto.getMessage());
	        notification.setStatut(NotificationStatus.PENDING);
	        notification.setIsRead(false);
	        notification.setActionable(true);
            notification.setMethod(NotificationMethod.BOTH); 
            
            Notification saved = notificationRepository.save(notification);
            
            messagingTemplate.convertAndSend("/topic/notifications/" + chef.getId(), NotificationDto.fromEntity(saved));
            String subject = "Demande d'approbation de annulation";
            String siteUrl = "https://votresite.com/demandes/" + demande.getId();
            String message = "Une demande d'approbation de annulation a été soumise pour la demande " + 
            		demande.getTitle() +".\n\n" + dto.getMessage()+
                    "\n\nVeuillez vous connecter à l'application pour traiter cette demande: " + siteUrl;
            
            emailService.sendNotificationEmail(
                chef, 
                user, 
                subject, 
                message, 
                NotificationType.MODIFICATION_REQUEST, 
                saved
            );
            
	        return NotificationDto.fromEntity(saved);
	    }
	    
	    
	    
	    /*	        notification.setMethod(NotificationMethod.NOTIFICATION);
	        if(!isUserOnline(chef)) {
	        	notification.setMethod(NotificationMethod.EMAIL);

	        }
	        Notification saved = notificationRepository.save(notification);

	        messagingTemplate.convertAndSend("/topic/notifications/" + chef.getId(), notification);
	        if(!isUserOnline(chef)) {
	        	String subject = "Demande d'approbation de annulation";
	            String message = "Une demande d'approbation de annulation a été soumise pour la demande " + 
	            		demande.getTitle() +".\n\n" + dto.getMessage();
	            emailService.sendNotificationEmail(chef,saved.getRequester(),subject, message,NotificationType.CANCELLATION_REQUEST, saved);
	            
	        } else {
		        messagingTemplate.convertAndSend("/topic/notifications/" + chef.getId(), notification);
	        }
	        return NotificationDto.fromEntity(saved);
	    }
	    */
	    
	    @Transactional
	    public NotificationDto rejectModificationOrCancellation(ApprobationDto dto) {
			    Demande demande = demandRepository.findById(dto.getDemandeId())
			        .orElseThrow(() -> new IllegalArgumentException("Demande not found."));
			    
			    AppUser requester = appUserRepository.findById(dto.getResponderId())
			            .orElseThrow(() -> new IllegalArgumentException("Requester not found."));
			    AppUser responder = appUserRepository.findById(dto.getUserId())
			            .orElseThrow(() -> new IllegalArgumentException("Responder not found."));
			
			    Notification newNotification = new Notification();
			    NotificationType notificationType;
			    String emailSubject;
			    String emailMessage;
			    
			    if (dto.getType() == NotificationType.MODIFICATION_REQUEST) {
			        notificationType = NotificationType.MODIFICATION_REJECTED;
			        newNotification.setMessage("Votre demande de modification est rejetée");
			        emailSubject = "Modification rejetée";
			        emailMessage = "Votre demande de modification pour la demande #" + 
			                      demande.getId() + " a été rejetée.\n\nMotif: " + dto.getMotifRejet();
			    } else {
			        notificationType = NotificationType.CANCELLATION_REJECTED;
			        newNotification.setMessage("Votre demande d'annulation est rejetée");
			        emailSubject = "Annulation rejetée";
			        emailMessage = "Votre demande d'annulation pour la demande " +
			                demande.getTitle() + " a été rejetée.\n\nMotif: " + dto.getMotifRejet();
			    }
			
			    newNotification.setMotif(dto.getMotifRejet());
			    newNotification.setDemande(demande);
			    newNotification.setRequester(requester);
			    newNotification.setResponder(responder);
			    newNotification.setDemandeTitle(demande.getTitle());
			    newNotification.setStatut(NotificationStatus.REJECTED);
			    newNotification.setIsRead(false);
			    newNotification.setCreatedAt(LocalDateTime.now());
			    newNotification.setType(notificationType);
			    newNotification.setMethod(NotificationMethod.BOTH); 
			    
			    Notification saved = notificationRepository.save(newNotification);
			
			    messagingTemplate.convertAndSend("/topic/notifications/" + responder.getId(), 
			                                   NotificationDto.fromEntity(saved));
			
			    String siteUrl = "https://votresite.com/demandes/" + demande.getId();
			    emailMessage += "\n\nPour plus de détails, veuillez consulter l'application: " + siteUrl;
			    
			    emailService.sendNotificationEmail(
			        responder,
			        requester,
			        emailSubject,
			        emailMessage,
			        notificationType,
			        saved
			    );
			
			    return NotificationDto.fromEntity(saved);
	    }
@Transactional
public NotificationDto approveModification(ApprobationDto dto) {
			    Demande demande = demandRepository.findById(dto.getDemandeId())
			            .orElseThrow(() -> new IllegalArgumentException("Demande not found."));
			    demande.setApprobationModification(true);
			    demande = demandRepository.save(demande);
			
			    AppUser requester = appUserRepository.findById(dto.getResponderId())
			            .orElseThrow(() -> new IllegalArgumentException("Requester not found."));
			    AppUser responder = appUserRepository.findById(dto.getUserId())
			            .orElseThrow(() -> new IllegalArgumentException("Responder not found."));
			
			    Notification newNotification = new Notification();
			    newNotification.setDemande(demande);
			    newNotification.setRequester(requester);
			    newNotification.setDemandeTitle(demande.getTitle());
			    newNotification.setResponder(responder);
			    newNotification.setStatut(NotificationStatus.APPROVED);
			    newNotification.setIsRead(false);
			    newNotification.setCreatedAt(LocalDateTime.now());
			    newNotification.setActionable(false);
			    newNotification.setType(NotificationType.MODIFICATION_APPROVED);
			    newNotification.setMethod(NotificationMethod.BOTH);
			    newNotification.setMessage("Votre demande de modification a été approuvée");
			
			    Notification saved = notificationRepository.save(newNotification);
			

			    messagingTemplate.convertAndSend("/topic/notifications/" + responder.getId(),
			                                   NotificationDto.fromEntity(saved));
			
			    String subject = "Modification approuvée";
			    String siteUrl = "https://votresite.com/demandes/" + demande.getId();
			    String message = "Votre demande de modification pour la demande " +
			            demande.getTitle() + " a été approuvée.\n\n" +
			            "Pour plus de détails, veuillez consulter l'application: " + siteUrl;
			    
			    emailService.sendNotificationEmail(
			        responder,
			        requester,
			        subject,
			        message,
			        NotificationType.MODIFICATION_APPROVED,
			        saved
			    );

			    	return NotificationDto.fromEntity(saved);
	}

	    public NotificationDto approveCancellation(ApprobationDto dto) {

	        Demande demande = demandRepository.findById(dto.getDemandeId())
	            .orElseThrow(() -> new IllegalArgumentException("Demande not found."));
	  
	        demande.setApprobationAnnulation(true);
	        demandRepository.save(demande);
	        AppUser requester = appUserRepository.findById(dto.getResponderId())
	                .orElseThrow(() -> new IllegalArgumentException("Requester not found."));
	        AppUser responder = appUserRepository.findById(dto.getUserId())
	                .orElseThrow(() -> new IllegalArgumentException("Responder not found."));
	        Notification newNotification = new Notification();
	        newNotification.setDemande(demande);
	        newNotification.setRequester(requester); 
	        newNotification.setResponder(responder);
	        newNotification.setDemandeTitle(demande.getTitle());
	        newNotification.setStatut(NotificationStatus.APPROVED);
	        newNotification.setIsRead(false);
	        newNotification.setActionable(false);
	        newNotification.setCreatedAt(LocalDateTime.now());
	        newNotification.setType(NotificationType.CANCELLATION_APPROVED);
	        newNotification.setMessage("Votre demande d'annulation a été approuvée");
	        newNotification.setMethod(NotificationMethod.BOTH);

	        Notification saved = notificationRepository.save(newNotification);

	        messagingTemplate.convertAndSend("/topic/notifications/" + responder.getId(),
	                                       NotificationDto.fromEntity(saved));

	        String subject = "Annulation approuvée";
	        String siteUrl = "https://votresite.com/demandes/" + demande.getId();
	        String message = "Votre demande d'annulation pour la demande " +
	                demande.getTitle() + " a été approuvée.\n\n" +
	                "Pour plus de détails, veuillez consulter l'application: " + siteUrl;
	        
	        emailService.sendNotificationEmail(
	            responder,
	            requester,
	            subject,
	            message,
	            NotificationType.CANCELLATION_APPROVED,
	            saved
	        );

	        return NotificationDto.fromEntity(saved);
	    }

	    @Transactional
	    public void approveByEmailAction(Long notificationId) {
	        Notification notification = notificationRepository.findById(notificationId)
	                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));
	        
	        ApprobationDto dto = new ApprobationDto();
	        dto.setDemandeId(notification.getDemande().getId());
	        dto.setUserId(notification.getRequester().getId());
	        dto.setResponderId(notification.getResponder().getId());
	        dto.setApprobation(true);
	        
	        System.out.println(dto);
	        if (notification.getType() == NotificationType.MODIFICATION_REQUEST) {
	            approveModification(dto);
	        } else if (notification.getType() == NotificationType.CANCELLATION_REQUEST) {
	            approveCancellation(dto);
	        } else {
	            throw new IllegalStateException("This notification type cannot be approved");
	        }
	    }

	    @Transactional
	    public void rejectByEmailAction(Long notificationId) {
	        Notification notification = notificationRepository.findById(notificationId)
	                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));
	        
	        ApprobationDto dto = new ApprobationDto();
	        dto.setDemandeId(notification.getDemande().getId());
	        dto.setUserId(notification.getRequester().getId());
	        dto.setResponderId(notification.getResponder().getId());
	        dto.setApprobation(false);
	        dto.setType(notification.getType());
	        dto.setMotifRejet("Rejeté via email"); 
	        System.out.println(dto.toString());
	        rejectModificationOrCancellation(dto);

	        
	    }
	    
	    
	    @Transactional
	    public NotificationDto markAsRead(Long id) {
	        Notification notification = notificationRepository.findById(id)
	                .orElseThrow(() -> new EntityNotFoundException("Notification not found with id: " + id));
	        
	        notification.setIsRead(true);
	        Notification savedNotification = notificationRepository.save(notification);
	        return NotificationDto.fromEntity(savedNotification);
	    }
	    
		  

    public void deleteNotification(Long id) {
        Notification notification = notificationRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Notification not found with id: " + id));
        notificationRepository.delete(notification);
    }


    @Transactional
    public int markAllUnreadAsRead() {
        return notificationRepository.markAllUnreadAsRead();
    }

 

    public List<NotificationDto> getAllUserNotificationsIsReadFalse(Long userId) {
        return notificationRepository.findByResponderIdAndIsReadFalseOrderByCreatedAtDesc(userId)
                .stream()
                .map(NotificationDto::fromEntity)
                .toList();
    }
    
    public List<NotificationDto> getAllUserNotifications(Long userId) {
        return notificationRepository.findByResponderId(userId)
                .stream()
                .map(NotificationDto::fromEntity)
                .toList();
    }
    public List<NotificationDto> getAllUserNotificationsNoTEmail(Long userId) {
        return notificationRepository.findByResponderIdAndMethod(userId,NotificationMethod.NOTIFICATION)
                .stream()
                .map(NotificationDto::fromEntity)
                .toList();
    }
    
 
    
    @Transactional
    @Scheduled(fixedDelay =5 * 60 * 1000)
    public void checkAndSendNotifications() {
    	LocalDate now = LocalDate.now();

        List<Demande> demandes = demandRepository.findByStatutAndDateAffectationBefore(StatutDemande.ACCEPTEE, now);

        for (Demande demande : demandes) {

            boolean exists = notificationRepository.existsByDemandeAndType(demande, NotificationType.DEMANDE_START);
            if (!exists) {
                Notification notification = new Notification();
                notification.setType(NotificationType.DEMANDE_START);
                notification.setMessage("Voulez-vous commencer le travail de cette demande envoyée par le responsable ?");
                notification.setIsRead(false);
                notification.setDemande(demande);

                if (demande.getRealisateur() != null && demande.getRealisateur().getResponsable() != null) {
                    notification.setRequester(demande.getRealisateur().getResponsable().getAgent());
                }

                if (demande.getRealisateur() != null) {
                    notification.setResponder(demande.getRealisateur().getAgent());
                }
 
                notificationRepository.save(notification);
            }
        }}

    @Transactional
    public void handleDemandeEnCour(Demande demande) {
        try {
            AppUser realisateur = demande.getRealisateur() != null ? demande.getRealisateur().getAgent() : null;
            AppUser demandeur = demande.getAgent();
            
            if (realisateur == null || demandeur == null) {
                return; 
            }

            String message = String.format(
                "Le travail sur votre demande \"%s\" (ID: %d) a commencé par %s %s",
                demande.getTitle(),
                demande.getId(),
                realisateur.getFirstName(),
                realisateur.getLastName()
            );

            Notification notification = createBaseNotification(
            		realisateur,  
            		demandeur,   
                demande,
                NotificationType.DEMANDE_START,
                message,
                false
            );
            
            deliverNotification(notification);
        } catch (Exception e) {
            System.err.println("Error handling demande en cours notification: " + e.getMessage());
            e.printStackTrace();
        }
    }
    @Transactional
    public void handleDemandeTerminee(Demande demande) {
        try {
            AppUser realisateur = demande.getRealisateur() != null ? demande.getRealisateur().getAgent() : null;
            AppUser demandeur = demande.getAgent();
            
            if (realisateur == null || demandeur == null) {
                return; 
            }

            String message = String.format(
                "Votre demande \"%s\" (ID: %d) a été marquée comme terminée par %s %s",
                demande.getTitle(),
                demande.getId(),
                realisateur.getFirstName(),
                realisateur.getLastName()
            );

            Notification notification = createBaseNotification(
                realisateur, 
                demandeur,   
                demande,
                NotificationType.REQUEST_APPROVED, 
                message,
                false
            );
            
            deliverNotification(notification);
        } catch (Exception e) {
            System.err.println("Error handling demande terminée notification: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
}
