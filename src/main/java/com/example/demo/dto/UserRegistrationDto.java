package com.example.demo.dto;

import java.time.LocalDateTime;

public class UserRegistrationDto {
	private Long id;
	private String firstName;
    private String lastName;
    private String email;
	private String password;
    private String competence;
    private String userType;
    private String chefId;
    private String responsableId;
    private String metier;
    
    private String departementId;

    public String getDepartementId() {
        return departementId;
    }

    public void setDepartementId(String departementId) {
        this.departementId = departementId;
    }
    
    
	@Override
	public String toString() {
		return "UserRegistrationDto [firstName=" + firstName + ", lastName=" + lastName + ", email=" + email
				+ ", password=" + password + ", competence=" + competence + ", userType=" + userType + ", chefId="
				+ chefId + ", responsableId=" + responsableId + ", metier=" + metier + "]";
	} 
    
    
    
    
    
    
    
    
    
    

    public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getMetier() {
		return metier;
	}
	public void setMetier(String metier) {
		this.metier = metier;
	}
	public String getCompetence() {
		return competence;
	}
	public void setCompetence(String competence) {
		this.competence = competence;
	}
	public String getChefId() {
		return chefId;
	}
	public void setChefId(String chefId) {
		this.chefId = chefId;
	}
	public String getResponsableId() {
		return responsableId;
	}
	public void setResponsableId(String responsableId) {
		this.responsableId = responsableId;
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
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}

	public String getUserType() {
		return userType;
	}
	public void setUserType(String userType) {
		this.userType = userType;
	}

}
