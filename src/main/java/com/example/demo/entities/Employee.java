package com.example.demo.entities;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
@Data
@Entity
@Table(name = "employee")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Employee{


	@Id
    @EqualsAndHashCode.Include
    private Long id;

    @OneToOne(fetch = FetchType.LAZY )
    @MapsId
    @JoinColumn(name = "id")
    private AppUser agent;
    @ManyToOne
    @JoinColumn(name = "chef_id")
    private Chef chef;
 
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
	public Chef getChef() {
		return chef;
	}
	public void setChef(Chef chef) {
		this.chef = chef;
	}
}