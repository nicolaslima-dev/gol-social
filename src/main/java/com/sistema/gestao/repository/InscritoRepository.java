package com.sistema.gestao.repository;

import com.sistema.gestao.entity.Inscrito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InscritoRepository extends JpaRepository<Inscrito, Long> {
}