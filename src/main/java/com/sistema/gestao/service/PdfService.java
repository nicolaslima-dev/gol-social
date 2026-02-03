package com.sistema.gestao.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.sistema.gestao.entity.Inscrito;
import com.sistema.gestao.entity.Instituicao;
import com.sistema.gestao.entity.RelatorioMensal;
import com.sistema.gestao.repository.InstituicaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class PdfService {

    @Autowired
    private InstituicaoRepository instituicaoRepository;

    private static final Color COR_CINZA_ESCURO = Color.DARK_GRAY;

    // =================================================================================
    // 1. FICHA DE INSCRIÇÃO (MANTIDA ORIGINAL)
    // =================================================================================
    public byte[] gerarFichaInscricao(Inscrito inscrito) {
        Document document = new Document(PageSize.A4, 36, 36, 36, 36);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter writer = PdfWriter.getInstance(document, out);
            document.open();

            adicionarLogoSimples(document);

            Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, COR_CINZA_ESCURO);
            Font fontSecao = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
            Font fontLabel = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.DARK_GRAY);
            Font fontDados = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);

            Paragraph titulo = new Paragraph("FICHA DE CADASTRO", fontTitulo);
            titulo.setAlignment(Element.ALIGN_CENTER);
            titulo.setSpacingAfter(20);
            document.add(titulo);

            adicionarTituloSecaoFicha(document, "DADOS PESSOAIS DO ALUNO", fontSecao);
            PdfPTable tabelaPessoal = criarTabelaGrade(new float[]{4f, 2f});
            addCelulaGrade(tabelaPessoal, "Nome Completo:", inscrito.getNomeCompleto(), fontLabel, fontDados);
            addCelulaGrade(tabelaPessoal, "CPF:", inscrito.getCpf(), fontLabel, fontDados);
            String dataNasc = inscrito.getDataNascimento() != null ? inscrito.getDataNascimento().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "";
            addCelulaGrade(tabelaPessoal, "Data de Nascimento:", dataNasc, fontLabel, fontDados);
            addCelulaGrade(tabelaPessoal, "Sexo:", inscrito.getSexo(), fontLabel, fontDados);
            document.add(tabelaPessoal);
            document.add(new Paragraph(" "));

            adicionarTituloSecaoFicha(document, "ENDEREÇO E CONTATO", fontSecao);
            PdfPTable tabelaEnd = criarTabelaGrade(new float[]{4f, 2f});
            addCelulaGrade(tabelaEnd, "Endereço:", inscrito.getEndereco(), fontLabel, fontDados);
            addCelulaGrade(tabelaEnd, "Bairro:", inscrito.getBairro(), fontLabel, fontDados);
            document.add(tabelaEnd);

            PdfPTable tabelaCidade = criarTabelaGrade(new float[]{1f});
            addCelulaGrade(tabelaCidade, "Cidade:", inscrito.getCidade(), fontLabel, fontDados);
            document.add(tabelaCidade);
            document.add(new Paragraph(" "));

            adicionarTituloSecaoFicha(document, "DADOS DO RESPONSÁVEL", fontSecao);
            PdfPTable tabelaResp = criarTabelaGrade(new float[]{3f, 2f});
            addCelulaGrade(tabelaResp, "Nome Responsável:", inscrito.getNomeResponsavel(), fontLabel, fontDados);
            addCelulaGrade(tabelaResp, "Telefone:", inscrito.getTelefone(), fontLabel, fontDados);
            addCelulaGrade(tabelaResp, "CPF Resp.:", inscrito.getCpfResponsavel(), fontLabel, fontDados);
            addCelulaGrade(tabelaResp, "E-mail:", inscrito.getEmail(), fontLabel, fontDados);
            document.add(tabelaResp);
            document.add(new Paragraph(" "));

            adicionarTituloSecaoFicha(document, "DADOS DA MATRÍCULA", fontSecao);
            PdfPTable tabelaMatricula = criarTabelaGrade(new float[]{1f, 1f, 1f});
            String modalidade = "Não informada", polo = "Não informado", horario = "Não informado";
            if (inscrito.getTurma() != null) {
                modalidade = inscrito.getTurma().getModalidade();
                polo = inscrito.getTurma().getNucleo();
                horario = inscrito.getTurma().getHorario();
            }
            addCelulaGrade(tabelaMatricula, "Modalidade:", modalidade, fontLabel, fontDados);
            addCelulaGrade(tabelaMatricula, "Polo/Núcleo:", polo, fontLabel, fontDados);
            addCelulaGrade(tabelaMatricula, "Horário:", horario, fontLabel, fontDados);
            document.add(tabelaMatricula);

            if (inscrito.getObservacoes() != null && !inscrito.getObservacoes().isEmpty()) {
                document.add(new Paragraph(" "));
                adicionarTituloSecaoFicha(document, "OBSERVAÇÕES", fontSecao);
                PdfPTable tabelaObs = criarTabelaGrade(new float[]{1f});
                PdfPCell cellObs = new PdfPCell(new Phrase(inscrito.getObservacoes(), fontDados));
                cellObs.setPadding(8);
                cellObs.setBorder(Rectangle.BOX);
                tabelaObs.addCell(cellObs);
                document.add(tabelaObs);
            }

            PdfPTable footerTable = new PdfPTable(1);
            footerTable.setTotalWidth(523);
            LocalDate hoje = LocalDate.now();
            Locale br = new Locale("pt", "BR");
            String dataExtenso = "Rio de Janeiro, " + hoje.getDayOfMonth() + " de " + hoje.getMonth().getDisplayName(java.time.format.TextStyle.FULL, br) + " de " + hoje.getYear();
            PdfPCell cellData = new PdfPCell(new Phrase(dataExtenso, fontDados));
            cellData.setBorder(Rectangle.NO_BORDER);
            cellData.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cellData.setPaddingBottom(30);
            footerTable.addCell(cellData);
            PdfPCell cellLinha = new PdfPCell(new Phrase("__________________________________________________________", fontDados));
            cellLinha.setBorder(Rectangle.NO_BORDER);
            cellLinha.setHorizontalAlignment(Element.ALIGN_CENTER);
            footerTable.addCell(cellLinha);
            PdfPCell cellAss = new PdfPCell(new Phrase("Assinatura do Responsável", fontLabel));
            cellAss.setBorder(Rectangle.NO_BORDER);
            cellAss.setHorizontalAlignment(Element.ALIGN_CENTER);
            footerTable.addCell(cellAss);
            footerTable.writeSelectedRows(0, -1, 36, 120, writer.getDirectContent());

            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return out.toByteArray();
    }

    // =================================================================================
    // 2. RELATÓRIO MENSAL (VERSÃO FINAL: HEADER CENTRALIZADO, FOOTER LADO A LADO)
    // =================================================================================
    public byte[] gerarRelatorioMensalPreenchido(RelatorioMensal relatorio, String descricao) {
        // Margem inferior 190 para caber o rodapé
        Document document = new Document(PageSize.A4, 30, 30, 20, 190);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Instituicao dadosInst = getDadosInstituicao();

        try {
            PdfWriter writer = PdfWriter.getInstance(document, out);
            document.open();

            // --- CABEÇALHO CORRIGIDO (Título Centralizado) ---
            adicionarCabecalhoInstitucional(document);

            Font fBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
            Font fNormal = FontFactory.getFont(FontFactory.HELVETICA, 10);

            document.add(new Paragraph(" "));

            // TABELA 1: INFORMAÇÕES GERAIS
            PdfPTable tabelaInfo = new PdfPTable(2);
            tabelaInfo.setWidthPercentage(100);
            adicionarCabecalhoRelatorio(tabelaInfo, "INFORMAÇÕES GERAIS", 2);
            adicionarCelulaComBorda(tabelaInfo, "Curso / Atividade:", relatorio.getCurso(), fBold, fNormal);
            adicionarCelulaComBorda(tabelaInfo, "Polo / Núcleo:", relatorio.getPolo(), fBold, fNormal);
            adicionarCelulaComBorda(tabelaInfo, "Cidade:", relatorio.getCidade(), fBold, fNormal);
            String prof = relatorio.getFuncionario() != null ? relatorio.getFuncionario().getNomeCompleto() : "";
            adicionarCelulaComBorda(tabelaInfo, "Nome:", prof, fBold, fNormal);
            document.add(tabelaInfo);
            document.add(new Paragraph(" "));

            // TABELA 2: PERÍODO
            PdfPTable tabelaPeriodo = new PdfPTable(2);
            tabelaPeriodo.setWidthPercentage(100);
            adicionarCabecalhoRelatorio(tabelaPeriodo, "PERÍODO DE REFERÊNCIA", 2);
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            String dataIni = relatorio.getDataInicio() != null ? relatorio.getDataInicio().format(fmt) : "";
            String dataFim = relatorio.getDataFim() != null ? relatorio.getDataFim().format(fmt) : "";
            adicionarCelulaComBorda(tabelaPeriodo, "Data Início:", dataIni, fBold, fNormal);
            adicionarCelulaComBorda(tabelaPeriodo, "Data Fim:", dataFim, fBold, fNormal);
            document.add(tabelaPeriodo);
            document.add(new Paragraph(" "));

            // --- TABELA 3: DESCRIÇÃO INTELIGENTE ---
            PdfPTable tabelaDescContainer = new PdfPTable(1);
            tabelaDescContainer.setWidthPercentage(100);
            adicionarCabecalhoRelatorio(tabelaDescContainer, "DESCRIÇÃO DAS ATIVIDADES / OCORRÊNCIAS", 1);

            PdfPCell celulaLinhas = new PdfPCell();
            celulaLinhas.setBorder(Rectangle.BOX);
            celulaLinhas.setPadding(0);

            PdfPTable tabelaLinhas = new PdfPTable(1);
            tabelaLinhas.setWidthPercentage(100);

            int totalLinhasDisponiveis = 16;
            float alturaLinha = 18f;
            Font fonteDesc = FontFactory.getFont(FontFactory.HELVETICA, 10);
            float larguraUtilLinha = 525f;

            List<String> linhasVisuais = quebrarTextoEmLinhasVisuais(descricao, fonteDesc, larguraUtilLinha);

            for (int i = 0; i < totalLinhasDisponiveis; i++) {
                String conteudo = (i < linhasVisuais.size()) ? linhasVisuais.get(i) : "";

                PdfPCell linha = new PdfPCell(new Paragraph(conteudo, fonteDesc));
                linha.setFixedHeight(alturaLinha);
                linha.setBorder(Rectangle.BOTTOM);
                linha.setBorderColor(Color.LIGHT_GRAY);
                linha.setVerticalAlignment(Element.ALIGN_BOTTOM);
                linha.setPaddingLeft(5);
                linha.setPaddingBottom(5);

                if (i == totalLinhasDisponiveis - 1) linha.setBorder(Rectangle.NO_BORDER);

                tabelaLinhas.addCell(linha);
            }

            celulaLinhas.addElement(tabelaLinhas);
            tabelaDescContainer.addCell(celulaLinhas);
            document.add(tabelaDescContainer);

            // --- RODAPÉ AJUSTADO (LOGOS AFASTADAS) ---
            adicionarRodapeInstitucional(writer, dadosInst.getEnderecoCompleto(), fBold);

            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return out.toByteArray();
    }

    // =================================================================================
    // CÁLCULO DE LARGURA DE TEXTO
    // =================================================================================
    private List<String> quebrarTextoEmLinhasVisuais(String texto, Font font, float larguraMaxima) {
        List<String> linhasFinais = new ArrayList<>();
        if (texto == null || texto.isEmpty()) return linhasFinais;

        BaseFont baseFont = font.getBaseFont();
        float tamanhoFonte = font.getSize();
        String[] paragrafos = texto.split("\\r?\\n");

        for (String paragrafo : paragrafos) {
            if (paragrafo.trim().isEmpty()) {
                linhasFinais.add("");
                continue;
            }
            String[] palavras = paragrafo.split(" ");
            StringBuilder linhaAtual = new StringBuilder();

            for (String palavra : palavras) {
                String testeLinha = linhaAtual.length() == 0 ? palavra : linhaAtual + " " + palavra;
                float larguraTeste = baseFont.getWidthPoint(testeLinha, tamanhoFonte);

                if (larguraTeste <= larguraMaxima) {
                    linhaAtual.append(linhaAtual.length() == 0 ? "" : " ").append(palavra);
                } else {
                    linhasFinais.add(linhaAtual.toString());
                    linhaAtual = new StringBuilder(palavra);
                }
            }
            if (linhaAtual.length() > 0) {
                linhasFinais.add(linhaAtual.toString());
            }
        }
        return linhasFinais;
    }

    // =================================================================================
    // MÉTODOS AUXILIARES DE DESIGN (AJUSTADOS)
    // =================================================================================

    private void adicionarCabecalhoInstitucional(Document document) throws DocumentException {
        // Tabela 3 colunas para garantir centralização perfeita:
        // [VAZIO 15%] [TEXTO 70%] [GOL SOCIAL 15%]
        PdfPTable header = new PdfPTable(3);
        header.setWidthPercentage(100);
        header.setWidths(new float[]{1.5f, 7f, 1.5f});
        header.setSpacingAfter(10);

        // 1. Coluna Vazia (Esquerda) - Serve para empurrar o texto para o meio exato
        PdfPCell cellVazia = new PdfPCell();
        cellVazia.setBorder(Rectangle.NO_BORDER);
        header.addCell(cellVazia);

        // 2. Texto (Centralizado na página)
        PdfPCell cellTexto = new PdfPCell();
        cellTexto.setBorder(Rectangle.NO_BORDER);
        cellTexto.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cellTexto.setHorizontalAlignment(Element.ALIGN_CENTER);

        Font fTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Paragraph p1 = new Paragraph("RELATÓRIO MENSAL", fTitulo);
        p1.setAlignment(Element.ALIGN_CENTER);

        cellTexto.addElement(p1);
        header.addCell(cellTexto);

        // 3. Logo Gol Social (Direita)
        PdfPCell cellGol = new PdfPCell();
        cellGol.setBorder(Rectangle.NO_BORDER);
        cellGol.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cellGol.setHorizontalAlignment(Element.ALIGN_RIGHT);
        try {
            ClassPathResource imgGol = new ClassPathResource("static/img/logo_gol_social.jpeg");
            Image logoGol = Image.getInstance(imgGol.getURL());
            logoGol.scaleToFit(80, 70);
            logoGol.setAlignment(Element.ALIGN_RIGHT);
            cellGol.addElement(logoGol);
        } catch (Exception e) { }
        header.addCell(cellGol);

        document.add(header);

        // Linha divisória
        PdfPTable linha = new PdfPTable(1);
        linha.setWidthPercentage(100);
        PdfPCell cellL = new PdfPCell();
        cellL.setBorder(Rectangle.BOTTOM);
        cellL.setBorderWidth(1.5f);
        cellL.setBorderColor(Color.LIGHT_GRAY);
        cellL.setFixedHeight(2f);
        linha.addCell(cellL);
        document.add(linha);
    }

    private void adicionarRodapeInstitucional(PdfWriter writer, String endereco, Font fontBold) {
        try {
            PdfPTable tableFooter = new PdfPTable(1);
            tableFooter.setTotalWidth(530);

            // 1. ASSINATURAS
            PdfPTable ass = new PdfPTable(2);
            ass.setWidths(new float[]{1f, 1f});
            ass.addCell(criarCelulaAssinatura("Assinatura do Instrutor", fontBold));
            ass.addCell(criarCelulaAssinatura("Visto da Coordenação", fontBold));

            PdfPCell cellAss = new PdfPCell(ass);
            cellAss.setBorder(Rectangle.NO_BORDER);
            cellAss.setPaddingBottom(15);
            tableFooter.addCell(cellAss);

            // 2. DUPLA DE LOGOS (ESQUERDA E DIREITA)
            PdfPTable logos = new PdfPTable(2);
            logos.setWidths(new float[]{1f, 1f});

            // A) Logo IDS (Extrema Esquerda)
            PdfPCell cellIds = new PdfPCell();
            cellIds.setBorder(Rectangle.NO_BORDER);
            cellIds.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cellIds.setHorizontalAlignment(Element.ALIGN_LEFT); // Alinhado à esquerda
            try {
                ClassPathResource imgIds = new ClassPathResource("static/img/logo_ids.png");
                Image logoIds = Image.getInstance(imgIds.getURL());
                logoIds.scaleToFit(90, 80);
                cellIds.addElement(logoIds);
            } catch(Exception e) {}
            logos.addCell(cellIds);

            // B) Logo Ministério (Extrema Direita)
            PdfPCell cellMin = new PdfPCell();
            cellMin.setBorder(Rectangle.NO_BORDER);
            cellMin.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cellMin.setHorizontalAlignment(Element.ALIGN_RIGHT); // Alinhado à direita
            try {
                ClassPathResource imgMin = new ClassPathResource("static/img/logo_ministerio_esporte.png");
                Image logoMin = Image.getInstance(imgMin.getURL());
                // Altura igualada ao da IDS para equilíbrio visual
                logoMin.scaleToFit(200, 80);
                logoMin.setAlignment(Element.ALIGN_RIGHT);
                cellMin.addElement(logoMin);
            } catch(Exception e) {
                cellMin.addElement(new Paragraph("MINISTÉRIO DO ESPORTE", fontBold));
            }
            logos.addCell(cellMin);

            PdfPCell cellLogosContainer = new PdfPCell(logos);
            cellLogosContainer.setBorder(Rectangle.NO_BORDER);
            cellLogosContainer.setPaddingBottom(5);
            tableFooter.addCell(cellLogosContainer);

            // 3. Endereço
            Font fontEnd = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.GRAY);
            Paragraph pEnd = new Paragraph(endereco, fontEnd);
            pEnd.setAlignment(Element.ALIGN_CENTER);

            PdfPCell cellEnd = new PdfPCell(pEnd);
            cellEnd.setHorizontalAlignment(Element.ALIGN_CENTER);
            cellEnd.setBorder(Rectangle.NO_BORDER);
            cellEnd.setPaddingTop(2);
            tableFooter.addCell(cellEnd);

            // ESCREVE O RODAPÉ
            tableFooter.writeSelectedRows(0, -1, 32, 175, writer.getDirectContent());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // MÉTODOS AUXILIARES COMUNS (MANTIDOS)
    private void adicionarTituloSecaoFicha(Document doc, String titulo, Font font) throws DocumentException {
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);
        PdfPCell cell = new PdfPCell(new Phrase(titulo.toUpperCase(), font));
        cell.setBackgroundColor(COR_CINZA_ESCURO);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(5);
        cell.setBorder(Rectangle.NO_BORDER);
        table.addCell(cell);
        doc.add(table);
    }

    private PdfPTable criarTabelaGrade(float[] larguras) throws DocumentException {
        PdfPTable table = new PdfPTable(larguras);
        table.setWidthPercentage(100);
        return table;
    }

    private void addCelulaGrade(PdfPTable table, String label, String valor, Font fLabel, Font fDados) {
        PdfPTable inner = new PdfPTable(1);
        PdfPCell cLabel = new PdfPCell(new Phrase(label, fLabel));
        cLabel.setBorder(Rectangle.NO_BORDER);
        PdfPCell cValor = new PdfPCell(new Phrase(valor != null ? valor : "", fDados));
        cValor.setBorder(Rectangle.NO_BORDER);
        inner.addCell(cLabel);
        inner.addCell(cValor);
        PdfPCell cellOuter = new PdfPCell(inner);
        cellOuter.setBorder(Rectangle.BOX);
        cellOuter.setPadding(4);
        table.addCell(cellOuter);
    }

    private void adicionarCabecalhoRelatorio(PdfPTable table, String texto, int colspan) {
        Font fontHeader = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
        PdfPCell header = new PdfPCell(new Phrase(texto, fontHeader));
        header.setBackgroundColor(COR_CINZA_ESCURO);
        header.setHorizontalAlignment(Element.ALIGN_LEFT);
        header.setVerticalAlignment(Element.ALIGN_MIDDLE);
        header.setPadding(6);
        header.setColspan(colspan);
        header.setBorder(Rectangle.BOX);
        table.addCell(header);
    }

    private void adicionarCelulaComBorda(PdfPTable table, String label, String valor, Font fBold, Font fNormal) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.BOX);
        cell.setPadding(6);
        Phrase p = new Phrase();
        p.add(new Chunk(label + " ", fBold));
        p.add(new Chunk(valor != null ? valor : "", fNormal));
        cell.addElement(p);
        table.addCell(cell);
    }

    private PdfPCell criarCelulaAssinatura(String texto, Font fBold) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        Paragraph linha = new Paragraph("___________________________________", fBold);
        linha.setAlignment(Element.ALIGN_CENTER);
        cell.addElement(linha);
        Paragraph pTexto = new Paragraph(texto, fBold);
        pTexto.setAlignment(Element.ALIGN_CENTER);
        cell.addElement(pTexto);
        return cell;
    }

    private void adicionarLogoSimples(Document doc) {
        try {
            ClassPathResource imageFile = new ClassPathResource("static/img/logo_gol_social.jpeg");
            Image logo = Image.getInstance(imageFile.getURL());
            logo.scaleToFit(100, 100);
            logo.setAlignment(Element.ALIGN_CENTER);
            logo.setSpacingAfter(5);
            doc.add(logo);
        } catch (Exception e) { }
    }

    private Instituicao getDadosInstituicao() {
        List<Instituicao> lista = instituicaoRepository.findAll();
        if (!lista.isEmpty()) return lista.get(0);
        Instituicao p = new Instituicao();
        p.setEnderecoCompleto("Endereço não cadastrado");
        return p;
    }
}