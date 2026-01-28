package com.sistema.gestao.controller;

import com.sistema.gestao.entity.Turma;
import com.sistema.gestao.repository.InscritoRepository;
import com.sistema.gestao.repository.TurmaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/turmas")
public class TurmaController {

    @Autowired
    private TurmaRepository turmaRepository;

    @Autowired
    private InscritoRepository inscritoRepository;

    // AQUI ESTAVA O ERRO ANTES:
    @GetMapping
    public String listar(Model model) {
        model.addAttribute("turmas", turmaRepository.findAll());
        // TEM QUE SER EXATAMENTE O NOME DO SEU ARQUIVO HTML
        return "listar_turmas";
    }

    @GetMapping("/novo")
    public String nova(Model model) {
        model.addAttribute("turma", new Turma());
        return "cadastro_turma"; // Verifique se seu arquivo de cadastro tem esse nome
    }

    @PostMapping("/salvar")
    public String salvar(Turma turma) {
        turmaRepository.save(turma);
        return "redirect:/turmas";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        Turma turma = turmaRepository.findById(id).orElseThrow();
        model.addAttribute("turma", turma);
        return "cadastro_turma";
    }

    @GetMapping("/excluir/{id}")
    public String excluir(@PathVariable Long id) {
        turmaRepository.deleteById(id);
        return "redirect:/turmas";
    }
}