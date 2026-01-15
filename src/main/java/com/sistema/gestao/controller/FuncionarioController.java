package com.sistema.gestao.controller;

import com.sistema.gestao.entity.Funcionario;
import com.sistema.gestao.repository.FuncionarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class FuncionarioController {

    @Autowired
    private FuncionarioRepository repository;

    // Nota: Removemos a injeção do PdfService aqui, pois a impressão
    // agora é feita pelo RelatorioMensalController.

    @GetMapping("/funcionarios")
    public String listar(Model model) {
        model.addAttribute("lista", repository.findAll());
        return "lista_funcionarios";
    }

    @GetMapping("/funcionarios/novo")
    public String novo(Model model) {
        model.addAttribute("funcionario", new Funcionario());
        return "form_funcionario";
    }

    @PostMapping("/funcionarios/salvar")
    public String salvar(@ModelAttribute Funcionario funcionario) {
        repository.save(funcionario);
        return "redirect:/funcionarios";
    }

    @GetMapping("/funcionarios/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        Funcionario func = repository.findById(id).orElse(null);
        model.addAttribute("funcionario", func);
        return "form_funcionario";
    }

    @GetMapping("/funcionarios/excluir/{id}")
    public String excluir(@PathVariable Long id) {
        repository.deleteById(id);
        return "redirect:/funcionarios";
    }

    // REMOVIDO: O método gerarRelatorio antigo que causava o erro.
    // Agora o botão na tela aponta para /relatorios/novo/{id}
}