package com.sistema.gestao.repository;

import com.sistema.gestao.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    // Busca o usuário pelo login para verificar a senha
    Optional<Usuario> findByLogin(String login);
}