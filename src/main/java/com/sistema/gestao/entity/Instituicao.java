package com.sistema.gestao.entity;

import jakarta.persistence.*; // O asterisco (*) importa tudo, inclusive @Lob e @Column

@Entity
public class Instituicao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nomeProjeto;
    private String enderecoCompleto;
    private String telefone;
    private String email;

    // --- NOVO CAMPO PARA O LOGO ---
    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] logo;

    // Construtor vazio
    public Instituicao() {}

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNomeProjeto() { return nomeProjeto; }
    public void setNomeProjeto(String nomeProjeto) { this.nomeProjeto = nomeProjeto; }

    public String getEnderecoCompleto() { return enderecoCompleto; }
    public void setEnderecoCompleto(String enderecoCompleto) { this.enderecoCompleto = enderecoCompleto; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    // Getter e Setter da Imagem
    public byte[] getLogo() { return logo; }
    public void setLogo(byte[] logo) { this.logo = logo; }
}