package com.karting.service;

import com.karting.client.*;
import com.karting.dto.CalculoPrecioResponse;
import com.karting.dto.ReservaRequest;
import com.karting.dto.ReservaResponse;
import com.karting.entity.ReservaEntity;
import com.karting.repository.ReservaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ReservaService {

    @Autowired
    private ReservaRepository reservaRepository;

    // Feign Clients para comunicación con microservicios
    @Autowired
    private TarifaClient tarifaClient;

    @Autowired
    private DescuentoPersonasClient descuentoPersonasClient;

    @Autowired
    private DescuentoClientesClient descuentoClientesClient;

    @Autowired
    private DescuentoCumpleanosClient descuentoCumpleanosClient;

    @Autowired
    private ClienteClient clienteClient;

    @Autowired
    private KartClient kartClient;

    // MÉTODO PRINCIPAL: Crear reserva completa (Orquestación)
    @Transactional
    public ReservaResponse crearReserva(ReservaRequest request) {
        try {
            // 1. Validaciones iniciales
            validarReservaRequest(request);

            // 2. Verificar que todos los clientes existen
            List<Long> clientesExistentes = verificarExistenciaClientes(request.getClientesIds());
            if (clientesExistentes.size() != request.getClientesIds().size()) {
                throw new RuntimeException("Algunos clientes no existen en el sistema");
            }

            // 3. Asignar karts (automático o específico)
            List<Long> kartsAsignados = asignarKarts(request);

            // 4. Verificar disponibilidad de karts en el horario solicitado
            verificarDisponibilidadHorario(kartsAsignados, request.getFechaHora(), request.getDuracionMinutos());

            // 5. Calcular precio completo con todos los descuentos
            CalculoPrecioResponse calculoPrecio = calcularPrecioCompleto(request);

            // 6. Crear la entidad reserva
            ReservaEntity reserva = new ReservaEntity(
                request.getFechaHora(),
                request.getDuracionMinutos(),
                request.getNumeroPersonas(),
                request.getClientesIds(),
                kartsAsignados
            );

            // 7. Asignar precios y descuentos calculados
            reserva.setPrecioBase(calculoPrecio.getPrecioBase());
            reserva.setDescuentoPersonas(calculoPrecio.getDescuentoPersonas());
            reserva.setDescuentoClientes(calculoPrecio.getDescuentoClientes());
            reserva.setDescuentoCumpleanos(calculoPrecio.getDescuentoCumpleanos());
            reserva.calcularPrecioTotal();
            reserva.setObservaciones(request.getObservaciones());

            // 8. Guardar reserva en estado PENDIENTE
            ReservaEntity reservaGuardada = reservaRepository.save(reserva);

            // 9. Reservar los karts en el kart-service
            reservarKartsEnServicio(kartsAsignados);

            // 10. Incrementar visitas de los clientes
            incrementarVisitasClientes(request.getClientesIds());

            // 11. Confirmar la reserva
            reservaGuardada.confirmar();
            reservaGuardada = reservaRepository.save(reservaGuardada);

            System.out.println("✅ Reserva creada exitosamente - ID: " + reservaGuardada.getId());
            return new ReservaResponse(reservaGuardada);

        } catch (Exception e) {
            System.err.println("❌ Error al crear reserva: " + e.getMessage());
            throw new RuntimeException("Error al crear reserva: " + e.getMessage(), e);
        }
    }

    // CÁLCULO COMPLETO DE PRECIOS (Orquestación de descuentos)
    public CalculoPrecioResponse calcularPrecioCompleto(ReservaRequest request) {
        try {
            // 1. Obtener precio base desde tarifa-service
            ResponseEntity<Double> tarifaResponse = tarifaClient.calcularTarifa(request.getDuracionMinutos());
            Double precioBase = tarifaResponse.getBody();
            if (precioBase == null) {
                throw new RuntimeException("No se pudo obtener la tarifa base");
            }

            // 2. Calcular descuento por número de personas
            Double descuentoPersonas = 0.0;
            try {
                ResponseEntity<Double> descPersonasResponse = descuentoPersonasClient.calcularDescuento(
                    request.getNumeroPersonas(), precioBase);
                descuentoPersonas = descPersonasResponse.getBody() != null ? descPersonasResponse.getBody() : 0.0;
            } catch (Exception e) {
                System.err.println("⚠️ Error al calcular descuento personas: " + e.getMessage());
            }

            // 3. Calcular descuento por clientes frecuentes (basado en el cliente con más visitas)
            Double descuentoClientes = 0.0;
            try {
                Integer maxVisitas = obtenerMaximoVisitasClientes(request.getClientesIds());
                if (maxVisitas > 0) {
                    ResponseEntity<Double> descClientesResponse = descuentoClientesClient.calcularDescuento(
                        maxVisitas, precioBase);
                    descuentoClientes = descClientesResponse.getBody() != null ? descClientesResponse.getBody() : 0.0;
                }
            } catch (Exception e) {
                System.err.println("⚠️ Error al calcular descuento clientes: " + e.getMessage());
            }

            // 4. Calcular descuento por cumpleaños (si algún cliente cumple años)
            Double descuentoCumpleanos = 0.0;
            try {
                if (verificarSiHayCumpleanos(request.getClientesIds())) {
                    ResponseEntity<Double> descCumpleanosResponse = descuentoCumpleanosClient.calcularDescuento(precioBase);
                    descuentoCumpleanos = descCumpleanosResponse.getBody() != null ? descCumpleanosResponse.getBody() : 0.0;
                }
            } catch (Exception e) {
                System.err.println("⚠️ Error al calcular descuento cumpleaños: " + e.getMessage());
            }

            // 5. Calcular precio final
            Double descuentoTotal = descuentoPersonas + descuentoClientes + descuentoCumpleanos;
            Double precioFinal = precioBase - descuentoTotal;
            if (precioFinal < 0) {
                precioFinal = 0.0;
            }

            return new CalculoPrecioResponse(precioBase, descuentoPersonas, descuentoClientes, 
                                           descuentoCumpleanos, precioFinal);

        } catch (Exception e) {
            throw new RuntimeException("Error al calcular precio: " + e.getMessage(), e);
        }
    }

    // ASIGNACIÓN INTELIGENTE DE KARTS
    private List<Long> asignarKarts(ReservaRequest request) {
        try {
            // Si se especificaron karts, verificar su disponibilidad
            if (request.getKartsIds() != null && !request.getKartsIds().isEmpty()) {
                // Verificar que hay suficientes karts especificados
                if (request.getKartsIds().size() < request.getNumeroPersonas()) {
                    throw new RuntimeException("No hay suficientes karts especificados para " + request.getNumeroPersonas() + " personas");
                }

                // Verificar disponibilidad de los karts específicos
                ResponseEntity<Boolean> disponibilidadResponse = kartClient.kartsEstanDisponibles(request.getKartsIds());
                if (disponibilidadResponse.getBody() != null && disponibilidadResponse.getBody()) {
                    return request.getKartsIds().subList(0, request.getNumeroPersonas());
                } else {
                    throw new RuntimeException("Algunos de los karts especificados no están disponibles");
                }
            }

            // Asignación automática: obtener karts disponibles optimizados
            ResponseEntity<List<Map<String, Object>>> kartsResponse = kartClient.obtenerKartsDisponiblesOptimizados(request.getNumeroPersonas());
            
            if (kartsResponse.getBody() == null || kartsResponse.getBody().size() < request.getNumeroPersonas()) {
                throw new RuntimeException("No hay suficientes karts disponibles para " + request.getNumeroPersonas() + " personas");
            }

            // Extraer IDs de los karts asignados
            return kartsResponse.getBody().stream()
                    .map(kart -> Long.valueOf(kart.get("id").toString()))
                    .limit(request.getNumeroPersonas())
                    .collect(Collectors.toList());

        } catch (Exception e) {
            throw new RuntimeException("Error al asignar karts: " + e.getMessage(), e);
        }
    }

    // VERIFICACIÓN DE EXISTENCIA DE CLIENTES
    private List<Long> verificarExistenciaClientes(List<Long> clientesIds) {
        try {
            ResponseEntity<List<Long>> response = clienteClient.verificarExistenciaClientes(clientesIds);
            return response.getBody() != null ? response.getBody() : new ArrayList<>();
        } catch (Exception e) {
            throw new RuntimeException("Error al verificar existencia de clientes: " + e.getMessage(), e);
        }
    }

    // INCREMENTAR VISITAS DE CLIENTES
    private void incrementarVisitasClientes(List<Long> clientesIds) {
        try {
            clienteClient.incrementarVisitas(clientesIds);
            System.out.println("✅ Visitas incrementadas para clientes: " + clientesIds);
        } catch (Exception e) {
            System.err.println("⚠️ Error al incrementar visitas: " + e.getMessage());
            // No lanzar excepción para no afectar la reserva
        }
    }

    // RESERVAR KARTS EN EL SERVICIO
    private void reservarKartsEnServicio(List<Long> kartsIds) {
        try {
            ResponseEntity<List<Map<String, Object>>> response = kartClient.reservarKarts(kartsIds);
            if (response.getBody() == null || response.getBody().size() != kartsIds.size()) {
                throw new RuntimeException("Error al reservar karts en el servicio");
            }
            System.out.println("✅ Karts reservados en el servicio: " + kartsIds);
        } catch (Exception e) {
            throw new RuntimeException("Error al reservar karts: " + e.getMessage(), e);
        }
    }

    // OBTENER MÁXIMO NÚMERO DE VISITAS DE LOS CLIENTES
    private Integer obtenerMaximoVisitasClientes(List<Long> clientesIds) {
        try {
            Integer maxVisitas = 0;
            for (Long clienteId : clientesIds) {
                ResponseEntity<Integer> visitasResponse = clienteClient.obtenerNumeroVisitas(clienteId);
                Integer visitas = visitasResponse.getBody() != null ? visitasResponse.getBody() : 0;
                if (visitas > maxVisitas) {
                    maxVisitas = visitas;
                }
            }
            return maxVisitas;
        } catch (Exception e) {
            System.err.println("⚠️ Error al obtener visitas de clientes: " + e.getMessage());
            return 0;
        }
    }

    // VERIFICAR SI HAY CLIENTES DE CUMPLEAÑOS
    private boolean verificarSiHayCumpleanos(List<Long> clientesIds) {
        try {
            ResponseEntity<List<Long>> response = clienteClient.verificarClientesCumpleanos(clientesIds);
            List<Long> clientesCumpleanos = response.getBody();
            return clientesCumpleanos != null && !clientesCumpleanos.isEmpty();
        } catch (Exception e) {
            System.err.println("⚠️ Error al verificar cumpleaños: " + e.getMessage());
            return false;
        }
    }

    // VERIFICAR DISPONIBILIDAD DE HORARIO
    private void verificarDisponibilidadHorario(List<Long> kartsIds, LocalDateTime fechaInicio, Integer duracionMinutos) {
        LocalDateTime fechaFin = fechaInicio.plusMinutes(duracionMinutos);
        
        List<ReservaEntity> conflictos = reservaRepository.findConflictosHorarios(fechaInicio, fechaFin, kartsIds);
        if (!conflictos.isEmpty()) {
            throw new RuntimeException("Algunos karts ya están reservados en el horario solicitado");
        }
    }

    // VALIDACIONES INICIALES
    private void validarReservaRequest(ReservaRequest request) {
        if (request.getFechaHora() == null) {
            throw new RuntimeException("La fecha y hora son obligatorias");
        }
        
        if (request.getFechaHora().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("No se pueden crear reservas en el pasado");
        }
        
        if (request.getDuracionMinutos() == null || request.getDuracionMinutos() <= 0) {
            throw new RuntimeException("La duración debe ser mayor a 0 minutos");
        }
        
        if (request.getNumeroPersonas() == null || request.getNumeroPersonas() <= 0) {
            throw new RuntimeException("El número de personas debe ser mayor a 0");
        }
        
        if (request.getClientesIds() == null || request.getClientesIds().isEmpty()) {
            throw new RuntimeException("Debe especificar al menos un cliente");
        }
        
        if (request.getClientesIds().size() > request.getNumeroPersonas()) {
            throw new RuntimeException("No puede haber más clientes que personas en la reserva");
        }
    }

    // CRUD BÁSICO DE RESERVAS

    // Obtener todas las reservas
    public List<ReservaResponse> obtenerTodasLasReservas() {
        List<ReservaEntity> reservas = reservaRepository.findAll();
        return reservas.stream()
                .map(ReservaResponse::new)
                .collect(Collectors.toList());
    }

    // Obtener reserva por ID
    public ReservaResponse obtenerReservaPorId(Long id) {
        Optional<ReservaEntity> reservaOpt = reservaRepository.findById(id);
        if (reservaOpt.isPresent()) {
            return new ReservaResponse(reservaOpt.get());
        }
        throw new RuntimeException("Reserva no encontrada con ID: " + id);
    }

    // Obtener reservas por estado
    public List<ReservaResponse> obtenerReservasPorEstado(ReservaEntity.EstadoReserva estado) {
        List<ReservaEntity> reservas = reservaRepository.findByEstado(estado);
        return reservas.stream()
                .map(ReservaResponse::new)
                .collect(Collectors.toList());
    }

    // Obtener reservas activas
    public List<ReservaResponse> obtenerReservasActivas() {
        List<ReservaEntity> reservas = reservaRepository.findReservasActivas();
        return reservas.stream()
                .map(ReservaResponse::new)
                .collect(Collectors.toList());
    }

    // Obtener reservas del día
    public List<ReservaResponse> obtenerReservasDelDia() {
        List<ReservaEntity> reservas = reservaRepository.findReservasDelDia();
        return reservas.stream()
                .map(ReservaResponse::new)
                .collect(Collectors.toList());
    }

    // GESTIÓN DE ESTADOS DE RESERVAS

    // Cancelar reserva
    @Transactional
    public ReservaResponse cancelarReserva(Long id, String motivo) {
        try {
            Optional<ReservaEntity> reservaOpt = reservaRepository.findById(id);
            if (!reservaOpt.isPresent()) {
                throw new RuntimeException("Reserva no encontrada con ID: " + id);
            }

            ReservaEntity reserva = reservaOpt.get();
            
            if (!reserva.puedeSerCancelada()) {
                throw new RuntimeException("Esta reserva no puede ser cancelada en su estado actual: " + reserva.getEstado());
            }

            // Liberar karts si estaban reservados
            if (reserva.getKartsIds() != null && !reserva.getKartsIds().isEmpty()) {
                try {
                    kartClient.liberarKarts(reserva.getKartsIds());
                    System.out.println("✅ Karts liberados: " + reserva.getKartsIds());
                } catch (Exception e) {
                    System.err.println("⚠️ Error al liberar karts: " + e.getMessage());
                }
            }

            // Cancelar la reserva
            reserva.cancelar(motivo);
            ReservaEntity reservaCancelada = reservaRepository.save(reserva);

            System.out.println("✅ Reserva cancelada - ID: " + id);
            return new ReservaResponse(reservaCancelada);

        } catch (Exception e) {
            throw new RuntimeException("Error al cancelar reserva: " + e.getMessage(), e);
        }
    }

    // Completar reserva
    @Transactional
    public ReservaResponse completarReserva(Long id) {
        try {
            Optional<ReservaEntity> reservaOpt = reservaRepository.findById(id);
            if (!reservaOpt.isPresent()) {
                throw new RuntimeException("Reserva no encontrada con ID: " + id);
            }

            ReservaEntity reserva = reservaOpt.get();

            // Liberar karts
            if (reserva.getKartsIds() != null && !reserva.getKartsIds().isEmpty()) {
                try {
                    kartClient.liberarKarts(reserva.getKartsIds());
                    System.out.println("✅ Karts liberados después de completar reserva: " + reserva.getKartsIds());
                } catch (Exception e) {
                    System.err.println("⚠️ Error al liberar karts: " + e.getMessage());
                }
            }

            // Completar la reserva
            reserva.completar();
            ReservaEntity reservaCompletada = reservaRepository.save(reserva);

            System.out.println("✅ Reserva completada - ID: " + id);
            return new ReservaResponse(reservaCompletada);

        } catch (Exception e) {
            throw new RuntimeException("Error al completar reserva: " + e.getMessage(), e);
        }
    }

    // MÉTODOS DE CONSULTA Y ESTADÍSTICAS

    // Verificar disponibilidad de karts para una fecha
    public boolean verificarDisponibilidadFecha(LocalDateTime fechaHora, Integer duracionMinutos, Integer numeroPersonas) {
        try {
            // Verificar capacidad general
            ResponseEntity<Long> capacidadResponse = kartClient.obtenerCapacidadDisponible();
            Long capacidadTotal = capacidadResponse.getBody() != null ? capacidadResponse.getBody() : 0L;
            
            if (capacidadTotal < numeroPersonas) {
                return false;
            }

            // Verificar conflictos horarios
            LocalDateTime fechaFin = fechaHora.plusMinutes(duracionMinutos);
            
            // Obtener karts disponibles
            ResponseEntity<List<Map<String, Object>>> kartsResponse = kartClient.obtenerKartsDisponibles();
            if (kartsResponse.getBody() == null || kartsResponse.getBody().isEmpty()) {
                return false;
            }

            List<Long> kartsDisponibles = kartsResponse.getBody().stream()
                    .map(kart -> Long.valueOf(kart.get("id").toString()))
                    .collect(Collectors.toList());

            // Verificar si hay conflictos
            List<ReservaEntity> conflictos = reservaRepository.findConflictosHorarios(fechaHora, fechaFin, kartsDisponibles);
            
            // Calcular karts realmente disponibles
            int kartsEnConflicto = conflictos.stream()
                    .mapToInt(reserva -> reserva.getKartsIds().size())
                    .sum();
            
            int kartsLibres = kartsDisponibles.size() - kartsEnConflicto;
            
            return kartsLibres >= numeroPersonas;

        } catch (Exception e) {
            System.err.println("⚠️ Error al verificar disponibilidad: " + e.getMessage());
            return false;
        }
    }

    // Obtener estadísticas de reservas
    public Map<String, Object> obtenerEstadisticasReservas() {
        try {
            List<Object[]> estadisticasPorEstado = reservaRepository.countReservasPorEstado();
            Double promedioPrecios = reservaRepository.findPromedioPrecioTotal();
            Double totalIngresos = reservaRepository.findTotalIngresos();
            
            return Map.of(
                "reservasPorEstado", estadisticasPorEstado,
                "promedioPrecios", promedioPrecios != null ? promedioPrecios : 0.0,
                "totalIngresos", totalIngresos != null ? totalIngresos : 0.0,
                "totalReservas", reservaRepository.count()
            );
        } catch (Exception e) {
            throw new RuntimeException("Error al obtener estadísticas: " + e.getMessage(), e);
        }
    }
}
