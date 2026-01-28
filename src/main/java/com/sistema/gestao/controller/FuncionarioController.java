package com.sistema.gestao.controller;

import com.sistema.gestao.entity.Funcionario;
import com.sistema.gestao.repository.FuncionarioRepository;
import com.sistema.gestao.repository.TurmaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/funcionarios") // Prefixo geral para todas as rotas
public class FuncionarioController {

    @Autowired
    private FuncionarioRepository funcionarioRepository;

    @Autowired
    private TurmaRepository turmaRepository;

    // --- LISTAR (Resolve o erro 404) ---
    @GetMapping
    public String listar(Model model) {
        // Seu HTML espera a variável "lista"
        model.addAttribute("lista", funcionarioRepository.findAll());
        return "lista_funcionarios"; // NOME EXATO DO SEU ARQUIVO HTML
    }

    // --- NOVO CADASTRO ---
    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("funcionario", new Funcionario());
        model.addAttribute("listaTurmas", turmaRepository.findAll());
        return "form_funcionario";
    }

    // --- SALVAR ---
    @PostMapping("/salvar")
    public String salvar(Funcionario funcionario) {
        funcionarioRepository.save(funcionario);
        return "redirect:/funcionarios"; // Volta para a lista
    }

    // --- EDITAR ---
    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        Funcionario f = funcionarioRepository.findById(id).orElseThrow();
        model.addAttribute("funcionario", f);
        model.addAttribute("listaTurmas", turmaRepository.findAll());
        return "form_funcionario";
    }

    // --- EXCLUIR / DESATIVAR (Conforme seu HTML) ---
    @GetMapping("/excluir/{id}")
    public String desativar(@PathVariable Long id) {
        Funcionario f = funcionarioRepository.findById(id).orElseThrow();
        f.setAtivo(false); // Apenas desativa, mantendo histórico
        funcionarioRepository.save(f);
        return "redirect:/funcionarios";
    }

    // --- REATIVAR (Conforme seu HTML) ---
    @GetMapping("/ativar/{id}")
    public String ativar(@PathVariable Long id) {
        Funcionario f = funcionarioRepository.findById(id).orElseThrow();
        f.setAtivo(true);
        funcionarioRepository.save(f);
        return "redirect:/funcionarios";
    }
}