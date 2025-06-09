package com.karting.controller;

import com.karting.dto.ReporteIngresosResponse;
import com.karting.service.ReportsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/reports")

public class ReportsController {

    @Autowired
    private ReportsService reportsService;

    /**
     * RF8 - Generar reporte mensual de ingresos
     */
    @GetMapping("/ingresos/mensual")
    public ResponseEntity<ReporteIngresosResponse> generarReporteMensual(
            @RequestParam Integer anio,
            @RequestParam Integer mes) {
        
        try {
            // Validaciones
            if (mes < 1 || mes > 12) {
                return ResponseEntity.badRequest().build();
            }
            
            if (anio < 2020 || anio > LocalDate.now().getYear() + 1) {
                return ResponseEntity.badRequest().build();
            }
            
            ReporteIngresosResponse reporte = reportsService.generarReporteMensual(anio, mes);
            return ResponseEntity.ok(reporte);
            
        } catch (Exception e) {
            System.err.println("❌ Error en endpoint reporte mensual: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Generar reporte anual (resumen por meses)
     */
    @GetMapping("/ingresos/anual")
    public ResponseEntity<Map<String, Object>> generarReporteAnual(
            @RequestParam Integer anio) {
        
        try {
            if (anio < 2020 || anio > LocalDate.now().getYear() + 1) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Año fuera del rango válido"));
            }
            
            Map<String, Object> reporte = reportsService.generarReporteAnual(anio);
            return ResponseEntity.ok(reporte);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error al generar reporte anual: " + e.getMessage()));
        }
    }

    /**
     * Obtener estadísticas comparativas con mes anterior
     */
    @GetMapping("/ingresos/comparativo")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticasComparativas(
            @RequestParam Integer anio,
            @RequestParam Integer mes) {
        
        try {
            Map<String, Object> estadisticas = reportsService.obtenerEstadisticasComparativas(anio, mes);
            return ResponseEntity.ok(estadisticas);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error al generar estadísticas comparativas: " + e.getMessage()));
        }
    }

    /**
     * Obtener reporte del mes actual
     */
    @GetMapping("/ingresos/actual")
    public ResponseEntity<ReporteIngresosResponse> obtenerReporteActual() {
        try {
            LocalDate hoy = LocalDate.now();
            ReporteIngresosResponse reporte = reportsService.generarReporteMensual(
                hoy.getYear(), 
                hoy.getMonthValue()
            );
            return ResponseEntity.ok(reporte);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Health check del reports service
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        try {
            return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "reports-service",
                "timestamp", java.time.LocalDateTime.now(),
                "mensaje", "Reports Service funcionando correctamente"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("status", "DOWN", "error", e.getMessage()));
        }
    }

    /**
     * Reporte de ingresos por número de vueltas (compatible con frontend)
     */
    @GetMapping("/ingresos-por-vueltas")
    public ResponseEntity<Map<String, Object>> obtenerReporteIngresosPorVueltas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        
        try {
            System.out.println("📊 Generando reporte por vueltas desde " + fechaInicio + " hasta " + fechaFin);
            
            // ✅ LLAMAR AL MÉTODO DEL SERVICE
            Map<String, Object> reporte = reportsService.generarReporteIngresosPorVueltas(fechaInicio, fechaFin);
            
            System.out.println("✅ Reporte por vueltas generado exitosamente");
            return ResponseEntity.ok(reporte);
            
        } catch (Exception e) {
            System.err.println("❌ Error en reporte por vueltas: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error interno del servidor", "mensaje", e.getMessage()));
        }
    }

    /**
     * Reporte de ingresos por número de personas (compatible con frontend)
     */
    @GetMapping("/ingresos-por-personas")
    public ResponseEntity<Map<String, Object>> obtenerReporteIngresosPorPersonas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        
        try {
            System.out.println("📊 Generando reporte por personas desde " + fechaInicio + " hasta " + fechaFin);
            
            // ✅ LLAMAR AL MÉTODO DEL SERVICE
            Map<String, Object> reporte = reportsService.generarReporteIngresosPorPersonas(fechaInicio, fechaFin);
            
            System.out.println("✅ Reporte por personas generado exitosamente");
            return ResponseEntity.ok(reporte);
            
        } catch (Exception e) {
            System.err.println("❌ Error en reporte por personas: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error interno del servidor", "mensaje", e.getMessage()));
        }
    }
}