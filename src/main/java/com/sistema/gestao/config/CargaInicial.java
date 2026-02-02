package com.sistema.gestao.config;

import com.sistema.gestao.entity.Funcionario; // Mudou de Usuario para Funcionario
import com.sistema.gestao.entity.Inscrito;
import com.sistema.gestao.entity.Instituicao;
import com.sistema.gestao.repository.FuncionarioRepository; // Repositorio novo
import com.sistema.gestao.repository.InscritoRepository;
import com.sistema.gestao.repository.InstituicaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Random;

@Configuration
public class CargaInicial implements CommandLineRunner {

    @Autowired
    private FuncionarioRepository funcionarioRepository; // Agora usamos Funcionário

    @Autowired
    private InscritoRepository inscritoRepository;

    @Autowired
    private InstituicaoRepository instituicaoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {

        // --- 1. CRIAR O ADMINISTRADOR (Para você logar agora) ---
        if (funcionarioRepository.findByEmail("admin@golsocial.com.br").isEmpty()) {
            Funcionario admin = new Funcionario();
            admin.setNomeCompleto("Administrador Geral");
            admin.setCpf("000.000.000-00");
            admin.setEmail("admin@golsocial.com.br");
            admin.setDataNascimento(LocalDate.of(1980, 1, 1));
            // A senha "123456" agora é salva CRIPTOGRAFADA
            admin.setSenha(passwordEncoder.encode("123456"));
            admin.setPerfil("ADMIN");
            admin.setCargo("Gestor");
            admin.setAtivo(true);

            funcionarioRepository.save(admin);
            System.out.println(">>> ADMIN CRIADO: admin@golsocial.com.br / 123456");
        }

        // --- 2. CRIAR USUÁRIO PARA TESTAR O "PRIMEIRO ACESSO" ---
        // Altere o email abaixo para o SEU email real se quiser receber o código de verdade
        if (funcionarioRepository.findByEmail("teste@golsocial.com.br").isEmpty()) {
            Funcionario novato = new Funcionario();
            novato.setNomeCompleto("Professor Teste Primeiro Acesso");
            novato.setCpf("111.111.111-11");
            novato.setEmail("nicolassilvap@gamil.com"); // <--- Coloque seu e-mail aqui para testar
            novato.setDataNascimento(LocalDate.of(1995, 5, 20));

            // IMPORTANTE: Senha NULA para forçar o fluxo de "Primeiro Acesso"
            novato.setSenha(null);

            novato.setPerfil("PROFESSOR");
            novato.setCargo("Treinador");
            novato.setAtivo(true);

            funcionarioRepository.save(novato);
            System.out.println(">>> USUÁRIO TESTE CRIADO (SEM SENHA). CPF: 111.111.111-11");
        }

        // --- 3. DADOS DA INSTITUIÇÃO (Mantido do seu código) ---
        if (instituicaoRepository.count() == 0) {
            Instituicao inst = new Instituicao();
            inst.setNomeProjeto("PROJETO GOL SOCIAL");
            inst.setEnderecoCompleto("Rua do Esporte, 100 - Centro - RJ");
            inst.setTelefone("(21) 99999-0000");
            inst.setEmail("contato@golsocial.com.br");
            instituicaoRepository.save(inst);
        }

        // --- 4. LISTA DE ALUNOS (Mantido do seu código) ---
        if (inscritoRepository.count() == 0) {
            criarAluno("João Silva", "111.111.111-11", "joao@email.com", "(21) 99888-1111", "Pendente RG", "Masculino");
            criarAluno("Pedro Santos", "222.222.222-22", "pedro@email.com", "(21) 98777-2222", null, "Masculino");
            criarAluno("Julia Costa", "333.333.333-33", "julia@email.com", "(21) 99666-3333", "Foto pendente", "Feminino");
            criarAluno("Gabriel Rocha", "444.444.444-44", "gabriel@email.com", "(21) 99555-4444", null, "Masculino");
            criarAluno("Bruno Castro", "555.555.555-55", "bruno@email.com", "(21) 99444-5555", null, "Masculino");
            criarAluno("Maria Oliveira", "666.666.666-66", "maria@email.com", "(21) 99333-6666", null, "Feminino");
            criarAluno("Ana Costa", "777.777.777-77", "ana@email.com", "(21) 99222-7777", "Asma leve", "Feminino");
            criarAluno("Lucas Pereira", "888.888.888-88", "lucas@email.com", "(21) 99111-8888", null, "Masculino");
            criarAluno("Marcos Lima", "999.999.999-99", "marcos@email.com", "(21) 99000-9999", null, "Masculino");
            criarAluno("Fernanda Alves", "000.000.000-00", "fernanda@email.com", "(21) 98999-0000", null, "Feminino");
            criarAluno("Beatriz Mendes", "121.121.121-12", "beatriz@email.com", "(21) 98888-1212", null, "Feminino");
            criarAluno("Rafael Dias", "131.131.131-13", "rafael@email.com", "(21) 98777-1313", "Goleiro", "Masculino");
            criarAluno("Camila Nunes", "141.141.141-14", "camila@email.com", "(21) 98666-1414", null, "Feminino");
            criarAluno("Larissa Martins", "151.151.151-15", "larissa@email.com", "(21) 98555-1515", null, "Feminino");
            criarAluno("Thiago Azevedo", "161.161.161-16", "thiago@email.com", "(21) 98444-1616", "Sub-15", "Masculino");
        }
    }

    // Método auxiliar para criar alunos (Mantido)
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
        int ano = 2010 + rand.nextInt(5);
        int mes = 1 + rand.nextInt(12);
        int dia = 1 + rand.nextInt(28);
        i.setDataNascimento(LocalDate.of(ano, mes, dia));
        i.setDataPreenchimento(LocalDate.now());

        i.setObservacoes(obs);
        i.setFichaAnexada(true);
        i.setAtivo(true);
        inscritoRepository.save(i);
    }
}