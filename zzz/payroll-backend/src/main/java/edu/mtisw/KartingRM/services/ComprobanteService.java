package edu.mtisw.KartingRM.services;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import edu.mtisw.KartingRM.entities.ReservaEntity;
import edu.mtisw.KartingRM.entities.ClienteEntity;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class ComprobanteService {

    public byte[] generarComprobante(ReservaEntity reserva) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
        
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

         
            document.add(new Paragraph("Comprobante de Reserva").setBold().setFontSize(16));

      
            document.add(new Paragraph("Código de Reserva: " + reserva.getId()));
            document.add(new Paragraph("Fecha: " + reserva.getFechaReserva()));
            document.add(new Paragraph("Hora: " + reserva.getHoraReserva()));
            document.add(new Paragraph("Número de Vueltas: " + reserva.getNumeroVueltas()));
            document.add(new Paragraph("Precio Base: $" + reserva.getPrecioBase()));

          
            double iva = reserva.getPrecioFinal() * 0.19;
            double precioConIva = reserva.getPrecioFinal() + iva;

            document.add(new Paragraph("IVA (19%): $" + Math.round(iva)));
            document.add(new Paragraph("Precio Final con IVA: $" + Math.round(precioConIva)));

          
            Table table = new Table(new float[]{3, 3, 3, 3, 3, 3, 3});
            table.addHeaderCell("Cliente");
            table.addHeaderCell("Tarifa Base");
            table.addHeaderCell("Descuento Grupo");
            table.addHeaderCell("Descuento Promoción");
            table.addHeaderCell("Monto Final");
            table.addHeaderCell("IVA");
            table.addHeaderCell("Total con IVA");

            for (ClienteEntity cliente : reserva.getClientes()) {
                double montoFinalCliente = reserva.getPrecioFinal();
                double ivaCliente = montoFinalCliente * 0.19;
                double totalConIvaCliente = montoFinalCliente + ivaCliente;

                table.addCell(cliente.getNombre());
                table.addCell("$" + reserva.getPrecioBase());
                table.addCell(reserva.getDescuentoPorPersonas() + "%");
                table.addCell((reserva.getDescuentoPorVisitas() + reserva.getDescuentoPorCumpleaños()) + "%");
                table.addCell("$" + Math.round(montoFinalCliente));
                table.addCell("$" + Math.round(ivaCliente));
                table.addCell("$" + Math.round(totalConIvaCliente));
            }

            document.add(table);

         
            document.close();

            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error al generar el comprobante en PDF", e);
        }
    }
}