package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import com.example.demo.entities.Employee;
@Repository
public interface EmpolyeRepository extends JpaRepository<Employee, Long>{

}
