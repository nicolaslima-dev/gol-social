package com.sistema.gestao.controller;

import com.sistema.gestao.entity.Instituicao;
import com.sistema.gestao.entity.Usuario;
import com.sistema.gestao.repository.InstituicaoRepository;
import com.sistema.gestao.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;

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
        model.addAttribute("usuarios", usuarioRepository.findAll());
        Instituicao instituicao = instituicaoRepository.findById(1L).orElse(new Instituicao());
        if (instituicao.getNomeProjeto() == null) instituicao.setNomeProjeto("PROJETO GOL SOCIAL");
        model.addAttribute("instituicao", instituicao);
        return "configuracoes";
    }

    @PostMapping("/usuarios/criar")
    public String criarUsuario(@RequestParam String login, @RequestParam String senha, @RequestParam String perfil) {
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

    // --- UPLOAD DA LOGO AQUI ---
    @PostMapping("/instituicao/salvar")
    public String salvarInstituicao(@ModelAttribute Instituicao instituicaoForm,
                                    @RequestParam("logoUpload") MultipartFile logoUpload) {

        Instituicao banco = instituicaoRepository.findById(1L).orElse(new Instituicao());

        // Atualiza dados
        banco.setId(1L);
        banco.setNomeProjeto(instituicaoForm.getNomeProjeto());
        banco.setEnderecoCompleto(instituicaoForm.getEnderecoCompleto());
        banco.setTelefone(instituicaoForm.getTelefone());
        banco.setEmail(instituicaoForm.getEmail());

        // Salva Logo se enviada
        if (!logoUpload.isEmpty()) {
            try {
                banco.setLogo(logoUpload.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        instituicaoRepository.save(banco);
        return "redirect:/configuracoes?aba=instituicao";
    }
}