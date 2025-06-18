package com.example.demo.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.ApprobationDto;
import com.example.demo.dto.InformationSupplementaireDto;
import com.example.demo.dto.NotificationDto;
import com.example.demo.entities.Notification;
import com.example.demo.service.NotificationService;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService ns;
    
    @Autowired 
    public NotificationController(NotificationService ns) {
        this.ns = ns;
    }

 

    @GetMapping("/all/{userId}")
    public ResponseEntity<List<NotificationDto>> getAllUserNotifications(@PathVariable Long userId) {
        return ResponseEntity.ok(ns.getAllUserNotifications(userId));
    }


 

    @PatchMapping("/{id}/read")
    public ResponseEntity<NotificationDto> markAsRead(@PathVariable Long id) {
        return ResponseEntity.ok(ns.markAsRead(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteNotification(@PathVariable Long id) {
        try {
            ns.deleteNotification(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Notification deleted successfully.");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/request/modification")
    public ResponseEntity<NotificationDto> requestModificationApproval(@RequestBody ApprobationDto dto) {
        try {
        	
        	NotificationDto notification = ns.requestModificationApproval(dto);
            return ResponseEntity.ok(notification);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    @PatchMapping("/mark-all-read")
    public ResponseEntity<Integer> markAllUnreadAsRead() {
        int updatedCount = ns.markAllUnreadAsRead();
        return ResponseEntity.ok(updatedCount);
    }


    @PostMapping("/approve/modification")
    public ResponseEntity<NotificationDto> approveModification(@RequestBody ApprobationDto dto) {
    	NotificationDto notification = ns.approveModification(dto);
        return ResponseEntity.ok(notification);
    }

    @PostMapping("/approve/cancellation")
    public ResponseEntity<NotificationDto> approveCancellation(@RequestBody ApprobationDto dto) {
    	NotificationDto notification = ns.approveCancellation(dto);
        return ResponseEntity.ok(notification);
    }

    @PostMapping("/request/cancellation")
    public ResponseEntity<NotificationDto> requestCancellationApproval(@RequestBody ApprobationDto dto) {
    	System.out.println(dto.toString());
    	NotificationDto notification = ns.requestCancellationApproval(dto);
        return ResponseEntity.ok(notification);
    }

    @PostMapping("/reject")
    public ResponseEntity<NotificationDto> rejectRequest(@RequestBody ApprobationDto dto) {
    	NotificationDto notification = ns.rejectModificationOrCancellation(dto);
        return ResponseEntity.ok(notification);
    }
    
    @PostMapping("/create/information-request")
    public ResponseEntity<NotificationDto> createInformationRequest(@RequestBody InformationSupplementaireDto dto) {

    	NotificationDto notification=ns.createAdditionalInformationRequest(dto);
        return ResponseEntity.ok(notification);
    }
    
    

}
