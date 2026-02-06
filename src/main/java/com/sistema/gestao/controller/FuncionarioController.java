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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/funcionarios")
public class FuncionarioController {

    @Autowired
    private FuncionarioRepository funcionarioRepository;

    @Autowired
    private TurmaRepository turmaRepository;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("lista", funcionarioRepository.findAll());
        return "lista_funcionarios";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("funcionario", new Funcionario());
        model.addAttribute("listaTurmas", turmaRepository.findAll());
        return "form_funcionario";
    }

    @PostMapping("/salvar")
    public String salvar(Funcionario funcionario, RedirectAttributes redirectAttributes) {
        funcionarioRepository.save(funcionario);
        redirectAttributes.addFlashAttribute("sucesso", "Funcionário salvo com sucesso!");
        return "redirect:/funcionarios";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        Funcionario f = funcionarioRepository.findById(id).orElseThrow();
        model.addAttribute("funcionario", f);
        model.addAttribute("listaTurmas", turmaRepository.findAll());
        return "form_funcionario";
    }

    @GetMapping("/excluir/{id}")
    public String desativar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Funcionario f = funcionarioRepository.findById(id).orElseThrow();

        if ("admin@golsocial.br".equals(f.getEmail())) {
            redirectAttributes.addFlashAttribute("erro", "Não é permitido desativar o Administrador Principal.");
            return "redirect:/funcionarios";
        }

        f.setAtivo(false);
        funcionarioRepository.save(f);
        redirectAttributes.addFlashAttribute("sucesso", "Funcionário desativado com sucesso.");
        return "redirect:/funcionarios";
    }

    @GetMapping("/remover-acesso/{id}")
    public String removerAcessoLogin(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Optional<Funcionario> opt = funcionarioRepository.findById(id);

        if (opt.isPresent()) {
            Funcionario f = opt.get();

            if ("admin@golsocial.br".equals(f.getEmail())) {
                redirectAttributes.addFlashAttribute("erro", "Não é permitido remover o acesso do Administrador Principal.");
                return "redirect:/funcionarios";
            }

            f.setSenha(null);
            funcionarioRepository.save(f);
            redirectAttributes.addFlashAttribute("sucesso", "Acesso ao sistema removido para " + f.getNomeCompleto());
        }
        return "redirect:/funcionarios";
    }

    @GetMapping("/ativar/{id}")
    public String ativar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Funcionario f = funcionarioRepository.findById(id).orElseThrow();
        f.setAtivo(true);
        funcionarioRepository.save(f);
        redirectAttributes.addFlashAttribute("sucesso", "Funcionário reativado com sucesso.");
        return "redirect:/funcionarios";
    }
}