package com.karting.service;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.karting.dto.PrecioIndividualCliente;
import com.karting.entity.ReservaEntity;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ComprobanteService {

    // Constante para el IVA (19%)
    private static final double IVA_PORCENTAJE = 0.19;

    public byte[] generarComprobante(ReservaEntity reserva) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Configurar fuente
            PdfFont font = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            PdfFont boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);

            // HEADER DEL COMPROBANTE
            document.add(new Paragraph("🏎️ KARTING RM - COMPROBANTE DE RESERVA")
                    .setFont(boldFont)
                    .setFontSize(18)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20));

            // INFORMACIÓN DE LA RESERVA
            document.add(new Paragraph("INFORMACIÓN DE LA RESERVA")
                    .setFont(boldFont)
                    .setFontSize(14)
                    .setMarginBottom(10));

            document.add(new Paragraph("Reserva ID: #" + reserva.getId())
                    .setFont(font)
                    .setFontSize(12));

            document.add(new Paragraph("Fecha y Hora: " + 
                    reserva.getFechaHora().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
                    .setFont(font)
                    .setFontSize(12));

            document.add(new Paragraph("Duración: " + reserva.getDuracionMinutos() + " minutos")
                    .setFont(font)
                    .setFontSize(12));

            document.add(new Paragraph("Número de Personas: " + reserva.getNumeroPersonas())
                    .setFont(font)
                    .setFontSize(12));

            document.add(new Paragraph("Estado: " + reserva.getEstado())
                    .setFont(font)
                    .setFontSize(12)
                    .setMarginBottom(20));

            // CLIENTES PARTICIPANTES
            document.add(new Paragraph("CLIENTES PARTICIPANTES")
                    .setFont(boldFont)
                    .setFontSize(14)
                    .setMarginBottom(10));

            if (reserva.getClientesIds() != null && !reserva.getClientesIds().isEmpty()) {
                for (int i = 0; i < reserva.getClientesIds().size(); i++) {
                    document.add(new Paragraph("Cliente " + (i + 1) + ": ID #" + reserva.getClientesIds().get(i))
                            .setFont(font)
                            .setFontSize(12));
                }
            }
            document.add(new Paragraph(" ").setMarginBottom(10)); // Espaciado

            // KARTS ASIGNADOS
            document.add(new Paragraph("KARTS ASIGNADOS")
                    .setFont(boldFont)
                    .setFontSize(14)
                    .setMarginBottom(10));

            if (reserva.getKartsIds() != null && !reserva.getKartsIds().isEmpty()) {
                for (int i = 0; i < reserva.getKartsIds().size(); i++) {
                    document.add(new Paragraph("Kart " + (i + 1) + ": ID #" + reserva.getKartsIds().get(i))
                            .setFont(font)
                            .setFontSize(12));
                }
            }
            document.add(new Paragraph(" ").setMarginBottom(10)); // Espaciado

            // DETALLE DE COSTOS
            document.add(new Paragraph("DETALLE DE COSTOS")
                    .setFont(boldFont)
                    .setFontSize(14)
                    .setMarginBottom(10));

            // Crear tabla de costos
            Table table = new Table(UnitValue.createPercentArray(new float[]{3, 2}))
                    .setWidth(UnitValue.createPercentValue(100));

            // Headers
            table.addHeaderCell(new Paragraph("Concepto").setFont(boldFont));
            table.addHeaderCell(new Paragraph("Monto").setFont(boldFont));

            // Precio base
            table.addCell(new Paragraph("Precio Base").setFont(font));
            table.addCell(new Paragraph("$" + String.format("%.0f", reserva.getPrecioBase())).setFont(font));

            // Descuentos
            if (reserva.getDescuentoPersonas() != null && reserva.getDescuentoPersonas() > 0) {
                table.addCell(new Paragraph("Descuento por Grupo").setFont(font));
                table.addCell(new Paragraph("-$" + String.format("%.0f", reserva.getDescuentoPersonas())).setFont(font));
            }

            if (reserva.getDescuentoClientes() != null && reserva.getDescuentoClientes() > 0) {
                table.addCell(new Paragraph("Descuento Clientes Frecuentes").setFont(font));
                table.addCell(new Paragraph("-$" + String.format("%.0f", reserva.getDescuentoClientes())).setFont(font));
            }

            if (reserva.getDescuentoCumpleanos() != null && reserva.getDescuentoCumpleanos() > 0) {
                table.addCell(new Paragraph("Descuento Cumpleaños").setFont(font));
                table.addCell(new Paragraph("-$" + String.format("%.0f", reserva.getDescuentoCumpleanos())).setFont(font));
            }

            // Subtotal (sin IVA)
            table.addCell(new Paragraph("Subtotal").setFont(boldFont));
            table.addCell(new Paragraph("$" + String.format("%.0f", reserva.getPrecioTotal())).setFont(boldFont));

            // IVA
            double iva = reserva.getPrecioTotal() * IVA_PORCENTAJE;
            table.addCell(new Paragraph("IVA (19%)").setFont(font));
            table.addCell(new Paragraph("$" + String.format("%.0f", iva)).setFont(font));

            // Total final con IVA
            double totalConIva = reserva.getPrecioTotal() + iva;
            table.addCell(new Paragraph("TOTAL CON IVA").setFont(boldFont).setFontSize(14));
            table.addCell(new Paragraph("$" + String.format("%.0f", totalConIva)).setFont(boldFont).setFontSize(14));

            document.add(table);

            // FOOTER
            document.add(new Paragraph(" ").setMarginTop(20));
            document.add(new Paragraph("¡Gracias por elegir Karting RM!")
                    .setFont(boldFont)
                    .setFontSize(12)
                    .setTextAlignment(TextAlignment.CENTER));

            document.add(new Paragraph("Para cualquier consulta, contactenos al +56 9 1234 5678")
                    .setFont(font)
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.CENTER));

            document.add(new Paragraph("Comprobante generado el: " + 
                    java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")))
                    .setFont(font)
                    .setFontSize(8)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(10));

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error al generar comprobante PDF: " + e.getMessage(), e);
        }
    }

    // VERSIÓN AVANZADA: Con detalle por cliente individual
    public byte[] generarComprobanteDetallado(ReservaEntity reserva, List<PrecioIndividualCliente> preciosIndividuales) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            PdfFont font = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            PdfFont boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);

            // HEADER
            document.add(new Paragraph("🏎️ KARTING RM - COMPROBANTE DETALLADO")
                    .setFont(boldFont)
                    .setFontSize(18)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20));

            // INFORMACIÓN BÁSICA
            document.add(new Paragraph("Reserva ID: #" + reserva.getId())
                    .setFont(boldFont)
                    .setFontSize(12));
            document.add(new Paragraph("Fecha: " + 
                    reserva.getFechaHora().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
                    .setFont(font)
                    .setFontSize(12)
                    .setMarginBottom(15));

            // TABLA DETALLADA POR CLIENTE
            document.add(new Paragraph("DETALLE POR CLIENTE")
                    .setFont(boldFont)
                    .setFontSize(14)
                    .setMarginBottom(10));

            Table tableClientes = new Table(UnitValue.createPercentArray(new float[]{2, 2, 2, 2, 2, 2}))
                    .setWidth(UnitValue.createPercentValue(100));

            // Headers
            tableClientes.addHeaderCell(new Paragraph("Cliente").setFont(boldFont));
            tableClientes.addHeaderCell(new Paragraph("Precio Base").setFont(boldFont));
            tableClientes.addHeaderCell(new Paragraph("Desc. Grupo").setFont(boldFont));
            tableClientes.addHeaderCell(new Paragraph("Desc. Cliente").setFont(boldFont));
            tableClientes.addHeaderCell(new Paragraph("Desc. Cumple").setFont(boldFont));
            tableClientes.addHeaderCell(new Paragraph("Total Cliente").setFont(boldFont));

            // Filas de clientes
            if (preciosIndividuales != null) {
                for (PrecioIndividualCliente precio : preciosIndividuales) {
                    tableClientes.addCell(new Paragraph("ID #" + precio.getClienteId()).setFont(font));
                    tableClientes.addCell(new Paragraph("$" + String.format("%.0f", precio.getPrecioBase())).setFont(font));
                    tableClientes.addCell(new Paragraph("-$" + String.format("%.0f", precio.getDescuentoGrupo())).setFont(font));
                    tableClientes.addCell(new Paragraph("-$" + String.format("%.0f", precio.getDescuentoClienteFrecuente())).setFont(font));
                    tableClientes.addCell(new Paragraph("-$" + String.format("%.0f", precio.getDescuentoCumpleanos())).setFont(font));
                    tableClientes.addCell(new Paragraph("$" + String.format("%.0f", precio.getPrecioFinal())).setFont(boldFont));
                }
            }

            document.add(tableClientes);

            // RESUMEN TOTAL
            document.add(new Paragraph(" ").setMarginTop(15));
            document.add(new Paragraph("RESUMEN TOTAL")
                    .setFont(boldFont)
                    .setFontSize(14)
                    .setMarginBottom(10));

            Table tableResumen = new Table(UnitValue.createPercentArray(new float[]{3, 2}))
                    .setWidth(UnitValue.createPercentValue(100));

            tableResumen.addCell(new Paragraph("Subtotal").setFont(boldFont));
            tableResumen.addCell(new Paragraph("$" + String.format("%.0f", reserva.getPrecioTotal())).setFont(boldFont));

            double iva = reserva.getPrecioTotal() * IVA_PORCENTAJE;
            tableResumen.addCell(new Paragraph("IVA (19%)").setFont(font));
            tableResumen.addCell(new Paragraph("$" + String.format("%.0f", iva)).setFont(font));

            double totalConIva = reserva.getPrecioTotal() + iva;
            tableResumen.addCell(new Paragraph("TOTAL FINAL").setFont(boldFont).setFontSize(14));
            tableResumen.addCell(new Paragraph("$" + String.format("%.0f", totalConIva)).setFont(boldFont).setFontSize(14));

            document.add(tableResumen);

            // FOOTER
            document.add(new Paragraph(" ").setMarginTop(20));
            document.add(new Paragraph("¡Gracias por elegir Karting RM!")
                    .setFont(boldFont)
                    .setTextAlignment(TextAlignment.CENTER));

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error al generar comprobante detallado: " + e.getMessage(), e);
        }
    }
}