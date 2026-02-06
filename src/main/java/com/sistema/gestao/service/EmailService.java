package com.sistema.gestao.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value; // Para pegar o e-mail das variáveis
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async; // Importante
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    // Pega o e-mail configurado nas variáveis de ambiente ou usa o padrão
    @Value("${spring.mail.username:seuemail@gmail.com}")
    private String emailOrigem;

    @Async // Executa em uma thread separada para não travar o login
    public void enviarCodigoRecuperacao(String para, String codigo) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(emailOrigem);
            message.setTo(para);
            message.setSubject("Código de Acesso - Sistema de Gestão");
            message.setText("Olá! \n\nSeu código de verificação é: " + codigo +
                    "\n\nEste código expira em 15 minutos.\nUse-o para criar sua senha.");

            mailSender.send(message);
            System.out.println("E-mail enviado com sucesso para: " + para);

        } catch (Exception e) {
            // Se der erro (ex: porta 587 bloqueada), o sistema não trava
            System.err.println("FALHA AO ENVIAR E-MAIL: " + e.getMessage());
            // Aqui você pode logar que o e-mail falhou, mas o código já está no banco para validação
        }
    }
}