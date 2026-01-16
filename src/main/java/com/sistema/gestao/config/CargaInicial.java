package com.sistema.gestao.config;

import com.sistema.gestao.entity.Inscrito;
import com.sistema.gestao.entity.Instituicao;
import com.sistema.gestao.entity.Usuario;
import com.sistema.gestao.repository.InscritoRepository;
import com.sistema.gestao.repository.InstituicaoRepository;
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
    private InstituicaoRepository instituicaoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {

        if (usuarioRepository.findByLogin("admin").isEmpty()) {
            criarUsuario("admin", "123456", "ADMIN");
            criarUsuario("prof", "123456", "PROFESSOR");
        }

        if (instituicaoRepository.count() == 0) {
            Instituicao inst = new Instituicao();
            inst.setNomeProjeto("PROJETO GOL SOCIAL");
            inst.setEnderecoCompleto("Rua do Esporte, 100 - Centro - RJ");
            inst.setTelefone("(21) 99999-0000");
            inst.setEmail("contato@golsocial.com.br");
            instituicaoRepository.save(inst);
        }

        if (inscritoRepository.count() == 0) {
            criarAluno("João Silva", "111.111.111-11", "joao@email.com", "(21) 99888-1111", "Pendente RG");
            criarAluno("Pedro Santos", "222.222.222-22", "pedro@email.com", "(21) 98777-2222", null);
            criarAluno("Julia Costa", "333.333.333-33", "julia@email.com", "(21) 99666-3333", "Foto pendente");
            criarAluno("Gabriel Rocha", "444.444.444-44", "gabriel@email.com", "(21) 99555-4444", null);
            criarAluno("Bruno Castro", "555.555.555-55", "bruno@email.com", "(21) 99444-5555", null);
            criarAluno("Maria Oliveira", "666.666.666-66", "maria@email.com", "(21) 99333-6666", null);
            criarAluno("Ana Costa", "777.777.777-77", "ana@email.com", "(21) 99222-7777", "Asma leve");
            criarAluno("Lucas Pereira", "888.888.888-88", "lucas@email.com", "(21) 99111-8888", null);
            criarAluno("Marcos Lima", "999.999.999-99", "marcos@email.com", "(21) 99000-9999", null);
            criarAluno("Fernanda Alves", "000.000.000-00", "fernanda@email.com", "(21) 98999-0000", null);
            criarAluno("Beatriz Mendes", "121.121.121-12", "beatriz@email.com", "(21) 98888-1212", null);
            criarAluno("Rafael Dias", "131.131.131-13", "rafael@email.com", "(21) 98777-1313", "Goleiro");
            criarAluno("Camila Nunes", "141.141.141-14", "camila@email.com", "(21) 98666-1414", null);
            criarAluno("Larissa Martins", "151.151.151-15", "larissa@email.com", "(21) 98555-1515", null);
            criarAluno("Thiago Azevedo", "161.161.161-16", "thiago@email.com", "(21) 98444-1616", "Sub-15");
        }
    }

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

        // Dados genéricos para evitar erro de NotNull
        i.setEndereco("Rua dos Alunos, S/N");
        i.setBairro("Centro");
        i.setCidade("Rio de Janeiro");
        i.setNomeResponsavel("Responsável pelo " + nome.split(" ")[0]);

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