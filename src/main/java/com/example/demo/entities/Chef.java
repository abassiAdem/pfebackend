package com.example.demo.entities;
import java.util.Set;

import jakarta.persistence.*;
import lombok.Data;
@Entity
@Table(name = "chef")
public class Chef {
    @Id
    private Long id;
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id")
    private AppUser agent;
    public void setAgent(AppUser agent) {
        this.agent = agent;
    }

    @OneToMany(mappedBy = "chef" )
    private Set<Employee> employees;
    
    
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Set<Employee> getEmployees() {
		return employees;
	}

	public void setEmployees(Set<Employee> employees) {
		this.employees = employees;
	}

	public AppUser getAgent() {
		return agent;
	}
}