package com.karting.controller;

import com.karting.dto.RackSemanalResponse;
import com.karting.service.RackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/rack")
@CrossOrigin("*")
public class RackController {

    @Autowired
    private RackService rackService;

    /**
     * RF7 - Obtener rack semanal completo
     */
    @GetMapping("/semanal")
    public ResponseEntity<RackSemanalResponse> obtenerRackSemanal(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        
        try {
            RackSemanalResponse rackSemanal;
            
            // Si no se proporcionan fechas, mostrar semana actual
            if (fechaInicio == null || fechaFin == null) {
                LocalDate hoy = LocalDate.now();
                fechaInicio = hoy.with(DayOfWeek.MONDAY);  // Lunes de esta semana
                fechaFin = fechaInicio.plusDays(6);        // Domingo de esta semana
                
                System.out.println("📅 Sin fechas especificadas, usando semana actual: " + fechaInicio + " - " + fechaFin);
                rackSemanal = rackService.obtenerRackSemanalPorFechas(fechaInicio, fechaFin);
            } else {
                System.out.println("📅 Fechas especificadas: " + fechaInicio + " - " + fechaFin);
                rackSemanal = rackService.obtenerRackSemanalPorFechas(fechaInicio, fechaFin);
            }

            return ResponseEntity.ok(rackSemanal);
            
        } catch (Exception e) {
            System.err.println("❌ Error en endpoint rack semanal: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtener estadísticas del rack semanal
     */
    @GetMapping("/semanal/estadisticas")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticasRack(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        
        try {
            Map<String, Object> estadisticas = rackService.obtenerEstadisticasRack(fechaInicio, fechaFin);
            return ResponseEntity.ok(estadisticas);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error al obtener estadísticas: " + e.getMessage()));
        }
    }

    /**
     * Verificar disponibilidad en un bloque específico
     */
    @GetMapping("/disponibilidad")
    public ResponseEntity<Map<String, Object>> verificarDisponibilidadBloque(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam String bloque,
            @RequestParam Integer numeroPersonas) {
        
        try {
            Map<String, Object> disponibilidad = rackService.verificarDisponibilidadBloque(fecha, bloque, numeroPersonas);
            return ResponseEntity.ok(disponibilidad);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Error al verificar disponibilidad: " + e.getMessage()));
        }
    }

    /**
     * Health check del rack service
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        try {
            return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "rack-service",
                "timestamp", java.time.LocalDateTime.now(),
                "mensaje", "Rack Service funcionando correctamente"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("status", "DOWN", "error", e.getMessage()));
        }
    }
}