package com.example.demo.dto;

public class DepartementDTO {
    private Long id;
    private String name;

    public DepartementDTO() { }

    public DepartementDTO(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    // getters / setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}