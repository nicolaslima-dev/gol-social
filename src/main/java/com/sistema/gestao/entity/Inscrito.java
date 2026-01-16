package com.sistema.gestao.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Inscrito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- DADOS PESSOAIS ---

    @NotBlank(message = "O nome é obrigatório")
    private String nomeCompleto;

    private String cpf;

    @NotBlank(message = "O sexo é obrigatório")
    private String sexo;

    @NotNull(message = "A data de nascimento é obrigatória")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dataNascimento;

    // --- ENDEREÇO ---

    @NotBlank(message = "O endereço é obrigatório")
    private String endereco;

    @NotBlank(message = "O bairro é obrigatório")
    private String bairro;

    @NotBlank(message = "A cidade é obrigatória")
    private String cidade;

    // --- RESPONSÁVEL ---

    @NotBlank(message = "O nome do responsável é obrigatório")
    private String nomeResponsavel;

    private String cpfResponsavel;

    @NotBlank(message = "O telefone é obrigatório")
    private String telefone;

    private String email;

    // --- MATRÍCULA ---

    @NotBlank(message = "A modalidade é obrigatória")
    private String modalidade;

    private String horario;

    @NotBlank(message = "O polo é obrigatório")
    private String polo;

    private boolean fichaAnexada;

    private String observacoes;

    // --- CAMPOS DE CONTROLE ---

    private boolean ativo = true;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dataPreenchimento;

    private String lancadoPor;

    // --- RELACIONAMENTOS (Correção do erro do HomeController) ---

    // Um inscrito pode ter várias frequências
    @OneToMany(mappedBy = "inscrito", cascade = CascadeType.ALL)
    private List<Frequencia> frequencias = new ArrayList<>();

    // =================================================================
    // GETTERS E SETTERS
    // =================================================================

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNomeCompleto() { return nomeCompleto; }
    public void setNomeCompleto(String nomeCompleto) { this.nomeCompleto = nomeCompleto; }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public String getSexo() { return sexo; }
    public void setSexo(String sexo) { this.sexo = sexo; }

    public LocalDate getDataNascimento() { return dataNascimento; }
    public void setDataNascimento(LocalDate dataNascimento) { this.dataNascimento = dataNascimento; }

    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = endereco; }

    public String getBairro() { return bairro; }
    public void setBairro(String bairro) { this.bairro = bairro; }

    public String getCidade() { return cidade; }
    public void setCidade(String cidade) { this.cidade = cidade; }

    public String getNomeResponsavel() { return nomeResponsavel; }
    public void setNomeResponsavel(String nomeResponsavel) { this.nomeResponsavel = nomeResponsavel; }

    public String getCpfResponsavel() { return cpfResponsavel; }
    public void setCpfResponsavel(String cpfResponsavel) { this.cpfResponsavel = cpfResponsavel; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getModalidade() { return modalidade; }
    public void setModalidade(String modalidade) { this.modalidade = modalidade; }

    public String getHorario() { return horario; }
    public void setHorario(String horario) { this.horario = horario; }

    public String getPolo() { return polo; }
    public void setPolo(String polo) { this.polo = polo; }

    public boolean isFichaAnexada() { return fichaAnexada; }
    public void setFichaAnexada(boolean fichaAnexada) { this.fichaAnexada = fichaAnexada; }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }

    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }

    public LocalDate getDataPreenchimento() { return dataPreenchimento; }
    public void setDataPreenchimento(LocalDate dataPreenchimento) { this.dataPreenchimento = dataPreenchimento; }

    public String getLancadoPor() { return lancadoPor; }
    public void setLancadoPor(String lancadoPor) { this.lancadoPor = lancadoPor; }

    // Getter e Setter da Lista de Frequências
    public List<Frequencia> getFrequencias() { return frequencias; }
    public void setFrequencias(List<Frequencia> frequencias) { this.frequencias = frequencias; }
}