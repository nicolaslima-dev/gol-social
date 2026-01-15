package com.sistema.gestao.controller;

import com.sistema.gestao.entity.Instituicao;
import com.sistema.gestao.entity.Usuario;
import com.sistema.gestao.repository.InstituicaoRepository;
import com.sistema.gestao.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ConfiguracaoController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private InstituicaoRepository instituicaoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/configuracoes")
    public String abrirConfiguracoes(Model model) {
        // Carrega usuários
        model.addAttribute("usuarios", usuarioRepository.findAll());

        // Carrega dados da instituição (sempre pega o ID 1 ou cria novo padrão)
        Instituicao instituicao = instituicaoRepository.findById(1L).orElse(new Instituicao());

        // Define valores padrão se estiver vazio (primeira vez)
        if (instituicao.getNomeProjeto() == null) {
            instituicao.setNomeProjeto("PROJETO GOL SOCIAL");
            instituicao.setEnderecoCompleto("Rua Exemplo, 123 - Rio de Janeiro - RJ");
        }

        model.addAttribute("instituicao", instituicao);

        return "configuracoes";
    }

    // --- USUÁRIOS ---
    @PostMapping("/usuarios/criar")
    public String criarUsuario(@RequestParam String login,
                               @RequestParam String senha,
                               @RequestParam String perfil) {
        if (usuarioRepository.findByLogin(login).isEmpty()) {
            Usuario novo = new Usuario();
            novo.setLogin(login);
            novo.setSenha(passwordEncoder.encode(senha));
            novo.setPerfil(perfil);
            usuarioRepository.save(novo);
        }
        return "redirect:/configuracoes";
    }

    @GetMapping("/usuarios/excluir/{id}")
    public String excluirUsuario(@PathVariable Long id) {
        if (id != 1) usuarioRepository.deleteById(id);
        return "redirect:/configuracoes";
    }

    // --- INSTITUIÇÃO (Salvar) ---
    @PostMapping("/instituicao/salvar")
    public String salvarInstituicao(@ModelAttribute Instituicao instituicao) {
        // Força o ID 1 para garantir que só existe um registro de configuração
        instituicao.setId(1L);
        instituicaoRepository.save(instituicao);
        return "redirect:/configuracoes";
    }
}