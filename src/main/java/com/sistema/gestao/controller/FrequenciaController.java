package com.sistema.gestao.controller;

import com.sistema.gestao.entity.Frequencia;
import com.sistema.gestao.entity.Inscrito;
import com.sistema.gestao.repository.FrequenciaRepository;
import com.sistema.gestao.repository.InscritoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@Controller
public class FrequenciaController {

    @Autowired
    private InscritoRepository inscritoRepository;

    @Autowired
    private FrequenciaRepository frequenciaRepository;

    // Tela de Chamada (Padrão: Data de Hoje)
    @GetMapping("/frequencia")
    public String abrirChamada(Model model) {
        model.addAttribute("dataHoje", LocalDate.now());
        // Busca apenas alunos ATIVOS para a chamada
        model.addAttribute("alunos", inscritoRepository.findAll());
        return "chamada";
    }

    // Salvar a Chamada
    @PostMapping("/frequencia/salvar")
    public String salvarChamada(
            @RequestParam("dataAula") LocalDate dataAula,
            @RequestParam(value = "presentes", required = false) List<Long> idsPresentes
    ) {
        // 1. Busca todos os alunos
        List<Inscrito> todosAlunos = inscritoRepository.findAll();

        // 2. Para cada aluno, cria um registro de frequência
        for (Inscrito aluno : todosAlunos) {
            if (!aluno.isAtivo()) continue; // Pula alunos inativos

            Frequencia freq = new Frequencia();
            freq.setDataAula(dataAula);
            freq.setInscrito(aluno);

            // Se o ID do aluno está na lista de 'presentes' enviada pelo formulário, true. Senão, false.
            if (idsPresentes != null && idsPresentes.contains(aluno.getId())) {
                freq.setPresente(true);
            } else {
                freq.setPresente(false);
            }

            frequenciaRepository.save(freq);
        }

        return "redirect:/"; // Volta pro Dashboard (ou exibe mensagem de sucesso)
    }
}