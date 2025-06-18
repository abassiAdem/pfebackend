package com.example.demo.dto;

import java.util.Objects;
 public class RealisateurGanttDTO implements Comparable<RealisateurGanttDTO> {
    private Long id;
    private String name;

    @Override
    public int compareTo(RealisateurGanttDTO other) {
        // Tri par ID (ou par name si préféré)
        return this.id.compareTo(other.id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RealisateurGanttDTO that = (RealisateurGanttDTO) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
  }