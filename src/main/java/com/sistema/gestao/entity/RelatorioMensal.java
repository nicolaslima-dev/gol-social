package com.sistema.gestao.entity;

import jakarta.persistence.*;
import lombok.Data; // Importação obrigatória do Lombok
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

@Entity
@Data // A mágica acontece aqui: Gera Getters, Setters, toString, etc.
public class RelatorioMensal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "funcionario_id")
    private Funcionario funcionario;

    private String curso;
    private String polo;
    private String cidade;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dataInicio;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dataFim;

    private LocalDate dataGeracao = LocalDate.now();
}