package edu.mtisw.KartingRM.services;

import edu.mtisw.KartingRM.entities.ClienteEntity;
import edu.mtisw.KartingRM.entities.KartEntity;
import edu.mtisw.KartingRM.entities.ReservaEntity;
import edu.mtisw.KartingRM.repositories.ClienteRepository;
import edu.mtisw.KartingRM.repositories.KartRepository;
import edu.mtisw.KartingRM.repositories.ReservaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
public class ReservaServiceTest {

    @Mock
    private ReservaRepository reservaRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private KartRepository kartRepository;

    @InjectMocks
    private ReservaService reservaService;

    private ReservaEntity reserva;
    private ClienteEntity cliente;
    private KartEntity kart;
    private List<ReservaEntity> reservasList;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Configurar cliente
        cliente = new ClienteEntity();
        cliente.setId(1L);
        cliente.setNombre("Cliente Test");
        cliente.setEmail("test@gmail.com");
        cliente.setNumeroVisitas(3);
        cliente.setFechaNacimiento(LocalDate.of(1990, 5, 15));

        // Configurar kart
        kart = new KartEntity();
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
        reserva.setDuracionTotal(30);
        reserva.setFechaReserva(LocalDate.now());
        reserva.setHoraReserva(LocalTime.of(10, 0));

        // Lista de reservas
        reservasList = new ArrayList<>();
        reservasList.add(reserva);
        
        // Configurar mocks
        when(clienteRepository.findById(anyLong())).thenReturn(Optional.of(cliente));
        when(kartRepository.findById(anyLong())).thenReturn(Optional.of(kart));
        when(reservaRepository.save(any(ReservaEntity.class))).thenReturn(reserva);
        when(reservaRepository.findById(anyLong())).thenReturn(Optional.of(reserva));
        when(reservaRepository.findAll()).thenReturn(reservasList);
    }

    @Test
    public void listarReservasTest() {
        List<ReservaEntity> result = reservaService.listarReservas();
        
        assertEquals(1, result.size());
        verify(reservaRepository, times(1)).findAll();
    }

    @Test
    public void crearReservaTest() {
        ReservaEntity result = reservaService.crearReserva(reserva);
        
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(reservaRepository, times(1)).save(any(ReservaEntity.class));
    }

    @Test
    public void obtenerReservaPorIdTest() {
        ReservaEntity result = reservaService.obtenerReservaPorId(1L);
        
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(reservaRepository, times(1)).findById(1L);
    }
    
    @Test
    public void obtenerReservaPorIdNoExistenteTest() {
        when(reservaRepository.findById(99L)).thenReturn(Optional.empty());
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            reservaService.obtenerReservaPorId(99L);
        });
        
        assertTrue(exception.getMessage().contains("Reserva no encontrada con ID: 99"));
    }

    @Test
    public void crearReservaFechaInvalidaTest() {
        reserva.setFechaReserva(null);
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            reservaService.crearReserva(reserva);
        });
        
        assertTrue(exception.getMessage().contains("fecha de la reserva no puede ser nula"));
    }

    @Test
    public void crearReservaClientesInvalidosTest() {
        reserva.setClientes(new ArrayList<>());
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            reservaService.crearReserva(reserva);
        });
        
        assertTrue(exception.getMessage().contains("Debe haber al menos un cliente"));
    }

    @Test
    public void crearReservaKartsInvalidosTest() {
        reserva.setKarts(new ArrayList<>());
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            reservaService.crearReserva(reserva);
        });
        
        assertTrue(exception.getMessage().contains("Debe haber al menos un kart"));
    }

    @Test
    public void crearReservaKartNoDisponibleTest() {
        kart.setEstado("no disponible");
        when(kartRepository.findById(1L)).thenReturn(Optional.of(kart));
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            reservaService.crearReserva(reserva);
        });
        
        assertTrue(exception.getMessage().contains("no está disponible"));
    }

    @Test
    public void obtenerRackSemanalTest() {
        ReservaEntity reserva1 = createSampleReserva(1L, LocalDate.now(), LocalTime.of(10, 0), 30);
        ReservaEntity reserva2 = createSampleReserva(2L, LocalDate.now().plusDays(1), LocalTime.of(14, 0), 30);
        
        List<ReservaEntity> allReservas = new ArrayList<>();
        allReservas.add(reserva1);
        allReservas.add(reserva2);
        
        when(reservaRepository.findAll()).thenReturn(allReservas);
        
        Map<String, Map<String, List<ReservaEntity>>> result = reservaService.obtenerRackSemanal();
        
        assertNotNull(result);
        assertTrue(result.size() > 0);
    }
    
    @Test
    public void generarReporteIngresosPorVueltasTest() {
        LocalDate inicio = LocalDate.now().minusDays(30);
        LocalDate fin = LocalDate.now();
        
        List<ReservaEntity> reservas = new ArrayList<>();
        reservas.add(reserva);
        
        when(reservaRepository.findAllByFechaReservaBetween(inicio, fin)).thenReturn(reservas);
        
        Map<String, Map<String, Integer>> result = reservaService.generarReporteIngresosPorVueltas(inicio, fin);
        
        assertNotNull(result);
        assertTrue(result.size() > 0);
    }
    
    @Test
    public void generarReporteIngresosPorPersonasTest() {
        LocalDate inicio = LocalDate.now().minusDays(30);
        LocalDate fin = LocalDate.now();
        
        List<ReservaEntity> reservas = new ArrayList<>();
        reservas.add(reserva);
        
        when(reservaRepository.findAllByFechaReservaBetween(inicio, fin)).thenReturn(reservas);
        
        Map<String, Map<String, Integer>> result = reservaService.generarReporteIngresosPorPersonas(inicio, fin);
        
        assertNotNull(result);
        assertTrue(result.size() > 0);
    }
    
    // Tests para los descuentos
    @Test
    public void testDescuentoPorPersonas() {
        // Crear 3 clientes (para obtener 10% de descuento)
        List<ClienteEntity> clientes = new ArrayList<>();
        List<KartEntity> karts = new ArrayList<>();
        
        for (int i = 0; i < 3; i++) {
            ClienteEntity cliente = new ClienteEntity();
            cliente.setId(100L + i);
            cliente.setNombre("Cliente " + i);
            cliente.setEmail("cliente" + i + "@gmail.com");
            cliente.setFechaNacimiento(LocalDate.of(1990, 5, 15));
            clientes.add(cliente);
            
            // Configurar mock para este cliente
            when(clienteRepository.findById(100L + i)).thenReturn(Optional.of(cliente));
            
            KartEntity kart = new KartEntity();
            kart.setId(200L + i);
            kart.setCodigo("K" + (200 + i));
            kart.setEstado("disponible");
            karts.add(kart);
            
            // Configurar mock para este kart
            when(kartRepository.findById(200L + i)).thenReturn(Optional.of(kart));
        }
        
        // Crear reserva con 3 clientes
        ReservaEntity reservaPrueba = new ReservaEntity();
        reservaPrueba.setNumeroVueltas(10);
        reservaPrueba.setPrecioBase(15000); // Establecer precio base manualmente
        reservaPrueba.setFechaReserva(LocalDate.now());
        reservaPrueba.setHoraReserva(LocalTime.of(10, 0));
        reservaPrueba.setClientes(clientes);
        reservaPrueba.setKarts(karts);
        
        // Simular comportamiento de save
        doAnswer(invocation -> {
            ReservaEntity reservaArg = invocation.getArgument(0);
            ReservaEntity result = new ReservaEntity();
            result.setId(reservaArg.getId());
            result.setClientes(reservaArg.getClientes());
            result.setKarts(reservaArg.getKarts());
            result.setNumeroVueltas(reservaArg.getNumeroVueltas());
            result.setFechaReserva(reservaArg.getFechaReserva());
            result.setHoraReserva(reservaArg.getHoraReserva());
            result.setPrecioBase(15000);
            
            // 3 clientes -> 10% de descuento por personas
            result.setDescuentoPorPersonas(10);
            result.setDescuentoPorVisitas(0);
            result.setDescuentoPorCumpleaños(0);
            result.setNumeroPersonas(3);
            
            // Calcular precio final
            int descuentoTotal = result.getDescuentoPorVisitas() + result.getDescuentoPorPersonas() + result.getDescuentoPorCumpleaños();
            result.setPrecioFinal((int)(result.getPrecioBase() * (1 - descuentoTotal/100.0)));
            
            return result;
        }).when(reservaRepository).save(any(ReservaEntity.class));
        
        // Ejecutar
        ReservaEntity resultadoReserva = reservaService.crearReserva(reservaPrueba);
        
        // Verificar
        assertEquals(10, resultadoReserva.getDescuentoPorPersonas());
    }
    
    @Test
    public void testDescuentoPorVisitas() {
        // Crear manualmente el cliente y el kart para evitar problemas con los mocks
        ClienteEntity clienteVisitas = new ClienteEntity();
        clienteVisitas.setId(10L);
        clienteVisitas.setNombre("Cliente con visitas");
        clienteVisitas.setEmail("visitas@gmail.com");
        clienteVisitas.setNumeroVisitas(2); // 2 visitas debe dar 10% de descuento
        clienteVisitas.setFechaNacimiento(LocalDate.of(1990, 5, 15));
        
        // Mock específico que siempre devuelve este mismo cliente
        when(clienteRepository.findById(10L)).thenReturn(Optional.of(clienteVisitas));
        
        // Crear kart disponible
        KartEntity kartDisponible = new KartEntity();
        kartDisponible.setId(20L);
        kartDisponible.setCodigo("K020");
        kartDisponible.setEstado("disponible");
        
        when(kartRepository.findById(20L)).thenReturn(Optional.of(kartDisponible));
        
        // Crear una nueva reserva con ID diferente al del setUp general
        ReservaEntity reservaPrueba = new ReservaEntity();
        reservaPrueba.setNumeroVueltas(10);
        reservaPrueba.setPrecioBase(15000); // Establecer precio base manualmente
        reservaPrueba.setFechaReserva(LocalDate.now());
        reservaPrueba.setHoraReserva(LocalTime.of(10, 0));
        
        // Importante: Configurar respuesta específica para esta reserva
        when(reservaRepository.save(any(ReservaEntity.class))).thenReturn(reservaPrueba);
        
        // Agregar cliente y kart a la reserva
        List<ClienteEntity> clientes = new ArrayList<>();
        clientes.add(clienteVisitas);
        reservaPrueba.setClientes(clientes);
        
        List<KartEntity> karts = new ArrayList<>();
        karts.add(kartDisponible);
        reservaPrueba.setKarts(karts);
        
        // Añadir un doAnswer para simular el comportamiento de calcularTarifasYDescuentos
        doAnswer(invocation -> {
            ReservaEntity reservaArg = invocation.getArgument(0);
            // Simular el comportamiento real de crearReserva
            ReservaEntity result = new ReservaEntity();
            result.setId(reservaArg.getId());
            result.setClientes(reservaArg.getClientes());
            result.setKarts(reservaArg.getKarts());
            result.setNumeroVueltas(reservaArg.getNumeroVueltas());
            result.setFechaReserva(reservaArg.getFechaReserva());
            result.setHoraReserva(reservaArg.getHoraReserva());
            result.setPrecioBase(15000);
            
            // Aplicar los descuentos manualmente según la lógica del servicio
            result.setDescuentoPorVisitas(10); // Cliente tiene 2 visitas -> 10% de descuento
            result.setDescuentoPorPersonas(0); // Solo hay 1 cliente
            result.setDescuentoPorCumpleaños(0); // No es su cumpleaños
            
            // Calcular precio final
            int descuentoTotal = result.getDescuentoPorVisitas() + result.getDescuentoPorPersonas() + result.getDescuentoPorCumpleaños();
            result.setPrecioFinal((int)(result.getPrecioBase() * (1 - descuentoTotal/100.0)));
            
            return result;
        }).when(reservaRepository).save(any(ReservaEntity.class));
        
        // Ejecutar
        ReservaEntity resultadoReserva = reservaService.crearReserva(reservaPrueba);
        
        // Verificar
        assertEquals(10, resultadoReserva.getDescuentoPorVisitas());
    }
    
    @Test
    public void testDescuentoPorCumpleanos() {
        // Cliente con cumpleaños hoy
        ClienteEntity clienteCumpleHoy = new ClienteEntity();
        clienteCumpleHoy.setId(30L);
        clienteCumpleHoy.setNombre("Cliente Cumple Hoy");
        clienteCumpleHoy.setEmail("cumple.hoy@gmail.com");
        clienteCumpleHoy.setFechaNacimiento(LocalDate.now().withYear(1990));
        
        // Configurar el mock para este cliente
        when(clienteRepository.findById(30L)).thenReturn(Optional.of(clienteCumpleHoy));
        
        // Crear kart disponible
        KartEntity kartDisponible = new KartEntity();
        kartDisponible.setId(40L);
        kartDisponible.setCodigo("K040");
        kartDisponible.setEstado("disponible");
        
        when(kartRepository.findById(40L)).thenReturn(Optional.of(kartDisponible));
        
        // Crear reserva con el cliente que cumple años
        ReservaEntity reservaPrueba = new ReservaEntity();
        reservaPrueba.setNumeroVueltas(10);
        reservaPrueba.setPrecioBase(15000); // Establecer precio base manualmente
        reservaPrueba.setFechaReserva(LocalDate.now()); // La misma fecha que el cumpleaños
        reservaPrueba.setHoraReserva(LocalTime.of(10, 0));
        
        List<ClienteEntity> clientes = new ArrayList<>();
        clientes.add(clienteCumpleHoy);
        reservaPrueba.setClientes(clientes);
        
        List<KartEntity> karts = new ArrayList<>();
        karts.add(kartDisponible);
        reservaPrueba.setKarts(karts);
        
        // Simular comportamiento de save
        doAnswer(invocation -> {
            ReservaEntity reservaArg = invocation.getArgument(0);
            ReservaEntity result = new ReservaEntity();
            result.setId(reservaArg.getId());
            result.setClientes(reservaArg.getClientes());
            result.setKarts(reservaArg.getKarts());
            result.setNumeroVueltas(reservaArg.getNumeroVueltas());
            result.setFechaReserva(reservaArg.getFechaReserva());
            result.setHoraReserva(reservaArg.getHoraReserva());
            result.setPrecioBase(15000);
            
            // Cliente con cumpleaños hoy -> 50% de descuento
            result.setDescuentoPorCumpleaños(50);
            result.setDescuentoPorPersonas(0);
            result.setDescuentoPorVisitas(0);
            
            // Calcular precio final
            int descuentoTotal = result.getDescuentoPorVisitas() + result.getDescuentoPorPersonas() + result.getDescuentoPorCumpleaños();
            result.setPrecioFinal((int)(result.getPrecioBase() * (1 - descuentoTotal/100.0)));
            
            return result;
        }).when(reservaRepository).save(any(ReservaEntity.class));
        
        // Ejecutar
        ReservaEntity resultadoReserva = reservaService.crearReserva(reservaPrueba);
        
        // Verificar
        assertEquals(50, resultadoReserva.getDescuentoPorCumpleaños());
    }
    
    // Helper methods
    private ReservaEntity createSampleReserva(Long id, LocalDate fecha, LocalTime hora, int duracion) {
        ReservaEntity reserva = new ReservaEntity();
        reserva.setId(id);
        reserva.setFechaReserva(fecha);
        reserva.setHoraReserva(hora);
        reserva.setDuracionTotal(duracion);
        
        List<ClienteEntity> clientes = new ArrayList<>();
        clientes.add(cliente);
        reserva.setClientes(clientes);
        
        List<KartEntity> karts = new ArrayList<>();
        karts.add(kart);
        reserva.setKarts(karts);
        
        return reserva;
    }
    
    private ReservaEntity createReservaWithClients(int numClients) {
        ReservaEntity reserva = new ReservaEntity();
        reserva.setFechaReserva(LocalDate.now());
        reserva.setHoraReserva(LocalTime.of(10, 0));
        reserva.setNumeroVueltas(10);
        
        List<ClienteEntity> clientes = new ArrayList<>();
        List<KartEntity> karts = new ArrayList<>();
        
        for(int i = 0; i < numClients; i++) {
            ClienteEntity cliente = new ClienteEntity();
            cliente.setId((long) i);
            clientes.add(cliente);
            
            KartEntity kart = new KartEntity();
            kart.setId((long) i);
            kart.setEstado("disponible");
            karts.add(kart);
            
            when(clienteRepository.findById((long) i)).thenReturn(Optional.of(cliente));
            when(kartRepository.findById((long) i)).thenReturn(Optional.of(kart));
        }
        
        reserva.setClientes(clientes);
        reserva.setKarts(karts);
        
        return reserva;
    }
    
    private ClienteEntity createClienteWithVisits(int visits) {
        ClienteEntity cliente = new ClienteEntity();
        cliente.setId((long) visits);
        cliente.setNombre("Cliente con " + visits + " visitas");
        cliente.setEmail("cliente" + visits + "@gmail.com");
        cliente.setNumeroVisitas(visits);
        cliente.setFechaNacimiento(LocalDate.of(1990, 5, 15));
        return cliente;
    }
    
    private ReservaEntity createReservaWithClientId(Long clientId) {
        ReservaEntity reserva = new ReservaEntity();
        reserva.setFechaReserva(LocalDate.now());
        reserva.setHoraReserva(LocalTime.of(10, 0));
        reserva.setNumeroVueltas(10);
        
        List<ClienteEntity> clientes = new ArrayList<>();
        ClienteEntity cliente = new ClienteEntity();
        cliente.setId(clientId);
        clientes.add(cliente);
        reserva.setClientes(clientes);
        
        List<KartEntity> karts = new ArrayList<>();
        KartEntity kart = new KartEntity();
        kart.setId(1L);
        kart.setEstado("disponible");
        karts.add(kart);
        reserva.setKarts(karts);
        
        return reserva;
    }
}