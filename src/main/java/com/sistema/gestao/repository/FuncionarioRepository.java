package com.sistema.gestao.repository;

import com.sistema.gestao.entity.Funcionario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface FuncionarioRepository extends JpaRepository<Funcionario, Long> {
    // Busca o funcionário pelo e-mail (que é o login do usuário)
    Optional<Funcionario> findByEmail(String email);
}