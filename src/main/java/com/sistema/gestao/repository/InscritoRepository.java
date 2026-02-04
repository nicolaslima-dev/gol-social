package com.sistema.gestao.repository;

import com.sistema.gestao.entity.Inscrito;
import com.sistema.gestao.entity.Turma;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InscritoRepository extends JpaRepository<Inscrito, Long> {

    // Busca todos os inscritos que tenham ESSA turma na sua lista de turmas
    List<Inscrito> findByTurmasContaining(Turma turma);

}