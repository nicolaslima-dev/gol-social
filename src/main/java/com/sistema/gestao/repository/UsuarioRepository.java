package com.sistema.gestao.repository;

import com.sistema.gestao.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByLogin(String login);

    // NECESSÁRIO para o ConfiguracaoController listar os admins
    List<Usuario> findByPerfil(String perfil);
}