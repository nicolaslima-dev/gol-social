package com.sistema.gestao.entity;

import jakarta.persistence.*;
import lombok.Data; // Lombok para Getters/Setters

@Entity
@Data
public class Instituicao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nomeProjeto;
    private String enderecoCompleto;
    private String telefone;
    private String email;
}