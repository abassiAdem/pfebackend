package com.example.demo.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.DemandeMetierPercentageDTO;
import com.example.demo.dto.TypeDemandePercentageDTO;
import com.example.demo.dto.UserActiveStatusDTO;
import com.example.demo.dto.UserActivityDTO;
import com.example.demo.enmus.StatutDemande;
import com.example.demo.service.DashboardService;
import com.example.demo.service.UserRegistrationService;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    
    private DashboardService dashboardService;
    private UserRegistrationService userservice;
    public DashboardController(DashboardService dashboardService,UserRegistrationService userservice) {
    	this.dashboardService=dashboardService;
    	this.userservice=userservice;
    }

    
    @GetMapping("/activity/last-7-days")
    public List<UserActivityDTO> getLast7DaysActivity() {
        return userservice.getUserActivityForLast7Days();
    }

    @GetMapping("/active-status")
    public ResponseEntity<List<UserActiveStatusDTO>> getActiveUserStatus() {
        List<UserActiveStatusDTO> statusList = userservice.getActiveUserStatus();
        return ResponseEntity.ok(statusList);
    }
    
    //new
    @GetMapping("/percentage-by-type/{responsableId}")
    public ResponseEntity<List<TypeDemandePercentageDTO>> getPercentageByType(@RequestParam(required = false) List<StatutDemande> statuses,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,@PathVariable Long responsableId) {

        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before end date");
        }
        System.out.println(startDate);
        System.out.println(endDate);

        return ResponseEntity.ok(dashboardService.getPercentageByType(statuses,startDate, endDate,responsableId));
    }

    
    //NEW
    @GetMapping("/percentage-by-metier/{responsableId}")
    public ResponseEntity<List<DemandeMetierPercentageDTO>> getDemandePercentageByMetier(@PathVariable Long responsableId ,@RequestParam(required = false) List<StatutDemande> statuses,  @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

    if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
        throw new IllegalArgumentException("Start date must be before end date");
    } 
   
        return ResponseEntity.ok(dashboardService.getDemandePercentageByMetier(statuses,startDate,endDate,responsableId));
    }
}
