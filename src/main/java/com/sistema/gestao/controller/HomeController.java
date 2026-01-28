package com.sistema.gestao.controller;

import com.sistema.gestao.entity.Inscrito;
import com.sistema.gestao.repository.AtividadeAulaRepository;
import com.sistema.gestao.repository.InscritoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Controller
public class HomeController {

    @Autowired
    private InscritoRepository inscritoRepository;

    @Autowired
    private AtividadeAulaRepository atividadeRepository;

    @GetMapping("/")
    public String dashboard(Model model) {
        // --- 1. CARDS DO TOPO ---
        List<Inscrito> todosAlunos = inscritoRepository.findAll();
        long totalInscritos = todosAlunos.size();
        model.addAttribute("totalInscritos", totalInscritos);

        long totalAulas = atividadeRepository.count();
        model.addAttribute("aulasMes", totalAulas);

        // --- 2. LÓGICA DE ALERTAS ---
        List<AlertaDTO> listaAlertas = new ArrayList<>();

        for (Inscrito aluno : todosAlunos) {
            // Pula alunos inativos (se houver essa lógica) ou continua normal
            if (!aluno.isAtivo()) continue;

            // REGRA 1: Faltas
            long faltas = 0;
            if (aluno.getFrequencias() != null) {
                // CORREÇÃO AQUI:
                // Antes: !f.isPresente()
                // Agora: Verificamos se o status é igual a "F"
                faltas = aluno.getFrequencias().stream()
                        .filter(f -> "F".equals(f.getStatus()))
                        .count();
            }

            if (faltas >= 3) {
                listaAlertas.add(new AlertaDTO(aluno.getNomeCompleto(), faltas + " Faltas", "danger"));
            }

            // REGRA 2: Documentos
            if (aluno.getObservacoes() != null && aluno.getObservacoes().toLowerCase().contains("pendente")) {
                listaAlertas.add(new AlertaDTO(aluno.getNomeCompleto(), "Documentação", "warning"));
            }
        }

        model.addAttribute("listaAlertas", listaAlertas);

        // --- 3. DADOS DO GRÁFICO ---
        String[] meses = new String[6];
        int[] dados = new int[6];
        LocalDate hoje = LocalDate.now();

        for (int i = 0; i < 6; i++) {
            LocalDate dataMes = hoje.minusMonths(5 - i);
            meses[i] = dataMes.getMonth().getDisplayName(TextStyle.SHORT, new Locale("pt", "BR"));
            // Lógica simples para preencher o gráfico (substitua por dados reais se quiser no futuro)
            dados[i] = (int) (totalInscritos / (6 - i > 0 ? 6 - i : 1));
        }

        model.addAttribute("graficoMeses", meses);
        model.addAttribute("graficoDados", dados);

        return "dashboard";
    }

    // DTO Interno para transportar dados para a tela
    public class AlertaDTO {
        public String nome;
        public String motivo;
        public String tipo; // 'danger', 'warning'

        public AlertaDTO(String nome, String motivo, String tipo) {
            this.nome = nome;
            this.motivo = motivo;
            this.tipo = tipo;
        }
    }
}