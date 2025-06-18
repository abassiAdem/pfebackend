package com.example.demo.config;

import java.util.Collections;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.ChampRequest;
import com.example.demo.dto.TypeRequest;
import com.example.demo.service.FormulaireService;

@Configuration
public class DataInitializer {

    private final FormulaireService typeService;

    public DataInitializer(FormulaireService typeService) {
        this.typeService = typeService;
    }

    @Bean
    CommandLineRunner initDefaultTypes() {
        return args -> initializeDefaults();
    }

    @Transactional
    public void initializeDefaults() {
        if (!typeService.existsByName("Normal")) {
            TypeRequest normal = new TypeRequest();
            normal.setTypeName("Normal");
            normal.setStatus("active");
            normal.setDureeEstimee(0L);
            normal.setFormulaireValide(false);  
            normal.setChamps(Collections.emptyList());
            typeService.createTypeWithForm(normal);
        }

        if (!typeService.existsByName("Congé")) {
            ChampRequest dateDebut = new ChampRequest();
            dateDebut.setNom("dateDebut");
            dateDebut.setType("date");
            dateDebut.setObligatoire(true);
            dateDebut.setOptions(null);

            ChampRequest dureeConge = new ChampRequest();
            dureeConge.setNom("dureeConge");
            dureeConge.setType("text");
            dureeConge.setObligatoire(true);
            dureeConge.setOptions(null);

            TypeRequest conge = new TypeRequest();
            conge.setTypeName("Congé");
            conge.setStatus("active");
            conge.setDureeEstimee(0L);
            conge.setFormulaireValide(false);
            conge.setChamps(List.of(dateDebut, dureeConge));
            typeService.createTypeWithForm(conge);
        }
    }
}