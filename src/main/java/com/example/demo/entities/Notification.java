package com.example.demo.entities;
import java.util.HashSet;
import java.util.Set;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.example.demo.enmus.NotificationType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.example.demo.enmus.NotificationMethod;
import com.example.demo.enmus.NotificationStatus; 

@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Table(name = "notification")
public class Notification {
		@Id
		@GeneratedValue(strategy = GenerationType.IDENTITY)
		private Long id;
		
		@Enumerated(EnumType.STRING)
		@Column(length = 50)
		private NotificationType type;
		
		@Enumerated(EnumType.STRING)
		@Column(name = "status")
		private NotificationStatus statut;
		
		@Column(name = "message", length = 4000)
		private String message;
		
		@Column(name = "motif", length = 4000)
		private String motif;
		
		@Column(name = "demande_title", length = 4000)
		private String demandeTitle;
		
		private Boolean isRead;
		
		@ManyToOne(fetch = FetchType.EAGER)
		@JoinColumn(name = "demande_id")
		private Demande demande;
		
		@ManyToOne
		@JoinColumn(name = "user_id")
		private AppUser requester;
		
		@ManyToOne
		@JoinColumn(name = "supervisor_id")
		private AppUser responder;
		
		@Column(name = "created_at")
		private LocalDateTime createdAt;
		
		@Column(name = "updated_at")
		private LocalDateTime updatedAt;
		
		@Column(nullable = false, columnDefinition = "boolean default false")
		private Boolean isActionable = false;
		
		@Enumerated(EnumType.STRING)
		@Column(name = "method")
		private NotificationMethod method = NotificationMethod.BOTH;
		
		@PrePersist
		protected void onCreate() {
		createdAt = LocalDateTime.now();
		updatedAt = LocalDateTime.now();
		isRead = false;
		}
		public String getDemandeTitle() {
		return demandeTitle;
		}



	public void setDemandeTitle(String demandeTitle) {
		this.demandeTitle = demandeTitle;
	}

    public NotificationMethod getMethod() {
		return method;
	}

	public void setMethod(NotificationMethod method) {
		this.method = method;
	}

	@PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public Notification() {}
    

    public boolean isActionable() {
		return isActionable;
	}

	public void setActionable(boolean isActionable) {
		this.isActionable = isActionable;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}


	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}


	public NotificationType getType() {
		return type;
	}

	public void setType(NotificationType type) {
		this.type = type;
	}

	public NotificationStatus getStatut() {
		return statut;
	}

	public void setStatut(NotificationStatus statut) {
		this.statut = statut;
	}

	public Boolean getIsRead() {
		return isRead;
	}

	public void setIsRead(Boolean isRead) {
		this.isRead = isRead;
	}

	public Demande getDemande() {
		return demande;
	}

	public void setDemande(Demande demande) {
		this.demande = demande;
	}


	public AppUser getRequester() {
		return requester;
	}

	public void setRequester(AppUser requester) {
		this.requester = requester;
	}

	public AppUser getResponder() {
		return responder;
	}

	public void setResponder(AppUser responder) {
		this.responder = responder;
	}



	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	public Boolean getIsActionable() {
		return isActionable;
	}

	public void setIsActionable(Boolean isActionable) {
		this.isActionable = isActionable;
	}

	public String getMotif() {
		return motif;
	}

	public void setMotif(String motif) {
		this.motif = motif;
	}
	
	
}