package com.sistema.gestao.repository;

import com.sistema.gestao.entity.Funcionario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.Optional;

public interface FuncionarioRepository extends JpaRepository<Funcionario, Long> {

    Optional<Funcionario> findByEmail(String email);
    Optional<Funcionario> findByCpf(String cpf);

    boolean existsByEmail(String email);
    boolean existsByCpf(String cpf);

    // BUSCA EXATA PARA O PRIMEIRO ACESSO
    Optional<Funcionario> findByCpfAndDataNascimento(String cpf, LocalDate dataNascimento);

    // BUSCA PELO TOKEN DO EMAIL
    Optional<Funcionario> findByTokenRecuperacao(String tokenRecuperacao);
}