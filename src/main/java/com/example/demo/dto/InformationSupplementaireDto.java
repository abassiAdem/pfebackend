package com.example.demo.dto;

public class InformationSupplementaireDto {
    private Long demandeId;
    private String message;
    private Long requesterId;
    private Long responderId;
    private Boolean isRead;

 
    @Override
	public String toString() {
		return "InformationSupplementaireDto [demandeId=" + demandeId + ", message=" + message + ", requesterId="
				+ requesterId + ", responderId=" + responderId + ", isRead=" + isRead + "]";
	}


	public Boolean getIsRead() {
		return isRead;
	}


	public void setIsRead(Boolean isRead) {
		this.isRead = isRead;
	}


	public InformationSupplementaireDto() {}

    
    public Long getDemandeId() {
        return demandeId;
    }

    public void setDemandeId(Long demandeId) {
        this.demandeId = demandeId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }



    public Long getRequesterId() {
        return requesterId;
    }

    public void setRequesterId(Long requesterId) {
        this.requesterId = requesterId;
    }

    public Long getResponderId() {
        return responderId;
    }

    public void setResponderId(Long responderId) {
        this.responderId = responderId;
    }
}
