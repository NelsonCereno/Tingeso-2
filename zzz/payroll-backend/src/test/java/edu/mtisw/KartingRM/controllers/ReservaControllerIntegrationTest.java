package edu.mtisw.KartingRM.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.mtisw.KartingRM.controllers.ReservaController;
import edu.mtisw.KartingRM.entities.ClienteEntity;
import edu.mtisw.KartingRM.entities.KartEntity;
import edu.mtisw.KartingRM.entities.ReservaEntity;
import edu.mtisw.KartingRM.services.ComprobanteService;
import edu.mtisw.KartingRM.services.EmailService;
import edu.mtisw.KartingRM.services.ReservaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReservaController.class)
public class ReservaControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReservaService reservaService;

    @MockBean
    private ComprobanteService comprobanteService;

    @MockBean
    private EmailService emailService;

    @Test
    public void listarReservasTest() throws Exception {
        // Preparar datos de prueba
        ClienteEntity cliente = new ClienteEntity();
        cliente.setId(1L);
        cliente.setNombre("Cliente Test");
        cliente.setEmail("test@gmail.com");

        KartEntity kart = new KartEntity();
        kart.setId(1L);
        kart.setCodigo("K001");

        ReservaEntity reserva = new ReservaEntity();
        reserva.setId(1L);
        List<ClienteEntity> clientes = new ArrayList<>();
        clientes.add(cliente);
        reserva.setClientes(clientes);
        List<KartEntity> karts = new ArrayList<>();
        karts.add(kart);
        reserva.setKarts(karts);
        reserva.setFechaReserva(LocalDate.now());
        reserva.setHoraReserva(LocalTime.of(10, 0));

        List<ReservaEntity> reservasList = new ArrayList<>();
        reservasList.add(reserva);

        when(reservaService.listarReservas()).thenReturn(reservasList);

        // Ejecutar y verificar
        mockMvc.perform(get("/api/reservas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].clientes[0].nombre").value("Cliente Test"))
                .andExpect(jsonPath("$[0].karts[0].codigo").value("K001"));
    }

    @Test
    public void crearReservaTest() throws Exception {
        // Preparar datos de prueba
        ClienteEntity cliente = new ClienteEntity();
        cliente.setId(1L);
        cliente.setNombre("Cliente Test");
        cliente.setEmail("test@gmail.com");

        KartEntity kart = new KartEntity();
        kart.setId(1L);
        kart.setCodigo("K001");

        ReservaEntity reserva = new ReservaEntity();
        reserva.setId(1L);
        List<ClienteEntity> clientes = new ArrayList<>();
        clientes.add(cliente);
        reserva.setClientes(clientes);
        List<KartEntity> karts = new ArrayList<>();
        karts.add(kart);
        reserva.setKarts(karts);
        reserva.setFechaReserva(LocalDate.now());
        reserva.setHoraReserva(LocalTime.of(10, 0));

        when(reservaService.crearReserva(any(ReservaEntity.class))).thenReturn(reserva);

        // Ejecutar y verificar
        mockMvc.perform(post("/api/reservas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reserva)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.clientes[0].nombre").value("Cliente Test"))
                .andExpect(jsonPath("$.karts[0].codigo").value("K001"));
    }

    @Test
    public void obtenerRackSemanalTest() throws Exception {
        // Preparar datos de prueba
        Map<String, Map<String, List<ReservaEntity>>> rackSemanal = new HashMap<>();
        Map<String, List<ReservaEntity>> bloquesLunes = new HashMap<>();
        bloquesLunes.put("09:00-10:00", new ArrayList<>());
        rackSemanal.put("Lunes", bloquesLunes);

        when(reservaService.obtenerRackSemanal()).thenReturn(rackSemanal);

        // Ejecutar y verificar
        mockMvc.perform(get("/api/reservas/rack-semanal"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.Lunes").exists())
                .andExpect(jsonPath("$.Lunes['09:00-10:00']").isArray());
    }

    @Test
    public void obtenerReporteIngresosPorVueltasTest() throws Exception {
        // Preparar datos de prueba
        Map<String, Map<String, Integer>> reporte = new HashMap<>();
        Map<String, Integer> ingresosPorMes = new HashMap<>();
        ingresosPorMes.put("APRIL", 50000);
        ingresosPorMes.put("TOTAL", 50000);
        reporte.put("10 vueltas o máx 30 min", ingresosPorMes);

        when(reservaService.generarReporteIngresosPorVueltas(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(reporte);

        // Ejecutar y verificar
        mockMvc.perform(get("/api/reservas/reporte-ingresos-vueltas")
                .param("inicio", LocalDate.now().minusMonths(1).toString())
                .param("fin", LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$['10 vueltas o máx 30 min'].APRIL").value(50000))
                .andExpect(jsonPath("$['10 vueltas o máx 30 min'].TOTAL").value(50000));
    }

    @Test
    public void enviarComprobanteTest() throws Exception {
        // Preparar datos de prueba
        ClienteEntity cliente = new ClienteEntity();
        cliente.setId(1L);
        cliente.setNombre("Cliente Test");
        cliente.setEmail("test@gmail.com");

        KartEntity kart = new KartEntity();
        kart.setId(1L);
        kart.setCodigo("K001");

        ReservaEntity reserva = new ReservaEntity();
        reserva.setId(1L);
        List<ClienteEntity> clientes = new ArrayList<>();
        clientes.add(cliente);
        reserva.setClientes(clientes);
        List<KartEntity> karts = new ArrayList<>();
        karts.add(kart);
        reserva.setKarts(karts);
        reserva.setFechaReserva(LocalDate.now());
        reserva.setHoraReserva(LocalTime.of(10, 0));

        byte[] comprobantePdf = new byte[100];

        when(reservaService.obtenerReservaPorId(anyLong())).thenReturn(reserva);
        when(comprobanteService.generarComprobante(any(ReservaEntity.class))).thenReturn(comprobantePdf);

        // Ejecutar y verificar
        mockMvc.perform(post("/api/reservas/1/enviar-comprobante"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("test@gmail.com"));
    }
}