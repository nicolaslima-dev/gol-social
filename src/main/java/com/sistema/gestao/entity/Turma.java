package com.sistema.gestao.entity;

import jakarta.persistence.*;
import java.util.List;
import java.util.ArrayList;

@Entity
public class Turma {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nucleo;
    private String modalidade;
    private String horario;
    private String diasSemana;

    // --- ESTES CAMPOS SÃO OBRIGATÓRIOS PARA O SEU HTML NOVO ---
    private String nome;
    private Integer capacidade;

    @ManyToMany(mappedBy = "turmas")
    private List<Funcionario> funcionarios = new ArrayList<>();

    @OneToMany(mappedBy = "turma")
    private List<Inscrito> inscritos = new ArrayList<>();

    // --- GETTERS E SETTERS ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNucleo() { return nucleo; }
    public void setNucleo(String nucleo) { this.nucleo = nucleo; }

    public String getModalidade() { return modalidade; }
    public void setModalidade(String modalidade) { this.modalidade = modalidade; }

    public String getHorario() { return horario; }
    public void setHorario(String horario) { this.horario = horario; }

    public String getDiasSemana() { return diasSemana; }
    public void setDiasSemana(String diasSemana) { this.diasSemana = diasSemana; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public Integer getCapacidade() { return capacidade; }
    public void setCapacidade(Integer capacidade) { this.capacidade = capacidade; }

    public List<Funcionario> getFuncionarios() { return funcionarios; }
    public void setFuncionarios(List<Funcionario> funcionarios) { this.funcionarios = funcionarios; }

    public List<Inscrito> getInscritos() { return inscritos; }
    public void setInscritos(List<Inscrito> inscritos) { this.inscritos = inscritos; }
}