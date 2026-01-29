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

    @ManyToOne
    @JoinColumn(name = "turma_id")
    private Turma turma;

    // --- NOVO CAMPO: Vínculo com o Professor ---
    @ManyToOne
    @JoinColumn(name = "funcionario_id")
    private Funcionario professor;

    @Column(columnDefinition = "TEXT")
    private String descricaoAtividade;

    @Column(columnDefinition = "TEXT")
    private String ocorrencias;
}