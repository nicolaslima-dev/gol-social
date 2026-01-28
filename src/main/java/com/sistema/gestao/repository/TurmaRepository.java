package com.sistema.gestao.repository;

import com.sistema.gestao.entity.Turma;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TurmaRepository extends JpaRepository<Turma, Long> {

    /**
     * Este método conta quantas turmas existem com a combinação exata de Núcleo + Modalidade.
     * Usado no Controller para impedir a criação de uma 3ª turma.
     */
    long countByNucleoAndModalidade(String nucleo, String modalidade);

}