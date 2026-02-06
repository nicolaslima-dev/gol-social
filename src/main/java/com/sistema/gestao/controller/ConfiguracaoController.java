package com.sistema.gestao.controller;

import com.sistema.gestao.entity.Funcionario;
import com.sistema.gestao.entity.Inscrito;
import com.sistema.gestao.entity.Instituicao;
import com.sistema.gestao.entity.RelatorioMensal;
import com.sistema.gestao.entity.Usuario;
import com.sistema.gestao.repository.FuncionarioRepository;
import com.sistema.gestao.repository.InscritoRepository;
import com.sistema.gestao.repository.InstituicaoRepository;
import com.sistema.gestao.repository.RelatorioMensalRepository;
import com.sistema.gestao.repository.UsuarioRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Controller
@PreAuthorize("hasRole('ADMIN')")
public class ConfiguracaoController {

    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private InstituicaoRepository instituicaoRepository;
    @Autowired private FuncionarioRepository funcionarioRepository;
    @Autowired private InscritoRepository inscritoRepository;
    @Autowired private RelatorioMensalRepository relatorioMensalRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    // --- TELA INICIAL (COM O FILTRO DE OURO) ---
    @GetMapping("/configuracoes")
    public String abrirConfiguracoes(Model model) {

        // 1. Busca todos que têm perfil ADMIN na tabela de Usuários
        List<Usuario> todosAdmins = usuarioRepository.findByPerfil("ADMIN");

        // 2. Cria uma lista para armazenar apenas os "Admins Puros" (Sem vínculo de funcionário)
        List<Usuario> adminsPuros = new ArrayList<>();

        for (Usuario u : todosAdmins) {
            // Se NÃO existir um funcionário com esse email, ele é um Admin de Sistema (Puro)
            if (funcionarioRepository.findByEmail(u.getLogin()).isEmpty()) {
                adminsPuros.add(u);
            }
        }

        // 3. Envia para a tela apenas os Admins Puros para evitar excluir funcionários por engano
        model.addAttribute("usuarios", adminsPuros);

        Instituicao instituicao = instituicaoRepository.findById(1L).orElse(new Instituicao());
        if (instituicao.getNomeProjeto() == null) instituicao.setNomeProjeto("PROJETO GOL SOCIAL");

        model.addAttribute("instituicao", instituicao);
        return "configuracoes";
    }

    // --- USUÁRIOS (ADMINISTRADORES PUROS) ---
    @PostMapping("/usuarios/criar")
    public String criarUsuario(@RequestParam String login,
                               @RequestParam String senha,
                               RedirectAttributes redirectAttributes) {

        if (usuarioRepository.findByLogin(login).isPresent()) {
            redirectAttributes.addFlashAttribute("erro", "Este login já está em uso!");
            return "redirect:/configuracoes";
        }

        // Validação extra: Não deixar criar login se já for e-mail de um funcionário
        if (funcionarioRepository.findByEmail(login).isPresent()) {
            redirectAttributes.addFlashAttribute("erro", "Este e-mail pertence a um funcionário. Use a tela de Funcionários.");
            return "redirect:/configuracoes";
        }

        try {
            Usuario novo = new Usuario();
            novo.setLogin(login);
            novo.setSenha(passwordEncoder.encode(senha));
            novo.setPerfil("ADMIN"); // Força ser ADMIN, já que é criado na tela de configuração
            usuarioRepository.save(novo);
            redirectAttributes.addFlashAttribute("sucesso", "Administrador criado com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao criar usuário.");
        }

        return "redirect:/configuracoes";
    }

    @GetMapping("/usuarios/excluir/{id}")
    public String excluirUsuario(@PathVariable Long id, Principal principal, RedirectAttributes redirectAttributes) {

        Optional<Usuario> usuarioAlvo = usuarioRepository.findById(id);

        if (usuarioAlvo.isEmpty()) {
            redirectAttributes.addFlashAttribute("erro", "Usuário não encontrado.");
            return "redirect:/configuracoes";
        }

        // Trava 1: Não excluir o Admin ID 1
        if (id == 1) {
            redirectAttributes.addFlashAttribute("erro", "O Administrador Principal (ID 1) não pode ser excluído.");
            return "redirect:/configuracoes";
        }

        // Trava 2: Não excluir a si mesmo
        if (principal.getName().equals(usuarioAlvo.get().getLogin())) {
            redirectAttributes.addFlashAttribute("erro", "Você não pode excluir seu próprio usuário logado.");
            return "redirect:/configuracoes";
        }

        usuarioRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("sucesso", "Administrador removido com sucesso.");

        return "redirect:/configuracoes";
    }

    // --- INSTITUIÇÃO ---
    @PostMapping("/instituicao/salvar")
    public String salvarInstituicao(@ModelAttribute Instituicao instituicaoForm,
                                    @RequestParam("logoUpload") MultipartFile logoUpload,
                                    RedirectAttributes redirectAttributes) {
        try {
            Instituicao banco = instituicaoRepository.findById(1L).orElse(new Instituicao());
            banco.setId(1L);
            banco.setNomeProjeto(instituicaoForm.getNomeProjeto());
            banco.setEnderecoCompleto(instituicaoForm.getEnderecoCompleto());
            banco.setTelefone(instituicaoForm.getTelefone());
            banco.setEmail(instituicaoForm.getEmail());

            if (!logoUpload.isEmpty()) {
                banco.setLogo(logoUpload.getBytes());
            }
            instituicaoRepository.save(banco);
            redirectAttributes.addFlashAttribute("sucesso", "Dados da instituição salvos!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao salvar instituição.");
        }

        return "redirect:/configuracoes?aba=instituicao";
    }

    // =================================================================================
    // MÉTODOS DE BACKUP
    // =================================================================================

    @GetMapping("/backup/funcionarios")
    public void baixarBackupFuncionarios(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=backup_funcionarios.csv");
        PrintWriter writer = response.getWriter();
        writer.write('\uFEFF');
        writer.println("ID;Nome Completo;Cargo;Data Admissao;Status;Telefone");
        List<Funcionario> lista = funcionarioRepository.findAll();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        for (Funcionario f : lista) {
            writer.println(String.format("%d;%s;%s;%s;%s;%s",
                    f.getId(), tratarTexto(f.getNomeCompleto()), tratarTexto(f.getCargo()),
                    f.getDataAdmissao() != null ? f.getDataAdmissao().format(fmt) : "",
                    f.isAtivo() ? "Ativo" : "Inativo", tratarTexto(f.getTelefone())));
        }
    }

    @GetMapping("/backup/alunos")
    public void baixarBackupAlunos(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=backup_alunos.csv");
        PrintWriter writer = response.getWriter();
        writer.write('\uFEFF');
        writer.println("ID;Nome Completo;Data Nascimento;Responsavel;Telefone;Bairro;Cidade");
        List<Inscrito> lista = inscritoRepository.findAll();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        for (Inscrito i : lista) {
            writer.println(String.format("%d;%s;%s;%s;%s;%s;%s",
                    i.getId(), tratarTexto(i.getNomeCompleto()),
                    i.getDataNascimento() != null ? i.getDataNascimento().format(fmt) : "",
                    tratarTexto(i.getNomeResponsavel()), tratarTexto(i.getTelefone()),
                    tratarTexto(i.getBairro()), tratarTexto(i.getCidade())));
        }
    }

    @GetMapping("/backup/relatorios")
    public void baixarBackupRelatorios(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=backup_relatorios.csv");
        PrintWriter writer = response.getWriter();
        writer.write('\uFEFF');
        writer.println("ID;Funcionario;Data Inicio;Data Fim;Cidade;Curso;Polo");
        List<RelatorioMensal> lista = relatorioMensalRepository.findAll();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        for (RelatorioMensal r : lista) {
            String nomeFunc = r.getFuncionario() != null ? r.getFuncionario().getNomeCompleto() : "N/A";
            writer.println(String.format("%d;%s;%s;%s;%s;%s;%s",
                    r.getId(), tratarTexto(nomeFunc),
                    r.getDataInicio() != null ? r.getDataInicio().format(fmt) : "",
                    r.getDataFim() != null ? r.getDataFim().format(fmt) : "",
                    tratarTexto(r.getCidade()), tratarTexto(r.getCurso()), tratarTexto(r.getPolo())));
        }
    }

    @GetMapping("/backup/completo")
    public void baixarBackupCompleto(HttpServletResponse response) throws IOException {
        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment; filename=backup_sistema_completo.zip");
        try (ZipOutputStream zos = new ZipOutputStream(response.getOutputStream())) {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            // Funcionarios
            zos.putNextEntry(new ZipEntry("funcionarios.csv"));
            PrintWriter writerFunc = new PrintWriter(new OutputStreamWriter(zos, StandardCharsets.UTF_8));
            writerFunc.write('\uFEFF');
            writerFunc.println("ID;Nome Completo;Cargo;Data Admissao;Status;Telefone");
            for (Funcionario f : funcionarioRepository.findAll()) {
                writerFunc.println(String.format("%d;%s;%s;%s;%s;%s",
                        f.getId(), tratarTexto(f.getNomeCompleto()), tratarTexto(f.getCargo()),
                        f.getDataAdmissao() != null ? f.getDataAdmissao().format(fmt) : "",
                        f.isAtivo() ? "Ativo" : "Inativo", tratarTexto(f.getTelefone())));
            }
            writerFunc.flush();
            zos.closeEntry();

            // Alunos
            zos.putNextEntry(new ZipEntry("alunos_inscritos.csv"));
            PrintWriter writerAlunos = new PrintWriter(new OutputStreamWriter(zos, StandardCharsets.UTF_8));
            writerAlunos.write('\uFEFF');
            writerAlunos.println("ID;Nome Completo;Data Nascimento;Responsavel;Telefone;Bairro;Cidade");
            for (Inscrito i : inscritoRepository.findAll()) {
                writerAlunos.println(String.format("%d;%s;%s;%s;%s;%s;%s",
                        i.getId(), tratarTexto(i.getNomeCompleto()),
                        i.getDataNascimento() != null ? i.getDataNascimento().format(fmt) : "",
                        tratarTexto(i.getNomeResponsavel()), tratarTexto(i.getTelefone()),
                        tratarTexto(i.getBairro()), tratarTexto(i.getCidade())));
            }
            writerAlunos.flush();
            zos.closeEntry();

            // Relatorios
            zos.putNextEntry(new ZipEntry("historico_relatorios.csv"));
            PrintWriter writerRel = new PrintWriter(new OutputStreamWriter(zos, StandardCharsets.UTF_8));
            writerRel.write('\uFEFF');
            writerRel.println("ID;Funcionario;Data Inicio;Data Fim;Cidade;Curso;Polo");
            for (RelatorioMensal r : relatorioMensalRepository.findAll()) {
                String nomeFunc = r.getFuncionario() != null ? r.getFuncionario().getNomeCompleto() : "N/A";
                writerRel.println(String.format("%d;%s;%s;%s;%s;%s;%s",
                        r.getId(), tratarTexto(nomeFunc),
                        r.getDataInicio() != null ? r.getDataInicio().format(fmt) : "",
                        r.getDataFim() != null ? r.getDataFim().format(fmt) : "",
                        tratarTexto(r.getCidade()), tratarTexto(r.getCurso()), tratarTexto(r.getPolo())));
            }
            writerRel.flush();
            zos.closeEntry();
        }
    }

    private String tratarTexto(String texto) {
        if (texto == null) return "";
        return texto.replace(";", ",").replace("\n", " ").replace("\r", "");
    }
}