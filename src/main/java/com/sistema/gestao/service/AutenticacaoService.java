package com.sistema.gestao.service;

import com.sistema.gestao.entity.Funcionario;
import com.sistema.gestao.entity.Usuario;
import com.sistema.gestao.repository.FuncionarioRepository;
import com.sistema.gestao.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AutenticacaoService implements UserDetailsService {

    @Autowired private FuncionarioRepository funcionarioRepository;
    @Autowired private UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // 1. Tenta achar Funcionário (Professor/Gestor)
        Optional<Funcionario> func = funcionarioRepository.findByEmail(email);
        if (func.isPresent()) {
            if (func.get().getSenha() == null || func.get().getSenha().isEmpty()) {
                throw new UsernameNotFoundException("Cadastro incompleto.");
            }
            return User.builder()
                    .username(func.get().getEmail())
                    .password(func.get().getSenha())
                    .roles(func.get().getPerfil() != null ? func.get().getPerfil() : "USER")
                    .disabled(!func.get().isAtivo()) // Trava de segurança
                    .build();
        }

        // 2. Tenta achar Admin Puro
        Optional<Usuario> user = usuarioRepository.findByLogin(email);
        if (user.isPresent() && "ADMIN".equals(user.get().getPerfil())) {
            return User.builder()
                    .username(user.get().getLogin())
                    .password(user.get().getSenha())
                    .roles("ADMIN")
                    .build();
        }

        throw new UsernameNotFoundException("Usuário inválido: " + email);
    }
}