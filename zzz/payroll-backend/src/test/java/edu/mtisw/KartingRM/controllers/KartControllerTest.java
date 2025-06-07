package edu.mtisw.KartingRM.controllers;

import edu.mtisw.KartingRM.entities.KartEntity;
import edu.mtisw.KartingRM.services.KartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
public class KartControllerTest {

    @Mock
    private KartService kartService;

    @InjectMocks
    private KartController kartController;

    private KartEntity kart;
    private List<KartEntity> kartsList;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // Configurar kart
        kart = new KartEntity();
        kart.setId(1L);
        kart.setCodigo("K001");
        kart.setEstado("disponible");

        // Lista de karts
        kartsList = new ArrayList<>();
        kartsList.add(kart);

        // Configurar mocks
        when(kartService.crearKart(any(KartEntity.class))).thenReturn(kart);
        when(kartService.listarKarts()).thenReturn(kartsList);
    }

    @Test
    public void listarKartsTest() {
        List<KartEntity> result = kartController.listarKarts();
        
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("K001", result.get(0).getCodigo());
        assertEquals("disponible", result.get(0).getEstado());
        verify(kartService, times(1)).listarKarts();
    }

    @Test
    public void crearKartTest() {
        KartEntity nuevoKart = new KartEntity();
        nuevoKart.setCodigo("K002");
        nuevoKart.setEstado("disponible");
        
        when(kartService.crearKart(any(KartEntity.class))).thenReturn(nuevoKart);
        
        KartEntity result = kartController.crearKart(nuevoKart);
        
        assertNotNull(result);
        assertEquals("K002", result.getCodigo());
        assertEquals("disponible", result.getEstado());
        verify(kartService, times(1)).crearKart(any(KartEntity.class));
    }

    @Test
    public void testEndpointTest() {
        String result = kartController.testEndpoint();
        
        assertEquals("El endpoint de karts está funcionando correctamente.", result);
    }
    
    @Test
    public void listarKartsVacioTest() {
        // Simular lista vacía
        when(kartService.listarKarts()).thenReturn(new ArrayList<>());
        
        List<KartEntity> result = kartController.listarKarts();
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
    
    @Test
    public void crearMultiplesKartsTest() {
        // CORREGIDO: Simplificar la configuración de los mocks
        KartEntity kart1 = new KartEntity();
        kart1.setId(1L);
        kart1.setCodigo("K001");
        kart1.setEstado("disponible");
        
        KartEntity kart2 = new KartEntity();
        kart2.setId(2L);
        kart2.setCodigo("K002");
        kart2.setEstado("disponible");
        
        // Configuración más simple usando answer
        when(kartService.crearKart(any(KartEntity.class)))
            .thenAnswer(invocation -> {
                KartEntity k = invocation.getArgument(0);
                if (k != null && "K002".equals(k.getCodigo())) {
                    return kart2;
                } else {
                    return kart1;
                }
            });
        
        // Crear ambos karts
        KartEntity resultado1 = kartController.crearKart(kart1);
        KartEntity resultado2 = kartController.crearKart(kart2);
        
        // Verificar resultados
        assertEquals("K001", resultado1.getCodigo());
        assertEquals("K002", resultado2.getCodigo());
        
        // Verificar que el servicio fue llamado dos veces
        verify(kartService, times(2)).crearKart(any(KartEntity.class));
    }
}