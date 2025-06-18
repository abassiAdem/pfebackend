package com.example.demo.entities;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
@Data
@Entity
@Table(name = "responsable_fonctionnel")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ResponsableFonctionnel {

    @Id
    @EqualsAndHashCode.Include
    private Long id;


    @OneToOne(fetch = FetchType.LAZY )
    @MapsId
    @JoinColumn(name = "id")
    private AppUser agent;
	@OneToMany(mappedBy = "responsable")
    private Set<Realisateur> realisateurs;



    public ResponsableFonctionnel() {}

    public void setAgent(AppUser agent) {
        this.agent = agent;
    }

    public AppUser getAgent() {
        return agent;
    }

    public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Set<Realisateur> getRealisateurs() {
		return realisateurs;
	}

	public void setRealisateurs(Set<Realisateur> realisateurs) {
		this.realisateurs = realisateurs;
	}


}