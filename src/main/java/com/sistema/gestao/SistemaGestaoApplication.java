package com.sistema.gestao;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync; // Importante

@SpringBootApplication
@EnableAsync // Habilita o processamento assíncrono
public class SistemaGestaoApplication {

    public static void main(String[] args) {
        SpringApplication.run(SistemaGestaoApplication.class, args);
    }
}