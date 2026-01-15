package com.sistema.gestao.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Data
public class Funcionario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nomeCompleto;
    private String cargo; // Ex: Treinador, Assistente Social
    private String cpf;
    private String telefone;
    private LocalDate dataAdmissao;

    @Column(columnDefinition = "TEXT")
    private String relatorioDesempenho; // Campo para o texto do relatório
}