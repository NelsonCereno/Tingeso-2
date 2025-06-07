package edu.mtisw.KartingRM.controllers;

import edu.mtisw.KartingRM.entities.ClienteEntity;
import edu.mtisw.KartingRM.services.ClienteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
public class ClienteControllerTest {

    @Mock
    private ClienteService clienteService;

    @InjectMocks
    private ClienteController clienteController;

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
        when(clienteService.crearCliente(any(ClienteEntity.class))).thenReturn(cliente);
        when(clienteService.actualizarCliente(anyLong(), any(ClienteEntity.class))).thenReturn(cliente);
        when(clienteService.listarClientes()).thenReturn(clientesList);
    }

    @Test
    public void listarClientesTest() {
        List<ClienteEntity> result = clienteController.listarClientes();
        
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Cliente Test", result.get(0).getNombre());
        verify(clienteService, times(1)).listarClientes();
    }

    @Test
    public void crearClienteTest() {
        ClienteEntity nuevoCliente = new ClienteEntity();
        nuevoCliente.setNombre("Nuevo Cliente");
        nuevoCliente.setEmail("nuevo@gmail.com");
        nuevoCliente.setFechaNacimiento(LocalDate.of(1995, 8, 20));
        
        when(clienteService.crearCliente(any(ClienteEntity.class))).thenReturn(nuevoCliente);
        
        ClienteEntity result = clienteController.crearCliente(nuevoCliente);
        
        assertNotNull(result);
        assertEquals("Nuevo Cliente", result.getNombre());
        assertEquals("nuevo@gmail.com", result.getEmail());
        verify(clienteService, times(1)).crearCliente(any(ClienteEntity.class));
    }

    @Test
    public void actualizarClienteTest() {
        ClienteEntity clienteActualizado = new ClienteEntity();
        clienteActualizado.setId(1L);
        clienteActualizado.setNombre("Cliente Actualizado");
        clienteActualizado.setEmail("actualizado@gmail.com");
        clienteActualizado.setNumeroVisitas(5);
        clienteActualizado.setFechaNacimiento(LocalDate.of(1992, 8, 20));
        
        when(clienteService.actualizarCliente(eq(1L), any(ClienteEntity.class))).thenReturn(clienteActualizado);
        
        ClienteEntity result = clienteController.actualizarCliente(1L, clienteActualizado);
        
        assertNotNull(result);
        assertEquals("Cliente Actualizado", result.getNombre());
        assertEquals("actualizado@gmail.com", result.getEmail());
        assertEquals(5, result.getNumeroVisitas());
        verify(clienteService, times(1)).actualizarCliente(eq(1L), any(ClienteEntity.class));
    }
    
    @Test
    public void actualizarClienteExcepcionTest() {
        ClienteEntity clienteActualizado = new ClienteEntity();
        clienteActualizado.setId(99L);
        clienteActualizado.setNombre("Cliente Actualizado");
        
        when(clienteService.actualizarCliente(eq(99L), any(ClienteEntity.class)))
            .thenThrow(new IllegalArgumentException("Cliente no encontrado"));
        
        // Verifica que la excepción se propague
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            clienteController.actualizarCliente(99L, clienteActualizado);
        });
        
        assertTrue(exception.getMessage().contains("Cliente no encontrado"));
    }
}