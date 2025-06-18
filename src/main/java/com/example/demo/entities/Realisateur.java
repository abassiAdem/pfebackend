package com.example.demo.entities;
import java.time.LocalDate;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.*;
import lombok.Data;
@Data
@Entity
@Table(name = "realisateur")
public class Realisateur {
	@Id
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id")
    private AppUser agent;

    @Column(columnDefinition = "TEXT")
    private String competences;

    @Column(columnDefinition = "TEXT")
    private String metier;
    
    @Column(columnDefinition = "TEXT")
    private String disponibilites;

    private String firstName;

    private String lastName;
    private String email;
    
    private LocalDate occupeAt;
    @ManyToOne
    @JoinColumn(name = "chef_id")
    private ResponsableFonctionnel responsable;

	public String getMetier() {
		return metier;
	}


	public void setMetier(String metier) {
		this.metier = metier;
	}


	public LocalDate getOccupeAt() {
		return occupeAt;
	}


	public void setOccupeAt(LocalDate occupeAt) {
		this.occupeAt = occupeAt;
	}


	public String getFirstName() {
		return firstName;
	}


	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}


	public String getLastName() {
		return lastName;
	}


	public void setLastName(String lastName) {
		this.lastName = lastName;
	}


	public String getEmail() {
		return email;
	}


	public void setEmail(String email) {
		this.email = email;
	}


	public ResponsableFonctionnel getResponsable() {
		return responsable;
	}


	public void setResponsable(ResponsableFonctionnel responsable) {
		this.responsable = responsable;
	}



    
    
    
    @PrePersist
    public void setDefaultDisponibilites() {
        if (this.disponibilites == null || this.disponibilites.isEmpty()) {
            this.disponibilites = "Disponible";
        }
    }
    
    

    public Long getId() {
		return id;
	}


	public void setId(Long id) {
		this.id = id;
	}


	public AppUser getAgent() {
		return agent;
	}


	public void setAgent(AppUser agent) {
		this.agent = agent;
	}


	public String getCompetences() {
		return competences;
	}


	public void setCompetences(String competences) {
		this.competences = competences;
	}


	public String getDisponibilites() {
		return disponibilites;
	}


	public void setDisponibilites(String disponibilites) {
		this.disponibilites = disponibilites;
	}





}