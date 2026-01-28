package com.sistema.gestao.repository;

import com.sistema.gestao.entity.Inscrito;
import com.sistema.gestao.entity.Turma;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InscritoRepository extends JpaRepository<Inscrito, Long> {

    List<Inscrito> findByTurma(Turma turma);

}