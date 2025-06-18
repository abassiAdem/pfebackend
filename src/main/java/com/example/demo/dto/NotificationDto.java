package com.example.demo.dto;

import java.time.LocalDateTime;

import com.example.demo.enmus.NotificationMethod;
import com.example.demo.entities.Notification;

public class NotificationDto {
    private Long id;
    private String type;
    private String statut;
    private String message;
    private String motif;
    private boolean isRead;
    private boolean actionable;
    private Long demandeId;
    private String demandeTitle;
    private Long requesterId;
    private String requesterName;
    private Long responderId;
    private String responderName;
    private LocalDateTime createdAt;
    private NotificationMethod methode;
    
    


	public NotificationDto() {}
    
public static NotificationDto fromEntity(Notification notification) {
    NotificationDto dto = new NotificationDto();
    dto.setId(notification.getId());
    dto.setType(notification.getType() != null ? notification.getType().name() : null);
    dto.setStatut(notification.getStatut() != null ? notification.getStatut().name() : null);
    dto.setMessage(notification.getMessage());
    dto.setMotif(notification.getMotif());
    dto.setRead(notification.getIsRead());
    dto.setActionable(notification.isActionable());
    dto.setMethode(notification.getMethod());
    dto.setDemandeTitle(notification.getDemandeTitle());
    
    if (notification.getDemande() != null) {
        dto.setDemandeId(notification.getDemande().getId());
    }

    if (notification.getRequester() != null) {
        dto.setRequesterId(notification.getRequester().getId());
        dto.setRequesterName(notification.getRequester().getFirstName() + " " + 
                           notification.getRequester().getLastName());
    }

    if (notification.getResponder() != null) {
        dto.setResponderId(notification.getResponder().getId());
        dto.setResponderName(notification.getResponder().getFirstName() + " " + 
                           notification.getResponder().getLastName());
    }

    if (notification.getCreatedAt() != null) {
        dto.setCreatedAt(notification.getCreatedAt());
    }

    return dto;
}
    
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getStatut() {
		return statut;
	}
	public void setStatut(String statut) {
		this.statut = statut;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getMotif() {
		return motif;
	}
	public void setMotif(String motif) {
		this.motif = motif;
	}
	public boolean isRead() {
		return isRead;
	}
	public void setRead(boolean isRead) {
		this.isRead = isRead;
	}
	public boolean isActionable() {
		return actionable;
	}
	public void setActionable(boolean actionable) {
		this.actionable = actionable;
	}
	public Long getDemandeId() {
		return demandeId;
	}
	public void setDemandeId(Long demandeId) {
		this.demandeId = demandeId;
	}
	public String getDemandeTitle() {
		return demandeTitle;
	}
	public void setDemandeTitle(String demandeTitle) {
		this.demandeTitle = demandeTitle;
	}
	public Long getRequesterId() {
		return requesterId;
	}
	public void setRequesterId(Long requesterId) {
		this.requesterId = requesterId;
	}
	public String getRequesterName() {
		return requesterName;
	}
	public void setRequesterName(String requesterName) {
		this.requesterName = requesterName;
	}
	public Long getResponderId() {
		return responderId;
	}
	public void setResponderId(Long responderId) {
		this.responderId = responderId;
	}
	public String getResponderName() {
		return responderName;
	}
	public void setResponderName(String responderName) {
		this.responderName = responderName;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public NotificationMethod getMethode() {
		return methode;
	}

	public void setMethode(NotificationMethod methode) {
		this.methode = methode;
	}

	
    
}
