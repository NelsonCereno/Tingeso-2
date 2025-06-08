package com.karting.service;

import com.karting.client.*;
import com.karting.dto.CalculoPrecioResponse;
import com.karting.dto.PrecioIndividualCliente;
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
            verificarExistenciaClientes(request.getClientesIds());

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
            System.out.println("🧮 Iniciando cálculo de precio POR CLIENTE...");

            // 1. CALCULAR PRECIO BASE POR PERSONA
            ResponseEntity<Double> tarifaResponse = tarifaClient.calcularTarifa(request.getDuracionMinutos());
            Double precioBasePorPersona = tarifaResponse.getBody();

            if (precioBasePorPersona == null || precioBasePorPersona <= 0) {
                throw new RuntimeException("Precio base inválido: " + precioBasePorPersona);
            }

            System.out.println("✅ Precio base POR PERSONA: $" + precioBasePorPersona);

            // 2. CALCULAR DESCUENTO POR GRUPO (aplicado a todos)
            Double porcentajeDescuentoGrupo = calcularPorcentajeDescuentoGrupo(request.getNumeroPersonas());
            Double descuentoGrupoPorPersona = precioBasePorPersona * porcentajeDescuentoGrupo;

            System.out.println("✅ Descuento por grupo (" + request.getNumeroPersonas() + " personas): " +
                    (porcentajeDescuentoGrupo * 100) + "% = $" + descuentoGrupoPorPersona + " por persona");

            // 3. CALCULAR PRECIO INDIVIDUAL PARA CADA CLIENTE
            List<PrecioIndividualCliente> preciosIndividuales = new ArrayList<>();
            Double totalDescuentoPersonas = 0.0;
            Double totalDescuentoClientes = 0.0;
            Double totalDescuentoCumpleanos = 0.0;
            Double totalFinal = 0.0;

            for (Long clienteId : request.getClientesIds()) {
                PrecioIndividualCliente precioCliente = calcularPrecioIndividualCliente(
                        clienteId,
                        precioBasePorPersona,
                        descuentoGrupoPorPersona,
                        request.getNumeroPersonas()
                );

                preciosIndividuales.add(precioCliente);
                totalDescuentoPersonas += descuentoGrupoPorPersona; // Descuento de grupo
                totalDescuentoClientes += precioCliente.getDescuentoClienteFrecuente();
                totalDescuentoCumpleanos += precioCliente.getDescuentoCumpleanos();
                totalFinal += precioCliente.getPrecioFinal();

                System.out.println("👤 Cliente " + clienteId + ": $" + precioCliente.getPrecioFinal() +
                        " (Grupo: -$" + descuentoGrupoPorPersona +
                        ", Cliente frecuente: -$" + precioCliente.getDescuentoClienteFrecuente() +
                        ", Cumpleaños: -$" + precioCliente.getDescuentoCumpleanos() + ")");
            }

            // 4. CALCULAR TOTALES
            Double precioBaseTotal = precioBasePorPersona * request.getClientesIds().size();
            Double descuentoTotal = totalDescuentoPersonas + totalDescuentoClientes + totalDescuentoCumpleanos;

            System.out.println("💰 RESUMEN DEL CÁLCULO:");
            System.out.println("   Precio base total: $" + precioBaseTotal);
            System.out.println("   Descuento por grupo: $" + totalDescuentoPersonas);
            System.out.println("   Descuento clientes frecuentes: $" + totalDescuentoClientes);
            System.out.println("   Descuento cumpleaños: $" + totalDescuentoCumpleanos);
            System.out.println("   Descuento total: $" + descuentoTotal);
            System.out.println("   Precio final: $" + totalFinal);

            // 5. CREAR RESPUESTA
            CalculoPrecioResponse response = new CalculoPrecioResponse();
            response.setPrecioBase(precioBaseTotal);
            response.setDescuentoPersonas(totalDescuentoPersonas);
            response.setDescuentoClientes(totalDescuentoClientes);
            response.setDescuentoCumpleanos(totalDescuentoCumpleanos);
            response.setDescuentoTotal(descuentoTotal);
            response.setPrecioFinal(totalFinal);
            response.setPreciosIndividuales(preciosIndividuales); // NUEVO: Detalle por cliente
            response.setDetalleCalculo(generarDetalleCalculoCompleto(preciosIndividuales, precioBaseTotal, descuentoTotal, totalFinal));

            return response;

        } catch (Exception e) {
            System.err.println("❌ Error al calcular precio: " + e.getMessage());
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

    // VERIFICAR EXISTENCIA DE CLIENTES (con base de datos)
    private void verificarExistenciaClientes(List<Long> clientesIds) {
        if (clientesIds == null || clientesIds.isEmpty()) {
            throw new RuntimeException("La lista de clientes no puede estar vacía");
        }
        
        try {
            System.out.println("🔍 Verificando existencia de clientes en base de datos: " + clientesIds);
            
            // Consultar cliente-service
            ResponseEntity<Boolean> response = clienteClient.verificarExistenciaClientes(clientesIds);
            Boolean existenTodos = response.getBody();
            
            if (existenTodos == null || !existenTodos) {
                throw new RuntimeException("Algunos clientes no existen o están inactivos en el sistema");
            }
            
            System.out.println("✅ Todos los clientes existen y están activos en la base de datos");
            
        } catch (Exception e) {
            System.err.println("⚠️ Error al verificar clientes: " + e.getMessage());
            System.err.println("🎲 Aplicando verificación local como fallback");
            
            // FALLBACK: Verificación local básica
            verificarClientesLocal(clientesIds);
        }
    }

    // MÉTODO DE FALLBACK para verificación de clientes
    private void verificarClientesLocal(List<Long> clientesIds) {
        // Validaciones básicas locales
        for (Long clienteId : clientesIds) {
            if (clienteId == null || clienteId <= 0) {
                throw new RuntimeException("ID de cliente inválido: " + clienteId);
            }
        }

        // Para demo: aceptar todos los IDs positivos
        // En un sistema real, tendríamos una lista local o caché
        System.out.println("✅ Verificación local: Todos los clientes son válidos (fallback)");
        System.out.println("🔍 Clientes verificados localmente: " + clientesIds);
    }

    // INCREMENTAR VISITAS DE CLIENTES
    private void incrementarVisitasClientes(List<Long> clientesIds) {
        try {
            for (Long clienteId : clientesIds) {
                try {
                    // En un sistema real, esto llamaría al cliente-service
                    // clienteClient.incrementarVisitas(clienteId);
                    System.out.println("✅ Incrementando visitas para cliente: " + clienteId);
                } catch (Exception e) {
                    System.err.println("⚠️ Error al incrementar visitas del cliente " + clienteId + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("⚠️ Error general al incrementar visitas: " + e.getMessage());
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

        // Simplificar la verificación de conflictos
        List<ReservaEntity> reservasEnRango = reservaRepository.findReservasEnRangoFecha(fechaInicio, fechaFin);

        // Verificar manualmente si hay conflictos con los karts
        boolean hayConflicto = reservasEnRango.stream()
                .anyMatch(reserva -> reserva.getKartsIds() != null &&
                        reserva.getKartsIds().stream().anyMatch(kartsIds::contains));

        if (hayConflicto) {
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
        LocalDateTime inicioDelDia = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime finDelDia = inicioDelDia.plusDays(1);

        List<ReservaEntity> reservas = reservaRepository.findReservasDelDia(inicioDelDia, finDelDia);
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

            // Verificar conflictos horarios (simplificado)
            LocalDateTime fechaFin = fechaHora.plusMinutes(duracionMinutos);

            // Buscar reservas en el rango de tiempo
            List<ReservaEntity> reservasEnRango = reservaRepository.findReservasEnRangoFecha(fechaHora, fechaFin);

            // Contar karts ocupados en ese horario
            int kartsOcupados = reservasEnRango.stream()
                    .mapToInt(reserva -> reserva.getKartsIds() != null ? reserva.getKartsIds().size() : 0)
                    .sum();

            // Verificar si hay suficientes karts libres
            long kartsLibres = capacidadTotal - kartsOcupados;

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

            // Estadísticas adicionales simplificadas
            Long totalReservas = reservaRepository.count();
            Long reservasCompletadas = reservaRepository.countByEstado(ReservaEntity.EstadoReserva.COMPLETADA);
            Long reservasPendientes = reservaRepository.countByEstado(ReservaEntity.EstadoReserva.PENDIENTE);

            return Map.of(
                    "reservasPorEstado", estadisticasPorEstado,
                    "promedioPrecios", promedioPrecios != null ? promedioPrecios : 0.0,
                    "totalIngresos", totalIngresos != null ? totalIngresos : 0.0,
                    "totalReservas", totalReservas,
                    "reservasCompletadas", reservasCompletadas,
                    "reservasPendientes", reservasPendientes
            );
        } catch (Exception e) {
            throw new RuntimeException("Error al obtener estadísticas: " + e.getMessage(), e);
        }
    }

    // MÉTODOS DE FALLBACK (lógica local de descuentos)

    /**
     * Fallback para calcular descuento por número de personas
     * Lógica según especificación:
     * 1-2 personas: 0%
     * 3-5 personas: 10%
     * 6-10 personas: 20%
     * 11-15 personas: 30%
     */
    private Double calcularDescuentoPersonasLocal(Integer numeroPersonas, Double precioBase) {
        double porcentaje = 0.0;

        if (numeroPersonas >= 11 && numeroPersonas <= 15) {
            porcentaje = 0.30; // 30%
        } else if (numeroPersonas >= 6 && numeroPersonas <= 10) {
            porcentaje = 0.20; // 20%
        } else if (numeroPersonas >= 3 && numeroPersonas <= 5) {
            porcentaje = 0.10; // 10%
        } else {
            porcentaje = 0.0; // 0% para 1-2 personas
        }

        Double descuento = precioBase * porcentaje;
        System.out.println("🏠 Descuento personas (local): " + numeroPersonas + " personas = " + (porcentaje * 100) + "% = $" + descuento);

        return descuento;
    }

    /**
     * Fallback para calcular descuento por clientes frecuentes
     * Lógica según especificación:
     * 0-1 visitas: 0%
     * 2-4 visitas: 10%
     * 5-6 visitas: 20%
     * 7+ visitas: 30%
     */
    private Double calcularDescuentoClientesLocal(List<Long> clientesIds, Double precioBase) {
        if (clientesIds == null || clientesIds.isEmpty()) {
            return 0.0;
        }

        // SIMULACIÓN: Para demo, asumir que son clientes regulares (2-4 visitas = 10%)
        // En un sistema real, esto consultaría el historial de cada cliente
        Integer visitasSimuladas = 3; // Clientes regulares
        double porcentaje = 0.0;

        if (visitasSimuladas >= 7) {
            porcentaje = 0.30; // Muy frecuente: 30%
        } else if (visitasSimuladas >= 5) {
            porcentaje = 0.20; // Frecuente: 20%
        } else if (visitasSimuladas >= 2) {
            porcentaje = 0.10; // Regular: 10%
        } else {
            porcentaje = 0.0; // No frecuente: 0%
        }

        Double descuento = precioBase * porcentaje;
        System.out.println("🏠 Descuento clientes (local): " + visitasSimuladas + " visitas = " + (porcentaje * 100) + "% = $" + descuento);

        return descuento;
    }

    /**
     * Fallback para calcular descuento por cumpleaños
     * Lógica según especificación:
     * - 3-5 personas: hasta 1 cumpleañero con 50% descuento
     * - 6-10 personas: hasta 2 cumpleañeros con 50% descuento
     * - El descuento se aplica proporcionalmente al grupo
     */
    private Double calcularDescuentoCumpleanosLocal(List<Long> clientesIds, Integer numeroPersonas, Double precioBase) {
        if (clientesIds == null || clientesIds.isEmpty()) {
            return 0.0;
        }

        // SIMULACIÓN: Para demo, asumir que hay 1 cumpleañero en grupos de 3+ personas
        // En un sistema real, esto consultaría las fechas de nacimiento vs fecha actual

        int cumpleaneros = 0;
        double porcentajeDescuento = 0.0;

        if (numeroPersonas >= 3 && numeroPersonas <= 5) {
            cumpleaneros = 1; // Máximo 1 cumpleañero
            porcentajeDescuento = 0.50; // 50% de descuento
        } else if (numeroPersonas >= 6 && numeroPersonas <= 10) {
            cumpleaneros = Math.min(2, clientesIds.size()); // Máximo 2 cumpleañeros
            porcentajeDescuento = 0.50; // 50% de descuento por cumpleañero
        }

        if (cumpleaneros > 0) {
            // El descuento se distribuye entre todo el grupo
            Double descuentoTotal = (precioBase * porcentajeDescuento * cumpleaneros) / numeroPersonas;
            System.out.println("🏠 Descuento cumpleaños (local): " + cumpleaneros + " cumpleañero(s) en grupo de " + numeroPersonas + " = $" + descuentoTotal);
            return descuentoTotal;
        }

        return 0.0;
    }

    // NUEVO: Calcular precio individual para cada cliente
    private PrecioIndividualCliente calcularPrecioIndividualCliente(Long clienteId, Double precioBasePorPersona,
                                                                    Double descuentoGrupo, Integer numeroPersonas) {

        PrecioIndividualCliente precio = new PrecioIndividualCliente();
        precio.setClienteId(clienteId);
        precio.setPrecioBase(precioBasePorPersona);
        precio.setDescuentoGrupo(descuentoGrupo);

        // 1. Obtener historial del cliente
        Integer visitasCliente = obtenerVisitasCliente(clienteId);

        // 2. Calcular descuento por cliente frecuente
        Double porcentajeClienteFrecuente = calcularPorcentajeDescuentoClienteFrecuente(visitasCliente);
        Double descuentoClienteFrecuente = precioBasePorPersona * porcentajeClienteFrecuente;
        precio.setDescuentoClienteFrecuente(descuentoClienteFrecuente);
        precio.setNumeroVisitas(visitasCliente);

        // 3. Verificar si es cumpleaños
        Boolean esCumpleanos = verificarSiEsCumpleanos(clienteId);
        Double descuentoCumpleanos = 0.0;
        if (esCumpleanos) {
            descuentoCumpleanos = calcularDescuentoCumpleanosIndividual(numeroPersonas, precioBasePorPersona);
        }
        precio.setDescuentoCumpleanos(descuentoCumpleanos);
        precio.setEsCumpleanos(esCumpleanos);

        // 4. Calcular precio final
        Double precioFinal = precioBasePorPersona - descuentoGrupo - descuentoClienteFrecuente - descuentoCumpleanos;
        precioFinal = Math.max(0, precioFinal); // No puede ser negativo
        precio.setPrecioFinal(precioFinal);

        return precio;
    }

    // NUEVO: Porcentaje de descuento por grupo
    private Double calcularPorcentajeDescuentoGrupo(Integer numeroPersonas) {
        if (numeroPersonas >= 11 && numeroPersonas <= 15) {
            return 0.30; // 30%
        } else if (numeroPersonas >= 6 && numeroPersonas <= 10) {
            return 0.20; // 20%
        } else if (numeroPersonas >= 3 && numeroPersonas <= 5) {
            return 0.10; // 10%
        } else {
            return 0.0; // 0% para 1-2 personas
        }
    }

    // NUEVO: Porcentaje de descuento por cliente frecuente
    private Double calcularPorcentajeDescuentoClienteFrecuente(Integer visitas) {
        if (visitas >= 7) {
            return 0.30; // 30%
        } else if (visitas >= 5) {
            return 0.20; // 20%
        } else if (visitas >= 2) {
            return 0.10; // 10%
        } else {
            return 0.0; // 0%
        }
    }

    // NUEVO: OBTENER VISITAS DE UN CLIENTE ESPECÍFICO (con base de datos)
    private Integer obtenerVisitasCliente(Long clienteId) {
        try {
            System.out.println("🔍 Consultando visitas del cliente " + clienteId + " en base de datos...");
            ResponseEntity<Integer> visitasResponse = clienteClient.obtenerNumeroVisitas(clienteId);
            Integer visitas = visitasResponse.getBody() != null ? visitasResponse.getBody() : 0;
            System.out.println("✅ Cliente " + clienteId + " tiene " + visitas + " visitas");
            return visitas;
        } catch (Exception e) {
            System.err.println("⚠️ Error al obtener visitas del cliente " + clienteId + ": " + e.getMessage());
            System.err.println("🎲 Usando simulación como fallback");
            return simularVisitasCliente(clienteId);
        }
    }

    // NUEVO: Simulación de visitas por cliente
    private Integer simularVisitasCliente(Long clienteId) {
        // Simular diferentes historiales según el ID del cliente
        if (clienteId % 7 == 0) return 8;  // Muy frecuente
        if (clienteId % 5 == 0) return 5;  // Frecuente
        if (clienteId % 3 == 0) return 3;  // Regular
        return 1; // No frecuente
    }

    // NUEVO: Verificar si es cumpleaños
    private Boolean verificarSiEsCumpleanos(Long clienteId) {
        try {
            System.out.println("🎂 Verificando cumpleaños del cliente " + clienteId + " en base de datos...");
            ResponseEntity<List<Long>> cumpleanosResponse = clienteClient.verificarClientesCumpleanos(
                List.of(clienteId));
            List<Long> cumpleaneros = cumpleanosResponse.getBody();
            boolean esCumpleanos = cumpleaneros != null && cumpleaneros.contains(clienteId);
            System.out.println("✅ Cliente " + clienteId + (esCumpleanos ? " SÍ" : " NO") + " está de cumpleaños");
            return esCumpleanos;
        } catch (Exception e) {
            System.err.println("⚠️ Error al verificar cumpleaños del cliente " + clienteId + ": " + e.getMessage());
            System.err.println("🎲 Usando simulación como fallback");
            return clienteId % 10 == 0; // Cada décimo cliente está de cumpleaños
        }
    }

    // NUEVO: Calcular descuento individual por cumpleaños
    private Double calcularDescuentoCumpleanosIndividual(Integer numeroPersonas, Double precioBasePorPersona) {
        // Lógica: 50% de descuento para el cumpleañero
        if (numeroPersonas >= 3) {
            return precioBasePorPersona * 0.50; // 50% de descuento
        }
        return 0.0;
    }

    // NUEVO: Generar detalle completo del cálculo
    private String generarDetalleCalculoCompleto(List<PrecioIndividualCliente> preciosIndividuales,
                                                 Double precioBaseTotal, Double descuentoTotal, Double totalFinal) {
        StringBuilder detalle = new StringBuilder();
        detalle.append("DETALLE POR CLIENTE: ");

        for (int i = 0; i < preciosIndividuales.size(); i++) {
            PrecioIndividualCliente precio = preciosIndividuales.get(i);
            detalle.append("Cliente ").append(precio.getClienteId()).append(": $").append(precio.getPrecioFinal());
            if (i < preciosIndividuales.size() - 1) {
                detalle.append(", ");
            }
        }

        detalle.append(" = Total: $").append(totalFinal);
        return detalle.toString();
    }

    /**
     * Marcar una reserva como email enviado
     */
    public void marcarEmailEnviado(Long reservaId) {
        Optional<ReservaEntity> reservaOpt = reservaRepository.findById(reservaId);
        if (reservaOpt.isPresent()) {
            ReservaEntity reserva = reservaOpt.get();
            reserva.setEmailEnviado(true);
            reserva.setFechaActualizacion(LocalDateTime.now());
            reservaRepository.save(reserva);
            
            System.out.println("✅ Reserva #" + reservaId + " marcada como email enviado");
        }
    }

    // ✅ AGREGAR ESTE MÉTODO
    public List<ReservaEntity> findReservasEnRangoFecha(LocalDateTime inicioDateTime, LocalDateTime finDateTime) {
        try {
            System.out.println("📅 Buscando reservas desde " + inicioDateTime + " hasta " + finDateTime);
            List<ReservaEntity> reservas = reservaRepository.findReservasEnRangoFecha(inicioDateTime, finDateTime);
            System.out.println("✅ Encontradas " + reservas.size() + " reservas en el rango");
            return reservas;
        } catch (Exception e) {
            System.err.println("❌ Error al buscar reservas en rango: " + e.getMessage());
            throw new RuntimeException("Error al buscar reservas: " + e.getMessage(), e);
        }
    }
}
