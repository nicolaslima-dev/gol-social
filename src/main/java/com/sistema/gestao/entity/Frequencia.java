package com.sistema.gestao.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Data
public class Frequencia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate dataAula;

    // Relacionamento: Uma frequência pertence a UM aluno
    @ManyToOne
    @JoinColumn(name = "inscrito_id")
    private Inscrito inscrito;

    private boolean presente; // true = veio, false = faltou
}