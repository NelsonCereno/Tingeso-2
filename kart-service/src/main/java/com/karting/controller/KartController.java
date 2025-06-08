package com.karting.controller;

import com.karting.dto.KartRequest;
import com.karting.dto.KartResponse;
import com.karting.entity.KartEntity;
import com.karting.service.KartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/karts")
@CrossOrigin("*")
public class KartController {

    @Autowired
    private KartService kartService;

    // Health check del servicio
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Kart Service is running!");
    }

    // CRUD básico
    
    // Crear kart
    @PostMapping
    public ResponseEntity<KartResponse> crearKart(@RequestBody KartRequest request) {
        try {
            if (request.getCodigo() == null || request.getCodigo().trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            
            KartResponse response = kartService.crearKart(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Obtener todos los karts
    @GetMapping
    public ResponseEntity<List<KartResponse>> obtenerTodosLosKarts() {
        List<KartResponse> karts = kartService.obtenerTodosLosKarts();
        if (karts.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(karts);
    }
    
    // Obtener kart por ID
    @GetMapping("/{id}")
    public ResponseEntity<KartResponse> obtenerKartPorId(@PathVariable Long id) {
        try {
            KartResponse response = kartService.obtenerKartPorId(id);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // Obtener kart por código
    @GetMapping("/codigo/{codigo}")
    public ResponseEntity<KartResponse> obtenerKartPorCodigo(@PathVariable String codigo) {
        try {
            KartResponse response = kartService.obtenerKartPorCodigo(codigo);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // Actualizar kart
    @PutMapping("/{id}")
    public ResponseEntity<KartResponse> actualizarKart(@PathVariable Long id, @RequestBody KartRequest request) {
        try {
            if (request.getCodigo() == null || request.getCodigo().trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            
            KartResponse response = kartService.actualizarKart(id, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Eliminar kart (soft delete)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarKart(@PathVariable Long id) {
        try {
            kartService.eliminarKart(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // Reactivar kart
    @PutMapping("/{id}/reactivar")
    public ResponseEntity<KartResponse> reactivarKart(@PathVariable Long id) {
        try {
            KartResponse response = kartService.reactivarKart(id);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Métodos principales para el orquestador (ReservaService)
    
    // Obtener karts disponibles (PRINCIPAL para el orquestador)
    @GetMapping("/disponibles")
    public ResponseEntity<List<KartResponse>> obtenerKartsDisponibles() {
        List<KartResponse> karts = kartService.obtenerKartsDisponibles();
        if (karts.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(karts);
    }
    
    // Verificar disponibilidad de karts específicos (PRINCIPAL para el orquestador)
    @PostMapping("/verificar-disponibilidad")
    public ResponseEntity<List<KartResponse>> verificarDisponibilidadKarts(@RequestBody List<Long> kartsIds) {
        try {
            if (kartsIds == null || kartsIds.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            
            List<KartResponse> kartsDisponibles = kartService.verificarDisponibilidadKarts(kartsIds);
            if (kartsDisponibles.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(kartsDisponibles);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Reservar karts (PRINCIPAL para el orquestador)
    @PostMapping("/reservar")
    public ResponseEntity<List<KartResponse>> reservarKarts(@RequestBody List<Long> kartsIds) {
        try {
            if (kartsIds == null || kartsIds.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            
            List<KartResponse> kartsReservados = kartService.reservarKarts(kartsIds);
            return ResponseEntity.ok(kartsReservados);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Liberar karts después del uso (PRINCIPAL para el orquestador)
    @PostMapping("/liberar")
    public ResponseEntity<List<KartResponse>> liberarKarts(@RequestBody List<Long> kartsIds) {
        try {
            if (kartsIds == null || kartsIds.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            
            List<KartResponse> kartsLiberados = kartService.liberarKarts(kartsIds);
            return ResponseEntity.ok(kartsLiberados);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Obtener karts disponibles optimizados (distribución equilibrada)
    @GetMapping("/disponibles/optimizados/{cantidad}")
    public ResponseEntity<List<KartResponse>> obtenerKartsDisponiblesOptimizados(@PathVariable int cantidad) {
        if (cantidad <= 0) {
            return ResponseEntity.badRequest().build();
        }
        
        List<KartResponse> karts = kartService.obtenerKartsDisponiblesOptimizados(cantidad);
        if (karts.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(karts);
    }
    
    // Obtener capacidad disponible (PRINCIPAL para el orquestador)
    @GetMapping("/capacidad-disponible")
    public ResponseEntity<Long> obtenerCapacidadDisponible() {
        Long capacidad = kartService.obtenerCapacidadDisponible();
        return ResponseEntity.ok(capacidad);
    }

    // Métodos de gestión de estados
    
    // Cambiar estado de kart
    @PutMapping("/{id}/estado")
    public ResponseEntity<KartResponse> cambiarEstadoKart(
            @PathVariable Long id, 
            @RequestParam KartEntity.EstadoKart estado,
            @RequestParam(required = false) String observaciones) {
        try {
            KartResponse response = kartService.cambiarEstadoKart(id, estado, observaciones);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // Enviar kart a mantenimiento
    @PutMapping("/{id}/mantenimiento")
    public ResponseEntity<KartResponse> enviarAMantenimiento(
            @PathVariable Long id, 
            @RequestParam String motivo) {
        try {
            if (motivo == null || motivo.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            
            KartResponse response = kartService.enviarAMantenimiento(id, motivo);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // Completar mantenimiento
    @PutMapping("/{id}/completar-mantenimiento")
    public ResponseEntity<KartResponse> completarMantenimiento(@PathVariable Long id) {
        try {
            KartResponse response = kartService.completarMantenimiento(id);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // Programar mantenimiento masivo
    @PostMapping("/mantenimiento-masivo")
    public ResponseEntity<List<KartResponse>> programarMantenimientoMasivo(
            @RequestBody List<Long> kartsIds,
            @RequestParam String motivo) {
        try {
            if (kartsIds == null || kartsIds.isEmpty() || motivo == null || motivo.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            
            List<KartResponse> karts = kartService.programarMantenimientoMasivo(kartsIds, motivo);
            return ResponseEntity.ok(karts);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Métodos de búsqueda y filtrado
    
    // Buscar karts por estado
    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<KartResponse>> buscarKartsPorEstado(@PathVariable KartEntity.EstadoKart estado) {
        List<KartResponse> karts = kartService.buscarKartsPorEstado(estado);
        if (karts.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(karts);
    }
    
    // Buscar karts por código similar
    @GetMapping("/buscar/codigo/{codigo}")
    public ResponseEntity<List<KartResponse>> buscarKartsPorCodigo(@PathVariable String codigo) {
        List<KartResponse> karts = kartService.buscarKartsPorCodigo(codigo);
        if (karts.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(karts);
    }
    
    // Buscar karts por rango de usos
    @GetMapping("/buscar/usos/{usosMin}/{usosMax}")
    public ResponseEntity<List<KartResponse>> buscarKartsPorRangoUsos(
            @PathVariable Integer usosMin, 
            @PathVariable Integer usosMax) {
        
        if (usosMin < 0 || usosMax < 0 || usosMin > usosMax) {
            return ResponseEntity.badRequest().build();
        }
        
        List<KartResponse> karts = kartService.buscarKartsPorRangoUsos(usosMin, usosMax);
        if (karts.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(karts);
    }

    // Métodos de mantenimiento
    
    // Obtener karts en mantenimiento
    @GetMapping("/mantenimiento")
    public ResponseEntity<List<KartResponse>> obtenerKartsEnMantenimiento() {
        List<KartResponse> karts = kartService.obtenerKartsEnMantenimiento();
        if (karts.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(karts);
    }
    
    // Obtener karts que necesitan mantenimiento
    @GetMapping("/necesitan-mantenimiento")
    public ResponseEntity<List<KartResponse>> obtenerKartsQueNecesitanMantenimiento() {
        List<KartResponse> karts = kartService.obtenerKartsQueNecesitanMantenimiento();
        if (karts.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(karts);
    }

    // Métodos de validación (para el orquestador)
    
    // Verificar si un código ya existe
    @GetMapping("/existe/codigo/{codigo}")
    public ResponseEntity<Boolean> existeCodigo(@PathVariable String codigo) {
        boolean existe = kartService.existeCodigo(codigo);
        return ResponseEntity.ok(existe);
    }
    
    // Verificar si un kart existe
    @GetMapping("/existe/{id}")
    public ResponseEntity<Boolean> existeKart(@PathVariable Long id) {
        boolean existe = kartService.existeKart(id);
        return ResponseEntity.ok(existe);
    }
    
    // Verificar si un kart está disponible
    @GetMapping("/disponible/{id}")
    public ResponseEntity<Boolean> kartEstaDisponible(@PathVariable Long id) {
        boolean disponible = kartService.kartEstaDisponible(id);
        return ResponseEntity.ok(disponible);
    }
    
    // Verificar disponibilidad de múltiples karts (validación rápida)
    @PostMapping("/estan-disponibles")
    public ResponseEntity<Boolean> kartsEstanDisponibles(@RequestBody List<Long> kartsIds) {
        if (kartsIds == null || kartsIds.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        boolean disponibles = kartService.kartsEstanDisponibles(kartsIds);
        return ResponseEntity.ok(disponibles);
    }

    // Métodos de estadísticas y reportes
    
    // Obtener estadísticas generales
    @GetMapping("/estadisticas/generales")
    public ResponseEntity<KartService.EstadisticasKartsResponse> obtenerEstadisticasGenerales() {
        KartService.EstadisticasKartsResponse estadisticas = kartService.obtenerEstadisticasGenerales();
        return ResponseEntity.ok(estadisticas);
    }
    
    // Obtener karts más utilizados
    @GetMapping("/mas-utilizados/{limite}")
    public ResponseEntity<List<KartResponse>> obtenerKartsMasUtilizados(@PathVariable int limite) {
        if (limite <= 0) {
            return ResponseEntity.badRequest().build();
        }
        
        List<KartResponse> karts = kartService.obtenerKartsMasUtilizados(limite);
        if (karts.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(karts);
    }
    
    // Obtener karts menos utilizados
    @GetMapping("/menos-utilizados/{limite}")
    public ResponseEntity<List<KartResponse>> obtenerKartsMenosUtilizados(@PathVariable int limite) {
        if (limite <= 0) {
            return ResponseEntity.badRequest().build();
        }
        
        List<KartResponse> karts = kartService.obtenerKartsMenosUtilizados(limite);
        if (karts.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(karts);
    }
    
    // Obtener karts sin uso reciente
    @GetMapping("/sin-uso-reciente/{dias}")
    public ResponseEntity<List<KartResponse>> obtenerKartsSinUsoReciente(@PathVariable int dias) {
        if (dias <= 0) {
            return ResponseEntity.badRequest().build();
        }
        
        List<KartResponse> karts = kartService.obtenerKartsSinUsoReciente(dias);
        if (karts.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(karts);
    }
}
