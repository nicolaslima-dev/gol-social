package com.sistema.gestao.config;

import com.sistema.gestao.entity.Funcionario;
import com.sistema.gestao.repository.FuncionarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

@Configuration
public class CargaInicial implements CommandLineRunner {

    @Autowired
    private FuncionarioRepository funcionarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {

        // O email que será o Login
        String emailAdmin = "admin@golsocial.br";

        Optional<Funcionario> adminExistente = funcionarioRepository.findByEmail(emailAdmin);

        if (adminExistente.isEmpty()) {
            Funcionario admin = new Funcionario();

            // Dados de Login
            admin.setEmail(emailAdmin);
            admin.setSenha(passwordEncoder.encode("580206"));
            admin.setPerfil("ADMIN");
            admin.setAtivo(true);

            // Dados "Fakes" obrigatórios (Só para o sistema aceitar salvar)
            admin.setNomeCompleto("ADMINISTRADOR DO SISTEMA"); // Nome genérico
            admin.setCpf("000.000.000-00"); // CPF Zerado
            admin.setDataNascimento(LocalDate.of(2000, 1, 1));
            admin.setCargo("Sistema");

            // Se houver validação de endereço no Funcionario, adicione strings vazias ou traços:
            // admin.setEndereco("-");
            // admin.setTelefone("-");

            funcionarioRepository.save(admin);

            // Log limpo
            System.out.println(">>> SUCESSO: Login de Admin configurado (" + emailAdmin + ")");
        }
    }
}