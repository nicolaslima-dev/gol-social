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

    private String nome;
    private Integer capacidade;

    @ManyToMany(mappedBy = "turmas")
    private List<Funcionario> funcionarios = new ArrayList<>();

    // --- ALTERADO PARA MANY TO MANY ---
    // "mappedBy = turmas" refere-se à lista "turmas" dentro de Inscrito
    @ManyToMany(mappedBy = "turmas")
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

    // --- NOVOS MÉTODOS DE VAGAS ---

    // Retorna quantos estão inscritos
    public int getQuantidadeInscritos() {
        if (inscritos == null) return 0;
        return inscritos.size();
    }

    // Calcula vagas restantes (Não é salvo no banco, calculado na hora)
    public int getVagasRestantes() {
        if (capacidade == null) return 0; // Ou retornar um valor alto se capacidade for opcional
        int ocupadas = getQuantidadeInscritos();
        return Math.max(0, capacidade - ocupadas); // Evita número negativo
    }
}