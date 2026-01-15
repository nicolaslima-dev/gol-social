package com.sistema.gestao.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;

@Entity
@Data
public class Inscrito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- DADOS DO ALUNO ---
    private String nomeCompleto;

    private String sexo; // Masculino, Feminino

    private String cpf; // CPF do Aluno

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dataNascimento;

    // --- ENDEREÇO ---
    private String endereco;
    private String bairro;
    private String cidade;

    // --- DADOS DO RESPONSÁVEL ---
    private String nomeResponsavel;
    private String cpfResponsavel;
    private String telefone; // Contato principal
    private String email;    // Email do responsável

    // --- DADOS DO PROJETO ---
    private String modalidade; // Ginástica, Dança
    private String polo;       // Polo 1 ao 10
    private String horario;    // Tarde, Noite

    // --- CONTROLE INTERNO ---
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dataPreenchimento; // Automático

    private String lancadoPor; // Automático (Nome do usuário logado)

    private boolean fichaAnexada; // Checkbox para confirmar se escaneou

    @Column(columnDefinition = "TEXT")
    private String observacoes;

    private boolean ativo = true;

    @OneToMany(mappedBy = "inscrito", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<Frequencia> frequencias;

    // Campo calculado (não salva no banco, calcula na hora)
    @Transient
    public int getIdade() {
        if (dataNascimento == null) return 0;
        return Period.between(dataNascimento, LocalDate.now()).getYears();
    }
}