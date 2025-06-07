package edu.mtisw.KartingRM.services;

import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
public class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private EmailService emailService;

    private byte[] comprobantePdf;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Crear un array de bytes simple para simular un PDF
        comprobantePdf = new byte[100];
        for (int i = 0; i < comprobantePdf.length; i++) {
            comprobantePdf[i] = (byte) i;
        }
        
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(mailSender).send(any(MimeMessage.class));
    }

    @Test
    public void enviarComprobanteTest() {
        assertDoesNotThrow(() -> {
            emailService.enviarComprobante("test@gmail.com", comprobantePdf);
        });
        
        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }
    
    @Test
    public void enviarComprobanteErrorTest() {
        // Simular un error al enviar el correo
        doThrow(new RuntimeException("Error al enviar correo")).when(mailSender).send(any(MimeMessage.class));
        
        Exception exception = assertThrows(RuntimeException.class, () -> {
            emailService.enviarComprobante("test@gmail.com", comprobantePdf);
        });
        
        assertTrue(exception.getMessage().contains("Error al enviar el correo"));
    }
}