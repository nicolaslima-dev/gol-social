package com.sistema.gestao.repository;

import com.sistema.gestao.entity.Frequencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface FrequenciaRepository extends JpaRepository<Frequencia, Long> {
    // Método para buscar chamada de um dia específico (para evitar duplicidade ou gerar relatório)
    List<Frequencia> findByDataAula(LocalDate data);
}