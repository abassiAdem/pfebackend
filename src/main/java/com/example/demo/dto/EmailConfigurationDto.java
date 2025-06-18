package com.example.demo.dto;

public class EmailConfigurationDto {
 
    @Override
	public String toString() {
		return "EmailConfigurationDto [smtpServer=" + smtpServer + ", port=" + port + ", username=" + username
				+ ", password=" + password + ", encryption=" + encryption + ", senderName=" + senderName
				+ ", senderEmail=" + senderEmail + ", replyTo=" + replyTo + ", testRecipient=" + testRecipient + "]";
	}
	private String smtpServer;
    

    private String port;
    

    private String username;

    private String password;

    private String encryption = "tls";

    private String senderName;
    

    private String senderEmail;
    
    private String replyTo;
    private String testRecipient;
	public String getSmtpServer() {
		return smtpServer;
	}
	public void setSmtpServer(String smtpServer) {
		this.smtpServer = smtpServer;
	}
	public String getPort() {
		return port;
	}
	public void setPort(String port) {
		this.port = port;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getEncryption() {
		return encryption;
	}
	public void setEncryption(String encryption) {
		this.encryption = encryption;
	}
	public String getSenderName() {
		return senderName;
	}
	public void setSenderName(String senderName) {
		this.senderName = senderName;
	}
	public String getSenderEmail() {
		return senderEmail;
	}
	public void setSenderEmail(String senderEmail) {
		this.senderEmail = senderEmail;
	}
	public String getReplyTo() {
		return replyTo;
	}
	public void setReplyTo(String replyTo) {
		this.replyTo = replyTo;
	}
	public String getTestRecipient() {
		return testRecipient;
	}
	public void setTestRecipient(String testRecipient) {
		this.testRecipient = testRecipient;
	}
    
}
