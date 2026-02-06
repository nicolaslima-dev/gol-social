package com.sistema.gestao.controller;

import com.sistema.gestao.entity.Funcionario;
import com.sistema.gestao.entity.HistoricoSenha;
import com.sistema.gestao.entity.Usuario;
import com.sistema.gestao.repository.FuncionarioRepository;
import com.sistema.gestao.repository.HistoricoSenhaRepository;
import com.sistema.gestao.repository.UsuarioRepository;
import com.sistema.gestao.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired private FuncionarioRepository funcionarioRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private HistoricoSenhaRepository historicoSenhaRepository;
    @Autowired private EmailService emailService;
    @Autowired private PasswordEncoder passwordEncoder;

    // --- TELAS (GET) ---

    @GetMapping("/primeiro_acesso")
    public String telaPrimeiroAcesso() {
        return "auth/primeiro_acesso";
    }

    @GetMapping("/recuperar")
    public String telaRecuperarSenha() {
        return "auth/recuperar";
    }

    @GetMapping("/validar_codigo")
    public String telaValidarCodigo() {
        return "auth/validar_codigo";
    }

    @GetMapping("/nova_senha")
    public String telaNovaSenha(@RequestParam String token, Model model) {
        model.addAttribute("token", token);
        return "auth/nova_senha";
    }

    // --- AÇÕES (POST) ---

    @PostMapping("/validar_primeiro_acesso")
    public String validarPrimeiroAcesso(
            @RequestParam String cpf,
            @RequestParam LocalDate dataNascimento,
            RedirectAttributes attributes) {

        Optional<Funcionario> funcOpt = funcionarioRepository.findByCpfAndDataNascimento(cpf, dataNascimento);

        if (funcOpt.isEmpty()) {
            attributes.addFlashAttribute("erro", "Dados não encontrados. Verifique seu CPF e Data de Nascimento.");
            return "redirect:/auth/primeiro_acesso";
        }

        Funcionario funcionario = funcOpt.get();

        // --- TRAVA DE SEGURANÇA 1: INATIVO NÃO ENTRA ---
        if (!funcionario.isAtivo()) {
            attributes.addFlashAttribute("erro", "Acesso revogado. Entre em contato com a administração.");
            return "redirect:/auth/primeiro_acesso";
        }
        // -----------------------------------------------

        // Validação original: Se já tem senha, manda recuperar
        if (funcionario.getSenha() != null && !funcionario.getSenha().isEmpty()) {
            attributes.addFlashAttribute("aviso", "Você já possui conta ativa! Use a recuperação de senha.");
            return "redirect:/auth/recuperar";
        }

        enviarCodigo(funcionario);
        attributes.addFlashAttribute("sucesso", "Identificado! Código enviado para: " + mascararEmail(funcionario.getEmail()));
        return "redirect:/auth/validar_codigo";
    }

    @PostMapping("/enviar_recuperacao")
    public String enviarRecuperacao(
            @RequestParam String email,
            @RequestParam String cpf,
            @RequestParam LocalDate dataNascimento,
            RedirectAttributes attributes) {

        Optional<Funcionario> funcOpt = funcionarioRepository.findByEmailAndCpfAndDataNascimento(email, cpf, dataNascimento);

        if (funcOpt.isEmpty()) {
            attributes.addFlashAttribute("erro", "Dados inválidos. Verifique se o E-mail, CPF e Data de Nascimento estão corretos.");
            return "redirect:/auth/recuperar";
        }

        Funcionario funcionario = funcOpt.get();

        // --- TRAVA DE SEGURANÇA 2: INATIVO NÃO RECUPERA SENHA ---
        if (!funcionario.isAtivo()) {
            attributes.addFlashAttribute("erro", "Acesso revogado. Entre em contato com a administração.");
            return "redirect:/auth/recuperar";
        }
        // --------------------------------------------------------

        enviarCodigo(funcionario);
        attributes.addFlashAttribute("sucesso", "Dados confirmados! O código foi enviado para seu e-mail.");
        return "redirect:/auth/validar_codigo";
    }

    @PostMapping("/confirmar_codigo")
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

    @PostMapping("/salvar_senha")
    public String salvarSenha(
            @RequestParam String token,
            @RequestParam String senha,
            @RequestParam String confirmaSenha,
            RedirectAttributes attributes) {

        if (!senha.equals(confirmaSenha)) {
            attributes.addFlashAttribute("erro", "As senhas não coincidem. Tente novamente.");
            return "redirect:/auth/nova_senha?token=" + token;
        }

        Optional<Funcionario> funcOpt = funcionarioRepository.findByTokenRecuperacao(token);
        if (funcOpt.isEmpty()) {
            return "redirect:/auth/login";
        }

        Funcionario funcionario = funcOpt.get();

        // Validação de Histórico Original
        if (funcionario.getSenha() != null && passwordEncoder.matches(senha, funcionario.getSenha())) {
            attributes.addFlashAttribute("erro", "Você não pode utilizar sua senha atual. Escolha uma nova.");
            return "redirect:/auth/nova_senha?token=" + token;
        }

        List<HistoricoSenha> senhasAntigas = historicoSenhaRepository.findByFuncionario(funcionario);
        for (HistoricoSenha historico : senhasAntigas) {
            if (passwordEncoder.matches(senha, historico.getSenhaHash())) {
                attributes.addFlashAttribute("erro", "Esta senha já foi utilizada anteriormente.");
                return "redirect:/auth/nova_senha?token=" + token;
            }
        }

        if (funcionario.getSenha() != null && !funcionario.getSenha().isEmpty()) {
            HistoricoSenha novaEntradaHistorico = new HistoricoSenha(funcionario.getSenha(), funcionario);
            historicoSenhaRepository.save(novaEntradaHistorico);
        }

        String senhaCriptografada = passwordEncoder.encode(senha);

        funcionario.setSenha(senhaCriptografada);
        funcionario.setTokenRecuperacao(null);
        funcionario.setTokenValidade(null);
        funcionarioRepository.save(funcionario);

        Usuario usuario = usuarioRepository.findByLogin(funcionario.getEmail()).orElse(new Usuario());
        usuario.setLogin(funcionario.getEmail());
        usuario.setSenha(senhaCriptografada);
        if (usuario.getPerfil() == null || usuario.getPerfil().isEmpty()) {
            usuario.setPerfil("PROFESSOR");
        }
        usuarioRepository.save(usuario);

        attributes.addFlashAttribute("sucesso", "Senha alterada com sucesso!");
        return "redirect:/login";
    }

    // --- AUXILIARES ---

    private void enviarCodigo(Funcionario f) {
        // Gera código de 6 dígitos
        String codigo = String.format("%06d", new Random().nextInt(999999));

        // Salva no banco para validação posterior
        f.setTokenRecuperacao(codigo);
        f.setTokenValidade(LocalDateTime.now().plusMinutes(15));
        funcionarioRepository.save(f);

        // Dispara o e-mail via Resend (Assíncrono)
        emailService.enviarCodigoRecuperacao(f.getEmail(), codigo);
    }

    private String mascararEmail(String email) {
        if (email == null) return "";
        int arroba = email.indexOf("@");
        if(arroba <= 1) return email;
        return email.substring(0, 2) + "***" + email.substring(arroba);
    }
}