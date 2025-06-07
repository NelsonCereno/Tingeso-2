package edu.mtisw.KartingRM.services;

import edu.mtisw.KartingRM.entities.ClienteEntity;
import edu.mtisw.KartingRM.repositories.ClienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
public class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private ClienteService clienteService;

    private ClienteEntity cliente;
    private List<ClienteEntity> clientesList;

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

        // Lista de clientes
        clientesList = new ArrayList<>();
        clientesList.add(cliente);

        // Configurar mocks
        when(clienteRepository.save(any(ClienteEntity.class))).thenReturn(cliente);
        when(clienteRepository.findById(anyLong())).thenReturn(Optional.of(cliente));
        when(clienteRepository.findAll()).thenReturn(clientesList);
    }

    @Test
    public void listarClientesTest() {
        List<ClienteEntity> result = clienteService.listarClientes();
        
        assertEquals(1, result.size());
        verify(clienteRepository, times(1)).findAll();
    }

    @Test
    public void crearClienteTest() {
        ClienteEntity result = clienteService.crearCliente(cliente);
        
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(clienteRepository, times(1)).save(any(ClienteEntity.class));
    }

    @Test
    public void actualizarClienteTest() {
        ClienteEntity clienteActualizado = new ClienteEntity();
        clienteActualizado.setId(1L);
        clienteActualizado.setNombre("Cliente Actualizado");
        clienteActualizado.setEmail("actualizado@gmail.com");
        clienteActualizado.setNumeroVisitas(5);
        clienteActualizado.setFechaNacimiento(LocalDate.of(1992, 8, 20));
        
        when(clienteRepository.save(any(ClienteEntity.class))).thenReturn(clienteActualizado);
        
        ClienteEntity result = clienteService.actualizarCliente(1L, clienteActualizado);
        
        assertNotNull(result);
        assertEquals("Cliente Actualizado", result.getNombre());
        assertEquals("actualizado@gmail.com", result.getEmail());
        assertEquals(5, result.getNumeroVisitas());
        assertEquals(LocalDate.of(1992, 8, 20), result.getFechaNacimiento());
        verify(clienteRepository, times(1)).save(any(ClienteEntity.class));
    }

    @Test
    public void actualizarClienteNoExistenteTest() {
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            clienteService.actualizarCliente(99L, cliente);
        });
        
        assertTrue(exception.getMessage().contains("Cliente no encontrado con ID: 99"));
    }

    @Test
    public void crearClienteCorreoInvalidoTest() {
        cliente.setEmail("correo-invalido");
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            clienteService.crearCliente(cliente);
        });
        
        assertTrue(exception.getMessage().contains("correo debe ser una dirección válida de Gmail"));
    }

    @Test
    public void crearClienteSinCorreoTest() {
        cliente.setEmail(null);
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            clienteService.crearCliente(cliente);
        });
        
        assertTrue(exception.getMessage().contains("correo electrónico es obligatorio"));
    }

    @Test
    public void crearClienteCorreoNoGmailTest() {
        cliente.setEmail("test@hotmail.com");
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            clienteService.crearCliente(cliente);
        });
        
        assertTrue(exception.getMessage().contains("correo debe ser una dirección válida de Gmail"));
    }
    
    @Test
    public void crearClienteCorreoValidoTest() {
        cliente.setEmail("valido@gmail.com");
        
        ClienteEntity result = clienteService.crearCliente(cliente);
        
        assertNotNull(result);
        assertEquals("valido@gmail.com", result.getEmail());
    }
}