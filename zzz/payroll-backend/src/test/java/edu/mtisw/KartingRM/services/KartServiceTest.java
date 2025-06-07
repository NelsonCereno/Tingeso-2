package edu.mtisw.KartingRM.services;

import edu.mtisw.KartingRM.entities.KartEntity;
import edu.mtisw.KartingRM.repositories.KartRepository;
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
public class KartServiceTest {

    @Mock
    private KartRepository kartRepository;

    @InjectMocks
    private KartService kartService;

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
        when(kartRepository.save(any(KartEntity.class))).thenReturn(kart);
        when(kartRepository.findAll()).thenReturn(kartsList);
    }

    @Test
    public void listarKartsTest() {
        List<KartEntity> result = kartService.listarKarts();
        
        assertEquals(1, result.size());
        assertEquals("K001", result.get(0).getCodigo());
        verify(kartRepository, times(1)).findAll();
    }

    @Test
    public void crearKartTest() {
        KartEntity result = kartService.crearKart(kart);
        
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("K001", result.getCodigo());
        assertEquals("disponible", result.getEstado());
        verify(kartRepository, times(1)).save(any(KartEntity.class));
    }
    
    @Test
    public void crearVariosKartsTest() {
        KartEntity kart2 = new KartEntity();
        kart2.setId(2L);
        kart2.setCodigo("K002");
        kart2.setEstado("disponible");
        
        when(kartRepository.save(kart2)).thenReturn(kart2);
        
        KartEntity result1 = kartService.crearKart(kart);
        KartEntity result2 = kartService.crearKart(kart2);
        
        assertNotNull(result1);
        assertNotNull(result2);
        assertEquals("K001", result1.getCodigo());
        assertEquals("K002", result2.getCodigo());
        verify(kartRepository, times(2)).save(any(KartEntity.class));
    }
}