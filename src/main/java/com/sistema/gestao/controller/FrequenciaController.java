package com.sistema.gestao.controller;

import com.sistema.gestao.entity.Frequencia;
import com.sistema.gestao.entity.Funcionario;
import com.sistema.gestao.entity.Inscrito;
import com.sistema.gestao.entity.Turma;
import com.sistema.gestao.repository.FrequenciaRepository;
import com.sistema.gestao.repository.FuncionarioRepository;
import com.sistema.gestao.repository.InscritoRepository;
import com.sistema.gestao.repository.TurmaRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/frequencia")
public class FrequenciaController {

    @Autowired private TurmaRepository turmaRepository;
    @Autowired private InscritoRepository inscritoRepository;
    @Autowired private FuncionarioRepository funcionarioRepository;
    @Autowired private FrequenciaRepository frequenciaRepository;

    // --- 1. TELA DE CHAMADA (GET) ---
    @GetMapping
    public String abrirChamada(
            @RequestParam(required = false) Long turmaId,
            Model model,
            Authentication authentication) {

        try {
            // 1. Identificar usuário e permissões
            String emailUsuario = authentication.getName();
            boolean isAdmin = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));

            // 2. Buscar turmas permitidas e colocar no Model
            List<Turma> turmasDisponiveis = obterTurmasPorPermissao(emailUsuario, isAdmin);
            model.addAttribute("listaTurmas", turmasDisponiveis);
            model.addAttribute("dataHoje", LocalDate.now());

            // 3. Se escolheu uma turma, validar e carregar dados dos alunos
            if (turmaId != null) {
                Optional<Turma> turmaOpt = turmaRepository.findById(turmaId);

                if (turmaOpt.isPresent()) {
                    Turma turmaSelecionada = turmaOpt.get();

                    // Verifica se o usuário tem permissão para esta turma específica
                    boolean temPermissao = isAdmin || turmasDisponiveis.stream()
                            .anyMatch(t -> t.getId().equals(turmaSelecionada.getId()));

                    if (temPermissao) {
                        carregarDadosDaChamada(model, turmaSelecionada);
                    } else {
                        model.addAttribute("mensagemErro", "Você não tem permissão para acessar esta turma.");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("mensagemErro", "Erro ao carregar página: " + e.getMessage());
        }

        return "chamada";
    }

    // --- 2. SALVAR CHAMADA (POST) ---
    @PostMapping("/salvar")
    public String salvarChamada(
            @RequestParam Long turmaId,
            @RequestParam LocalDate dataAula,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes
    ) {
        try {
            Turma turma = turmaRepository.findById(turmaId)
                    .orElseThrow(() -> new IllegalArgumentException("Turma não encontrada"));

            // CORREÇÃO AQUI: Usando findByTurmasContaining para buscar na lista de turmas
            List<Inscrito> alunos = inscritoRepository.findByTurmasContaining(turma);

            boolean salvouAlgo = false;

            for (Inscrito aluno : alunos) {
                // Pega os dados do formulário (Radio Button + Campo de Texto)
                String status = request.getParameter("presenca_" + aluno.getId());
                String obs = request.getParameter("obs_" + aluno.getId());

                if (status != null && !status.isEmpty()) {
                    Frequencia f = new Frequencia();
                    f.setTurma(turma);
                    f.setInscrito(aluno);
                    f.setDataAula(dataAula);
                    f.setStatus(status);
                    f.setObservacao(obs);

                    frequenciaRepository.save(f);
                    salvouAlgo = true;
                }
            }

            if (salvouAlgo) {
                redirectAttributes.addFlashAttribute("mensagemSucesso", "Chamada realizada com sucesso!");
            } else {
                redirectAttributes.addFlashAttribute("mensagemErro", "Nenhuma presença foi marcada.");
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Erro ao salvar: " + e.getMessage());
        }

        return "redirect:/frequencia?turmaId=" + turmaId;
    }

    // --- MÉTODOS AUXILIARES ---

    private List<Turma> obterTurmasPorPermissao(String email, boolean isAdmin) {
        if (isAdmin) {
            return turmaRepository.findAll();
        }
        return funcionarioRepository.findByEmail(email)
                .map(Funcionario::getTurmas)
                .orElse(new ArrayList<>());
    }

    private void carregarDadosDaChamada(Model model, Turma turma) {
        // CORREÇÃO AQUI: Usando findByTurmasContaining
        List<Inscrito> alunosBanco = inscritoRepository.findByTurmasContaining(turma);

        List<DadosAlunoChamada> listaComStats = new ArrayList<>();

        if (alunosBanco != null) {
            for (Inscrito aluno : alunosBanco) {
                try {
                    // Consultas ao banco para contar presenças e totais
                    long totalAulas = frequenciaRepository.countByInscritoAndTurma(aluno, turma);
                    long presencas = frequenciaRepository.countByInscritoAndTurmaAndStatus(aluno, turma, "P");

                    // Busca histórico das últimas 5 aulas
                    List<String> historico = new ArrayList<>();
                    try {
                        List<Frequencia> ultimas = frequenciaRepository.findTop5ByInscritoOrderByDataAulaDesc(aluno);
                        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM");

                        for (Frequencia f : ultimas) {
                            // Garante que a frequência é desta turma (importante agora que ele tem várias)
                            if (f.getTurma() != null && f.getTurma().getId().equals(turma.getId())) {
                                String st = "P".equals(f.getStatus()) ? "Presente" : ("F".equals(f.getStatus()) ? "Falta" : "Justif.");
                                historico.add(f.getDataAula().format(fmt) + " - " + st);
                            }
                        }
                    } catch (Exception ex) {
                        // Ignora erro no histórico para não travar a lista principal
                    }

                    // Cria o objeto DTO com todos os cálculos
                    listaComStats.add(new DadosAlunoChamada(aluno, totalAulas, presencas, historico));

                } catch (Exception e) {
                    // Erro ignorado para não travar a lista
                }
            }
        }

        model.addAttribute("turmaSelecionada", turma);
        model.addAttribute("alunosStats", listaComStats);
    }

    // --- DTO (CLASSE INTERNA) ---
    public static class DadosAlunoChamada {
        private final Inscrito aluno;
        private final long totalAulas;
        private final long presencas;
        private final long totalFaltas;
        private final int porcentagem;
        private final String cor;
        private final List<String> historicoResumido;

        public DadosAlunoChamada(Inscrito aluno, long totalAulas, long presencas, List<String> historico) {
            this.aluno = aluno;
            this.totalAulas = totalAulas;
            this.presencas = presencas;
            this.historicoResumido = historico;

            // CÁLCULO DAS FALTAS
            this.totalFaltas = (totalAulas >= presencas) ? (totalAulas - presencas) : 0;

            if (totalAulas == 0) {
                this.porcentagem = 100;
                this.cor = "success";
            } else {
                this.porcentagem = (int) ((presencas * 100) / totalAulas);

                if (this.porcentagem < 70) this.cor = "danger";
                else if (this.porcentagem < 80) this.cor = "warning";
                else this.cor = "success";
            }
        }

        // GETTERS OBRIGATÓRIOS PARA O THYMELEAF
        public Inscrito getAluno() { return aluno; }
        public long getTotalAulas() { return totalAulas; }
        public long getTotalPresencas() { return presencas; }
        public long getTotalFaltas() { return totalFaltas; }
        public int getPorcentagem() { return porcentagem; }
        public String getCor() { return cor; }
        public List<String> getHistoricoResumido() { return historicoResumido; }
    }
}