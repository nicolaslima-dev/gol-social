package com.sistema.gestao.controller;

import com.sistema.gestao.entity.Turma;
import com.sistema.gestao.repository.InscritoRepository;
import com.sistema.gestao.repository.TurmaRepository;
import com.sistema.gestao.service.PdfService; // Importado
import jakarta.servlet.http.HttpServletResponse; // Importado
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException; // Importado

@Controller
@RequestMapping("/turmas")
public class TurmaController {

    @Autowired
    private TurmaRepository turmaRepository;

    @Autowired
    private InscritoRepository inscritoRepository;

    @Autowired
    private PdfService pdfService; // Injeção do Service

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("turmas", turmaRepository.findAll());
        return "listar_turmas";
    }

    @GetMapping("/novo")
    public String nova(Model model) {
        model.addAttribute("turma", new Turma());
        return "cadastro_turma";
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

    // --- NOVO MÉTODO PARA GERAR PDF DA TURMA ---
    @GetMapping("/pdf/{id}")
    public void gerarPdfTurma(@PathVariable Long id, HttpServletResponse response) throws IOException {
        Turma turma = turmaRepository.findById(id).orElse(null);

        if (turma != null) {
            response.setContentType("application/pdf");
            String headerKey = "Content-Disposition";
            // Nome do arquivo ex: Turma_Futsal_Nucleo1.pdf
            String nomeArquivo = "Turma_" + turma.getModalidade() + "_" + turma.getNucleo();
            nomeArquivo = nomeArquivo.replaceAll("\\s+", "_"); // Remove espaços
            String headerValue = "attachment; filename=" + nomeArquivo + ".pdf";

            response.setHeader(headerKey, headerValue);

            // Gera o PDF usando o serviço
            byte[] pdfBytes = pdfService.gerarListaTurma(turma);
            response.getOutputStream().write(pdfBytes);
        }
    }
}