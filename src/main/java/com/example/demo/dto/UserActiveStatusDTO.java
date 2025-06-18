package com.example.demo.dto;

public class UserActiveStatusDTO {
    private String status;
    private Long count;
    private Double percentage; 

    public UserActiveStatusDTO() {
    }

    public UserActiveStatusDTO(String status, Long count, Double percentage) {
        this.status = status;
        this.count = count;
        this.percentage = percentage;
    }

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Long getCount() {
		return count;
	}

	public void setCount(Long count) {
		this.count = count;
	}

	public Double getPercentage() {
		return percentage;
	}

	public void setPercentage(Double percentage) {
		this.percentage = percentage;
	}
    
}
