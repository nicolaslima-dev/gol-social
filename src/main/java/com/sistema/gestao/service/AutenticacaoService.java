package com.sistema.gestao.service;

import com.sistema.gestao.entity.Funcionario;
import com.sistema.gestao.repository.FuncionarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AutenticacaoService implements UserDetailsService {

    @Autowired
    private FuncionarioRepository repository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Busca agora na tabela Funcionario
        Funcionario funcionario = repository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + email));

        // Impede login se não tiver senha definida (obriga a fazer Primeiro Acesso)
        if (funcionario.getSenha() == null || funcionario.getSenha().isEmpty()) {
            throw new UsernameNotFoundException("Cadastro incompleto. Realize o Primeiro Acesso.");
        }

        return User.builder()
                .username(funcionario.getEmail())
                .password(funcionario.getSenha())
                .roles(funcionario.getPerfil() != null ? funcionario.getPerfil() : "USER")
                .build();
    }
}