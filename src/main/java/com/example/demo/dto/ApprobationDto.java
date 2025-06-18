package com.example.demo.dto;

import com.example.demo.enmus.NotificationType;

public class ApprobationDto {
    @Override
	public String toString() {
		return "ApprobationDto [  demandeId=" + demandeId + ", message=" + message
				+ ", motifRejet=" + motifRejet + ", userId=" + userId + ", isRead=" + isRead + ", approbation="
				+ approbation + "]";
	}

    private Long demandeId;
    private String message;
    private String motifRejet;
    private Long userId;
    private Long responderId;
    private Boolean isRead;
    private NotificationType	type;
    private Boolean approbation; 
    private Boolean  isActionable;
    public Boolean getIsActionable() {
		return isActionable;
	}

	public void setIsActionable(Boolean isActionable) {
		this.isActionable = isActionable;
	}

	public String getMessage() {
		return message;
	}

	public Long getResponderId() {
		return responderId;
	}

	public void setResponderId(Long responderId) {
		this.responderId = responderId;
	}

	public void setMessage(String message) {
		this.message = message;
	}


    public Boolean getIsRead() {
		return isRead;
	}

	public void setIsRead(Boolean isRead) {
		this.isRead = isRead;
	}


    public Boolean getApprobation() {
		return approbation;
	}

	public void setApprobation(Boolean approbation) {
		this.approbation = approbation;
	}



    public ApprobationDto() {}


    public Long getDemandeId() {
        return demandeId;
    }

    public void setDemandeId(Long demandeId) {
        this.demandeId = demandeId;
    }


	public String getMotifRejet() {
        return motifRejet;
    }

    public void setMotifRejet(String motifRejet) {
        this.motifRejet = motifRejet;
    }


    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

	public NotificationType getType() {
		return type;
	}

	public void setType(NotificationType type) {
		this.type = type;
	}
    
    

}