package edu.mtisw.KartingRM.services;

import jakarta.mail.util.ByteArrayDataSource;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    public void enviarComprobante(String email, byte[] comprobantePdf) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(email);
            helper.setSubject("Comprobante de Pago - Kartódromo");
            helper.setText("Adjunto encontrarás el comprobante de tu reserva.");

            helper.addAttachment("Comprobante.pdf", new ByteArrayDataSource(comprobantePdf, "application/pdf"));

            mailSender.send(message);


            logger.info("Comprobante enviado a: {}", email);
        } catch (Exception e) {
            throw new RuntimeException("Error al enviar el correo", e);
        }
    }
}