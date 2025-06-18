package com.example.demo.controller;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.EmailConfigurationDto;
import com.example.demo.entities.EmailConfiguration;
import com.example.demo.repository.EmailConfigurationRepository;
import com.example.demo.service.EmailConfigurationService;

@RestController
@RequestMapping("/api/admin/email-config")
public class EmailController {
	private final EmailConfigurationRepository emailConfigRepository;
	private final EmailConfigurationService emailConfigService;
	public  EmailController(EmailConfigurationRepository emailConfigRepository,EmailConfigurationService emailConfigService) {
		this.emailConfigRepository=emailConfigRepository;
		this.emailConfigService=emailConfigService;
	}
@PostMapping("")
public ResponseEntity<?> saveEmailConfig(@RequestBody EmailConfigurationDto configDto) {
    EmailConfiguration config = emailConfigRepository.findFirstByOrderById()
            .orElse(new EmailConfiguration());


    try {
        if (configDto.getPassword() != null && !configDto.getPassword().isEmpty()) {
        	config.setSmtpPassword(configDto.getPassword());
        }

        if (configDto.getSmtpServer() == null || configDto.getSmtpServer().isEmpty()) {
            throw new IllegalArgumentException("SMTP server is required");
        }

        if (configDto.getPort() == null || configDto.getPort().isEmpty()) {
            throw new IllegalArgumentException("Port is required");
        }


        if (configDto.getUsername() == null || configDto.getUsername().isEmpty()) {
            throw new IllegalArgumentException("Username is required");
        }

        if (configDto.getEncryption() == null || configDto.getEncryption().isEmpty()) {
            throw new IllegalArgumentException("Encryption method is required");
        }
        if (!Arrays.asList("ssl", "tls", "none").contains(configDto.getEncryption().toLowerCase())) {
            throw new IllegalArgumentException("Encryption must be either SSL, TLS or none");
        }
        int port;
        try {
            port = Integer.parseInt(configDto.getPort());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Port must be a valid number");
        }

        if (configDto.getEncryption() == null || configDto.getEncryption().isEmpty()) {
            throw new IllegalArgumentException("Encryption method is required");
        }
        if (!Arrays.asList("ssl", "tls", "none").contains(configDto.getEncryption().toLowerCase())) {
            throw new IllegalArgumentException("Encryption must be either SSL, TLS or none");
        }

            config.setSmtpHost(configDto.getSmtpServer());
            config.setSmtpPort(port);
            config.setSmtpUsername(configDto.getUsername());
            config.setSmtpPassword(configDto.getPassword());
            config.setSmtpEncryption(configDto.getEncryption());
            config.setFromEmail(configDto.getSenderEmail());
            config.setFromName(configDto.getSenderName());
            config.setMailer("smtp");
            config.setLastUpdated(LocalDateTime.now());

            emailConfigRepository.save(config);

        
        return ResponseEntity.ok(Map.of("message", "Email configuration saved successfully"));

    } catch (IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    } catch (Exception e) {
        return ResponseEntity.internalServerError().body(Map.of("error", "An unexpected error occurred"));
    }
}

@PostMapping("/test")
    public ResponseEntity<?> testEmailConfig(@RequestBody Map<String, String> testRequest) {
        String testRecipient = testRequest.get("testRecipient");
        if (testRecipient == null || testRecipient.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Test recipient email is required"));
        }
        
        boolean success = emailConfigService.sendTestEmail(testRecipient);
        
        if (success) {
            return ResponseEntity.ok(Map.of("message", "Test email sent successfully"));
        } else {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to send test email. Please check configuration."));
        }
    }
    
    
}
