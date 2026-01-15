package com.sistema.gestao.controller;

import com.sistema.gestao.entity.Funcionario;
import com.sistema.gestao.entity.RelatorioMensal;
import com.sistema.gestao.repository.FuncionarioRepository;
import com.sistema.gestao.repository.RelatorioMensalRepository;
import com.sistema.gestao.service.PdfService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;

@Controller
public class RelatorioMensalController {

    @Autowired
    private RelatorioMensalRepository relatorioRepository;

    @Autowired
    private FuncionarioRepository funcionarioRepository;

    @Autowired
    private PdfService pdfService;

    @GetMapping("/relatorios/novo/{funcionarioId}")
    public String novoRelatorio(@PathVariable Long funcionarioId, Model model) {
        Funcionario func = funcionarioRepository.findById(funcionarioId).orElse(null);
        if (func == null) return "redirect:/funcionarios";

        RelatorioMensal relatorio = new RelatorioMensal();
        relatorio.setFuncionario(func);
        relatorio.setCidade("Rio de Janeiro");
        relatorio.setDataInicio(LocalDate.now().withDayOfMonth(1));
        relatorio.setDataFim(LocalDate.now());

        model.addAttribute("relatorio", relatorio);
        return "form_relatorio_mensal";
    }

    @PostMapping("/relatorios/salvar")
    public void salvarEGerarPdf(@ModelAttribute RelatorioMensal relatorio,
                                @RequestParam(value = "descricao", required = false) String descricao,
                                HttpServletResponse response) throws IOException {

        // 1. Recarrega o funcionário do banco para ter o Nome correto
        if (relatorio.getFuncionario() != null && relatorio.getFuncionario().getId() != null) {
            Funcionario f = funcionarioRepository.findById(relatorio.getFuncionario().getId()).orElse(null);
            if (f != null) {
                relatorio.setFuncionario(f);
            }
        }

        // 2. Salva no banco
        relatorioRepository.save(relatorio);

        // 3. Define nome do arquivo (Proteção contra nome vazio)
        String nomeProf = "Funcionario";
        if (relatorio.getFuncionario() != null && relatorio.getFuncionario().getNomeCompleto() != null) {
            nomeProf = relatorio.getFuncionario().getNomeCompleto();
        }
        String nomeArquivo = "Relatorio_" + nomeProf.replace(" ", "_") + ".pdf";

        // 4. Gera PDF
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=" + nomeArquivo);

        byte[] pdfBytes = pdfService.gerarRelatorioMensalPreenchido(relatorio, descricao);
        response.getOutputStream().write(pdfBytes);
    }
}