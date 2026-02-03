package com.sistema.gestao.repository;

import com.sistema.gestao.entity.Funcionario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface FuncionarioRepository extends JpaRepository<Funcionario, Long> {

    // Busca para validação de login (se necessário) ou recuperação simples
    Optional<Funcionario> findByEmail(String email);

    // Busca para o Primeiro Acesso
    Optional<Funcionario> findByCpfAndDataNascimento(String cpf, LocalDate dataNascimento);

    // Busca pelo Token (validar código ou salvar nova senha)
    Optional<Funcionario> findByTokenRecuperacao(String tokenRecuperacao);

    // --- NOVO: Busca Rigorosa para Recuperação de Senha ---
    // Só retorna o funcionário se E-mail, CPF e Data de Nascimento baterem exatamente
    Optional<Funcionario> findByEmailAndCpfAndDataNascimento(String email, String cpf, LocalDate dataNascimento);
}