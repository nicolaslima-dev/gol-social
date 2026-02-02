package com.sistema.gestao.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void enviarCodigoRecuperacao(String para, String codigo) {
        SimpleMailMessage message = new SimpleMailMessage();
        // Coloque aqui o e-mail que você configurou no application.properties
        message.setFrom("seuemail@gmail.com");
        message.setTo(para);
        message.setSubject("Código de Acesso - Sistema de Gestão");
        message.setText("Olá! \n\nSeu código de verificação é: " + codigo +
                "\n\nEste código expira em 15 minutos.\nUse-o para criar sua senha.");

        mailSender.send(message);
    }
}