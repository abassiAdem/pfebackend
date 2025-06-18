

package com.example.demo.dto;
import java.util.List;

import lombok.Data;

@Data
public class ChampRequest {
    private Long id;
    private String nom;
    private String type;
    private Boolean obligatoire;
    private List<String> options;
    
    public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getNom() {
		return nom;
	}
	public void setNom(String nom) {
		this.nom = nom;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public Boolean getObligatoire() {
		return obligatoire;
	}
	public void setObligatoire(Boolean obligatoire) {
		this.obligatoire = obligatoire;
	}
	public List<String> getOptions() {
		return options;
	}
	public void setOptions(List<String> options) {
		this.options = options;
	}
}

