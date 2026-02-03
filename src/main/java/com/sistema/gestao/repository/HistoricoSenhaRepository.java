package com.sistema.gestao.repository;

import com.sistema.gestao.entity.Funcionario;
import com.sistema.gestao.entity.HistoricoSenha;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoricoSenhaRepository extends JpaRepository<HistoricoSenha, Long> {
    // Busca todas as senhas antigas de um funcionário
    List<HistoricoSenha> findByFuncionario(Funcionario funcionario);
}