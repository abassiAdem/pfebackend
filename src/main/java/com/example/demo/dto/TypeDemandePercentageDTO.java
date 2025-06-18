package com.example.demo.dto;

import com.example.demo.enmus.StatutDemande;

public class TypeDemandePercentageDTO {
    private String typeName;
    private double percentage;
    private StatutDemande statut;
    
    public TypeDemandePercentageDTO(String typeName, double percentage) {
        this.typeName = typeName;
        this.percentage = percentage;
    }

    public String getTypeName() {
        return typeName;
    }

    public double getPercentage() {
        return percentage;
    }
}
