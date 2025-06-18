package com.example.demo.dto;

public class UserRoleDTO {
	    private Long id;
	    private String firstName;
	    private String lastName;
	    private String email;
	    private String keycloakId;
	    private String role;
	    private Long roleId;
	    private Boolean isActive;
	    
        private String departement;
        
        private String competence;
        private String metier;
	    

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
	    
	    public String getDepartement() {
	        return departement;
	    }
	    
	    public void setDepartement(String departement) {
	        this.departement = departement;
	    }
	    


		public Boolean getIsActive() {
			return isActive;
		}

		public void setIsActive(Boolean isActive) {
			this.isActive = isActive;
		}

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
	    
	    public String getKeycloakId() {
	        return keycloakId;
	    }
	    
	    public void setKeycloakId(String keycloakId) {
	        this.keycloakId = keycloakId;
	    }
	    
	    public String getRole() {
	        return role;
	    }
	    
	    public void setRole(String role) {
	        this.role = role;
	    }
	    
	    public Long getRoleId() {
	        return roleId;
	    }
	    
	    public void setRoleId(Long roleId) {
	        this.roleId = roleId;
	    }
}
