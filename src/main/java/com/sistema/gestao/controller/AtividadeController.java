package com.sistema.gestao.controller;

import com.sistema.gestao.entity.AtividadeAula;
import com.sistema.gestao.repository.AtividadeAulaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.time.LocalDate;

@Controller
public class AtividadeController {

    @Autowired
    private AtividadeAulaRepository repository;

    @GetMapping("/atividades")
    public String listar(Model model) {
        model.addAttribute("lista", repository.findAllByOrderByDataDesc());
        return "lista_atividades";
    }

    @GetMapping("/atividades/nova")
    public String nova(Model model) {
        AtividadeAula atividade = new AtividadeAula();
        atividade.setData(LocalDate.now()); // Data de hoje padrão
        model.addAttribute("atividade", atividade);
        return "form_atividade";
    }

    @PostMapping("/atividades/salvar")
    public String salvar(@ModelAttribute AtividadeAula atividade) {
        repository.save(atividade);
        return "redirect:/atividades";
    }

    @GetMapping("/atividades/excluir/{id}")
    public String excluir(@PathVariable Long id) {
        repository.deleteById(id);
        return "redirect:/atividades";
    }
}