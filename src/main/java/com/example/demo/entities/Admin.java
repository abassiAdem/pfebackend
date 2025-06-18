package com.example.demo.entities;
import jakarta.persistence.*;
import lombok.Data;
@Data
@Entity
@Table(name = "admin")
public class Admin {
    @Id
    private Long id;
    @OneToOne(fetch = FetchType.LAZY )
    @MapsId
    @JoinColumn(name = "id")
    private AppUser agent;
    public void setAgent(AppUser agent) {
        this.agent = agent;
    }
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public AppUser getAgent() {
		return agent;
	}
}