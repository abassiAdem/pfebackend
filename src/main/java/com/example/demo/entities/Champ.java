package com.example.demo.entities;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@Entity
@Table(name = "champ")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Champ {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(length = 100)
    private String nom;

    @Column(length = 50)
    private String type;

    private Boolean obligatoire;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "formulaire_id", nullable = false)
    @JsonBackReference
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Formulaire formulaire;

    @OneToMany(mappedBy = "champ", cascade = CascadeType.ALL, orphanRemoval = true ,fetch= FetchType.EAGER  )
  
    private List<Option> options = new ArrayList<>();
    
    
    
    
    
    /*public void addToFormulaire(Formulaire formulaire) {
        this.formulaires.add(formulaire);
        formulaire.getChamps().add(this);
    }

    public void removeFromFormulaire(Formulaire formulaire) {
        this.formulaires.remove(formulaire);
        formulaire.getChamps().remove(this);
    }*/

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

	public Formulaire getFormulaire() {
		return formulaire;
	}

	public void setFormulaire(Formulaire formulaire) {
		this.formulaire = formulaire;
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

	public List<Option> getOptions() {
		return options;
	}

	public void setOptions(List<Option> options) {
		this.options = options;
	}
}