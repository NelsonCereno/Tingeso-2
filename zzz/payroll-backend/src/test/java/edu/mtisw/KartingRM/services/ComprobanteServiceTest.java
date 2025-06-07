package edu.mtisw.KartingRM.services;

import edu.mtisw.KartingRM.entities.ClienteEntity;
import edu.mtisw.KartingRM.entities.KartEntity;
import edu.mtisw.KartingRM.entities.ReservaEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
public class ComprobanteServiceTest {

    @InjectMocks
    private ComprobanteService comprobanteService;

    private ReservaEntity reserva;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // Configurar cliente
        ClienteEntity cliente = new ClienteEntity();
        cliente.setId(1L);
        cliente.setNombre("Cliente Test");
        cliente.setEmail("test@gmail.com");
        cliente.setNumeroVisitas(3);
        cliente.setFechaNacimiento(LocalDate.of(1990, 5, 15));

        // Configurar kart
        KartEntity kart = new KartEntity();
        kart.setId(1L);
        kart.setCodigo("K001");
        kart.setEstado("disponible");

        // Configurar reserva
        reserva = new ReservaEntity();
        reserva.setId(1L);
        
        List<ClienteEntity> clientes = new ArrayList<>();
        clientes.add(cliente);
        reserva.setClientes(clientes);
        
        List<KartEntity> karts = new ArrayList<>();
        karts.add(kart);
        reserva.setKarts(karts);
        
        reserva.setNumeroVueltas(10);
        reserva.setTiempoMaximo(10);
        reserva.setPrecioBase(15000);
        reserva.setPrecioFinal(13500);  // 10% de descuento aplicado
        reserva.setDescuentoPorVisitas(10);
        reserva.setDescuentoPorCumpleaños(0);
        reserva.setDescuentoPorPersonas(0);
        reserva.setDuracionTotal(30);
        reserva.setFechaReserva(LocalDate.now());
        reserva.setHoraReserva(LocalTime.of(10, 0));
    }

    @Test
    public void generarComprobanteTest() {
        byte[] pdfBytes = comprobanteService.generarComprobante(reserva);
        
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
    }
    
    @Test
    public void generarComprobanteDatosCompletosTest() {
        // Agregar más clientes y karts para probar con datos más completos
        ClienteEntity cliente2 = new ClienteEntity();
        cliente2.setId(2L);
        cliente2.setNombre("Cliente Test 2");
        cliente2.setEmail("test2@gmail.com");
        
        reserva.getClientes().add(cliente2);
        
        KartEntity kart2 = new KartEntity();
        kart2.setId(2L);
        kart2.setCodigo("K002");
        kart2.setEstado("disponible");
        
        reserva.getKarts().add(kart2);
        
        // Modificar los descuentos para probar diferentes escenarios
        reserva.setDescuentoPorPersonas(10);
        reserva.setDescuentoPorVisitas(20);
        reserva.setDescuentoPorCumpleaños(0);
        
        byte[] pdfBytes = comprobanteService.generarComprobante(reserva);
        
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
    }
}