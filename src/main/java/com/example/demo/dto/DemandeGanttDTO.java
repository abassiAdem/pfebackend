package com.example.demo.dto;

import java.time.LocalDate;
import java.util.List;

import com.example.demo.enmus.StatutDemande;

public class DemandeGanttDTO {
    private String id;
    private String title;
    private StatutDemande statut;
    private LocalDate startDate;
    private LocalDate endDate;
    private String color;
    private List<Long> dependencies; 
    //private RealisateurGanttDTO realisateur;
    private Long realisateurId; 
    private String realisateurName; 
    private String metier;
    
    public String getMetier() {
		return metier;
	}
	public void setMetier(String metier) {
		this.metier = metier;
	}
	public void setId(String string) {
        this.id=string;

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
	public LocalDate getStartDate() {
		return startDate;
	}
	public void setStartDate(LocalDate startDate) {
		this.startDate = startDate;
	}
	public LocalDate getEndDate() {
		return endDate;
	}
	public void setEndDate(LocalDate endDate) {
		this.endDate = endDate;
	}
	public String getColor() {
		return color;
	}
	public void setColor(String color) {
		this.color = color;
	}
	public List<Long> getDependencies() {
		return dependencies;
	}
	public void setDependencies(List<Long> dependencies) {
		this.dependencies = dependencies;
	}
	public Long getRealisateurId() {
		return realisateurId;
	}
	public void setRealisateurId(Long realisateurId) {
		this.realisateurId = realisateurId;
	}
	public String getRealisateurName() {
		return realisateurName;
	}
	public void setRealisateurName(String realisateurName) {
		this.realisateurName = realisateurName;
	}
	public String getId() {
		return id;
	}
    
}
