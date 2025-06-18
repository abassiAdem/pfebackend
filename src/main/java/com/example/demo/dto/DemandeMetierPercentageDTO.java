package com.example.demo.dto;

import com.example.demo.enmus.StatutDemande;

public class DemandeMetierPercentageDTO {
    private String metier;
    private double percentage;
    private StatutDemande statut;
    
    public DemandeMetierPercentageDTO(String metier, double percentage) {
        this.metier = metier;
        this.percentage = percentage;
    }

    public String getMetier() {
        return metier;
    }

    public double getPercentage() {
        return percentage;
    }

}
