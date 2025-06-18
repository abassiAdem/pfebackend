package com.example.demo.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
@Entity
@Table(name = "departement")
public class Departement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    // One Departement can have many AppUsers
    @OneToMany(mappedBy = "departement", cascade = CascadeType.ALL)
    private List<AppUser> appUsers;
    
    public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Long getId() {
		// TODO Auto-generated method stub
		return this.id;
	}
}