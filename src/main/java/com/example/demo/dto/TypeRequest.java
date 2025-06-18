package com.example.demo.dto;

import java.util.List;



public class TypeRequest {

	private String typeName;
    private Long dureeEstimee;
    private String status;
    private Boolean valide;
    private Boolean formulaireValide;
    private List<ChampRequest> champs;
    
    
    public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public Long getDureeEstimee() {
		return dureeEstimee;
	}
	public void setDureeEstimee(Long dureeEstimee) {
		this.dureeEstimee = dureeEstimee;
	}
	public Boolean getValide() {
		return valide;
	}
	public void setValide(Boolean valide) {
		this.valide = valide;
	}

	public String getTypeName() {
		return typeName;
	}
	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}
	public Boolean getFormulaireValide() {
		return formulaireValide;
	}
	public void setFormulaireValide(Boolean formulaireValide) {
		this.formulaireValide = formulaireValide;
	}
	public List<ChampRequest> getChamps() {
		return champs;
	}
	public void setChamps(List<ChampRequest> champs) {
		this.champs = champs;
	}
}
