package com.sistema.gestao.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.sistema.gestao.entity.Inscrito;
import com.sistema.gestao.entity.RelatorioMensal;
import com.sistema.gestao.entity.Instituicao;
import com.sistema.gestao.repository.InstituicaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class PdfService {

    @Autowired
    private InstituicaoRepository instituicaoRepository;

    private Instituicao getDadosInstituicao() {
        return instituicaoRepository.findById(1L).orElseGet(() -> {
            Instituicao inst = new Instituicao();
            inst.setNomeProjeto("PROJETO GOL SOCIAL");
            inst.setEnderecoCompleto("Rua Exemplo, 123 - Rio de Janeiro - RJ");
            return inst;
        });
    }

    // =================================================================================

// TIPO 1: FICHA DE INSCRIÇÃO (MANTIDA IGUAL)

// =================================================================================

    public byte[] gerarFichaInscricao(Inscrito inscrito) {

        Document document = new Document(PageSize.A4, 40, 40, 40, 40);

        ByteArrayOutputStream out = new ByteArrayOutputStream();



        try {

            PdfWriter.getInstance(document, out);

            document.open();



// LOGO

            try {

                ClassPathResource img = new ClassPathResource("static/img/logo_gol_social.png");

                if(img.exists()) {

                    Image logo = Image.getInstance(img.getURL());

                    logo.scaleToFit(520, 100);

                    logo.setAlignment(Element.ALIGN_CENTER);

                    document.add(logo);

                }

            } catch(Exception e){}



            document.add(new Paragraph("\n"));



            Font fTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20);

            Paragraph titulo = new Paragraph("FICHA DE INSCRIÇÃO", fTitulo);

            titulo.setAlignment(Element.ALIGN_CENTER);

            titulo.setSpacingAfter(20);

            document.add(titulo);



            Font fBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);

            Font fNormal = FontFactory.getFont(FontFactory.HELVETICA, 12);



// DADOS DO ALUNO

            adicionarTituloSecao(document, "DADOS PESSOAIS DO ALUNO", fBold);

            PdfPTable table1 = new PdfPTable(2); table1.setWidthPercentage(100); table1.setWidths(new float[]{1.5f, 1f}); table1.setSpacingAfter(15);

            adicionarCelula(table1, "Nome Completo:", inscrito.getNomeCompleto(), fBold, fNormal, 2);

            String cpf = (inscrito.getCpf() != null) ? inscrito.getCpf() : "";

            adicionarCelula(table1, "CPF:", cpf, fBold, fNormal, 1);

            adicionarCelula(table1, "Sexo:", inscrito.getSexo(), fBold, fNormal, 1);

            String nasc = inscrito.getDataNascimento() != null ? inscrito.getDataNascimento().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "";

            adicionarCelula(table1, "Data de Nascimento:", nasc, fBold, fNormal, 1);



            String idadeTexto = "";

            try {

                if (inscrito.getDataNascimento() != null) {

                    int anos = java.time.Period.between(inscrito.getDataNascimento(), LocalDate.now()).getYears();

                    idadeTexto = anos + " anos";

                }

            } catch (Exception e) {}

            adicionarCelula(table1, "Idade:", idadeTexto, fBold, fNormal, 1);

            document.add(table1);



// ENDEREÇO

            adicionarTituloSecao(document, "ENDEREÇO E CONTATO", fBold);

            PdfPTable table2 = new PdfPTable(2); table2.setWidthPercentage(100); table2.setSpacingAfter(15);

            adicionarCelula(table2, "Endereço:", inscrito.getEndereco(), fBold, fNormal, 2);

            adicionarCelula(table2, "Bairro:", inscrito.getBairro(), fBold, fNormal, 1);

            adicionarCelula(table2, "Cidade:", inscrito.getCidade(), fBold, fNormal, 1);

            document.add(table2);



// RESPONSÁVEL

            adicionarTituloSecao(document, "DADOS DO RESPONSÁVEL", fBold);

            PdfPTable table3 = new PdfPTable(2); table3.setWidthPercentage(100); table3.setSpacingAfter(15);

            adicionarCelula(table3, "Nome Responsável:", inscrito.getNomeResponsavel(), fBold, fNormal, 2);

            adicionarCelula(table3, "Telefone:", inscrito.getTelefone(), fBold, fNormal, 1);

            adicionarCelula(table3, "CPF Resp.:", inscrito.getCpfResponsavel(), fBold, fNormal, 1);

            adicionarCelula(table3, "E-mail:", inscrito.getEmail(), fBold, fNormal, 2);

            document.add(table3);



// MATRÍCULA

            adicionarTituloSecao(document, "DADOS DA MATRÍCULA", fBold);

            PdfPTable table4 = new PdfPTable(3); table4.setWidthPercentage(100); table4.setSpacingAfter(15);

            adicionarCelula(table4, "Modalidade:", "Futebol", fBold, fNormal, 1);

            adicionarCelula(table4, "Polo:", inscrito.getPolo(), fBold, fNormal, 1);

            adicionarCelula(table4, "Horário:", inscrito.getHorario(), fBold, fNormal, 1);

            document.add(table4);



// RODAPÉ FIXO

            PdfPTable tableAss = new PdfPTable(1);

            tableAss.setWidthPercentage(100);

            tableAss.getDefaultCell().setBorder(Rectangle.NO_BORDER);

            tableAss.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);



            String dataHoje = LocalDate.now().format(DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy"));

            tableAss.addCell(new Paragraph("Rio de Janeiro, " + dataHoje, fNormal));

            tableAss.addCell(new Paragraph("\n\n\n"));

            tableAss.addCell(new Paragraph("___________________________________________________", fBold));

            tableAss.addCell(new Paragraph("Assinatura do Responsável", fBold));



            document.add(new Paragraph("\n\n"));

            document.add(tableAss);



            document.close();

        } catch (Exception e) {

            e.printStackTrace();

        }

        return out.toByteArray();

    }



    // =================================================================================
    // RELATÓRIO MENSAL — PADRÃO PROFISSIONAL
    // =================================================================================
    public byte[] gerarRelatorioMensalPreenchido(RelatorioMensal relatorio, String descricao) {

        Document document = new Document(PageSize.A4, 30, 30, 20, 20);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Instituicao dadosInst = getDadosInstituicao();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // LOGO
            try {
                ClassPathResource img = new ClassPathResource("static/img/logo_gol_social.png");
                if (img.exists()) {
                    Image logo = Image.getInstance(img.getURL());
                    logo.scaleToFit(500, 70);
                    logo.setAlignment(Element.ALIGN_CENTER);
                    document.add(logo);
                }
            } catch (Exception ignored) {}

            Font fTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Paragraph titulo = new Paragraph("RELATÓRIO MENSAL", fTitulo);
            titulo.setAlignment(Element.ALIGN_CENTER);
            titulo.setSpacingAfter(15);
            document.add(titulo);

            Font fBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
            Font fNormal = FontFactory.getFont(FontFactory.HELVETICA, 10);

            // INFORMAÇÕES GERAIS
            adicionarTituloSecao(document, "INFORMAÇÕES GERAIS", fBold);

            PdfPTable info = new PdfPTable(2);
            info.setWidthPercentage(100);

            adicionarCelula(info, "Curso / Atividade:", relatorio.getCurso(), fBold, fNormal, 1);
            adicionarCelula(info, "Polo / Núcleo:", relatorio.getPolo(), fBold, fNormal, 1);
            adicionarCelula(info, "Cidade:", relatorio.getCidade(), fBold, fNormal, 1);

            String prof = relatorio.getFuncionario() != null
                    ? relatorio.getFuncionario().getNomeCompleto()
                    : "";

            adicionarCelula(info, "Professor(a):", prof, fBold, fNormal, 1);
            document.add(info);

            // PERÍODO
            adicionarTituloSecao(document, "PERÍODO DE REFERÊNCIA", fBold);

            PdfPTable periodo = new PdfPTable(2);
            periodo.setWidthPercentage(100);

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            adicionarCelula(periodo, "Data Início:",
                    relatorio.getDataInicio() != null ? relatorio.getDataInicio().format(fmt) : "",
                    fBold, fNormal, 1);

            adicionarCelula(periodo, "Data Fim:",
                    relatorio.getDataFim() != null ? relatorio.getDataFim().format(fmt) : "",
                    fBold, fNormal, 1);

            document.add(periodo);

            // ================= DESCRIÇÃO COM LINHAS (ESTÁVEL – OPENPDF) =================
            adicionarTituloSecao(document, "DESCRIÇÃO DAS ATIVIDADES / OCORRÊNCIAS", fBold);

            Font fonteDesc = FontFactory.getFont(FontFactory.HELVETICA, 10);

// CONFIGURAÇÕES
            int totalLinhas = 20;
            float alturaLinha = 18f;

// TEXTO DO USUÁRIO
            String texto = descricao != null ? descricao : "";

// Quebra manual por ENTER
            String[] linhasTexto = texto.split("\\r?\\n");

// TABELA EXTERNA (BORDA GERAL)
            PdfPTable tabelaContainer = new PdfPTable(1);
            tabelaContainer.setWidthPercentage(100);

            PdfPCell container = new PdfPCell();
            container.setBorder(Rectangle.BOX);
            container.setBorderWidth(1f);
            container.setPadding(0);

// TABELA INTERNA (LINHAS)
            PdfPTable tabelaLinhas = new PdfPTable(1);
            tabelaLinhas.setWidthPercentage(100);

            for (int i = 0; i < totalLinhas; i++) {

                String conteudoLinha = "";
                if (i < linhasTexto.length) {
                    conteudoLinha = linhasTexto[i];
                    // evita texto grande demais na linha
                    if (conteudoLinha.length() > 110) {
                        conteudoLinha = conteudoLinha.substring(0, 110);
                    }
                }

                PdfPCell linha = new PdfPCell(new Paragraph(conteudoLinha, fonteDesc));
                linha.setFixedHeight(alturaLinha);
                linha.setPaddingLeft(6);
                linha.setPaddingBottom(4);
                linha.setVerticalAlignment(Element.ALIGN_BOTTOM);

                // borda apenas embaixo (linha de caderno)
                linha.setBorder(Rectangle.BOTTOM);
                linha.setBorderColor(Color.LIGHT_GRAY);

                // última linha sem borda inferior
                if (i == totalLinhas - 1) {
                    linha.setBorder(Rectangle.NO_BORDER);
                }

                tabelaLinhas.addCell(linha);
            }

            container.addElement(tabelaLinhas);
            tabelaContainer.addCell(container);
            document.add(tabelaContainer);


            // ESPAÇO ANTES DAS ASSINATURAS
            document.add(new Paragraph("\n\n\n"));

            // ASSINATURAS (CENTRALIZADAS E MAIS ABAIXO)
            PdfPTable ass = new PdfPTable(2);
            ass.setWidthPercentage(70);
            ass.setHorizontalAlignment(Element.ALIGN_CENTER);
            ass.setWidths(new float[]{1.1f, 1.1f});

            ass.addCell(criarCelulaAssinatura("Assinatura do Instrutor", fBold, fNormal));
            ass.addCell(criarCelulaAssinatura("Visto da Coordenação", fBold, fNormal));

            document.add(ass);

            // ENDEREÇO
            document.add(new Paragraph("\n"));
            Paragraph footer = new Paragraph(
                    dadosInst.getEnderecoCompleto(),
                    FontFactory.getFont(FontFactory.HELVETICA, 8, Color.GRAY)
            );
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return out.toByteArray();
    }

    // =================================================================================
    // MÉTODOS AUXILIARES
    // =================================================================================
    private void adicionarTituloSecao(Document doc, String titulo, Font font) throws DocumentException {
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10);
        PdfPCell cell = new PdfPCell(new Paragraph(titulo, font));
        cell.setBackgroundColor(Color.LIGHT_GRAY);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(4);
        table.addCell(cell);
        doc.add(table);
    }

    private void adicionarCelula(PdfPTable table, String label, String valor,
                                 Font fBold, Font fNormal, int colspan) {

        PdfPCell cell = new PdfPCell();
        cell.setColspan(colspan);
        cell.setPadding(5);

        Paragraph p = new Paragraph();
        p.add(new Chunk(label + " ", fBold));
        p.add(new Chunk(valor != null ? valor : "", fNormal));

        cell.addElement(p);
        table.addCell(cell);
    }

    private PdfPCell criarCelulaAssinatura(String texto, Font fBold, Font fNormal) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.addElement(new Paragraph("_______________________________", fNormal));
        cell.addElement(new Paragraph(texto, fBold));
        return cell;
    }
}
