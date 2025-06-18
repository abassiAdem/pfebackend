package com.example.demo.dto;

import java.time.LocalDate;
import java.util.Map;

import com.example.demo.enmus.StatutDemande;

public class ResponseDem {
    private Long id;
    private StatutDemande statut;
    private String urgence;
    private String justification;
    private LocalDate dateCreation;
    private LocalDate dateEnCours;
    private LocalDate dateTerminee;
    private String title;
    private String type;
    private String demandeurName;
    private Boolean approbationModification;
    private Boolean approbationAnnulation;
    private String infoSup;
    private Boolean   depend;
    
    
    
    
    
    
	public Boolean getDepend() {
		return depend;
	}
	public void setDepend(Boolean depend) {
		this.depend = depend;
	}
	public String getInfoSup() {
		return infoSup;
	}
	public void setInfoSup(String infoSup) {
		this.infoSup = infoSup;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
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
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
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
	

}
