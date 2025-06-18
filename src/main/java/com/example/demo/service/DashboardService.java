package com.example.demo.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.dto.DemandeMetierPercentageDTO;
import com.example.demo.dto.TypeDemandePercentageDTO;
import com.example.demo.enmus.StatutDemande;
import com.example.demo.repository.DemandRepository;


@Service
public class DashboardService {


    private final DemandRepository demandeRepository;
	   public DashboardService( DemandRepository demandeRepository){

		   this.demandeRepository=demandeRepository;
	   }
	   
	   public List<TypeDemandePercentageDTO> getPercentageByType( List<StatutDemande> statuses,LocalDate startDate, LocalDate endDate,Long responsableId) {
		    System.out.println("Filtering between: " + startDate + " and " + endDate);
		    List<String> statusStrings = statuses != null 
		            ? statuses.stream().map(Enum::name).collect(Collectors.toList())
		            : null;
		    List<Object[]> results = demandeRepository.countDemandesByTypeStatusDateRangeAndResponsableDepartement(
		    		statusStrings,
		        startDate != null ? startDate.toString() : null,
		        endDate != null ? endDate.toString() : null,
		        		responsableId
		    );
		    System.out.println("Raw results from DB:");
		    results.forEach(row -> System.out.println(row[0] + ": " + row[1]));

		    return results.stream()
		                 .map(row -> new TypeDemandePercentageDTO(
		                     (String)row[0], 
		                     ((Number)row[1]).longValue()
		                 ))
		                 .collect(Collectors.toList());
		}
	   public List<DemandeMetierPercentageDTO> getDemandePercentageByMetier( List<StatutDemande> statuses,LocalDate startDate, LocalDate endDate,Long responsableId) {

		    String startDateStr = startDate != null ? startDate.toString() : null;
		    String endDateStr = endDate != null ? endDate.toString() : null;
		    List<String> statusStrings = statuses != null 
		            ? statuses.stream().map(Enum::name).collect(Collectors.toList())
		            : null;
		    List<Object[]> results = demandeRepository.countByMetierStatusDateRangeAndResponsableDept(statusStrings,startDateStr, endDateStr,responsableId);
		    
		    System.out.println("Querying demandes by métier between: " + startDateStr + " and " + endDateStr);
		    results.forEach(row -> System.out.println("Métier: " + row[0] + ", Count: " + row[1]));
		    
		    return results.stream()
		            .map(row -> new DemandeMetierPercentageDTO(
		                (String) row[0],  
		                ((Number) row[1]).longValue()  
		            ))
		            .collect(Collectors.toList());
		}  
}
