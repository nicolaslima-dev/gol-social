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

    // Mantenha o seu relacionamento com Inscrito
    @ManyToOne
    @JoinColumn(name = "inscrito_id")
    private Inscrito inscrito;

    // NOVO: Adicione a Turma para facilitar a busca (Inscrito está na turma, mas a aula também)
    @ManyToOne
    @JoinColumn(name = "turma_id")
    private Turma turma;

    // ALTERAÇÃO: Trocamos 'boolean presente' por 'String status' para aceitar "P", "F", "J"
    private String status;

    // NOVO: Para salvar o texto que o professor digitar
    private String observacao;
}