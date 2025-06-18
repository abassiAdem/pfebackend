package com.example.demo.dto;
import java.util.List;
public class ChampDTO {
	 private Long id;
	    private String nom;
	    private String type;
	    private List<OptionDTO> options;
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
		private Boolean obligatoire;
	   public List<OptionDTO> getOptions() {
			return options;
		}
		public void setOptions(List<OptionDTO> options) {
			this.options = options;
		}
	
}