package com.sistema.gestao.service;

import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.*;

public class LinhasDescricaoService implements PdfPCellEvent {

    private final float alturaLinha;
    private final int totalLinhas;

    public LinhasDescricaoService(float alturaLinha, int totalLinhas) {
        this.alturaLinha = alturaLinha;
        this.totalLinhas = totalLinhas;
    }

    @Override
    public void cellLayout(PdfPCell cell, Rectangle position, PdfContentByte[] canvases) {
        PdfContentByte cb = canvases[PdfPTable.LINECANVAS];
        cb.setLineWidth(0.5f);

        float y = position.getTop() - alturaLinha;

        for (int i = 0; i < totalLinhas; i++) {
            cb.moveTo(position.getLeft(), y);
            cb.lineTo(position.getRight(), y);
            cb.stroke();
            y -= alturaLinha;
        }
    }
}
