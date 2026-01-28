package com.sistema.gestao.controller;

import com.sistema.gestao.entity.Frequencia;
import com.sistema.gestao.entity.Funcionario;
import com.sistema.gestao.entity.Inscrito;
import com.sistema.gestao.entity.ResumoFrequencia;
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

    @GetMapping
    public String abrirChamada(
            @RequestParam(required = false) Long turmaId,
            Model model,
            Authentication authentication) {

        String emailUsuario = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));

        List<Turma> turmasDisponiveis = new ArrayList<>();

        // 1. Lógica de Permissão (Admin vê tudo, Prof vê suas turmas)
        if (isAdmin) {
            turmasDisponiveis = turmaRepository.findAll();
        } else {
            Optional<Funcionario> funcOpt = funcionarioRepository.findByEmail(emailUsuario);
            if (funcOpt.isPresent()) {
                turmasDisponiveis = funcOpt.get().getTurmas();
            }
        }

        model.addAttribute("listaTurmas", turmasDisponiveis);
        model.addAttribute("dataHoje", LocalDate.now());

        // 2. Se uma turma foi selecionada, calcula a frequência REAL
        if (turmaId != null) {
            Turma turmaSelecionada = turmaRepository.findById(turmaId).orElse(null);

            if (turmaSelecionada != null && (isAdmin || turmasDisponiveis.contains(turmaSelecionada))) {

                List<Inscrito> alunosBanco = inscritoRepository.findByTurma(turmaSelecionada);
                List<ResumoFrequencia> listaComStats = new ArrayList<>();

                for (Inscrito aluno : alunosBanco) {

                    // -- DADOS VINDOS DO BANCO DE DADOS --

                    // Total de aulas dadas para essa turma com esse aluno
                    long totalAulas = frequenciaRepository.countByInscritoAndTurma(aluno, turmaSelecionada);

                    // Total de presenças
                    long presencas = frequenciaRepository.countByInscritoAndTurmaAndStatus(aluno, turmaSelecionada, "P");

                    // Histórico das últimas 5 aulas
                    List<String> historico = new ArrayList<>();
                    List<Frequencia> ultimas = frequenciaRepository.findTop5ByInscritoOrderByDataAulaDesc(aluno);
                    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM");

                    for(Frequencia f : ultimas) {
                        // Garante que só mostra histórico da turma atual
                        if(f.getTurma().getId().equals(turmaSelecionada.getId())) {
                            String statusTexto = "";
                            if(f.getStatus().equals("P")) statusTexto = "Presente";
                            else if(f.getStatus().equals("F")) statusTexto = "Falta";
                            else statusTexto = "Justificado";

                            historico.add(f.getDataAula().format(fmt) + " - " + statusTexto);
                        }
                    }

                    // Adiciona na lista de exibição
                    listaComStats.add(new ResumoFrequencia(aluno, totalAulas, presencas, historico));
                }

                model.addAttribute("turmaSelecionada", turmaSelecionada);
                model.addAttribute("alunosStats", listaComStats);
            }
        }

        return "chamada";
    }

    // 3. Método para SALVAR a chamada no banco
    @PostMapping("/salvar")
    public String salvarChamada(
            @RequestParam Long turmaId,
            @RequestParam LocalDate dataAula,
            HttpServletRequest request
    ) {
        Turma turma = turmaRepository.findById(turmaId).orElseThrow();
        List<Inscrito> alunos = inscritoRepository.findByTurma(turma);

        for (Inscrito aluno : alunos) {
            // Recupera o que foi marcado no rádio (P, F ou J)
            String status = request.getParameter("presenca_" + aluno.getId());
            // Recupera a observação
            String obs = request.getParameter("obs_" + aluno.getId());

            if (status != null) {
                // Salva no banco
                Frequencia f = new Frequencia();
                f.setTurma(turma);
                f.setInscrito(aluno);
                f.setDataAula(dataAula);
                f.setStatus(status);
                f.setObservacao(obs);

                frequenciaRepository.save(f);
            }
        }

        return "redirect:/frequencia?turmaId=" + turmaId;
    }
}