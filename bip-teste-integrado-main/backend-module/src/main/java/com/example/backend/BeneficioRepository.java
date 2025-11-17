package com.example.backend;

import com.example.beneficio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// Interface de operações do CRUD do Spring Data JPA
@Repository
public interface BeneficioRepository extends JpaRepository<Beneficio, Long> {
}
