package com.sistema.gestao.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.sistema.gestao.entity.Inscrito;
import com.sistema.gestao.entity.Instituicao;
import com.sistema.gestao.entity.RelatorioMensal;
import com.sistema.gestao.entity.Turma;
import com.sistema.gestao.repository.InstituicaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
public class PdfService {

    @Autowired
    private InstituicaoRepository instituicaoRepository;

    private static final Color COR_CINZA_ESCURO = Color.DARK_GRAY;

    // =================================================================================
    // MÉTODO 1: GERA A FICHA DE INSCRIÇÃO DO ALUNO (INDIVIDUAL)
    // =================================================================================
    public byte[] gerarFichaInscricao(Inscrito inscrito) {
        Document document = new Document(PageSize.A4, 36, 36, 20, 110);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Instituicao dadosInst = getDadosInstituicao();

        try {
            PdfWriter writer = PdfWriter.getInstance(document, out);
            document.open();

            // --- CABEÇALHO ---
            PdfPTable headerTable = new PdfPTable(2);
            headerTable.setWidthPercentage(100);
            headerTable.setWidths(new float[]{3f, 1f});

            Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, COR_CINZA_ESCURO);
            PdfPCell cellTitulo = new PdfPCell(new Phrase("FICHA DE CADASTRO", fontTitulo));
            cellTitulo.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cellTitulo.setBorder(Rectangle.NO_BORDER);
            cellTitulo.setPaddingTop(10);
            headerTable.addCell(cellTitulo);

            PdfPCell cellLogo = new PdfPCell();
            cellLogo.setBorder(Rectangle.NO_BORDER);
            cellLogo.setHorizontalAlignment(Element.ALIGN_RIGHT);
            try {
                ClassPathResource imgGol = new ClassPathResource("static/img/logo_gol_social.jpeg");
                Image logoGol = Image.getInstance(imgGol.getURL());
                logoGol.scaleToFit(80, 70);
                logoGol.setAlignment(Element.ALIGN_RIGHT);
                cellLogo.addElement(logoGol);
            } catch (Exception e) { }
            headerTable.addCell(cellLogo);
            document.add(headerTable);

            // Linha divisória
            PdfPTable linhaDiv = new PdfPTable(1);
            linhaDiv.setWidthPercentage(100);
            PdfPCell cLinha = new PdfPCell();
            cLinha.setBorder(Rectangle.BOTTOM);
            cLinha.setBorderWidth(1.5f);
            cLinha.setBorderColor(Color.LIGHT_GRAY);
            cLinha.setFixedHeight(5f);
            linhaDiv.addCell(cLinha);
            document.add(linhaDiv);

            document.add(new Paragraph(" "));

            // --- FONTES ---
            Font fontSecao = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
            Font fontLabel = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.DARK_GRAY);
            Font fontDados = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);

            // --- DADOS PESSOAIS ---
            adicionarTituloSecaoFicha(document, "DADOS PESSOAIS DO ALUNO", fontSecao);
            PdfPTable tabelaPessoal = criarTabelaGrade(new float[]{4f, 2f});
            addCelulaGrade(tabelaPessoal, "Nome Completo:", inscrito.getNomeCompleto(), fontLabel, fontDados);
            addCelulaGrade(tabelaPessoal, "CPF:", inscrito.getCpf(), fontLabel, fontDados);
            String dataNasc = inscrito.getDataNascimento() != null ? inscrito.getDataNascimento().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "";
            addCelulaGrade(tabelaPessoal, "Data de Nascimento:", dataNasc, fontLabel, fontDados);
            addCelulaGrade(tabelaPessoal, "Sexo:", inscrito.getSexo(), fontLabel, fontDados);
            document.add(tabelaPessoal);

            document.add(new Paragraph(" "));

            // --- ENDEREÇO ---
            adicionarTituloSecaoFicha(document, "ENDEREÇO E CONTATO", fontSecao);
            PdfPTable tabelaEnd = criarTabelaGrade(new float[]{4f, 2f});
            addCelulaGrade(tabelaEnd, "Endereço:", inscrito.getEndereco(), fontLabel, fontDados);
            addCelulaGrade(tabelaEnd, "Bairro:", inscrito.getBairro(), fontLabel, fontDados);
            document.add(tabelaEnd);

            PdfPTable tabelaCidade = criarTabelaGrade(new float[]{1f});
            addCelulaGrade(tabelaCidade, "Cidade:", inscrito.getCidade(), fontLabel, fontDados);
            document.add(tabelaCidade);

            document.add(new Paragraph(" "));

            // --- RESPONSÁVEL ---
            adicionarTituloSecaoFicha(document, "DADOS DO RESPONSÁVEL", fontSecao);
            PdfPTable tabelaResp = criarTabelaGrade(new float[]{3f, 2f});
            addCelulaGrade(tabelaResp, "Nome Responsável:", inscrito.getNomeResponsavel(), fontLabel, fontDados);
            addCelulaGrade(tabelaResp, "Telefone:", inscrito.getTelefone(), fontLabel, fontDados);
            addCelulaGrade(tabelaResp, "CPF Resp.:", inscrito.getCpfResponsavel(), fontLabel, fontDados);
            addCelulaGrade(tabelaResp, "E-mail:", inscrito.getEmail(), fontLabel, fontDados);
            document.add(tabelaResp);

            document.add(new Paragraph(" "));

            // --- DADOS DA MATRÍCULA E ESCOLARIDADE ---
            adicionarTituloSecaoFicha(document, "DADOS DA MATRÍCULA E ESCOLARIDADE", fontSecao);

            PdfPTable tabelaMatricula = criarTabelaGrade(new float[]{3.5f, 2f, 1.5f});
            if (inscrito.getTurmas() != null && !inscrito.getTurmas().isEmpty()) {
                for (Turma t : inscrito.getTurmas()) {
                    String nomeCompletoTurma = (t.getModalidade() != null ? t.getModalidade() : "") + " - " + (t.getNome() != null ? t.getNome() : "");
                    String nomeNucleoCompleto = getDescricaoNucleo(t.getNucleo());

                    addCelulaGrade(tabelaMatricula, "Modalidade / Turma:", nomeCompletoTurma, fontLabel, fontDados);
                    addCelulaGrade(tabelaMatricula, "Núcleo / Polo:", nomeNucleoCompleto, fontLabel, fontDados);
                    addCelulaGrade(tabelaMatricula, "Horário:", t.getHorario(), fontLabel, fontDados);
                }
            } else {
                PdfPCell aviso = new PdfPCell(new Phrase("Nenhuma turma selecionada.", fontDados));
                aviso.setColspan(3);
                aviso.setPadding(5);
                tabelaMatricula.addCell(aviso);
            }
            document.add(tabelaMatricula);

            PdfPTable tabelaEscola = criarTabelaGrade(new float[]{2f, 4f});
            addCelulaGrade(tabelaEscola, "Escolaridade:", inscrito.getEscolaridade(), fontLabel, fontDados);
            addCelulaGrade(tabelaEscola, "Nome da Escola:", inscrito.getNomeEscola(), fontLabel, fontDados);
            document.add(tabelaEscola);

            document.add(new Paragraph(" "));

            // --- ANÁLISE SOCIAL ---
            adicionarTituloSecaoFicha(document, "ANÁLISE SOCIAL", fontSecao);
            PdfPTable tabelaSocial = criarTabelaGrade(new float[]{2f, 2f, 2f});
            addCelulaGrade(tabelaSocial, "Recebe Benefício Social:", inscrito.getRecebeBeneficio(), fontLabel, fontDados);
            addCelulaGrade(tabelaSocial, "Renda Familiar:", inscrito.getRendaFamiliar(), fontLabel, fontDados);

            String pessoas = inscrito.getQuantasPessoasCasa() != null ? String.valueOf(inscrito.getQuantasPessoasCasa()) : "";
            addCelulaGrade(tabelaSocial, "Pessoas na Casa:", pessoas, fontLabel, fontDados);
            document.add(tabelaSocial);

            PdfPTable tabelaParentesco = criarTabelaGrade(new float[]{1f});
            addCelulaGrade(tabelaParentesco, "Grau de Parentesco (Responsável):", inscrito.getGrauParentesco(), fontLabel, fontDados);
            document.add(tabelaParentesco);

            // --- AVISO DE DOCUMENTOS NECESSÁRIOS ---
            Paragraph pAviso = new Paragraph();
            pAviso.setSpacingBefore(5f);
            pAviso.setSpacingAfter(5f);
            pAviso.setAlignment(Element.ALIGN_LEFT);

            Font fontNegrito = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.BLACK);
            Chunk cTitulo = new Chunk("DOCUMENTOS NECESSÁRIOS: ", fontNegrito);

            Font fontNormal = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.BLACK);
            Chunk cLista = new Chunk("IDENTIDADE, CPF, COMPROVANTE DE RESIDÊNCIA (DE AMBOS) E DECLARAÇÃO ESCOLAR", fontNormal);

            pAviso.add(cTitulo);
            pAviso.add(cLista);
            document.add(pAviso);

            // --- DATA E ASSINATURA ---
            PdfPTable assinaturaTable = new PdfPTable(1);
            assinaturaTable.setTotalWidth(523);

            LocalDate hoje = LocalDate.now();
            Locale br = new Locale("pt", "BR");
            String dataExtenso = "Rio de Janeiro, " + hoje.getDayOfMonth() + " de " + hoje.getMonth().getDisplayName(java.time.format.TextStyle.FULL, br) + " de " + hoje.getYear();

            PdfPCell cellData = new PdfPCell(new Phrase(dataExtenso, fontDados));
            cellData.setBorder(Rectangle.NO_BORDER);
            cellData.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cellData.setPaddingBottom(15);
            assinaturaTable.addCell(cellData);

            PdfPCell cellLinhaAss = new PdfPCell(new Phrase("__________________________________________________________", fontDados));
            cellLinhaAss.setBorder(Rectangle.NO_BORDER);
            cellLinhaAss.setHorizontalAlignment(Element.ALIGN_CENTER);
            assinaturaTable.addCell(cellLinhaAss);

            PdfPCell cellAss = new PdfPCell(new Phrase("Assinatura do Responsável", fontLabel));
            cellAss.setBorder(Rectangle.NO_BORDER);
            cellAss.setHorizontalAlignment(Element.ALIGN_CENTER);
            assinaturaTable.addCell(cellAss);

            // POSIÇÃO ASSINATURA: Baixei de 172 para 160.
            assinaturaTable.writeSelectedRows(0, -1, 36, 160, writer.getDirectContent());

            // --- RODAPÉ ---
            adicionarRodapeComLogosFixos(writer, dadosInst.getEnderecoCompleto(), true);

            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return out.toByteArray();
    }

    // =================================================================================
    // MÉTODO 2: GERA A LISTA DE PRESENÇA/TURMA
    // =================================================================================
    public byte[] gerarListaTurma(Turma turma) {
        Document document = new Document(PageSize.A4, 20, 20, 20, 100);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Instituicao dadosInst = getDadosInstituicao();

        try {
            PdfWriter writer = PdfWriter.getInstance(document, out);
            document.open();

            adicionarCabecalhoInstitucional(document, "LISTA DE BENEFICIÁRIOS");

            Font fBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
            Font fNormal = FontFactory.getFont(FontFactory.HELVETICA, 10);

            document.add(new Paragraph(" "));

            PdfPTable tabelaInfo = new PdfPTable(2);
            tabelaInfo.setWidthPercentage(100);
            adicionarCabecalhoRelatorio(tabelaInfo, "DETALHES DA TURMA", 2);
            adicionarCelulaComBorda(tabelaInfo, "Nome da Turma:", turma.getNome(), fBold, fNormal);
            adicionarCelulaComBorda(tabelaInfo, "Núcleo/Polo:", turma.getNucleo(), fBold, fNormal);
            adicionarCelulaComBorda(tabelaInfo, "Modalidade:", turma.getModalidade(), fBold, fNormal);
            adicionarCelulaComBorda(tabelaInfo, "Horário:", turma.getHorario(), fBold, fNormal);
            adicionarCelulaComBorda(tabelaInfo, "Capacidade:", turma.getCapacidade() + " Vagas", fBold, fNormal);
            int qtdInscritos = (turma.getInscritos() != null) ? turma.getInscritos().size() : 0;
            adicionarCelulaComBorda(tabelaInfo, "Total Inscritos:", String.valueOf(qtdInscritos), fBold, fNormal);
            document.add(tabelaInfo);
            document.add(new Paragraph(" "));

            PdfPTable tabelaAlunos = new PdfPTable(new float[]{0.8f, 6f, 3f, 2.5f});
            tabelaAlunos.setWidthPercentage(100);

            Font fHeaderTable = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);
            String[] headers = {"#", "NOME COMPLETO", "CPF", "NASCIMENTO"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, fHeaderTable));
                cell.setBackgroundColor(COR_CINZA_ESCURO);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                cell.setPadding(5);
                tabelaAlunos.addCell(cell);
            }

            Font fCell = FontFactory.getFont(FontFactory.HELVETICA, 9);
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            if (turma.getInscritos() != null && !turma.getInscritos().isEmpty()) {
                List<Inscrito> listaOrdenada = new ArrayList<>(turma.getInscritos());
                listaOrdenada.sort(Comparator.comparing(Inscrito::getNomeCompleto));

                int contador = 1;
                for (Inscrito aluno : listaOrdenada) {
                    float paddingOtimizado = 4.5f;
                    PdfPCell c1 = new PdfPCell(new Phrase(String.valueOf(contador++), fCell));
                    c1.setHorizontalAlignment(Element.ALIGN_CENTER);
                    c1.setVerticalAlignment(Element.ALIGN_MIDDLE);
                    c1.setPadding(paddingOtimizado);
                    tabelaAlunos.addCell(c1);

                    PdfPCell c2 = new PdfPCell(new Phrase(aluno.getNomeCompleto().toUpperCase(), fCell));
                    c2.setVerticalAlignment(Element.ALIGN_MIDDLE);
                    c2.setPadding(paddingOtimizado);
                    tabelaAlunos.addCell(c2);

                    PdfPCell c3 = new PdfPCell(new Phrase(aluno.getCpf(), fCell));
                    c3.setHorizontalAlignment(Element.ALIGN_CENTER);
                    c3.setVerticalAlignment(Element.ALIGN_MIDDLE);
                    c3.setPadding(paddingOtimizado);
                    tabelaAlunos.addCell(c3);

                    String dn = (aluno.getDataNascimento() != null) ? aluno.getDataNascimento().format(fmt) : "-";
                    PdfPCell c4 = new PdfPCell(new Phrase(dn, fCell));
                    c4.setHorizontalAlignment(Element.ALIGN_CENTER);
                    c4.setVerticalAlignment(Element.ALIGN_MIDDLE);
                    c4.setPadding(paddingOtimizado);
                    tabelaAlunos.addCell(c4);
                }
            } else {
                PdfPCell vazio = new PdfPCell(new Phrase("Nenhum beneficiário cadastrado nesta turma.", fCell));
                vazio.setColspan(4);
                vazio.setHorizontalAlignment(Element.ALIGN_CENTER);
                vazio.setPadding(10);
                tabelaAlunos.addCell(vazio);
            }

            document.add(tabelaAlunos);

            adicionarRodapeSemAssinatura(writer, dadosInst.getEnderecoCompleto(), fBold);

            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return out.toByteArray();
    }

    // =================================================================================
    // MÉTODO 3: GERA O RELATÓRIO MENSAL
    // =================================================================================
    public byte[] gerarRelatorioMensalPreenchido(RelatorioMensal relatorio, String descricao) {
        Document document = new Document(PageSize.A4, 30, 30, 20, 190);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Instituicao dadosInst = getDadosInstituicao();

        try {
            PdfWriter writer = PdfWriter.getInstance(document, out);
            document.open();

            adicionarCabecalhoInstitucional(document, "RELATÓRIO MENSAL");

            Font fBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
            Font fNormal = FontFactory.getFont(FontFactory.HELVETICA, 10);

            document.add(new Paragraph(" "));

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

            adicionarRodapeInstitucional(writer, dadosInst.getEnderecoCompleto(), fBold);

            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return out.toByteArray();
    }

    // =================================================================================
    // MÉTODOS AUXILIARES
    // =================================================================================

    private String getDescricaoNucleo(String codigo) {
        if (codigo == null) return "";
        switch (codigo) {
            case "Nucleo 01": return "Núcleo 01 - Praça da Cedae";
            case "Nucleo 02": return "Núcleo 02 - Pavuna (Campo da Embau)";
            case "Nucleo 03": return "Núcleo 03 - Pavuna (Campo da praça do Chico)";
            case "Nucleo 04": return "Núcleo 04 - Guadalupe";
            case "Nucleo 05": return "Núcleo 05 - Vila kennedy";
            case "Nucleo 06": return "Núcleo 06 - Cosmos (Vila olímpica)";
            case "Nucleo 07": return "Núcleo 07 - Cosmos (Praça do Redondo)";
            case "Nucleo 08": return "Núcleo 08 - Benfica";
            case "Nucleo 09": return "Núcleo 09 - Barra de Guaratiba";
            case "Nucleo 10": return "Núcleo 10 - Costa Barros";
            default: return codigo;
        }
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
        cellOuter.setPadding(5);
        table.addCell(cellOuter);
    }

    // --- RODAPÉ MANTIDO (Y=135) ---
    private void adicionarRodapeComLogosFixos(PdfWriter writer, String endereco, boolean logoGrande) {
        try {
            PdfPTable tableFooter = new PdfPTable(1);
            tableFooter.setTotalWidth(523);

            adicionarLogosEEndereco(tableFooter, endereco, logoGrande);

            // Mantido em 135 conforme solicitado (não abaixar endereço)
            tableFooter.writeSelectedRows(0, -1, 36, 135, writer.getDirectContent());
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void adicionarLogosEEndereco(PdfPTable tableFooter, String endereco, boolean aumentarLogoMinisterio) {
        try {
            PdfPTable logos = new PdfPTable(2);
            logos.setWidths(new float[]{1f, 1f});

            PdfPCell cellIds = new PdfPCell();
            cellIds.setBorder(Rectangle.NO_BORDER);
            cellIds.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cellIds.setHorizontalAlignment(Element.ALIGN_LEFT);
            try {
                ClassPathResource imgIds = new ClassPathResource("static/img/logo_ids.png");
                Image logoIds = Image.getInstance(imgIds.getURL());
                logoIds.scaleToFit(90, 80);
                cellIds.addElement(logoIds);
            } catch(Exception e) {}
            logos.addCell(cellIds);

            PdfPCell cellMin = new PdfPCell();
            cellMin.setBorder(Rectangle.NO_BORDER);
            cellMin.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cellMin.setHorizontalAlignment(Element.ALIGN_RIGHT);
            try {
                ClassPathResource imgMin = new ClassPathResource("static/img/logo_ministerio_esporte.png");
                Image logoMin = Image.getInstance(imgMin.getURL());
                if (aumentarLogoMinisterio) {
                    logoMin.scaleToFit(240, 100);
                } else {
                    logoMin.scaleToFit(200, 80);
                }
                logoMin.setAlignment(Element.ALIGN_RIGHT);
                cellMin.addElement(logoMin);
            } catch(Exception e) { }
            logos.addCell(cellMin);

            PdfPCell cellLogosContainer = new PdfPCell(logos);
            cellLogosContainer.setBorder(Rectangle.NO_BORDER);
            cellLogosContainer.setPaddingBottom(0); // Colado no endereço
            tableFooter.addCell(cellLogosContainer);

            Font fontEnd = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.GRAY);
            Paragraph pEnd = new Paragraph(endereco, fontEnd);
            pEnd.setAlignment(Element.ALIGN_CENTER);
            pEnd.setSpacingBefore(0);

            PdfPCell cellEnd = new PdfPCell(pEnd);
            cellEnd.setHorizontalAlignment(Element.ALIGN_CENTER);
            cellEnd.setBorder(Rectangle.NO_BORDER);
            cellEnd.setPaddingTop(0); // Colado nas logos
            tableFooter.addCell(cellEnd);

        } catch (Exception e) { e.printStackTrace(); }
    }

    // =================================================================================
    // MÉTODOS AUXILIARES ORIGINAIS
    // =================================================================================

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

    private void adicionarCabecalhoInstitucional(Document document, String titulo) throws DocumentException {
        PdfPTable header = new PdfPTable(3);
        header.setWidthPercentage(100);
        header.setWidths(new float[]{1.5f, 7f, 1.5f});
        header.setSpacingAfter(10);

        PdfPCell cellVazia = new PdfPCell();
        cellVazia.setBorder(Rectangle.NO_BORDER);
        header.addCell(cellVazia);

        PdfPCell cellTexto = new PdfPCell();
        cellTexto.setBorder(Rectangle.NO_BORDER);
        cellTexto.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cellTexto.setHorizontalAlignment(Element.ALIGN_CENTER);

        Font fTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Paragraph p1 = new Paragraph(titulo, fTitulo);
        p1.setAlignment(Element.ALIGN_CENTER);
        cellTexto.addElement(p1);
        header.addCell(cellTexto);

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

            PdfPTable ass = new PdfPTable(2);
            ass.setWidths(new float[]{1f, 1f});
            ass.addCell(criarCelulaAssinatura("Assinatura do Instrutor", fontBold));
            ass.addCell(criarCelulaAssinatura("Visto da Coordenação", fontBold));

            PdfPCell cellAss = new PdfPCell(ass);
            cellAss.setBorder(Rectangle.NO_BORDER);
            cellAss.setPaddingBottom(15);
            tableFooter.addCell(cellAss);

            adicionarLogosEEndereco(tableFooter, endereco, false);

            tableFooter.writeSelectedRows(0, -1, 32, 175, writer.getDirectContent());
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void adicionarRodapeSemAssinatura(PdfWriter writer, String endereco, Font fontBold) {
        try {
            PdfPTable tableFooter = new PdfPTable(1);
            tableFooter.setTotalWidth(550);
            adicionarLogosEEndereco(tableFooter, endereco, true);
            tableFooter.writeSelectedRows(0, -1, 22, 130, writer.getDirectContent());
        } catch (Exception e) { e.printStackTrace(); }
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
            if (linhaAtual.length() > 0) linhasFinais.add(linhaAtual.toString());
        }
        return linhasFinais;
    }

    private Instituicao getDadosInstituicao() {
        List<Instituicao> lista = instituicaoRepository.findAll();
        if (!lista.isEmpty()) return lista.get(0);
        Instituicao p = new Instituicao();
        p.setEnderecoCompleto("Endereço não cadastrado");
        return p;
    }
}