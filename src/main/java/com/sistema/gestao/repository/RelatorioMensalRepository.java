package com.sistema.gestao.repository;

import com.sistema.gestao.entity.RelatorioMensal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RelatorioMensalRepository extends JpaRepository<RelatorioMensal, Long> {
    // Buscar relatórios de um funcionário específico
    List<RelatorioMensal> findByFuncionarioIdOrderByDataInicioDesc(Long funcionarioId);
}