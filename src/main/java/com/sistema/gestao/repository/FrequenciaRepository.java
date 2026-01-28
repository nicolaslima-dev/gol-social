package com.sistema.gestao.repository;

import com.sistema.gestao.entity.Frequencia;
import com.sistema.gestao.entity.Inscrito;
import com.sistema.gestao.entity.Turma;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FrequenciaRepository extends JpaRepository<Frequencia, Long> {

    // 1. Conta quantas frequências existem para este aluno nesta turma (Total de Aulas)
    long countByInscritoAndTurma(Inscrito inscrito, Turma turma);

    // 2. Conta quantas vezes o status foi igual ao solicitado (ex: "P" para Presença)
    long countByInscritoAndTurmaAndStatus(Inscrito inscrito, Turma turma, String status);

    // 3. Busca as últimas 5 frequências lançadas para este aluno (para o histórico)
    List<Frequencia> findTop5ByInscritoOrderByDataAulaDesc(Inscrito inscrito);

    // Verifica duplicidade (opcional)
    boolean existsByInscritoAndTurmaAndDataAula(Inscrito inscrito, Turma turma, java.time.LocalDate dataAula);
}