package com.example.demo.dto;

import java.util.List;
import java.util.Set;

public class FormulaireDTO {
	private Long id;
    private Boolean valide;
    private List<ChampDTO> champs;
    private Long dureEstime;
    
    

	@Override
	public String toString() {
		return "FormulaireDTO [id=" + id + ", valide=" + valide + ", champs=" + champs + ", dureEstime=" + dureEstime
				+ "]";
	}
	public Long getDureEstime() {
		return dureEstime;
	}
	public void setDureEstime(Long dureEstime) {
		this.dureEstime = dureEstime;
	}
	public void setChamps(List<ChampDTO> champs) {
		this.champs = champs;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Boolean getValide() {
		return valide;
	}
	public void setValide(Boolean valide) {
		this.valide = valide;
	}
	public List<ChampDTO> getChamps() {
		return champs;
	}

}