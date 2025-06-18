package com.example.demo.dto;

import java.time.LocalDateTime;

public class TypeDTO {
	private Long id;
    private String name;
    private long dureeEstimee;
    private String status;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
    private FormulaireDTO formulaire;
    
    
    
    
    
    
    
    
    
    
    public long getDureeEstimee() {
		return dureeEstimee;
	}
	public void setDureeEstimee(long dureeEstimee) {
		this.dureeEstimee = dureeEstimee;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public LocalDateTime getDateCreation() {
		return dateCreation;
	}
	public void setDateCreation(LocalDateTime dateCreation) {
		this.dateCreation = dateCreation;
	}
	public LocalDateTime getDateModification() {
		return dateModification;
	}
	public void setDateModification(LocalDateTime dateModification) {
		this.dateModification = dateModification;
	}
	public FormulaireDTO getFormulaire() {
		return formulaire;
	}
	public void setFormulaire(FormulaireDTO formulaire) {
		this.formulaire = formulaire;
	}
}
