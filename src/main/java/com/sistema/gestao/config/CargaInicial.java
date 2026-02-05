package com.sistema.gestao.config;

import com.sistema.gestao.entity.Funcionario;
import com.sistema.gestao.entity.Inscrito;
import com.sistema.gestao.entity.Instituicao;
import com.sistema.gestao.repository.FuncionarioRepository;
import com.sistema.gestao.repository.InscritoRepository;
import com.sistema.gestao.repository.InstituicaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Random;

@Configuration
public class CargaInicial implements CommandLineRunner {

    @Autowired
    private FuncionarioRepository funcionarioRepository;

    @Autowired
    private InscritoRepository inscritoRepository;

    @Autowired
    private InstituicaoRepository instituicaoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {

        // --- 1. LOGIN DE ADMINISTRADOR ---
        Optional<Funcionario> adminExistente = funcionarioRepository.findByEmail("admin@golsocial.com.br");
        Funcionario admin;

        if (adminExistente.isPresent()) {
            admin = adminExistente.get();
        } else {
            admin = new Funcionario();
            admin.setEmail("admin@golsocial.com.br");
            admin.setCpf("000.000.000-00");
        }

        admin.setNomeCompleto("Administrador Geral");
        admin.setDataNascimento(LocalDate.of(1980, 1, 1));
        admin.setPerfil("ADMIN");
        admin.setCargo("Gestor");
        admin.setAtivo(true);
        admin.setSenha(passwordEncoder.encode("580206")); // Senha atualizada

        funcionarioRepository.save(admin);
        System.out.println(">>> ADMIN ATUALIZADO: admin@golsocial.com.br / 580206");


        // --- 2. LOGIN DE PROFESSOR (ALTERADO) ---
        // Mudamos para professor@gmail.com
        Optional<Funcionario> profExistente = funcionarioRepository.findByEmail("professor@gmail.com");
        Funcionario professor;

        if (profExistente.isPresent()) {
            professor = profExistente.get();
        } else {
            professor = new Funcionario();
            professor.setEmail("professor@gmail.com"); // E-mail novo
            professor.setCpf("111.111.111-11");
        }

        professor.setNomeCompleto("Professor de Teste");
        professor.setDataNascimento(LocalDate.of(1995, 5, 20));
        professor.setSenha(passwordEncoder.encode("580206")); // Senha atualizada

        professor.setPerfil("PROFESSOR");
        professor.setCargo("Treinador");
        professor.setAtivo(true);

        funcionarioRepository.save(professor);
        System.out.println(">>> PROFESSOR PRONTO: professor@gmail.com / 580206");


        // --- 3. DADOS DA INSTITUIÇÃO ---
        if (instituicaoRepository.count() == 0) {
            Instituicao inst = new Instituicao();
            inst.setNomeProjeto("PROJETO GOL SOCIAL");
            inst.setEnderecoCompleto("Rua do Esporte, 100 - Centro - RJ");
            inst.setTelefone("(21) 99999-0000");
            inst.setEmail("contato@golsocial.com.br");
            instituicaoRepository.save(inst);
        }

        // --- 4. LISTA DE ALUNOS ---
        if (inscritoRepository.count() == 0) {
            criarAluno("João Silva", "111.111.111-11", "joao@email.com", "(21) 99888-1111", "Pendente RG", "Masculino");
            criarAluno("Pedro Santos", "222.222.222-22", "pedro@email.com", "(21) 98777-2222", null, "Masculino");
            criarAluno("Julia Costa", "333.333.333-33", "julia@email.com", "(21) 99666-3333", "Foto pendente", "Feminino");
            criarAluno("Gabriel Rocha", "444.444.444-44", "gabriel@email.com", "(21) 99555-4444", null, "Masculino");
            criarAluno("Bruno Castro", "555.555.555-55", "bruno@email.com", "(21) 99444-5555", null, "Masculino");
        }
    }

    private void criarAluno(String nome, String cpf, String email, String telefone, String obs, String sexo) {
        Inscrito i = new Inscrito();
        i.setNomeCompleto(nome);
        i.setCpf(cpf);
        i.setEmail(email);
        i.setTelefone(telefone);
        i.setSexo(sexo);
        i.setEndereco("Rua dos Alunos, S/N");
        i.setBairro("Centro");
        i.setCidade("Rio de Janeiro");
        i.setNomeResponsavel("Responsável pelo " + nome.split(" ")[0]);
        i.setCpfResponsavel(cpf);
        Random rand = new Random();
        i.setDataNascimento(LocalDate.of(2010 + rand.nextInt(5), 1 + rand.nextInt(12), 1 + rand.nextInt(28)));
        i.setDataPreenchimento(LocalDate.now());
        i.setObservacoes(obs);
        i.setFichaAnexada(true);
        i.setAtivo(true);
        inscritoRepository.save(i);
    }
}