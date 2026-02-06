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
        Resend resend = new Resend(resendApiKey);

        // Agora enviando do seu domínio personalizado
        CreateEmailOptions params = CreateEmailOptions.builder()
                .from("sistema@golsocial.app.br") //
                .to(para)
                .subject("Código de Acesso - Gol Social")
                .html("Olá! Seu código de verificação é: <strong>" + codigo + "</strong>")
                .build();

        try {
            resend.emails().send(params);
            System.out.println("E-mail enviado via seu domínio oficial!");
        } catch (Exception e) {
            System.err.println("Erro ao enviar e-mail: " + e.getMessage());
        }
    }
}