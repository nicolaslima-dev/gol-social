package com.sistema.gestao.repository;

import com.sistema.gestao.entity.AtividadeAula;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AtividadeAulaRepository extends JpaRepository<AtividadeAula, Long> {
    // Busca ordenando pelas mais recentes primeiro
    List<AtividadeAula> findAllByOrderByDataDesc();
}