package com.sistema.gestao.service;

import com.resend.Resend;
import com.resend.services.emails.model.CreateEmailOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Value("${resend.api.key}")
    private String resendApiKey;

    @Async
    public void enviarCodigoRecuperacao(String para, String codigo) {
        // Agora usando a variável correta da linha 4
        Resend resend = new Resend(resendApiKey);

        // Ajustado para CreateEmailOptions (padrão da v3.1.0)
        CreateEmailOptions params = CreateEmailOptions.builder()
                .from("onboarding@resend.dev")
                .to(para)
                .subject("Código de Acesso - Sistema de Gestão")
                .html("Olá! Seu código de verificação é: <strong>" + codigo + "</strong>")
                .build();

        try {
            resend.emails().send(params);
            System.out.println("E-mail enviado via API Resend!");
        } catch (Exception e) {
            System.err.println("Erro ao enviar e-mail: " + e.getMessage());
        }
    }
}