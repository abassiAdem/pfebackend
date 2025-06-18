package com.example.demo.entities;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
@Data
@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "formulaire")
public class Formulaire {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    private Boolean valide;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "type_id", nullable = false)
    @JsonManagedReference 
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Type typeformulaire;

    @OneToMany(
            mappedBy = "formulaire",
            cascade = CascadeType.ALL,
            orphanRemoval = true
        )
        @JsonManagedReference
        @EqualsAndHashCode.Exclude
        @ToString.Exclude
        private Set<Champ> champs = new HashSet<>();
    
    public void addChamp(Champ champ) {
        champs.add(champ);
        champ.setFormulaire(this);
    }

    public void removeChamp(Champ champ) {
        champs.remove(champ);
        champ.setFormulaire(null);
    }
    
    
    
    
    
    
    
    
    
    
    
    public void setTypeformulaire(Type type) {
        if (this.typeformulaire != null) {
            this.typeformulaire.setFormulaire(null);
        }
        this.typeformulaire = type;
        if (type != null) {
            type.setFormulaire(this);
        }
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

	public Set<Champ> getChamps() {
		return champs;
	}

	public void setChamps(Set<Champ> champs) {
		this.champs = champs;
	}

	public Type getTypeformulaire() {
		return typeformulaire;
	}
}