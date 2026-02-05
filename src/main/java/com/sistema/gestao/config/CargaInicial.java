package com.sistema.gestao.config;

import com.sistema.gestao.entity.Funcionario;
import com.sistema.gestao.entity.Inscrito;
import com.sistema.gestao.entity.Instituicao;
import com.sistema.gestao.entity.Nucleo;
import com.sistema.gestao.entity.Turma;
import com.sistema.gestao.repository.FuncionarioRepository;
import com.sistema.gestao.repository.InscritoRepository;
import com.sistema.gestao.repository.InstituicaoRepository;
import com.sistema.gestao.repository.NucleoRepository;
import com.sistema.gestao.repository.TurmaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Random;

@Configuration
public class CargaInicial implements CommandLineRunner {

    @Autowired private FuncionarioRepository funcionarioRepository;
    @Autowired private InscritoRepository inscritoRepository;
    @Autowired private InstituicaoRepository instituicaoRepository;
    @Autowired private TurmaRepository turmaRepository;
    @Autowired private NucleoRepository nucleoRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {

        // --- 1. ADMIN DE EMERGÊNCIA (CPF NOVO FINAL 99 PARA NÃO TRAVAR) ---
        Optional<Funcionario> adminExistente = funcionarioRepository.findByEmail("admin@golsocial.com.br");
        Funcionario admin;

        if (adminExistente.isPresent()) {
            admin = adminExistente.get();
        } else {
            admin = new Funcionario();
            admin.setEmail("admin@golsocial.com.br");
            // Mudei o CPF para evitar conflito com o antigo que ficou preso no banco
            admin.setCpf("000.000.000-99"); 
        }

        admin.setNomeCompleto("Administrador Geral");
        admin.setPerfil("ADMIN");
        admin.setCargo("Gestor");
        admin.setAtivo(true);
        admin.setSenha(passwordEncoder.encode("580206"));

        funcionarioRepository.save(admin);
        System.out.println(">>> ADMIN RECRIADO COM SUCESSO");


        // --- 2. PROFESSOR (CPF NOVO FINAL 99 PARA NÃO TRAVAR) ---
        Optional<Funcionario> profExistente = funcionarioRepository.findByEmail("professor@gmail.com");
        Funcionario professor;

        if (profExistente.isPresent()) {
            professor = profExistente.get();
        } else {
            professor = new Funcionario();
            professor.setEmail("professor@gmail.com");
            // Mudei o CPF para evitar conflito
            professor.setCpf("111.111.111-99"); 
        }

        professor.setNomeCompleto("Nicolas Silva (Professor)");
        professor.setSenha(passwordEncoder.encode("580206"));
        professor.setPerfil("PROFESSOR");
        professor.setCargo("Treinador");
        professor.setAtivo(true);

        professor = funcionarioRepository.save(professor);
        System.out.println(">>> PROFESSOR RECRIADO COM SUCESSO");


        // --- 3. INSTITUIÇÃO ---
        Instituicao instituicao;
        if (instituicaoRepository.count() == 0) {
            instituicao = new Instituicao();
            instituicao.setNomeProjeto("PROJETO GOL SOCIAL");
            instituicao.setEmail("contato@golsocial.com.br");
            instituicao = instituicaoRepository.save(instituicao);
        } else {
            instituicao = instituicaoRepository.findAll().get(0);
        }
        
        // --- 4. VINCULAR TURMA AO PROFESSOR ---
        // Garante que o professor tenha uma turma para mostrar na tela
        if (turmaRepository.count() == 0) {
            Turma turma = new Turma();
            turma.setNome("Futebol Sub-17 (Manhã)");
            turma.setDiasSemana("Seg/Qua/Sex");
            turma.setHorario("09:00 - 11:00");
            turma.setLocal("Campo Principal");
            turma.setCapacidade(30);
            turma.setProfessor(professor);     // Vincula ao professor novo
            turma.setInstituicao(instituicao);
            turmaRepository.save(turma);
        }

        // --- 5. NÚCLEOS ---
        criarOuAtualizarNucleo("01", "Praça da Cedae", "Rocha Miranda");
        criarOuAtualizarNucleo("02", "Campo da Embaú", "Pavuna");
        criarOuAtualizarNucleo("03", "Praça do Chico", "Pavuna");
        
         // --- 6. ALUNOS ---
         if (inscritoRepository.count() == 0) {
             criarAluno("João Silva", "999.999.999-01");
             criarAluno("Pedro Santos", "999.999.999-02");
         }
    }

    private void criarOuAtualizarNucleo(String numero, String nome, String bairro) {
        Optional<Nucleo> existente = nucleoRepository.findByNumero(numero);
        Nucleo n = existente.orElse(new Nucleo());
        n.setNumero(numero);
        n.setNome(nome);
        n.setBairro(bairro);
        nucleoRepository.save(n);
    }
    
    private void criarAluno(String nome, String cpf) {
        Inscrito i = new Inscrito();
        i.setNomeCompleto(nome);
        i.setCpf(cpf); // CPF ficticio pra nao dar erro
        i.setEmail(nome.split(" ")[0].toLowerCase() + "@teste.com");
        i.setTelefone("21999999999");
        i.setAtivo(true);
        i.setDataNascimento(LocalDate.of(2010, 1, 1));
        inscritoRepository.save(i);
    }
}

