package com.sistema.gestao.controller;

import com.sistema.gestao.entity.Funcionario;
import com.sistema.gestao.repository.FuncionarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.Optional;

@Controller
@RequestMapping("/perfil")
public class PerfilController {

    @Autowired
    private FuncionarioRepository funcionarioRepository;

    @GetMapping
    public String visualizarPerfil(Principal principal, Model model) {
        // Pega o e-mail do usuário logado no Spring Security
        String emailLogado = principal.getName();

        // Busca o funcionário dono deste e-mail
        Optional<Funcionario> funcOpt = funcionarioRepository.findByEmail(emailLogado);

        if (funcOpt.isPresent()) {
            model.addAttribute("funcionario", funcOpt.get());
        } else {
            // Prevenção de erro caso o usuário não seja achado
            model.addAttribute("funcionario", new Funcionario());
        }

        return "perfil";
    }
}