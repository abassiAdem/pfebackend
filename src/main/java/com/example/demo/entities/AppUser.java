
package com.example.demo.entities;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;


@Entity
@Table(name = "agent")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class AppUser {

		public AppUser(Long id, String firstName, String lastName, String email, 
				String keycloakId,
	            Realisateur realisateur, ResponsableFonctionnel responsable, 
	            Chef chef, Admin admin) {
	 this.id = id;
	 this.firstName = firstName;
	 this.lastName = lastName;
	 this.email = email;
	 this.keycloakId=keycloakId;

	 this.realisateur = realisateur;
	 this.responsable = responsable;
	 this.chef = chef;
	 this.admin = admin;
	}
    public AppUser() {
    	
    }

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
	
	
	
	public String getRole() {
		return role;
	}
	public void setRole(String role) {
		this.role = role;
	}

	private String firstName;
	private String lastName;
	private String email;
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "departement_id")
    private Departement departement;

    private Boolean isActive = false; 


    
    @OneToOne(mappedBy = "agent", cascade = CascadeType.ALL, orphanRemoval = false)
    @JsonIgnore  
    private Realisateur realisateur;
    @OneToOne(mappedBy = "agent",cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore  
    private Employee employee;
    @OneToOne(mappedBy = "agent",cascade = CascadeType.ALL, orphanRemoval = false)
    @JsonIgnore  
    private ResponsableFonctionnel responsable;
    
    @OneToOne(mappedBy = "agent", cascade = CascadeType.ALL, orphanRemoval = false)
    @JsonIgnore 
    private Chef chef;
    
    @OneToOne(mappedBy = "agent",cascade = CascadeType.ALL, orphanRemoval = false)
    @JsonIgnore 
    private Admin admin;
    @Column(name = "role")
    private String role;
    @Column(name = "last_login")
    private LocalDateTime lastLogin;
    @Column(unique = true)
    private String keycloakId;
    
    private boolean isUserOnline(AppUser user) {
        return user.getIsActive() != null && user.getIsActive();
    }
    public LocalDateTime getLastLogin() {
		return lastLogin;
	}
	public void setLastLogin(LocalDateTime lastLogin) {
		this.lastLogin = lastLogin;
	}
	public Long getId() {
        return this.id;
    }
	public String getKeycloakId() {
		return keycloakId;
	}

	public void setKeycloakId(String keycloakId) {
		this.keycloakId = keycloakId;
	}



	public Realisateur getRealisateur() {
		return realisateur;
	}

	public void setRealisateur(Realisateur realisateur) {
		this.realisateur = realisateur;
	}

	public ResponsableFonctionnel getResponsable() {
		return responsable;
	}

	public void setResponsable(ResponsableFonctionnel responsable) {
		this.responsable = responsable;
	}

	public Chef getChef() {
		return chef;
	}

	public void setChef(Chef chef) {
		this.chef = chef;
	}

	public Admin getAdmin() {
		return admin;
	}

	public void setAdmin(Admin admin) {
		this.admin = admin;
	}

	public void setId(Long id) {
		this.id = id;
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

    public Employee getEmployee() {
		return employee;
	}
	public void setEmployee(Employee employee) {
		this.employee = employee;
	}
	public Boolean getIsActive() {
		return isActive;
	}
	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
	}

	
	public Departement getDepartement() {
		return departement;
	}

	public void setDepartement(Departement departement) {
		this.departement = departement;
	}

}
