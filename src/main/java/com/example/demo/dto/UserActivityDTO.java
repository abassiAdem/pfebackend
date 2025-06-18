package com.example.demo.dto;

import java.time.LocalDateTime;

public class UserActivityDTO {
    private LocalDateTime date;
    private Long count;
    public LocalDateTime getDate() {
		return date;
	}

	public void setDate(LocalDateTime date) {
		this.date = date;
	}

	public Long getCount() {
		return count;
	}

	public void setCount(Long count) {
		this.count = count;
	}

	public UserActivityDTO() {
    }

    public UserActivityDTO(LocalDateTime date, Long count) {
        this.date = date;
        this.count = count;
    }
}
