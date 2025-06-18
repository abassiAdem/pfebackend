package com.example.demo.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

public class AffectationRequest {
    private LocalDate  dateAffectation;
    private Long dureEstimee;


	public Long getDureEstimee() {
		return dureEstimee;
	}

	public void setDureEstimee(Long dureEstimee) {
		this.dureEstimee = dureEstimee;
	}

	public LocalDate getDateAffectation() {
        return dateAffectation;
    }
    
    public void setDateAffectation(LocalDate dateAffectation) {
        this.dateAffectation = dateAffectation;
    }
    

	@Override
	public String toString() {
		return "AffectationRequest [dateAffectation=" + dateAffectation + ", dureEstimee=" + dureEstimee + "]";
	}
    
    
    
    
    
    


}
