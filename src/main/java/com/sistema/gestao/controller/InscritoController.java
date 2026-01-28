package com.sistema.gestao.controller;

import com.sistema.gestao.entity.Inscrito;
import com.sistema.gestao.repository.InscritoRepository;
import com.sistema.gestao.repository.TurmaRepository;
import com.sistema.gestao.service.PdfService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.time.LocalDate;

@Controller
public class InscritoController {

    @Autowired
    private InscritoRepository repository;

    @Autowired
    private TurmaRepository turmaRepository; // Necessário para os Cards

    @Autowired
    private PdfService pdfService;

    // LISTAR
    @GetMapping("/inscritos")
    public String listar(Model model) {
        model.addAttribute("lista", repository.findAll());
        return "lista_inscritos"; // Ou o nome da sua lista
    }

    // NOVO (Chama o formulario.html)
    @GetMapping("/inscritos/novo")
    public String novo(Model model) {
        model.addAttribute("inscrito", new Inscrito());
        model.addAttribute("listaTurmas", turmaRepository.findAll()); // Envia turmas para os Cards
        return "formulario"; // <--- AJUSTADO AQUI
    }

    // SALVAR
    @PostMapping("/inscritos/salvar")
    public String salvar(@Valid @ModelAttribute Inscrito inscrito, BindingResult result, Model model) {

        if (result.hasErrors()) {
            // Se der erro, precisamos reenviar a lista de turmas para o formulário não quebrar
            model.addAttribute("listaTurmas", turmaRepository.findAll());
            return "formulario"; // <--- AJUSTADO AQUI
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String usuarioLogado = (auth != null) ? auth.getName() : "Sistema";

        if (inscrito.getId() == null) {
            inscrito.setDataPreenchimento(LocalDate.now());
        }
        inscrito.setLancadoPor(usuarioLogado);

        repository.save(inscrito);
        return "redirect:/inscritos";
    }

    // EDITAR (Chama o formulario.html)
    @GetMapping("/inscritos/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        Inscrito inscrito = repository.findById(id).orElse(null);

        if (inscrito != null) {
            model.addAttribute("inscrito", inscrito);
            model.addAttribute("listaTurmas", turmaRepository.findAll()); // Envia turmas
            return "formulario"; // <--- AJUSTADO AQUI
        }
        return "redirect:/inscritos";
    }

    // ... (Mantenha os métodos de inativar, ativar, excluir e PDF iguais ao que você já tinha) ...
    @GetMapping("/inscritos/inativar/{id}")
    public String inativar(@PathVariable Long id) {
        Inscrito inscrito = repository.findById(id).orElse(null);
        if (inscrito != null) {
            inscrito.setAtivo(false);
            repository.save(inscrito);
        }
        return "redirect:/inscritos";
    }

    @GetMapping("/inscritos/ativar/{id}")
    public String ativar(@PathVariable Long id) {
        Inscrito inscrito = repository.findById(id).orElse(null);
        if (inscrito != null) {
            inscrito.setAtivo(true);
            repository.save(inscrito);
        }
        return "redirect:/inscritos";
    }

    @GetMapping("/inscritos/excluir/{id}")
    public String excluir(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            repository.deleteById(id);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Não é possível excluir. O beneficiário possui registros vinculados.");
        }
        return "redirect:/inscritos";
    }

    @GetMapping("/inscritos/pdf/{id}")
    public void gerarPdf(@PathVariable Long id, HttpServletResponse response) throws IOException {
        Inscrito inscrito = repository.findById(id).orElse(null);

        if (inscrito != null) {
            response.setContentType("application/pdf");
            String headerKey = "Content-Disposition";
            String headerValue = "attachment; filename=ficha_" + inscrito.getNomeCompleto().replace(" ", "_") + ".pdf";
            response.setHeader(headerKey, headerValue);

            byte[] pdfBytes = pdfService.gerarFichaInscricao(inscrito);
            response.getOutputStream().write(pdfBytes);
        }
    }
}