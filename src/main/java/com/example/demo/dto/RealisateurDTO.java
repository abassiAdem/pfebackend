package com.example.demo.dto;

import java.time.LocalDate;

public class RealisateurDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String competences;
    private String metier;
    private String disponibilites;

    private LocalDate firstOccupiedDate;
    private LocalDate firstAvailableDate;
    private Double workloadNext7Days;  // Percentage of occupation in next 7 days
    private Double workloadNext30Days; // Percentage of occupation in next 30 days
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
	public String getCompetences() {
		return competences;
	}
	public void setCompetences(String competences) {
		this.competences = competences;
	}
	public String getMetier() {
		return metier;
	}
	public void setMetier(String metier) {
		this.metier = metier;
	}
	public String getDisponibilites() {
		return disponibilites;
	}
	public void setDisponibilites(String disponibilites) {
		this.disponibilites = disponibilites;
	}
	public LocalDate getFirstOccupiedDate() {
		return firstOccupiedDate;
	}
	public void setFirstOccupiedDate(LocalDate firstOccupiedDate) {
		this.firstOccupiedDate = firstOccupiedDate;
	}
	public LocalDate getFirstAvailableDate() {
		return firstAvailableDate;
	}
	public void setFirstAvailableDate(LocalDate firstAvailableDate) {
		this.firstAvailableDate = firstAvailableDate;
	}
	public Double getWorkloadNext7Days() {
		return workloadNext7Days;
	}
	public void setWorkloadNext7Days(Double workloadNext7Days) {
		this.workloadNext7Days = workloadNext7Days;
	}
	public Double getWorkloadNext30Days() {
		return workloadNext30Days;
	}
	public void setWorkloadNext30Days(Double workloadNext30Days) {
		this.workloadNext30Days = workloadNext30Days;
	}
    
    
}
