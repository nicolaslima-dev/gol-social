package com.sistema.gestao.controller;

import com.sistema.gestao.entity.Inscrito;
import com.sistema.gestao.entity.Turma;
import com.sistema.gestao.repository.InscritoRepository;
import com.sistema.gestao.repository.TurmaRepository;
import com.sistema.gestao.service.PdfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/inscritos") // A URL no navegador continua sendo /inscritos
public class InscritoController {

    @Autowired
    private InscritoRepository inscritoRepository;

    @Autowired
    private TurmaRepository turmaRepository;

    @Autowired
    private PdfService pdfService;

    // --- LISTAR ---
    @GetMapping
    public String listar(Model model) {
        model.addAttribute("lista", inscritoRepository.findAll());
        // CORRIGIDO: Procura direto na pasta templates
        return "lista_inscritos";
    }

    // --- NOVO CADASTRO ---
    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("inscrito", new Inscrito());
        model.addAttribute("listaTurmas", turmaRepository.findAll());
        // CORRIGIDO: Procura direto na pasta templates
        return "formulario";
    }

    // --- SALVAR ---
    @PostMapping("/salvar")
    public String salvar(@Valid Inscrito inscrito,
                         BindingResult result,
                         @RequestParam(required = false) List<Long> turmasSelecionadas,
                         Model model,
                         RedirectAttributes attributes) {

        // 1. Validação básica de campos
        if (result.hasErrors()) {
            model.addAttribute("listaTurmas", turmaRepository.findAll());
            return "formulario"; // CORRIGIDO
        }

        // 2. Prepara a lista de turmas
        List<Turma> novasTurmas = new ArrayList<>();
        if (turmasSelecionadas != null && !turmasSelecionadas.isEmpty()) {
            novasTurmas = turmaRepository.findAllById(turmasSelecionadas);
        }

        // --- REGRA 1: MÁXIMO DE 2 TURMAS ---
        if (novasTurmas.size() > 2) {
            model.addAttribute("erroRegra", "Não é permitido se inscrever em mais de 2 turmas.");
            model.addAttribute("listaTurmas", turmaRepository.findAll());
            return "formulario"; // CORRIGIDO
        }

        // --- REGRA 2: VALIDAÇÃO DE MODALIDADES ---
        if (novasTurmas.size() == 2) {
            String modalidade1 = novasTurmas.get(0).getModalidade();
            String modalidade2 = novasTurmas.get(1).getModalidade();

            if (modalidade1 != null && modalidade1.equalsIgnoreCase(modalidade2)) {
                model.addAttribute("erroRegra", "Para fazer duas atividades, deve ser uma de FUTEBOL e uma de FUTSAL. Não é permitido repetir a modalidade.");
                model.addAttribute("listaTurmas", turmaRepository.findAll());
                return "formulario"; // CORRIGIDO
            }
        }

        // --- REGRA 3: VERIFICAR VAGAS ---
        Inscrito alunoAntigo = null;
        if (inscrito.getId() != null) {
            Optional<Inscrito> opt = inscritoRepository.findById(inscrito.getId());
            if (opt.isPresent()) alunoAntigo = opt.get();
        }

        for (Turma t : novasTurmas) {
            boolean jaEstavaNessaTurma = false;
            if (alunoAntigo != null && alunoAntigo.getTurmas() != null) {
                if (alunoAntigo.getTurmas().stream().anyMatch(old -> old.getId().equals(t.getId()))) {
                    jaEstavaNessaTurma = true;
                }
            }

            if (!jaEstavaNessaTurma) {
                int inscritosAtuais = (t.getInscritos() != null) ? t.getInscritos().size() : 0;
                if (inscritosAtuais >= t.getCapacidade()) {
                    model.addAttribute("erroRegra", "A turma " + t.getNome() + " já está lotada!");
                    model.addAttribute("listaTurmas", turmaRepository.findAll());
                    return "formulario"; // CORRIGIDO
                }
            }
        }

        inscrito.setTurmas(novasTurmas);
        inscritoRepository.save(inscrito);

        attributes.addFlashAttribute("mensagem", "Beneficiário salvo com sucesso!");
        return "redirect:/inscritos";
    }

    // --- EDITAR ---
    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        Optional<Inscrito> inscrito = inscritoRepository.findById(id);
        if (inscrito.isPresent()) {
            model.addAttribute("inscrito", inscrito.get());
            model.addAttribute("listaTurmas", turmaRepository.findAll());
            return "formulario"; // CORRIGIDO
        }
        return "redirect:/inscritos";
    }

    // --- EXCLUIR ---
    @GetMapping("/excluir/{id}")
    public String excluir(@PathVariable Long id, RedirectAttributes attributes) {
        inscritoRepository.deleteById(id);
        attributes.addFlashAttribute("mensagem", "Beneficiário removido com sucesso.");
        return "redirect:/inscritos";
    }

    // --- PDF ---
    @GetMapping("/pdf/{id}")
    public ResponseEntity<byte[]> gerarFichaPdf(@PathVariable Long id) {
        Optional<Inscrito> inscritoOpt = inscritoRepository.findById(id);
        if (inscritoOpt.isPresent()) {
            byte[] pdfBytes = pdfService.gerarFichaInscricao(inscritoOpt.get());
            String nomeArquivo = "Ficha_" + inscritoOpt.get().getNomeCompleto().replace(" ", "_") + ".pdf";
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + nomeArquivo)
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);
        }
        return ResponseEntity.notFound().build();
    }
}