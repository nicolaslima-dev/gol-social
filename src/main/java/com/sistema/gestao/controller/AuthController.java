package com.sistema.gestao.controller;

import com.sistema.gestao.entity.Funcionario;
import com.sistema.gestao.repository.FuncionarioRepository;
import com.sistema.gestao.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired private FuncionarioRepository funcionarioRepository;
    @Autowired private EmailService emailService;
    @Autowired private PasswordEncoder passwordEncoder;

    // --- TELAS (GET) ---

    // MUDOU DE TRAÇO (-) PARA UNDERLINE (_) NA URL E NO ARQUIVO
    @GetMapping("/primeiro_acesso")
    public String telaPrimeiroAcesso() {
        return "auth/primeiro_acesso"; // Busca o arquivo primeiro_acesso.html
    }

    @GetMapping("/recuperar")
    public String telaRecuperarSenha() {
        return "auth/recuperar";
    }

    // MUDOU PARA UNDERLINE (_)
    @GetMapping("/validar_codigo")
    public String telaValidarCodigo() {
        return "auth/validar_codigo"; // Busca o arquivo validar_codigo.html
    }

    // MUDOU PARA UNDERLINE (_)
    @GetMapping("/nova_senha")
    public String telaNovaSenha(@RequestParam String token, Model model) {
        model.addAttribute("token", token);
        return "auth/nova_senha"; // Busca o arquivo nova_senha.html
    }

    // --- AÇÕES (POST) ---

    // 1. PRIMEIRO ACESSO (CPF + DATA)
    @PostMapping("/validar_primeiro_acesso") // Padronizei para underline aqui também
    public String validarPrimeiroAcesso(
            @RequestParam String cpf,
            @RequestParam LocalDate dataNascimento,
            RedirectAttributes attributes) {

        Optional<Funcionario> funcOpt = funcionarioRepository.findByCpfAndDataNascimento(cpf, dataNascimento);

        if (funcOpt.isEmpty()) {
            attributes.addFlashAttribute("erro", "Dados não encontrados. Verifique seu CPF e Data de Nascimento.");
            return "redirect:/auth/primeiro_acesso"; // Redireciona para a nova URL
        }

        Funcionario funcionario = funcOpt.get();

        if (funcionario.getSenha() != null && !funcionario.getSenha().isEmpty()) {
            attributes.addFlashAttribute("aviso", "Você já possui conta ativa! Use a recuperação de senha abaixo.");
            return "redirect:/auth/recuperar";
        }

        enviarCodigo(funcionario);
        attributes.addFlashAttribute("sucesso", "Identificado! Código enviado para: " + mascararEmail(funcionario.getEmail()));
        return "redirect:/auth/validar_codigo"; // Redireciona para a nova URL
    }

    // 2. RECUPERAÇÃO (SÓ EMAIL)
    @PostMapping("/enviar_recuperacao") // Padronizei
    public String enviarRecuperacao(@RequestParam String email, RedirectAttributes attributes) {
        Optional<Funcionario> funcOpt = funcionarioRepository.findByEmail(email);

        if (funcOpt.isPresent()) {
            enviarCodigo(funcOpt.get());
        }
        attributes.addFlashAttribute("sucesso", "Se o e-mail estiver cadastrado, você receberá um código.");
        return "redirect:/auth/validar_codigo";
    }

    // 3. CONFIRMAR CÓDIGO
    @PostMapping("/confirmar_codigo") // Padronizei
    public String confirmarCodigo(@RequestParam String codigo, RedirectAttributes attributes) {
        Optional<Funcionario> funcOpt = funcionarioRepository.findByTokenRecuperacao(codigo);

        if (funcOpt.isEmpty()) {
            attributes.addFlashAttribute("erro", "Código inválido.");
            return "redirect:/auth/validar_codigo";
        }

        Funcionario funcionario = funcOpt.get();

        if (funcionario.getTokenValidade().isBefore(LocalDateTime.now())) {
            attributes.addFlashAttribute("erro", "Código expirado. Solicite novamente.");
            return "redirect:/auth/recuperar";
        }

        return "redirect:/auth/nova_senha?token=" + codigo;
    }

    // 4. SALVAR NOVA SENHA
    @PostMapping("/salvar_senha") // Padronizei
    public String salvarSenha(
            @RequestParam String token,
            @RequestParam String senha,
            RedirectAttributes attributes) {

        Optional<Funcionario> funcOpt = funcionarioRepository.findByTokenRecuperacao(token);

        if (funcOpt.isEmpty()) {
            return "redirect:/auth/login";
        }

        Funcionario funcionario = funcOpt.get();
        funcionario.setSenha(passwordEncoder.encode(senha));
        funcionario.setTokenRecuperacao(null);
        funcionario.setTokenValidade(null);

        funcionarioRepository.save(funcionario);

        attributes.addFlashAttribute("sucesso", "Senha criada com sucesso! Faça login.");
        return "redirect:/login";
    }

    // AUXILIARES
    private void enviarCodigo(Funcionario f) {
        String codigo = String.format("%06d", new Random().nextInt(999999));
        f.setTokenRecuperacao(codigo);
        f.setTokenValidade(LocalDateTime.now().plusMinutes(15));
        funcionarioRepository.save(f);
        emailService.enviarCodigoRecuperacao(f.getEmail(), codigo);
    }

    private String mascararEmail(String email) {
        int arroba = email.indexOf("@");
        if(arroba <= 1) return email;
        return email.substring(0, 2) + "***" + email.substring(arroba);
    }
}