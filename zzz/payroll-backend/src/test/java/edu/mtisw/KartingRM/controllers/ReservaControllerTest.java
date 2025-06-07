package edu.mtisw.KartingRM.controllers;

import edu.mtisw.KartingRM.entities.ClienteEntity;
import edu.mtisw.KartingRM.entities.KartEntity;
import edu.mtisw.KartingRM.entities.ReservaEntity;
import edu.mtisw.KartingRM.services.ComprobanteService;
import edu.mtisw.KartingRM.services.EmailService;
import edu.mtisw.KartingRM.services.ReservaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
public class ReservaControllerTest {

    @Mock
    private ReservaService reservaService;

    @Mock
    private ComprobanteService comprobanteService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private ReservaController reservaController;

    private ReservaEntity reserva;
    private List<ReservaEntity> reservasList;
    private byte[] comprobantePdf;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // Crear un cliente para la reserva
        ClienteEntity cliente = new ClienteEntity();
        cliente.setId(1L);
        cliente.setNombre("Cliente Test");
        cliente.setEmail("test@gmail.com");

        // Crear un kart para la reserva
        KartEntity kart = new KartEntity();
        kart.setId(1L);
        kart.setCodigo("K001");
        kart.setEstado("disponible");

        // Crear una reserva
        reserva = new ReservaEntity();
        reserva.setId(1L);
        
        List<ClienteEntity> clientes = new ArrayList<>();
        clientes.add(cliente);
        reserva.setClientes(clientes);
        
        List<KartEntity> karts = new ArrayList<>();
        karts.add(kart);
        reserva.setKarts(karts);
        
        reserva.setNumeroVueltas(10);
        reserva.setPrecioBase(15000);
        reserva.setPrecioFinal(13500);
        reserva.setFechaReserva(LocalDate.now());
        reserva.setHoraReserva(LocalTime.of(10, 0));
        reserva.setDuracionTotal(30);

        // Lista de reservas
        reservasList = new ArrayList<>();
        reservasList.add(reserva);

        // Crear un array de bytes simple para simular un PDF
        comprobantePdf = new byte[100];
        
        // Configurar mocks
        when(reservaService.listarReservas()).thenReturn(reservasList);
        when(reservaService.crearReserva(any(ReservaEntity.class))).thenReturn(reserva);
        when(reservaService.obtenerReservaPorId(1L)).thenReturn(reserva);
        when(comprobanteService.generarComprobante(any(ReservaEntity.class))).thenReturn(comprobantePdf);
        doNothing().when(emailService).enviarComprobante(anyString(), any(byte[].class));
    }

    @Test
    public void listarReservasTest() {
        List<ReservaEntity> result = reservaController.listarReservas();
        
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(reservaService, times(1)).listarReservas();
    }

    @Test
    public void crearReservaTest() {
        ResponseEntity<ReservaEntity> response = reservaController.crearReserva(reserva);
        
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        verify(reservaService, times(1)).crearReserva(any(ReservaEntity.class));
    }

    @Test
    public void crearReservaErrorTest() {
        // Simular un error en la creación de la reserva
        when(reservaService.crearReserva(any(ReservaEntity.class)))
            .thenThrow(new IllegalArgumentException("Error en la reserva"));
        
        ResponseEntity<ReservaEntity> response = reservaController.crearReserva(reserva);
        
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    public void crearReservaSinHoraTest() {
        // Crear reserva sin hora
        ReservaEntity reservaSinHora = new ReservaEntity();
        reservaSinHora.setHoraReserva(null);
        
        ResponseEntity<ReservaEntity> response = reservaController.crearReserva(reservaSinHora);
        
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void obtenerRackSemanalTest() {
        Map<String, Map<String, List<ReservaEntity>>> rackSemanal = new HashMap<>();
        Map<String, List<ReservaEntity>> bloquesLunes = new HashMap<>();
        bloquesLunes.put("09:00-10:00", reservasList);
        rackSemanal.put("Lunes", bloquesLunes);
        
        when(reservaService.obtenerRackSemanal()).thenReturn(rackSemanal);
        
        ResponseEntity<Map<String, Map<String, List<ReservaEntity>>>> response = reservaController.obtenerRackSemanal();
        
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("Lunes"));
        verify(reservaService, times(1)).obtenerRackSemanal();
    }

    @Test
    public void enviarComprobanteTest() {
        ResponseEntity<?> response = reservaController.enviarComprobante(1L);
        
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(reservaService, times(1)).obtenerReservaPorId(1L);
        verify(comprobanteService, times(1)).generarComprobante(any(ReservaEntity.class));
        verify(emailService, times(1)).enviarComprobante(anyString(), any(byte[].class));
    }

    @Test
    public void enviarComprobanteReservaNoExisteTest() {
        // CORREGIDO: Usar doThrow para configurar el comportamiento del mock
        doThrow(new IllegalArgumentException("Reserva no encontrada"))
            .when(reservaService).obtenerReservaPorId(99L);
        
        // Ejecutar y capturar excepción para manejarla como lo haría el controlador
        ResponseEntity<?> response = reservaController.enviarComprobante(99L);
        
        // Verificar
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void obtenerReporteIngresosPorVueltasTest() {
        LocalDate inicio = LocalDate.now().minusMonths(1);
        LocalDate fin = LocalDate.now();
        
        Map<String, Map<String, Integer>> reporte = new HashMap<>();
        Map<String, Integer> ingresosPorMes = new HashMap<>();
        ingresosPorMes.put("APRIL", 50000);
        ingresosPorMes.put("TOTAL", 50000);
        reporte.put("10 vueltas o máx 30 min", ingresosPorMes);
        
        when(reservaService.generarReporteIngresosPorVueltas(any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(reporte);
        
        ResponseEntity<Map<String, Map<String, Integer>>> response = 
            reservaController.obtenerReporteIngresosPorVueltas(inicio, fin);
        
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("10 vueltas o máx 30 min"));
        verify(reservaService, times(1)).generarReporteIngresosPorVueltas(inicio, fin);
    }

    @Test
    public void obtenerReporteIngresosPorPersonasTest() {
        LocalDate inicio = LocalDate.now().minusMonths(1);
        LocalDate fin = LocalDate.now();
        
        Map<String, Map<String, Integer>> reporte = new HashMap<>();
        Map<String, Integer> ingresosPorMes = new HashMap<>();
        ingresosPorMes.put("APRIL", 30000);
        ingresosPorMes.put("TOTAL", 30000);
        reporte.put("1-2 personas", ingresosPorMes);
        
        when(reservaService.generarReporteIngresosPorPersonas(any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(reporte);
        
        ResponseEntity<Map<String, Map<String, Integer>>> response = 
            reservaController.obtenerReporteIngresosPorPersonas(inicio, fin);
        
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("1-2 personas"));
        verify(reservaService, times(1)).generarReporteIngresosPorPersonas(inicio, fin);
    }
}