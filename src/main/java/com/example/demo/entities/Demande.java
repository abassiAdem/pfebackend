package com.example.demo.entities;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.stereotype.Service;
import java.time.Duration;
import com.example.demo.enmus.StatutDemande;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDate;
import jakarta.persistence.*;
import lombok.Data;
@Data
@Entity
@Table(name = "demande")
public class Demande {
	 	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}



	public Map<String, Object> getData() {
		return data;
	}

	public void setData(Map<String, Object> data) {
		this.data = data;
	}

	public String getJustification() {
		return justification;
	}

	public void setJustification(String justification) {
		this.justification = justification;
	}

	public Long getDureeEstimee() {
		return dureeEstimee;
	}

	public void setDureeEstimee(Long dureeEstimee) {
		this.dureeEstimee = dureeEstimee;
	}

	public LocalDate getDateEstimee() {
		return dateEstimee;
	}

	public void setDateEstimee(LocalDate dateEstimee) {
		this.dateEstimee = dateEstimee;
	}

	public LocalDate getDateDependence() {
		return dateDependence;
	}

	public void setDateDependence(LocalDate dateDependence) {
		this.dateDependence = dateDependence;
	}

	public LocalDate getDateCreation() {
		return dateCreation;
	}

	public void setDateCreation(LocalDate dateCreation) {
		this.dateCreation = dateCreation;
	}

	public LocalDate getDateModification() {
		return dateModification;
	}

	public void setDateModification(LocalDate dateModification) {
		this.dateModification = dateModification;
	}

	public LocalDate getDateAcceptation() {
		return dateAcceptation;
	}

	public void setDateAcceptation(LocalDate dateAcceptation) {
		this.dateAcceptation = dateAcceptation;
	}

	public LocalDate getDateEnCours() {
		return dateEnCours;
	}

	public void setDateEnCours(LocalDate dateEnCours) {
		this.dateEnCours = dateEnCours;
	}

	public LocalDate getDateTerminee() {
		return dateTerminee;
	}

	public void setDateTerminee(LocalDate dateTerminee) {
		this.dateTerminee = dateTerminee;
	}

	public Boolean getApprobationModification() {
		return approbationModification;
	}

	public void setApprobationModification(Boolean approbationModification) {
		this.approbationModification = approbationModification;
	}

	public Boolean getApprobationAnnulation() {
		return approbationAnnulation;
	}

	public void setApprobationAnnulation(Boolean approbationAnnulation) {
		this.approbationAnnulation = approbationAnnulation;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public Demande getDemandeParent() {
		return demandeParent;
	}

	public void setDemandeParent(Demande demandeParent) {
		this.demandeParent = demandeParent;
	}

	public List<Demande> getDependentDemandes() {
		return dependentDemandes;
	}

	public void setDependentDemandes(List<Demande> dependentDemandes) {
		this.dependentDemandes = dependentDemandes;
	}

	public AppUser getAgent() {
		return agent;
	}

	public void setAgent(AppUser agent) {
		this.agent = agent;
	}

	public Realisateur getRealisateur() {
		return realisateur;
	}

	public void setRealisateur(Realisateur realisateur) {
		this.realisateur = realisateur;
	}

    public String getUrgence() {
		return urgence;
	}

	public void setUrgence(String urgence) {
		this.urgence = urgence;
	}
	public StatutDemande getStatut() {
		return statut;
	}
	
		@Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Long id;

	    @Enumerated(EnumType.STRING)
	    @Column(length = 50)
	    private StatutDemande statut = StatutDemande.EN_ATTENTE_DE_CHEF; 

	    private String urgence;


		@JdbcTypeCode(SqlTypes.JSON)
	    @Column(columnDefinition = "jsonb")
	    private Map<String, Object> data;

	    @Column(columnDefinition = "TEXT")
	    private String justification;
	  
	    @Column(columnDefinition = "TEXT")
	    private String infoSup;

	    @Column(name = "duree_estimee")
	    private Long dureeEstimee;
	    
	    @Column(name = "date_estimee")
	    private LocalDate dateEstimee;
	    
	    @Column(name = "date_dependence")
	    private LocalDate dateDependence;

	    @Column(name = "date_creation")
	    @Temporal(TemporalType.DATE)
	    private LocalDate dateCreation;

	    @Column(name = "date_modification")
	    private LocalDate dateModification;

	    @Column(name = "date_acceptation")
	    private LocalDate dateAcceptation;

	    @Column(name = "date_en_cours")
	    private LocalDate dateEnCours;
	    

	    @Column(name = "date_terminee")
	    private LocalDate dateTerminee;

	    @Column(name = "date_affectation")
	    private LocalDate dateAffectation;

	    @Column(name = "approbation_modification")
	    private Boolean approbationModification = false;

	    @Column(name = "approbation_annulation")
	    private Boolean approbationAnnulation = false;


	    @ManyToOne
	    @JoinColumn(name = "type_id")
	    @JsonIgnoreProperties({"demandes", "formulaire"}) 
	    private Type type;

	    @ManyToOne(fetch = FetchType.LAZY)
	    @JoinColumn(name = "demande_parent_id")
	    @JsonIgnore
	    private Demande demandeParent;

	    @OneToMany(mappedBy = "demandeParent", cascade = CascadeType.ALL)
	    private List<Demande> dependentDemandes = new ArrayList<>();

	    @ManyToOne(fetch = FetchType.LAZY)
	    @JoinColumn(name = "agent_id")
	    @JsonIgnore
	    private AppUser agent;

	    @ManyToOne
	    @JoinColumn(name = "realisateur_id")
	    private Realisateur realisateur;
	    @Column(name = "is_attached")
	    private Boolean isAttached = false;
	    
	    /*
	    @Column(name = "depend")
	    private Boolean depend = false;
	    
	    public Boolean getDepend() {
			return depend;
		}

		public void setDepend(Boolean depend) {
			this.depend = depend;
		}*/
	    
		public Boolean getIsAttached() {
			return isAttached;
		}

		public void setIsAttached(Boolean isAttached) {
			this.isAttached = isAttached;
		}

		@PrePersist
	    protected void onCreate() {
	        dateCreation = LocalDate.now();
	        dateModification = LocalDate.now();
	    }

	    @PreUpdate
	    protected void onUpdate() {
	        dateModification = LocalDate.now();
	    }

	    private String title;
	    @OneToMany(mappedBy = "demande", cascade = CascadeType.ALL, orphanRemoval = true)
	    private List<Notification> notifications = new ArrayList<>();
	    
		public List<Notification> getNotifications() {
			return notifications;
		}

		public void setNotifications(List<Notification> notifications) {
			this.notifications = notifications;
		}
	    public LocalDate getDateAffectation() {
			return dateAffectation;
		}

		public void setDateAffectation(LocalDate dateAffectation) {
			this.dateAffectation = dateAffectation;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}
		public void setStatut(StatutDemande statut) {
            this.statut = statut;

            switch (statut) {
                case ACCEPTEE:
                    this.dateAcceptation = LocalDate.now();
                    break;
                case EN_COURS:
                    if (this.dateEnCours == null) {
                        this.dateEnCours = LocalDate.now();
                    }
                    break;
                case TERMINEE:
                    this.dateTerminee = LocalDate.now();
                    break;
                case ANNULEE:
                    this.dateTerminee = LocalDate.now();
                case REJECTEE:
                    this.dateTerminee = LocalDate.now();

                default:
                    break;
            }
            this.dateModification = LocalDate.now();
        }

		public String getInfoSup() {
			return infoSup;
		}

		public void setInfoSup(String infoSup) {
			this.infoSup = infoSup;
		}

}