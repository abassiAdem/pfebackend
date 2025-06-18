package com.example.demo.dto;

import com.example.demo.enmus.StatutDemande;

public interface StatusMetierCountDTO {
	   StatutDemande getStatut();
	   String getMetier(); 
	   Long getCount();
	   
}
