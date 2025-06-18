package com.example.demo.dto;

import com.example.demo.enmus.StatutDemande;

public interface AgentDemandesCountDTO {
    StatutDemande getStatut();
    Long getCount();
}