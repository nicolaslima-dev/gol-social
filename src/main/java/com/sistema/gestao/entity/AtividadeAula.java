package com.sistema.gestao.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

@Entity
@Data
public class AtividadeAula {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate data;

    private String turma; // Ex: Sub-11, Sub-13, Matutino

    @Column(columnDefinition = "TEXT")
    private String descricaoAtividade; // O que foi feito na aula

    @Column(columnDefinition = "TEXT")
    private String ocorrencias; // Machucados, brigas, avisos
}