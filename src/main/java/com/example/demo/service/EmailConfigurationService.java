package com.example.demo.service;

import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.example.demo.enmus.NotificationType;
import com.example.demo.enmus.StatutDemande;
import com.example.demo.entities.AppUser;
import com.example.demo.entities.Demande; 
import com.example.demo.entities.EmailConfiguration;
import com.example.demo.entities.Notification;
import com.example.demo.repository.AgentRepository;
import com.example.demo.repository.ChefRepository; 
import com.example.demo.repository.EmailConfigurationRepository;

import jakarta.mail.internet.MimeMessage;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
public class EmailConfigurationService {

    private final EmailConfigurationRepository configRepository;
    private final KeycloakService userService;
    private final ChefRepository chefRepository;
    private final AgentRepository appUserRepository;
 
    public EmailConfigurationService(KeycloakService userService,EmailConfigurationRepository configRepository,ChefRepository chefRepository,AgentRepository appUserRepository) {
    	this.userService=userService;
    	this.configRepository=configRepository;
     
    	this.chefRepository=chefRepository;
    	this.appUserRepository=appUserRepository;
    }

    private boolean isUserOnline(AppUser user) {
        return user.getIsActive() != null && user.getIsActive();
    }


    public EmailConfiguration getEmailConfiguration() {
        Optional<EmailConfiguration> configOpt = configRepository.findFirstByOrderById();
        System.out.println("Config found: " + configOpt.isPresent());
        
        if (configOpt.isPresent()) {
            EmailConfiguration config = configOpt.get();
            System.out.println("SMTP Config Details:");
            System.out.println("Host: " + config.getSmtpHost());
            System.out.println("Port: " + config.getSmtpPort());
            System.out.println("Username: " + config.getSmtpUsername());
            System.out.println("Encryption: " + config.getSmtpEncryption());
            System.out.println("From Email: " + config.getFromEmail());
            return config;
        }
        
        System.out.println("No config found, returning new empty config");
        return new EmailConfiguration();
    }
    
 
    
    private boolean isValidEmailConfig(EmailConfiguration config) {
        return config.getSmtpHost() != null && 
               config.getSmtpPort() != null &&
               config.getSmtpUsername() != null && 
               config.getSmtpUsername().contains("@") &&
               config.getSmtpPassword() != null &&
               !config.getSmtpPassword().isEmpty();
    }
    			
    
    
    
public void sendNotificationEmail(AppUser recipient, AppUser sender, String subject, 
        String message, NotificationType type, Notification notification) {
    try {
        if (recipient.getEmail() == null || recipient.getEmail().isEmpty()) {
            System.out.println("Recipient has no email address, skipping email notification");
            return;
        }
        
        EmailConfiguration config = getEmailConfiguration();
        
        if (!isValidEmailConfig(config)) {
            System.out.println("Invalid email configuration");
            return;
        }
        
        if (!config.getFromEmail().equals(config.getSmtpUsername())) {
            System.out.println("Warning: From email should match SMTP username for Gmail");
            config.setFromEmail(config.getSmtpUsername());
        }
        config.setFromName(sender.getFirstName() + " " + sender.getLastName());
        
        if (config.getSmtpHost() == null || config.getSmtpHost().isEmpty() ||
            config.getSmtpUsername() == null || config.getSmtpUsername().isEmpty() ||
            config.getSmtpPassword() == null || config.getSmtpPassword().isEmpty()) {
            System.out.println("Email configuration is incomplete, cannot send email");
            return;
        }
        
        JavaMailSender mailSender = createMailSender(config);
        MimeMessage emailMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(emailMessage, true, "UTF-8");
 
        helper.setFrom(config.getFromEmail(), config.getFromName());
        
        if (sender.getEmail() != null && !sender.getEmail().isEmpty()) {
            helper.setReplyTo(sender.getEmail());
        }
        
        helper.setTo(recipient.getEmail());
        helper.setSubject(subject);
            
            String siteUrl = "http://localhost:5174/" ; 
            String plainText = message + "\n\nPour traiter cette demande, veuillez vous connecter à l'application: " + siteUrl;
            
            String htmlContent = "<html><body style='font-family: Arial, sans-serif; padding: 20px;'>"
                + "<h2 style='color: #2a3f54;'>Notification</h2>"
                + "<p>" + message.replace("\n", "<br/>") + "</p>"
                + "<p>Pour traiter cette demande, veuillez <a href='" + siteUrl + "'>vous connecter à l'application</a>.</p>"
                + "<p style='font-size: 12px; color: #777; margin-top: 30px;'>"
                + "Cette notification est envoyée automatiquement. Veuillez ne pas répondre à cet e-mail.</p>"
                + "</body></html>";
            
            helper.setText(plainText, htmlContent);
            mailSender.send(emailMessage);
            System.out.println("Notification email sent to: " + recipient.getEmail());
        } catch (Exception e) {
            System.out.println("Failed to send notification email: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    
private JavaMailSender createMailSender(EmailConfiguration config) {
    JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
    mailSender.setHost(config.getSmtpHost());
    mailSender.setPort(config.getSmtpPort());
     
    String username = config.getSmtpUsername();
    String password = config.getSmtpPassword();
     
    if (password != null) {
        password = password.replaceAll("\\s+", "");
    }
    
    mailSender.setUsername(username);
    mailSender.setPassword(password);

    Properties props = mailSender.getJavaMailProperties();
    
    if ("ssl".equalsIgnoreCase(config.getSmtpEncryption())) {
        props.put("mail.transport.protocol", "smtps"); 
        props.put("mail.smtp.ssl.enable", "true");
    } else if ("tls".equalsIgnoreCase(config.getSmtpEncryption())) {
        props.put("mail.smtp.starttls.enable", "true");
    } 
     
    if (config.getSmtpHost() != null && config.getSmtpHost().contains("gmail.com")) {
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
    }
    
    props.put("mail.smtp.auth", "true");
    props.put("mail.smtp.timeout", "10000");
    props.put("mail.smtp.connectiontimeout", "10000");
    props.put("mail.debug", "true");

    return mailSender;
}


    
    
    
    
    
    
    
    
 



public boolean sendTestEmail(String testRecipient) {
    try {
        EmailConfiguration config = getEmailConfiguration();
        
        JavaMailSender mailSender = createMailSender(config);
        System.out.println("config.getFromEmail(): " + config.getFromEmail());
        System.out.println("config.getFromName(): " + config.getFromName());
        
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        
        helper.setReplyTo(config.getFromEmail());
        message.addHeader("X-Priority", "1");
        message.addHeader("X-MSMail-Priority", "High");
        message.addHeader("Importance", "High");

        helper.setFrom(config.getFromEmail(), config.getFromName());
        helper.setTo(testRecipient);
        helper.setSubject("Test Email Configuration");
        helper.setText("This is a test email to verify your email configuration settings.");
        
        mailSender.send(message);
        return true;
    } catch (Exception e) {
        e.printStackTrace();
        return false;
    }
}


 

    
}
