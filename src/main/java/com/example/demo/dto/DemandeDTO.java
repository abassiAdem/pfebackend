package com.example.demo.dto;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DemandeDTO {

	@Override
	public String toString() {
		return "DemandeDTO [urgence=" + urgence + ", data=" + data + ", justification=" + justification + ", agentId="
				+ agentId + ", parentId=" + parentId + ", dureeEstimee=" + dureeEstimee + ", typeId=" + type
				 + ", duree_dependence=" + duree_dependence + "]";
	}

	private String urgence;
	@JsonProperty("data")
    private Map<String, Object> data;
    private String justification;
    private Long agentId;     
    private Long parentId;    
    private String infoSup;
    private Long dureeEstimee; 
    private String type;
    private Long duree_dependence;
    private String title;
    private Boolean isAttached;

    
    
    
	public String getInfoSup() {
		return infoSup;
	}
	public void setInfoSup(String infoSup) {
		this.infoSup = infoSup;
	}
	public Boolean getIsAttached() {
		return isAttached;
	}
	public void setIsAttached(Boolean isAttached) {
		this.isAttached = isAttached;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
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
	public Long getAgentId() {
		return agentId;
	}
	public void setAgentId(Long agentId) {
		this.agentId = agentId;
	}
	public Long getParentId() {
		return parentId;
	}
	public void setParentId(Long parentId) {
		this.parentId = parentId;
	}
	public Long getDureeEstimee() {
		return dureeEstimee;
	}
	public void setDureeEstimee(Long dureeEstimee) {
		this.dureeEstimee = dureeEstimee;
	}
	public Long getDuree_dependence() {
		return duree_dependence;
	}
	public void setDuree_dependence(Long duree_dependence) {
		this.duree_dependence = duree_dependence;
	}
}
