package com.example.demo.dto;

public class UserUpdateDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String password; // Optional for updates
    private String userType; // In case role needs to change
    private String chefId;
    private String responsableId;
    private String competence;
    private String metier;
	public Long getId() {
		return id;
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
	public String getCompetence() {
		return competence;
	}
	public void setCompetence(String competence) {
		this.competence = competence;
	}
	public String getMetier() {
		return metier;
	}
	public void setMetier(String metier) {
		this.metier = metier;
	}
}
