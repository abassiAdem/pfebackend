package com.example.demo.entities;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "email_configuration")
public class EmailConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String mailer = "smtp";  // Default to SMTP

    @Column(name = "smtp_host")
    private String smtpHost;  // Changed from hoteSmtp

    @Column(name = "smtp_port")
    private Integer smtpPort;  // Changed from portSmtp

    @Column(name = "smtp_username")
    private String smtpUsername;  // Changed from nomUtilisateurSmtp

    @Column(name = "smtp_password")
    private String smtpPassword;  // Changed from motPasseSmtp

    @Column(name = "smtp_encryption")
    private String smtpEncryption;  // Changed from chiffrementSmtp

    @Column(name = "from_email")
    private String fromEmail;  // Changed from emailExpediteur

    @Column(name = "from_name")
    private String fromName;  // Changed from nomExpediteur

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;  // Changed from derniereMiseAJour

    @Column(name = "updated_by")
    private String updatedBy;  // Changed from modifiePar

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getMailer() {
		return mailer;
	}

	public void setMailer(String mailer) {
		this.mailer = mailer;
	}

	public String getSmtpHost() {
		return smtpHost;
	}

	public void setSmtpHost(String smtpHost) {
		this.smtpHost = smtpHost;
	}

	public Integer getSmtpPort() {
		return smtpPort;
	}

	public void setSmtpPort(Integer smtpPort) {
		this.smtpPort = smtpPort;
	}

	public String getSmtpUsername() {
		return smtpUsername;
	}

	public void setSmtpUsername(String smtpUsername) {
		this.smtpUsername = smtpUsername;
	}

	public String getSmtpPassword() {
		return smtpPassword;
	}

	public void setSmtpPassword(String smtpPassword) {
		this.smtpPassword = smtpPassword;
	}

	public String getSmtpEncryption() {
		return smtpEncryption;
	}

	public void setSmtpEncryption(String smtpEncryption) {
		this.smtpEncryption = smtpEncryption;
	}

	public String getFromEmail() {
		return fromEmail;
	}

	public void setFromEmail(String fromEmail) {
		this.fromEmail = fromEmail;
	}

	public String getFromName() {
		return fromName;
	}

	public void setFromName(String fromName) {
		this.fromName = fromName;
	}

	public LocalDateTime getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(LocalDateTime lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

	public String getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}


	
	
}