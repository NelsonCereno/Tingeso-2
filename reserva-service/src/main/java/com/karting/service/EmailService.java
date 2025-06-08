package com.karting.service;

import javax.mail.internet.MimeMessage;
import javax.mail.util.ByteArrayDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired(required = false) // ✅ Hacer la inyección opcional
    private JavaMailSender mailSender;

    @PostConstruct
    public void verificarConfiguracion() {
        if (mailSender != null) {
            logger.info("✅ JavaMailSender configurado correctamente");
            try {
                mailSender.createMimeMessage(); // Test básico
                logger.info("✅ Conexión de email disponible");
            } catch (Exception e) {
                logger.warn("⚠️ Email configurado pero con problemas de conexión: {}", e.getMessage());
            }
        } else {
            logger.warn("⚠️ JavaMailSender no configurado - funcionalidad de email deshabilitada");
        }
    }

    /**
     * Enviar comprobante de reserva a un cliente individual
     */
    public void enviarComprobante(String email, byte[] comprobantePdf, Long reservaId) {
        if (mailSender == null) {
            logger.warn("⚠️ No se puede enviar email - JavaMailSender no configurado");
            throw new RuntimeException("Servicio de email no disponible");
        }

        try {
            logger.info("🚀 Iniciando envío de comprobante a: {} para reserva #{}", email, reservaId);
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // Configurar destinatario y remitente
            helper.setTo(email);
            helper.setSubject("🏎️ Comprobante de Reserva Karting RM - #" + reservaId);
            
            // Crear contenido HTML del email
            String contenidoHtml = generarContenidoEmail(reservaId);
            helper.setText(contenidoHtml, true); // true indica que es HTML

            // Adjuntar PDF
            String nombreArchivo = "Comprobante_Reserva_" + reservaId + ".pdf";
            helper.addAttachment(nombreArchivo, new ByteArrayDataSource(comprobantePdf, "application/pdf"));

            // Enviar email
            mailSender.send(message);
            
            logger.info("✅ Comprobante enviado exitosamente a: {} para reserva #{}", email, reservaId);
            
        } catch (Exception e) {
            logger.error("❌ Error al enviar comprobante a {}: {}", email, e.getMessage(), e);
            throw new RuntimeException("Error al enviar el correo a " + email + ": " + e.getMessage(), e);
        }
    }

    /**
     * Enviar comprobante a múltiples clientes de una reserva
     */
    public List<String> enviarComprobanteMultiple(List<String> emails, byte[] comprobantePdf, Long reservaId) {
        List<String> correosEnviados = new ArrayList<>();
        List<String> correosConError = new ArrayList<>();

        if (mailSender == null) {
            logger.warn("⚠️ No se puede enviar emails - JavaMailSender no configurado");
            return correosEnviados; // Retorna lista vacía
        }

        logger.info("📧 Iniciando envío múltiple para reserva #{} a {} destinatarios", reservaId, emails.size());

        for (String email : emails) {
            try {
                enviarComprobante(email, comprobantePdf, reservaId);
                correosEnviados.add(email);
                logger.info("✅ Enviado correctamente a: {}", email);
            } catch (Exception e) {
                logger.error("❌ Error al enviar a {}: {}", email, e.getMessage());
                correosConError.add(email + " (Error: " + e.getMessage() + ")");
            }
        }

        // Log de resumen
        logger.info("📊 Resumen envío reserva #{}: {} exitosos, {} con errores", 
                    reservaId, correosEnviados.size(), correosConError.size());

        if (!correosConError.isEmpty()) {
            logger.warn("⚠️ Correos con errores: {}", correosConError);
        }

        return correosEnviados;
    }

    /**
     * Generar contenido HTML personalizado para el email
     */
    private String generarContenidoEmail(Long reservaId) {
        String fechaActual = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        
        // Para Spring Boot 2.5.4, usar concatenación en lugar de text blocks
        return "<html>" +
                "<head>" +
                "<style>" +
                "body { font-family: Arial, sans-serif; margin: 20px; color: #333; }" +
                ".header { background-color: #ff6b35; color: white; padding: 20px; text-align: center; border-radius: 8px; }" +
                ".content { padding: 20px; background-color: #f9f9f9; border-radius: 8px; margin-top: 10px; }" +
                ".footer { margin-top: 20px; text-align: center; color: #666; font-size: 12px; }" +
                ".highlight { background-color: #fffacd; padding: 10px; border-radius: 5px; margin: 10px 0; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='header'>" +
                "<h1>🏎️ Karting RM</h1>" +
                "<h2>Comprobante de Reserva</h2>" +
                "</div>" +
                "<div class='content'>" +
                "<p><strong>¡Hola!</strong></p>" +
                "<p>Te enviamos el comprobante de tu reserva en <strong>Karting RM</strong>.</p>" +
                "<div class='highlight'>" +
                "<p><strong>📋 Reserva #" + reservaId + "</strong></p>" +
                "<p>📅 Generado el: " + fechaActual + "</p>" +
                "</div>" +
                "<p><strong>📎 Adjunto:</strong> Encontrarás el comprobante detallado en formato PDF.</p>" +
                "<h3>🏁 ¡Prepárate para la adrenalina!</h3>" +
                "<p>Recuerda llegar <strong>15 minutos antes</strong> de tu horario reservado para el briefing de seguridad.</p>" +
                "<h4>📋 Recomendaciones:</h4>" +
                "<ul>" +
                "<li>🦺 Usar ropa cómoda y cerrada</li>" +
                "<li>👟 Calzado deportivo (no sandalias)</li>" +
                "<li>🧢 Cabello recogido si es largo</li>" +
                "<li>📱 Presentar este comprobante al llegar</li>" +
                "</ul>" +
                "</div>" +
                "<div class='footer'>" +
                "<p>🏎️ <strong>Karting RM</strong> - La mejor experiencia de karting</p>" +
                "<p>📧 Para consultas: info@kartingmr.com | 📞 +56 9 1234 5678</p>" +
                "<p>📍 Dirección: Av. Libertador 1234, Santiago, Chile</p>" +
                "</div>" +
                "</body>" +
                "</html>";
    }

    /**
     * Validar email antes de enviar
     */
    public boolean validarEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        
        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        return email.matches(emailRegex);
    }

    /**
     * Enviar email de confirmación de reserva (sin PDF)
     */
    public void enviarConfirmacionReserva(String email, Long reservaId, String fechaHora) {
        if (mailSender == null) {
            logger.warn("⚠️ No se puede enviar confirmación - JavaMailSender no configurado");
            return;
        }

        try {
            logger.info("📧 Enviando confirmación de reserva a: {} para reserva #{}", email, reservaId);
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(email);
            helper.setSubject("✅ Confirmación de Reserva Karting RM - #" + reservaId);
            
            String contenido = "<html>" +
                    "<body style='font-family: Arial, sans-serif; margin: 20px;'>" +
                    "<h2 style='color: #ff6b35;'>🏎️ ¡Reserva Confirmada!</h2>" +
                    "<p>Tu reserva <strong>#" + reservaId + "</strong> ha sido confirmada exitosamente.</p>" +
                    "<p><strong>📅 Fecha y Hora:</strong> " + fechaHora + "</p>" +
                    "<p>Te enviaremos el comprobante detallado por separado.</p>" +
                    "<p style='color: #ff6b35;'><strong>¡Nos vemos en la pista! 🏁</strong></p>" +
                    "</body>" +
                    "</html>";
            
            helper.setText(contenido, true);
            mailSender.send(message);
            
            logger.info("✅ Confirmación enviada a: {} para reserva #{}", email, reservaId);
            
        } catch (Exception e) {
            logger.error("❌ Error al enviar confirmación: {}", e.getMessage());
            // No lanzar excepción para que no bloquee la creación de la reserva
        }
    }

    /**
     * Test de conectividad del servicio de email
     */
    public boolean testConexion() {
        if (mailSender == null) {
            logger.warn("⚠️ JavaMailSender no configurado");
            return false;
        }

        try {
            logger.info("🔍 Probando conexión del servicio de email...");
            mailSender.createMimeMessage();
            logger.info("✅ Servicio de email disponible");
            return true;
        } catch (Exception e) {
            logger.error("❌ Error en servicio de email: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Verificar si el servicio de email está disponible
     */
    public boolean isEmailDisponible() {
        return mailSender != null;
    }
}