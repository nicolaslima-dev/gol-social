package com.sistema.gestao.entity;

import java.util.List;

public class ResumoFrequencia {

    private Inscrito aluno;
    private long totalAulas;
    private long totalPresencas;
    private long totalFaltas;
    private int porcentagem;
    private List<String> historicoResumido;

    // Construtor
    public ResumoFrequencia(Inscrito aluno, long totalAulas, long totalPresencas, List<String> historicoResumido) {
        this.aluno = aluno;
        this.totalAulas = totalAulas;
        this.totalPresencas = totalPresencas;
        this.totalFaltas = totalAulas - totalPresencas;
        this.historicoResumido = historicoResumido;

        // Cálculo da porcentagem (evita divisão por zero)
        if (totalAulas > 0) {
            this.porcentagem = (int) ((totalPresencas * 100) / totalAulas);
        } else {
            this.porcentagem = 100;
        }
    }

    // Lógica para definir a cor no HTML (Bootstrap)
    public String getCor() {
        if (porcentagem < 70) {
            return "danger"; // Vermelho
        } else if (porcentagem < 80) {
            return "warning"; // Amarelo
        } else {
            return "success"; // Verde
        }
    }

    // Getters
    public Inscrito getAluno() { return aluno; }
    public long getTotalAulas() { return totalAulas; }
    public long getTotalPresencas() { return totalPresencas; }
    public long getTotalFaltas() { return totalFaltas; }
    public int getPorcentagem() { return porcentagem; }
    public List<String> getHistoricoResumido() { return historicoResumido; }
}