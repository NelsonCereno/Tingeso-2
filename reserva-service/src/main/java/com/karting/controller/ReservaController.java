package com.karting.controller;

import com.karting.client.ClienteClient;
import com.karting.dto.CalculoPrecioResponse;
import com.karting.dto.ReservaDto;
import com.karting.dto.ReservaRequest;
import com.karting.dto.ReservaResponse;
import com.karting.entity.ReservaEntity;
import com.karting.service.ComprobanteService;
import com.karting.service.EmailService;
import com.karting.service.ReservaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/reservas")

public class ReservaController {

    @Autowired
    private ReservaService reservaService;
    
    @Autowired
    private ComprobanteService comprobanteService;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private ClienteClient clienteClient;

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

    // NUEVO: Endpoint para rack-service (USANDO SERVICE)
    @GetMapping("/por-fechas")
    public ResponseEntity<List<ReservaResponse>> obtenerReservasPorFechas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        
        try {
            System.out.println("📅 Endpoint /por-fechas llamado: " + fechaInicio + " - " + fechaFin);
            
            LocalDateTime inicioDateTime = fechaInicio.atStartOfDay();
            LocalDateTime finDateTime = fechaFin.atTime(23, 59, 59);
            
            // ✅ USAR EL SERVICE
            List<ReservaEntity> reservas = reservaService.findReservasEnRangoFecha(inicioDateTime, finDateTime);
            
            List<ReservaResponse> reservasResponse = reservas.stream()
                .map(ReservaResponse::new)
                .collect(Collectors.toList());
            
            System.out.println("✅ Retornando " + reservasResponse.size() + " reservas");
            return ResponseEntity.ok(reservasResponse);
            
        } catch (Exception e) {
            System.err.println("❌ Error en endpoint por-fechas: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ArrayList<>());
        }
    }

    // Agregar después de la línea 150:

    @GetMapping("/entre-fechas")
    public ResponseEntity<List<ReservaDto>> obtenerReservasEntreFechas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        
        try {
            System.out.println("📊 Obteniendo reservas entre " + fechaInicio + " y " + fechaFin + " para reports-service");
            
            List<ReservaResponse> reservas = reservaService.obtenerReservasEntreFechas(fechaInicio, fechaFin);
            
            // ✅ CONVERTIR ReservaResponse a ReservaDto
            List<ReservaDto> reservasDto = reservas.stream()
                .map(this::convertirAReservaDto)
                .collect(Collectors.toList());
            
            System.out.println("✅ Encontradas " + reservasDto.size() + " reservas convertidas a DTO");
            return ResponseEntity.ok(reservasDto);
            
        } catch (Exception e) {
            System.err.println("❌ Error al obtener reservas entre fechas: " + e.getMessage());
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

    // ================================
    // GENERACIÓN DE COMPROBANTES
    // ================================

    // Generar comprobante PDF básico
    @GetMapping("/{id}/comprobante")
    public ResponseEntity<byte[]> generarComprobante(@PathVariable Long id) {
        try {
            // Obtener la reserva
            ReservaResponse reservaResponse = reservaService.obtenerReservaPorId(id);
            
            // Convertir a entidad para el comprobante
            ReservaEntity reserva = convertirResponseAEntity(reservaResponse);
            
            // Generar PDF
            byte[] comprobantePdf = comprobanteService.generarComprobante(reserva);
            
            // Configurar headers para descarga
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "comprobante_reserva_" + id + ".pdf");
            headers.setContentLength(comprobantePdf.length);
            
            return new ResponseEntity<>(comprobantePdf, headers,  HttpStatus.OK);
            
        } catch (Exception e) {
            System.err.println("❌ Error al generar comprobante: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // Generar comprobante PDF detallado (con información por cliente)
    @GetMapping("/{id}/comprobante-detallado")
    public ResponseEntity<byte[]> generarComprobanteDetallado(@PathVariable Long id) {
        try {
            // Obtener la reserva
            ReservaResponse reservaResponse = reservaService.obtenerReservaPorId(id);
            ReservaEntity reserva = convertirResponseAEntity(reservaResponse);
            
            // Recalcular precios individuales para el detalle
            ReservaRequest request = convertirEntityARequest(reserva);
            CalculoPrecioResponse calculoPrecio = reservaService.calcularPrecioCompleto(request);
            
            // Generar PDF detallado
            byte[] comprobantePdf = comprobanteService.generarComprobanteDetallado(
                reserva, 
                calculoPrecio.getPreciosIndividuales()
            );
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "comprobante_detallado_" + id + ".pdf");
            headers.setContentLength(comprobantePdf.length);
            
            return new ResponseEntity<>(comprobantePdf, headers, HttpStatus.OK);
            
        } catch (Exception e) {
            System.err.println("❌ Error al generar comprobante detallado: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // ================================
    // ENVÍO DE COMPROBANTES POR EMAIL
    // ================================

    // Endpoint compatible con el monolítico (MEJORADO)
    @PostMapping("/{id}/enviar-comprobante")
    public ResponseEntity<Map<String, Object>> enviarComprobante(@PathVariable Long id) {
        try {
            System.out.println("🚀 Iniciando envío de comprobante para reserva #" + id);
            
            // 1. Obtener la reserva
            ReservaResponse reservaResponse = reservaService.obtenerReservaPorId(id);
            if (reservaResponse == null) {
                return ResponseEntity.notFound().build();
            }

            // 2. Obtener emails de los clientes
            List<String> emailsClientes = obtenerEmailsDeClientes(reservaResponse.getClientesIds());
            if (emailsClientes.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "No se encontraron emails válidos para los clientes de la reserva"));
            }

            // 3. Generar comprobante PDF
            ReservaEntity reserva = convertirResponseAEntity(reservaResponse);
            byte[] comprobantePdf = comprobanteService.generarComprobante(reserva);
            System.out.println("✅ Comprobante PDF generado: " + comprobantePdf.length + " bytes");

            // 4. Enviar emails
            List<String> correosEnviados = emailService.enviarComprobanteMultiple(
                emailsClientes, 
                comprobantePdf, 
                id
            );

            // 5. Marcar como enviado en la base de datos
            if (!correosEnviados.isEmpty()) {
                reservaService.marcarEmailEnviado(id);
            }

            // 6. Preparar respuesta
            Map<String, Object> response = Map.of(
                "mensaje", "Comprobante procesado",
                "reservaId", id,
                "correosEnviados", correosEnviados,
                "totalEnviados", correosEnviados.size(),
                "totalClientes", emailsClientes.size(),
                "tamanoArchivo", comprobantePdf.length + " bytes"
            );

            System.out.println("✅ Envío completado: " + correosEnviados.size() + "/" + emailsClientes.size());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ Error al enviar comprobante: " + e.getMessage());
            e.printStackTrace();
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "error", "Error al enviar comprobante: " + e.getMessage(),
                    "reservaId", id
                ));
        }
    }

    // Enviar a email específico
    @PostMapping("/{id}/enviar-comprobante/{email}")
    public ResponseEntity<Map<String, Object>> enviarComprobanteAEmail(
            @PathVariable Long id, 
            @PathVariable String email) {
        try {
            // Validar email
            if (!emailService.validarEmail(email)) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Email inválido: " + email));
            }

            // Obtener reserva y generar comprobante
            ReservaResponse reservaResponse = reservaService.obtenerReservaPorId(id);
            ReservaEntity reserva = convertirResponseAEntity(reservaResponse);
            byte[] comprobantePdf = comprobanteService.generarComprobante(reserva);

            // Enviar email
            emailService.enviarComprobante(email, comprobantePdf, id);

            Map<String, Object> response = Map.of(
                "mensaje", "Comprobante enviado exitosamente",
                "reservaId", id,
                "emailEnviado", email,
                "tamanoArchivo", comprobantePdf.length + " bytes"
            );

            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error al enviar a " + email + ": " + e.getMessage()));
        }
    }

    // ================================
    // ENDPOINTS DE VALIDACIÓN (útiles para frontend)
    // ================================

    // Agregar este endpoint temporal para testing
    @GetMapping("/test-email")
    public ResponseEntity<Map<String, Object>> testEmail() {
        try {
            boolean emailDisponible = emailService.isEmailDisponible();
            boolean conexionOk = emailService.testConexion();
            
            Map<String, Object> response = Map.of(
                "emailDisponible", emailDisponible,
                "conexionOk", conexionOk,
                "mensaje", emailDisponible ? "Email configurado correctamente" : "Email no configurado"
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Map.of("error", "Error al probar email: " + e.getMessage()));
        }
    }

    // ================================
    // MÉTODOS AUXILIARES
    // ================================

    private ReservaEntity convertirResponseAEntity(ReservaResponse response) {
        ReservaEntity entity = new ReservaEntity();
        entity.setId(response.getId());
        entity.setFechaHora(response.getFechaHora());
        entity.setDuracionMinutos(response.getDuracionMinutos());
        entity.setNumeroPersonas(response.getNumeroPersonas());
        entity.setClientesIds(response.getClientesIds());
        entity.setKartsIds(response.getKartsIds());
        entity.setPrecioBase(response.getPrecioBase());
        entity.setDescuentoPersonas(response.getDescuentoPersonas());
        entity.setDescuentoClientes(response.getDescuentoClientes());
        entity.setDescuentoCumpleanos(response.getDescuentoCumpleanos());
        entity.calcularPrecioTotal(); // Calcula el precio total
        entity.setEstado(response.getEstado());
        entity.setObservaciones(response.getObservaciones());
        return entity;
    }

    private ReservaRequest convertirEntityARequest(ReservaEntity entity) {
        ReservaRequest request = new ReservaRequest();
        request.setFechaHora(entity.getFechaHora());
        request.setDuracionMinutos(entity.getDuracionMinutos());
        request.setNumeroPersonas(entity.getNumeroPersonas());
        request.setClientesIds(entity.getClientesIds());
        request.setKartsIds(entity.getKartsIds());
        request.setObservaciones(entity.getObservaciones());
        return request;
    }

    private List<String> obtenerEmailsDeClientes(List<Long> clientesIds) {
        List<String> emails = new ArrayList<>();
        
        for (Long clienteId : clientesIds) {
            try {
                // Llamar al cliente-service para obtener el email
                ResponseEntity<Object> response = clienteClient.obtenerClientePorId(clienteId);
                
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    // Parsear la respuesta del cliente-service
                    Map<String, Object> clienteData = (Map<String, Object>) response.getBody();
                    String email = (String) clienteData.get("email");
                    
                    if (email != null && emailService.validarEmail(email)) {
                        emails.add(email);
                        System.out.println("✅ Email obtenido para cliente " + clienteId + ": " + email);
                    } else {
                        System.out.println("⚠️ Email inválido para cliente " + clienteId + ": " + email);
                    }
                }
            } catch (Exception e) {
                System.err.println("❌ Error al obtener email del cliente " + clienteId + ": " + e.getMessage());
            }
        }
        
        return emails;
    }

    // ✅ MÉTODO HELPER: Convertir ReservaResponse a ReservaDto
    private ReservaDto convertirAReservaDto(ReservaResponse reservaResponse) {
        ReservaDto dto = new ReservaDto();
        dto.setId(reservaResponse.getId());
        dto.setFechaHora(reservaResponse.getFechaHora());
        dto.setDuracionMinutos(reservaResponse.getDuracionMinutos());
        dto.setNumeroPersonas(reservaResponse.getNumeroPersonas());
        dto.setClientesIds(reservaResponse.getClientesIds());
        dto.setKartsIds(reservaResponse.getKartsIds());
        dto.setPrecioBase(reservaResponse.getPrecioBase());
        dto.setDescuentoPersonas(reservaResponse.getDescuentoPersonas());
        dto.setDescuentoClientes(reservaResponse.getDescuentoClientes());
        dto.setDescuentoCumpleanos(reservaResponse.getDescuentoCumpleanos());
        dto.setDescuentoTotal(reservaResponse.getDescuentoTotal());
        dto.setPrecioTotal(reservaResponse.getPrecioTotal());
        dto.setEstado(reservaResponse.getEstado() != null ? reservaResponse.getEstado().toString() : "PENDIENTE");
        dto.setObservaciones(reservaResponse.getObservaciones());
        dto.setFechaCreacion(reservaResponse.getFechaCreacion());
        dto.setFechaActualizacion(reservaResponse.getFechaActualizacion());
        return dto;
    }
}
