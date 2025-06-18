package com.example.demo.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.example.demo.enmus.StatutDemande;

public class DemandeResponseDTO {
    private Long id;
    private String title;
    private String infoSup;
    private StatutDemande statut;
    private String urgence;
    private Map<String, Object> data;
    private String justification;
    private LocalDate dateCreation;
    private LocalDate dateAcceptation;
    private LocalDate dateEnCours;
    private LocalDate dateTerminee;
    private LocalDate dateDependence;
    private String demandeurName;
    private String realisateurName;
    private Long dureeTravailRealisateur;
    private Long dureeRetardDependence;
    private Long dureEstimee;
    private  LocalDate dateAffectation;
    private Long delayCreationAccept;
    private Long delayAcceptEnCours;
    private Long totalMainDelay;
    private Boolean approbationModification;
    private Boolean approbationAnnulation;
    private List<DemandeResponseDTO> dependentDemandes;
    private String type;
    private LocalDate dateEstime;
    private Long parentDemandeId;
    private String parentDemandeTitle;
    private Boolean isAttached;
    private Boolean   depend;
    
    
    
    
    
    
	public Boolean getDepend() {
		return depend;
	}
	public void setDepend(Boolean depend) {
		this.depend = depend;
	}
	
	public LocalDate getDateAffectation() {
		return dateAffectation;
	}
	public void setDateAffectation(LocalDate dateAffectation) {
		this.dateAffectation = dateAffectation;
	}
	
	public LocalDate getDateDependence() {
		return dateDependence;
	}
	public void setDateDependence(LocalDate dateDependence) {
		this.dateDependence = dateDependence;
	}
	
	public Long getParentDemandeId() {
		return parentDemandeId;
	}
	public void setParentDemandeId(Long parentDemandeId) {
		this.parentDemandeId = parentDemandeId;
	}
	public String getParentDemandeTitle() {
		return parentDemandeTitle;
	}
	public void setParentDemandeTitle(String parentDemandeTitle) {
		this.parentDemandeTitle = parentDemandeTitle;
	}
	public Boolean getIsAttached() {
		return isAttached;
	}
	public void setIsAttached(Boolean isAttached) {
		this.isAttached = isAttached;
	}
	public Long getDureEstimee() {
		return dureEstimee;
	}
	public void setDureEstimee(Long dureEstimee) {
		this.dureEstimee = dureEstimee;
	}
	public String getRealisateurName() {
		return realisateurName;
	}
	public void setRealisateurName(String realisateurName) {
		this.realisateurName = realisateurName;
	}
	public Long getDureeTravailRealisateur() {
		return dureeTravailRealisateur;
	}
	public void setDureeTravailRealisateur(Long dureeTravailRealisateur) {
		this.dureeTravailRealisateur = dureeTravailRealisateur;
	}
	public Long getDureeRetardDependence() {
		return dureeRetardDependence;
	}
	public void setDureeRetardDependence(Long dureeRetardDependence) {
		this.dureeRetardDependence = dureeRetardDependence;
	}
	public Long getDelayCreationAccept() {
		return delayCreationAccept;
	}
	public void setDelayCreationAccept(Long delayCreationAccept) {
		this.delayCreationAccept = delayCreationAccept;
	}
	public Long getDelayAcceptEnCours() {
		return delayAcceptEnCours;
	}
	public void setDelayAcceptEnCours(Long delayAcceptEnCours) {
		this.delayAcceptEnCours = delayAcceptEnCours;
	}
	public Long getTotalMainDelay() {
		return totalMainDelay;
	}
	public void setTotalMainDelay(Long totalMainDelay) {
		this.totalMainDelay = totalMainDelay;
	}
	public List<DemandeResponseDTO> getDependentDemandes() {
		return dependentDemandes;
	}
	public void setDependentDemandes(List<DemandeResponseDTO> dependentDemandes) {
		this.dependentDemandes = dependentDemandes;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public LocalDate getDateEstime() {
		return dateEstime;
	}
	public void setDateEstime(LocalDate dateEstime) {
		this.dateEstime = dateEstime;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public StatutDemande getStatut() {
		return statut;
	}
	public void setStatut(StatutDemande statut) {
		this.statut = statut;
	}
	public String getUrgence() {
		return urgence;
	}
	public void setUrgence(String urgence) {
		this.urgence = urgence;
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
	public LocalDate getDateCreation() {
		return dateCreation;
	}
	public void setDateCreation(LocalDate dateCreation) {
		this.dateCreation = dateCreation;
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
	public String getDemandeurName() {
		return demandeurName;
	}
	public void setDemandeurName(String demandeurName) {
		this.demandeurName = demandeurName;
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
	public String getInfoSup() {
		return infoSup;
	}
	public void setInfoSup(String infoSup) {
		this.infoSup = infoSup;
	}
	
    
 }