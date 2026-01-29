package com.sistema.gestao.controller;

import com.sistema.gestao.entity.Frequencia;
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
            // Pula inativos ou alunos sem turma vinculada
            if (!aluno.isAtivo() || aluno.getTurma() == null) continue;

            // --- CÁLCULO DE FREQUÊNCIA (Importado do FrequenciaController) ---
            List<Frequencia> historico = aluno.getFrequencias();

            // Filtra apenas aulas da turma atual do aluno
            long totalAulasTurma = historico.stream()
                    .filter(f -> f.getTurma() != null && f.getTurma().getId().equals(aluno.getTurma().getId()))
                    .count();

            if (totalAulasTurma > 0) {
                // Conta quantas presenças ("P") ele teve nessa turma
                long presencas = historico.stream()
                        .filter(f -> f.getTurma() != null && f.getTurma().getId().equals(aluno.getTurma().getId()))
                        .filter(f -> "P".equals(f.getStatus()))
                        .count();

                // Matemática simples: (Presenças / Total) * 100
                double porcentagem = (double) presencas / totalAulasTurma * 100;

                // REGRA: Se for menor que 70%, gera o alerta
                if (porcentagem < 70.0) {

                    // Monta o texto bonito: "Futsal Sub-11 (Núc. 10) - 65% Freq"
                    String infoTurma = aluno.getTurma().getModalidade() + " " + aluno.getTurma().getNome();
                    String infoNucleo = "(Núc. " + aluno.getTurma().getNucleo() + ")";
                    String textoAlerta = String.format("%s %s - %.0f%% Freq", infoTurma, infoNucleo, porcentagem);

                    listaAlertas.add(new AlertaDTO(aluno.getNomeCompleto(), textoAlerta, "danger"));
                }
            }

            // --- OUTRAS REGRAS (Mantidas) ---
            // REGRA: Documentos Pendentes
            if (aluno.getObservacoes() != null && aluno.getObservacoes().toLowerCase().contains("pendente")) {
                listaAlertas.add(new AlertaDTO(aluno.getNomeCompleto(), "Documentação Pendente", "warning"));
            }
        }

        model.addAttribute("listaAlertas", listaAlertas);

        // --- 3. DADOS DO GRÁFICO (Mantido igual) ---
        String[] meses = new String[6];
        int[] dados = new int[6];
        LocalDate hoje = LocalDate.now();

        for (int i = 0; i < 6; i++) {
            LocalDate dataMes = hoje.minusMonths(5 - i);
            meses[i] = dataMes.getMonth().getDisplayName(TextStyle.SHORT, new Locale("pt", "BR"));
            dados[i] = (int) (totalInscritos / (6 - i > 0 ? 6 - i : 1));
        }

        model.addAttribute("graficoMeses", meses);
        model.addAttribute("graficoDados", dados);

        return "dashboard";
    }

    // DTO Interno para transportar dados para a tela
    public class AlertaDTO {
        public String nome;
        public String motivo; // Agora carrega Turma + Núcleo + %
        public String tipo; // 'danger', 'warning'

        public AlertaDTO(String nome, String motivo, String tipo) {
            this.nome = nome;
            this.motivo = motivo;
            this.tipo = tipo;
        }
    }
}