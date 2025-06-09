package com.karting.service;

import com.karting.client.ReservaClient;
import com.karting.dto.ReservaDto;
import com.karting.dto.RackSemanalResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RackService {

    @Autowired
    private ReservaClient reservaClient;

    // ✅ CAMBIAR: Usar String en lugar de List
    @Value("${karting.rack.bloques-horario:09:00-10:00,10:00-11:00,11:00-12:00,12:00-13:00,14:00-15:00,15:00-16:00,16:00-17:00,17:00-18:00,18:00-19:00,19:00-20:00}")
    private String bloquesHorarioString;

    // ✅ AGREGAR: Método para obtener los bloques como lista
    private List<String> getBloquesHorario() {
        return Arrays.stream(bloquesHorarioString.split(","))
            .map(String::trim)
            .sorted(this::compararBloques) // ✅ Ordenar cronológicamente
            .collect(Collectors.toList());
    }

    /**
     * ✅ NUEVO: Comparador para ordenar bloques horarios cronológicamente
     */
    private int compararBloques(String bloque1, String bloque2) {
        try {
            // Extraer hora de inicio de cada bloque
            String horaInicio1 = bloque1.split("-")[0].trim();
            String horaInicio2 = bloque2.split("-")[0].trim();
            
            LocalTime hora1 = LocalTime.parse(horaInicio1);
            LocalTime hora2 = LocalTime.parse(horaInicio2);
            
            return hora1.compareTo(hora2);
        } catch (Exception e) {
            // En caso de error, usar comparación alfabética como fallback
            return bloque1.compareTo(bloque2);
        }
    }

    /**
     * RF7 - Obtener rack semanal completo
     */
    public RackSemanalResponse obtenerRackSemanal() {
        try {
            System.out.println("🗓️ Generando rack semanal completo");
            
            // Obtener todas las reservas
            List<ReservaDto> reservas = obtenerReservasDesdeServicio(null, null);
            
            // Procesar y organizar en rack semanal
            return procesarRackSemanal(reservas, null, null);
            
        } catch (Exception e) {
            System.err.println("❌ Error al obtener rack semanal: " + e.getMessage());
            throw new RuntimeException("Error al generar rack semanal: " + e.getMessage(), e);
        }
    }

    /**
     * RF7 - Obtener rack semanal filtrado por fechas
     */
    public RackSemanalResponse obtenerRackSemanalPorFechas(LocalDate fechaInicio, LocalDate fechaFin) {
        try {
            System.out.println("🗓️ Generando rack semanal desde " + fechaInicio + " hasta " + fechaFin);
            
            // Obtener reservas filtradas por fechas
            List<ReservaDto> reservas = obtenerReservasDesdeServicio(fechaInicio, fechaFin);
            
            // Procesar y organizar en rack semanal
            return procesarRackSemanal(reservas, fechaInicio, fechaFin);
            
        } catch (Exception e) {
            System.err.println("❌ Error al obtener rack semanal por fechas: " + e.getMessage());
            throw new RuntimeException("Error al generar rack semanal filtrado: " + e.getMessage(), e);
        }
    }

    /**
     * Obtener estadísticas del rack semanal
     */
    public Map<String, Object> obtenerEstadisticasRack(LocalDate fechaInicio, LocalDate fechaFin) {
        try {
            RackSemanalResponse rack = obtenerRackSemanalPorFechas(fechaInicio, fechaFin);
            
            int totalReservas = 0;
            int bloquesOcupados = 0;
            int totalBloques = 0;
            Map<String, Integer> reservasPorDia = new HashMap<>();

            for (String dia : rack.getRackSemanal().keySet()) {
                int reservasDia = 0;
                for (String bloque : rack.getRackSemanal().get(dia).keySet()) {
                    totalBloques++;
                    List<ReservaDto> reservasBloque = rack.getRackSemanal().get(dia).get(bloque);
                    if (!reservasBloque.isEmpty()) {
                        bloquesOcupados++;
                        reservasDia += reservasBloque.size();
                        totalReservas += reservasBloque.size();
                    }
                }
                reservasPorDia.put(dia, reservasDia);
            }

            double porcentajeOcupacion = totalBloques > 0 ? (double) bloquesOcupados / totalBloques * 100 : 0;

            return Map.of(
                "totalReservas", totalReservas,
                "bloquesOcupados", bloquesOcupados,
                "totalBloques", totalBloques,
                "porcentajeOcupacion", Math.round(porcentajeOcupacion * 100.0) / 100.0,
                "reservasPorDia", reservasPorDia,
                "fechaInicio", fechaInicio.toString(),
                "fechaFin", fechaFin.toString()
            );
        } catch (Exception e) {
            throw new RuntimeException("Error al calcular estadísticas del rack: " + e.getMessage(), e);
        }
    }

    /**
     * Verificar disponibilidad en un bloque específico
     */
    public Map<String, Object> verificarDisponibilidadBloque(LocalDate fecha, String bloque, Integer numeroPersonas) {
        try {
            // Obtener reservas para esa fecha específica
            List<ReservaDto> reservas = obtenerReservasDesdeServicio(fecha, fecha);
            
            // Filtrar reservas que ocupan ese bloque
            List<ReservaDto> reservasEnBloque = reservas.stream()
                .filter(reserva -> reservaOcupaBloque(reserva, bloque))
                .collect(Collectors.toList());

            // Calcular ocupación (simplificado)
            int personasOcupadas = reservasEnBloque.stream()
                .mapToInt(ReservaDto::getNumeroPersonas)
                .sum();

            // Asumir capacidad máxima de 20 personas por bloque (configurable)
            int capacidadMaxima = 20;
            boolean disponible = (personasOcupadas + numeroPersonas) <= capacidadMaxima;

            return Map.of(
                "disponible", disponible,
                "fecha", fecha.toString(),
                "bloque", bloque,
                "numeroPersonas", numeroPersonas,
                "personasOcupadas", personasOcupadas,
                "capacidadMaxima", capacidadMaxima,
                "reservasEnBloque", reservasEnBloque.size()
            );
            
        } catch (Exception e) {
            throw new RuntimeException("Error al verificar disponibilidad: " + e.getMessage(), e);
        }
    }

    // ================================
    // MÉTODOS PRIVADOS DE PROCESAMIENTO
    // ================================

    /**
     * Obtener reservas desde el reserva-service
     */
    private List<ReservaDto> obtenerReservasDesdeServicio(LocalDate fechaInicio, LocalDate fechaFin) {
        try {
            if (fechaInicio != null && fechaFin != null) {
                // Usar endpoint con filtros de fecha
                return reservaClient.obtenerReservasPorFechas(fechaInicio, fechaFin).getBody();
            } else {
                // Obtener todas las reservas
                return reservaClient.obtenerTodasLasReservas().getBody();
            }
        } catch (Exception e) {
            System.err.println("❌ Error al obtener reservas: " + e.getMessage());
            return new ArrayList<>(); // Fallback: lista vacía
        }
    }

    /**
     * Procesar reservas y organizarlas en rack semanal
     */
    private RackSemanalResponse procesarRackSemanal(List<ReservaDto> reservas, LocalDate fechaInicio, LocalDate fechaFin) {
        Map<String, Map<String, List<ReservaDto>>> rackSemanal = new LinkedHashMap<>(); // ✅ Usar LinkedHashMap para mantener orden

        // Inicializar estructura del rack
        String[] diasSemana = {"Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"};
        List<String> bloquesHorario = getBloquesHorario(); // ✅ Ya ordenados cronológicamente

        // Inicializar días y bloques vacíos
        for (String dia : diasSemana) {
            rackSemanal.put(dia, new LinkedHashMap<>()); // ✅ Usar LinkedHashMap para mantener orden
            for (String bloque : bloquesHorario) {
                rackSemanal.get(dia).put(bloque, new ArrayList<>());
            }
        }

        // Organizar reservas en bloques
        int totalReservas = 0;
        int bloquesOcupados = 0;

        for (ReservaDto reserva : reservas) {
            try {
                if (reserva.getFechaHora() == null || reserva.getDuracionMinutos() == null) {
                    System.err.println("⚠️ Reserva " + reserva.getId() + " sin fecha o duración válida");
                    continue;
                }

                String dia = obtenerDiaSemana(reserva.getFechaHora().toLocalDate());

                // Asignar reserva a todos los bloques que ocupa
                for (String bloque : bloquesHorario) {
                    if (reservaOcupaBloque(reserva, bloque)) {
                        rackSemanal.get(dia).get(bloque).add(reserva);
                        totalReservas++;
                    }
                }

            } catch (Exception e) {
                System.err.println("⚠️ Error al procesar reserva " + reserva.getId() + ": " + e.getMessage());
            }
        }

        // Contar bloques ocupados
        for (String dia : rackSemanal.keySet()) {
            for (String bloque : rackSemanal.get(dia).keySet()) {
                if (!rackSemanal.get(dia).get(bloque).isEmpty()) {
                    bloquesOcupados++;
                }
            }
        }

        // Calcular porcentaje de ocupación
        int totalBloques = diasSemana.length * bloquesHorario.size();
        double porcentajeOcupacion = totalBloques > 0 ? (double) bloquesOcupados / totalBloques * 100 : 0;

        // Preparar respuesta
        RackSemanalResponse response = new RackSemanalResponse();
        response.setRackSemanal(rackSemanal);
        response.setFechaInicio(fechaInicio);
        response.setFechaFin(fechaFin);
        response.setTotalReservas(totalReservas);
        response.setBloquesOcupados(bloquesOcupados);
        response.setPorcentajeOcupacion(Math.round(porcentajeOcupacion * 100.0) / 100.0);

        System.out.println("✅ Rack semanal generado - " + totalReservas + " reservas, " + bloquesOcupados + "/" + totalBloques + " bloques ocupados");
        return response;
    }

    /**
     * Verificar si una reserva ocupa un bloque horario específico
     */
    private boolean reservaOcupaBloque(ReservaDto reserva, String bloque) {
        try {
            LocalTime horaInicio = reserva.getFechaHora().toLocalTime();
            LocalTime horaFin = horaInicio.plusMinutes(reserva.getDuracionMinutos());

            String[] partes = bloque.split("-");
            LocalTime inicioBloque = LocalTime.parse(partes[0]);
            LocalTime finBloque = LocalTime.parse(partes[1]);

            // Verificar solapamiento
            return horaInicio.isBefore(finBloque) && horaFin.isAfter(inicioBloque);
        } catch (Exception e) {
            System.err.println("⚠️ Error al parsear bloque horario: " + bloque);
            return false;
        }
    }

    /**
     * Obtener día de la semana en español
     */
    private String obtenerDiaSemana(LocalDate fecha) {
        switch (fecha.getDayOfWeek()) {
            case MONDAY: return "Lunes";
            case TUESDAY: return "Martes";
            case WEDNESDAY: return "Miércoles";
            case THURSDAY: return "Jueves";
            case FRIDAY: return "Viernes";
            case SATURDAY: return "Sábado";
            case SUNDAY: return "Domingo";
            default: throw new IllegalArgumentException("Día no válido: " + fecha.getDayOfWeek());
        }
    }
}