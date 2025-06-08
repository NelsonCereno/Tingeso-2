package com.karting.controller;

import com.karting.dto.CalculoPrecioResponse;
import com.karting.dto.ReservaRequest;
import com.karting.dto.ReservaResponse;
import com.karting.entity.ReservaEntity;
import com.karting.service.ReservaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/reservas")
@CrossOrigin("*")
public class ReservaController {

    @Autowired
    private ReservaService reservaService;

    // Health check del servicio orquestador
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Reserva Service (Orquestador) is running! 🎭");
    }

    // ================================
    // ENDPOINTS PRINCIPALES - ORQUESTACIÓN
    // ================================

    // CREAR RESERVA COMPLETA (Orquestación de todos los microservicios)
    @PostMapping
    public ResponseEntity<ReservaResponse> crearReserva(@RequestBody ReservaRequest request) {
        try {
            if (request.getFechaHora() == null || request.getDuracionMinutos() == null || 
                request.getNumeroPersonas() == null || request.getClientesIds() == null || 
                request.getClientesIds().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            
            ReservaResponse response = reservaService.crearReserva(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            System.err.println("❌ Error al crear reserva: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            System.err.println("❌ Error interno: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // CALCULAR PRECIO COMPLETO (Sin crear reserva)
    @PostMapping("/calcular-precio")
    public ResponseEntity<CalculoPrecioResponse> calcularPrecio(@RequestBody ReservaRequest request) {
        try {
            if (request.getDuracionMinutos() == null || request.getNumeroPersonas() == null || 
                request.getClientesIds() == null || request.getClientesIds().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            
            CalculoPrecioResponse response = reservaService.calcularPrecioCompleto(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            System.err.println("❌ Error al calcular precio: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // VERIFICAR DISPONIBILIDAD PARA UNA FECHA/HORA
    @GetMapping("/verificar-disponibilidad")
    public ResponseEntity<Boolean> verificarDisponibilidad(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaHora,
            @RequestParam Integer duracionMinutos,
            @RequestParam Integer numeroPersonas) {
        
        try {
            if (fechaHora == null || duracionMinutos <= 0 || numeroPersonas <= 0) {
                return ResponseEntity.badRequest().build();
            }
            
            boolean disponible = reservaService.verificarDisponibilidadFecha(fechaHora, duracionMinutos, numeroPersonas);
            return ResponseEntity.ok(disponible);
        } catch (Exception e) {
            System.err.println("❌ Error al verificar disponibilidad: " + e.getMessage());
            return ResponseEntity.ok(false);
        }
    }

    // ================================
    // CRUD BÁSICO DE RESERVAS
    // ================================

    // Obtener todas las reservas
    @GetMapping
    public ResponseEntity<List<ReservaResponse>> obtenerTodasLasReservas() {
        try {
            List<ReservaResponse> reservas = reservaService.obtenerTodasLasReservas();
            if (reservas.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(reservas);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Obtener reserva por ID
    @GetMapping("/{id}")
    public ResponseEntity<ReservaResponse> obtenerReservaPorId(@PathVariable Long id) {
        try {
            ReservaResponse response = reservaService.obtenerReservaPorId(id);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ================================
    // GESTIÓN DE ESTADOS
    // ================================

    // Cancelar reserva
    @PutMapping("/{id}/cancelar")
    public ResponseEntity<ReservaResponse> cancelarReserva(@PathVariable Long id, @RequestParam String motivo) {
        try {
            if (motivo == null || motivo.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            
            ReservaResponse response = reservaService.cancelarReserva(id, motivo);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            System.err.println("❌ Error al cancelar reserva: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Completar reserva
    @PutMapping("/{id}/completar")
    public ResponseEntity<ReservaResponse> completarReserva(@PathVariable Long id) {
        try {
            ReservaResponse response = reservaService.completarReserva(id);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            System.err.println("❌ Error al completar reserva: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ================================
    // CONSULTAS POR ESTADO
    // ================================

    // Obtener reservas por estado
    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<ReservaResponse>> obtenerReservasPorEstado(@PathVariable ReservaEntity.EstadoReserva estado) {
        try {
            List<ReservaResponse> reservas = reservaService.obtenerReservasPorEstado(estado);
            if (reservas.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(reservas);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Obtener reservas activas (no canceladas ni completadas)
    @GetMapping("/activas")
    public ResponseEntity<List<ReservaResponse>> obtenerReservasActivas() {
        try {
            List<ReservaResponse> reservas = reservaService.obtenerReservasActivas();
            if (reservas.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(reservas);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Obtener reservas pendientes
    @GetMapping("/pendientes")
    public ResponseEntity<List<ReservaResponse>> obtenerReservasPendientes() {
        try {
            List<ReservaResponse> reservas = reservaService.obtenerReservasPorEstado(ReservaEntity.EstadoReserva.PENDIENTE);
            if (reservas.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(reservas);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Obtener reservas confirmadas
    @GetMapping("/confirmadas")
    public ResponseEntity<List<ReservaResponse>> obtenerReservasConfirmadas() {
        try {
            List<ReservaResponse> reservas = reservaService.obtenerReservasPorEstado(ReservaEntity.EstadoReserva.CONFIRMADA);
            if (reservas.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(reservas);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Obtener reservas en proceso
    @GetMapping("/en-proceso")
    public ResponseEntity<List<ReservaResponse>> obtenerReservasEnProceso() {
        try {
            List<ReservaResponse> reservas = reservaService.obtenerReservasPorEstado(ReservaEntity.EstadoReserva.EN_PROCESO);
            if (reservas.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(reservas);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Obtener reservas completadas
    @GetMapping("/completadas")
    public ResponseEntity<List<ReservaResponse>> obtenerReservasCompletadas() {
        try {
            List<ReservaResponse> reservas = reservaService.obtenerReservasPorEstado(ReservaEntity.EstadoReserva.COMPLETADA);
            if (reservas.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(reservas);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Obtener reservas canceladas
    @GetMapping("/canceladas")
    public ResponseEntity<List<ReservaResponse>> obtenerReservasCanceladas() {
        try {
            List<ReservaResponse> reservas = reservaService.obtenerReservasPorEstado(ReservaEntity.EstadoReserva.CANCELADA);
            if (reservas.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(reservas);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ================================
    // CONSULTAS POR FECHA
    // ================================

    // Obtener reservas del día actual
    @GetMapping("/hoy")
    public ResponseEntity<List<ReservaResponse>> obtenerReservasDelDia() {
        try {
            List<ReservaResponse> reservas = reservaService.obtenerReservasDelDia();
            if (reservas.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(reservas);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ================================
    // ESTADÍSTICAS Y REPORTES
    // ================================

    // Obtener estadísticas generales de reservas
    @GetMapping("/estadisticas")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticasReservas() {
        try {
            Map<String, Object> estadisticas = reservaService.obtenerEstadisticasReservas();
            return ResponseEntity.ok(estadisticas);
        } catch (Exception e) {
            System.err.println("❌ Error al obtener estadísticas: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // ================================
    // ENDPOINTS DE VALIDACIÓN (útiles para frontend)
    // ================================

    // Validar datos de reserva sin crearla
    @PostMapping("/validar")
    public ResponseEntity<Map<String, Object>> validarReserva(@RequestBody ReservaRequest request) {
        try {
            // Validaciones básicas
            if (request.getFechaHora() == null || request.getDuracionMinutos() == null || 
                request.getNumeroPersonas() == null || request.getClientesIds() == null || 
                request.getClientesIds().isEmpty()) {
                return ResponseEntity.ok(Map.of(
                    "valida", false,
                    "mensaje", "Faltan datos obligatorios"
                ));
            }

            if (request.getFechaHora().isBefore(LocalDateTime.now())) {
                return ResponseEntity.ok(Map.of(
                    "valida", false,
                    "mensaje", "No se pueden crear reservas en el pasado"
                ));
            }

            // Verificar disponibilidad
            boolean disponible = reservaService.verificarDisponibilidadFecha(
                request.getFechaHora(), 
                request.getDuracionMinutos(), 
                request.getNumeroPersonas()
            );

            if (!disponible) {
                return ResponseEntity.ok(Map.of(
                    "valida", false,
                    "mensaje", "No hay suficientes karts disponibles para la fecha solicitada"
                ));
            }

            // Calcular precio estimado
            CalculoPrecioResponse precio = reservaService.calcularPrecioCompleto(request);

            return ResponseEntity.ok(Map.of(
                "valida", true,
                "mensaje", "Reserva válida",
                "precioEstimado", precio
            ));

        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                "valida", false,
                "mensaje", "Error al validar reserva: " + e.getMessage()
            ));
        }
    }

    // ================================
    // ENDPOINTS DE AYUDA Y CONFIGURACIÓN
    // ================================

    // Obtener horarios disponibles para una fecha
    @GetMapping("/horarios-disponibles")
    public ResponseEntity<List<String>> obtenerHorariosDisponibles(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) String fecha,
            @RequestParam Integer duracionMinutos,
            @RequestParam Integer numeroPersonas) {
        
        try {
            // Generar horarios cada hora de 8:00 a 20:00
            List<String> horariosDisponibles = java.util.stream.IntStream.range(8, 21)
                .mapToObj(hora -> {
                    LocalDateTime fechaHora = LocalDateTime.parse(fecha + "T" + String.format("%02d:00:00", hora));
                    boolean disponible = reservaService.verificarDisponibilidadFecha(fechaHora, duracionMinutos, numeroPersonas);
                    return disponible ? String.format("%02d:00", hora) : null;
                })
                .filter(java.util.Objects::nonNull)
                .collect(java.util.stream.Collectors.toList());

            if (horariosDisponibles.isEmpty()) {
                return ResponseEntity.noContent().build();
            }

            return ResponseEntity.ok(horariosDisponibles);
        } catch (Exception e) {
            System.err.println("❌ Error al obtener horarios: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // Obtener configuración para frontend
    @GetMapping("/configuracion")
    public ResponseEntity<Map<String, Object>> obtenerConfiguracion() {
        return ResponseEntity.ok(Map.of(
            "duracionesDisponibles", List.of(30, 60, 90, 120, 180),
            "numeroMaximoPersonas", 20,
            "horarioApertura", "08:00",
            "horarioCierre", "20:00",
            "estados", List.of(
                Map.of("codigo", "PENDIENTE", "descripcion", "Pendiente de confirmación"),
                Map.of("codigo", "CONFIRMADA", "descripcion", "Confirmada y lista"),
                Map.of("codigo", "EN_PROCESO", "descripcion", "En proceso - Karts asignados"),
                Map.of("codigo", "COMPLETADA", "descripcion", "Completada exitosamente"),
                Map.of("codigo", "CANCELADA", "descripcion", "Cancelada")
            )
        ));
    }

    // ================================
    // ENDPOINTS DE MONITOREO
    // ================================

    // Estado del sistema (conexiones con microservicios)
    @GetMapping("/estado-sistema")
    public ResponseEntity<Map<String, Object>> estadoSistema() {
        return ResponseEntity.ok(Map.of(
            "servicio", "reserva-service",
            "estado", "ACTIVO",
            "version", "1.0.0",
            "timestamp", LocalDateTime.now(),
            "descripcion", "Servicio orquestador funcionando correctamente",
            "microserviciosConectados", List.of(
                "tarifa-service",
                "descuento-personas-service", 
                "descuento-clientes-service",
                "descuento-cumpleanos-service",
                "cliente-service",
                "kart-service"
            )
        ));
    }
}
