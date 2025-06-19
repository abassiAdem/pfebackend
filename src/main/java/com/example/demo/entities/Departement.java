package com.example.demo.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;


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
	 public void setId(Long id) {
        this.id = id;
    }
    public List<AppUser> getAppUsers() {
        return appUsers;
    }

    public void setAppUsers(List<AppUser> appUsers) {
        this.appUsers = appUsers;
    }
}
