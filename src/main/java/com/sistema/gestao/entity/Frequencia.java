package com.sistema.gestao.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class Frequencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate dataAula;

    @ManyToOne
    @JoinColumn(name = "inscrito_id")
    private Inscrito inscrito;

    @ManyToOne
    @JoinColumn(name = "turma_id")
    private Turma turma;

    // "P" = Presença, "F" = Falta, "J" = Justificado
    private String status;

    private String observacao;

    // =================================================================
    // GETTERS E SETTERS (Padrão Java Bean)
    // =================================================================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getDataAula() {
        return dataAula;
    }

    public void setDataAula(LocalDate dataAula) {
        this.dataAula = dataAula;
    }

    public Inscrito getInscrito() {
        return inscrito;
    }

    public void setInscrito(Inscrito inscrito) {
        this.inscrito = inscrito;
    }

    public Turma getTurma() {
        return turma;
    }

    public void setTurma(Turma turma) {
        this.turma = turma;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }
}