package com.sistema.gestao.config;

import com.sistema.gestao.entity.Inscrito;
import com.sistema.gestao.entity.Usuario;
import com.sistema.gestao.repository.InscritoRepository;
import com.sistema.gestao.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Random;

@Configuration
public class CargaInicial implements CommandLineRunner {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private InscritoRepository inscritoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {

        // 1. GARANTIR QUE OS USUÁRIOS DE LOGIN EXISTAM
        if (usuarioRepository.findByLogin("admin").isEmpty()) {
            criarUsuario("admin", "123456", "ADMIN");
            criarUsuario("prof", "123456", "PROFESSOR");
            // REMOVIDO: Usuário FINANCEIRO
            System.out.println(">>> Usuários de acesso criados (Senha: 123456).");
        }

        // 2. CRIAR 15 BENEFICIÁRIOS FICTÍCIOS (Se o banco estiver vazio)
        if (inscritoRepository.count() == 0) {
            System.out.println(">>> Iniciando carga de 15 beneficiários...");

            // GRUPO 1: Com Pendências
            criarAluno("João Silva", "111.111.111-11", "joao.silva@email.com", "(21) 99888-1111", "pendente RG do responsável");
            criarAluno("Pedro Santos", "222.222.222-22", "pedrinho@email.com", "(21) 98777-2222", "Atestado médico pendente");
            criarAluno("Julia Costa", "333.333.333-33", "julia.costa@email.com", "(21) 99666-3333", "Foto 3x4 pendente");
            criarAluno("Gabriel Rocha", "444.444.444-44", "gabriel.r@email.com", "(21) 99555-4444", "Comprovante de residência pendente");
            criarAluno("Bruno Castro", "555.555.555-55", "bruno.c@email.com", "(21) 99444-5555", "pendente assinatura na ficha");

            // GRUPO 2: Regulares
            criarAluno("Maria Oliveira", "666.666.666-66", "maria.oli@email.com", "(21) 99333-6666", null);
            criarAluno("Ana Costa", "777.777.777-77", "ana.costa@email.com", "(21) 99222-7777", "Aluno possui asma leve (usa bombinha)");
            criarAluno("Lucas Pereira", "888.888.888-88", "lucas.p@email.com", "(21) 99111-8888", null);
            criarAluno("Marcos Lima", "999.999.999-99", "marcos.lima@email.com", "(21) 99000-9999", "Irmão da aluna Fernanda");
            criarAluno("Fernanda Alves", "000.000.000-00", "nanda.alves@email.com", "(21) 98999-0000", null);
            criarAluno("Beatriz Mendes", "121.121.121-12", "bea.mendes@email.com", "(21) 98888-1212", null);
            criarAluno("Rafael Dias", "131.131.131-13", "rafa.dias@email.com", "(21) 98777-1313", "Goleiro titular");
            criarAluno("Camila Nunes", "141.141.141-14", "camila.n@email.com", "(21) 98666-1414", null);
            criarAluno("Larissa Martins", "151.151.151-15", "lari.martins@email.com", "(21) 98555-1515", null);
            criarAluno("Thiago Azevedo", "161.161.161-16", "thiago.az@email.com", "(21) 98444-1616", "Joga na categoria Sub-15");

            System.out.println(">>> 15 Beneficiários criados com sucesso!");
        }
    }

    // --- MÉTODOS AUXILIARES ---

    private void criarUsuario(String login, String senha, String perfil) {
        Usuario u = new Usuario();
        u.setLogin(login);
        u.setSenha(passwordEncoder.encode(senha));
        u.setPerfil(perfil);
        usuarioRepository.save(u);
    }

    private void criarAluno(String nome, String cpf, String email, String telefone, String obs) {
        Inscrito i = new Inscrito();
        i.setNomeCompleto(nome);
        i.setCpf(cpf);
        i.setEmail(email);
        i.setTelefone(telefone);

        Random rand = new Random();
        int ano = 2010 + rand.nextInt(5);
        int mes = 1 + rand.nextInt(12);
        int dia = 1 + rand.nextInt(28);
        i.setDataNascimento(LocalDate.of(ano, mes, dia));

        i.setObservacoes(obs);
        i.setAtivo(true);
        inscritoRepository.save(i);
    }
}