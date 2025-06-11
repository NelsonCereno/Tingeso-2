package com.karting.service;

import com.karting.client.ReservaClient;
import com.karting.dto.ReservaDto;
import com.karting.dto.RackSemanalResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.DayOfWeek;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RackService {

    @Autowired
    private ReservaClient reservaClient;

    // ✅ BLOQUE HORARIO COMPLETO CON 13:00-14:00
    @Value("${karting.rack.bloques-horario:09:00-10:00,10:00-11:00,11:00-12:00,12:00-13:00,13:00-14:00,14:00-15:00,15:00-16:00,16:00-17:00,17:00-18:00,18:00-19:00,19:00-20:00}")
    private String bloquesHorarioString;

    /**
     * Obtener los bloques horarios como lista ordenada
     */
    private List<String> getBloquesHorario() {
        List<String> bloques = Arrays.stream(bloquesHorarioString.split(","))
            .map(String::trim)
            .sorted(this::compararBloques)
            .collect(Collectors.toList());
            
        System.out.println("🕐 Bloques horarios configurados: " + bloques);
        return bloques;
    }

    /**
     * Comparador para ordenar bloques horarios cronológicamente
     */
    private int compararBloques(String bloque1, String bloque2) {
        try {
            String horaInicio1 = bloque1.split("-")[0].trim();
            String horaInicio2 = bloque2.split("-")[0].trim();
            
            LocalTime hora1 = LocalTime.parse(horaInicio1);
            LocalTime hora2 = LocalTime.parse(horaInicio2);
            
            return hora1.compareTo(hora2);
        } catch (Exception e) {
            return bloque1.compareTo(bloque2);
        }
    }

    /**
     * RF7 - Obtener rack semanal completo
     */
    public RackSemanalResponse obtenerRackSemanal() {
        try {
            System.out.println("🗓️ Generando rack semanal completo");
            
            List<ReservaDto> reservas = obtenerReservasDesdeServicio(null, null);
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
            
            List<ReservaDto> reservas = obtenerReservasDesdeServicio(fechaInicio, fechaFin);
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
            List<ReservaDto> reservas = obtenerReservasDesdeServicio(fecha, fecha);
            
            List<ReservaDto> reservasEnBloque = reservas.stream()
                .filter(reserva -> reservaOcupaBloque(reserva, bloque, fecha))
                .collect(Collectors.toList());

            int personasOcupadas = reservasEnBloque.stream()
                .mapToInt(ReservaDto::getNumeroPersonas)
                .sum();

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
    // MÉTODOS PRIVADOS
    // ================================

    /**
     * Obtener reservas desde el reserva-service
     */
    private List<ReservaDto> obtenerReservasDesdeServicio(LocalDate fechaInicio, LocalDate fechaFin) {
        try {
            if (fechaInicio != null && fechaFin != null) {
                return reservaClient.obtenerReservasPorFechas(fechaInicio, fechaFin).getBody();
            } else {
                return reservaClient.obtenerTodasLasReservas().getBody();
            }
        } catch (Exception e) {
            System.err.println("❌ Error al obtener reservas: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Procesar reservas y organizarlas en rack semanal
     */
    private RackSemanalResponse procesarRackSemanal(List<ReservaDto> reservas, LocalDate fechaInicio, LocalDate fechaFin) {
        Map<String, Map<String, List<ReservaDto>>> rackSemanal = new LinkedHashMap<>();
        
        String[] diasSemana = {"Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"};
        List<String> bloquesHorario = getBloquesHorario();

        // Inicializar estructura
        for (String dia : diasSemana) {
            rackSemanal.put(dia, new LinkedHashMap<>());
            for (String bloque : bloquesHorario) {
                rackSemanal.get(dia).put(bloque, new ArrayList<>());
            }
        }

        int totalReservas = 0;
        System.out.println("🔍 Procesando " + reservas.size() + " reservas para el rack semanal (" + fechaInicio + " - " + fechaFin + ")");

        for (ReservaDto reserva : reservas) {
            try {
                if (reserva.getFechaHora() == null || reserva.getDuracionMinutos() == null) {
                    System.err.println("⚠️ Reserva " + reserva.getId() + " sin fecha o duración válida");
                    continue;
                }

                LocalDate fechaReserva = reserva.getFechaHora().toLocalDate();
                
                // ✅ FILTRO CRÍTICO: Verificar rango de fechas
                if (fechaInicio != null && fechaFin != null) {
                    if (fechaReserva.isBefore(fechaInicio) || fechaReserva.isAfter(fechaFin)) {
                        System.out.println("⏭️ Reserva " + reserva.getId() + " fuera del rango solicitado (" + fechaReserva + ")");
                        continue;
                    }
                }

                String dia = obtenerDiaSemana(fechaReserva);
                
                System.out.println("🔍 Procesando reserva " + reserva.getId() + 
                                 " - Fecha: " + fechaReserva + 
                                 " - Día: " + dia + 
                                 " - Hora: " + reserva.getFechaHora().toLocalTime() +
                                 " - Duración: " + reserva.getDuracionMinutos() + " min");

                if (!rackSemanal.containsKey(dia)) {
                    System.err.println("⚠️ Día no encontrado en rack: " + dia + " para fecha: " + fechaReserva);
                    continue;
                }

                // ✅ CAMBIO CRÍTICO: Pasar fecha específica y agregar más logs
                boolean reservaAsignada = false;
                for (String bloque : bloquesHorario) {
                    if (reservaOcupaBloque(reserva, bloque, fechaReserva)) {
                        rackSemanal.get(dia).get(bloque).add(reserva);
                        reservaAsignada = true;
                        System.out.println("✅ Reserva " + reserva.getId() + " asignada al bloque " + bloque + " del " + dia);
                    }
                }
                
                if (reservaAsignada) {
                    totalReservas++;
                    System.out.println("✅ Reserva " + reserva.getId() + " asignada correctamente");
                } else {
                    System.err.println("⚠️ Reserva " + reserva.getId() + " no se asignó a ningún bloque. Hora: " + reserva.getFechaHora().toLocalTime());
                    System.err.println("🕐 Bloques disponibles: " + bloquesHorario);
                }

            } catch (Exception e) {
                System.err.println("⚠️ Error al procesar reserva " + reserva.getId() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }

        // Calcular estadísticas
        int bloquesOcupados = 0;
        for (String dia : rackSemanal.keySet()) {
            for (String bloque : rackSemanal.get(dia).keySet()) {
                if (!rackSemanal.get(dia).get(bloque).isEmpty()) {
                    bloquesOcupados++;
                }
            }
        }

        int totalBloques = diasSemana.length * bloquesHorario.size();
        double porcentajeOcupacion = totalBloques > 0 ? (double) bloquesOcupados / totalBloques * 100 : 0;

        RackSemanalResponse response = new RackSemanalResponse();
        response.setRackSemanal(rackSemanal);
        response.setFechaInicio(fechaInicio);
        response.setFechaFin(fechaFin);
        response.setTotalReservas(totalReservas);
        response.setBloquesOcupados(bloquesOcupados);
        response.setPorcentajeOcupacion(Math.round(porcentajeOcupacion * 100.0) / 100.0);

        System.out.println("✅ Rack semanal generado - " + totalReservas + " reservas procesadas, " + 
                          bloquesOcupados + "/" + totalBloques + " bloques ocupados (" + 
                          Math.round(porcentajeOcupacion * 100.0) / 100.0 + "% ocupación)");
        
        return response;
    }

    /**
     * ✅ MÉTODO MEJORADO CON MÁS LOGS (con verificación de fecha)
     */
    private boolean reservaOcupaBloque(ReservaDto reserva, String bloque, LocalDate fechaObjetivo) {
        try {
            LocalDateTime fechaHoraReserva = reserva.getFechaHora();
            LocalDate fechaReserva = fechaHoraReserva.toLocalDate();
            
            // ✅ VERIFICACIÓN CRÍTICA: Que la reserva sea del día correcto
            if (!fechaReserva.equals(fechaObjetivo)) {
                return false;
            }
            
            LocalTime horaInicio = fechaHoraReserva.toLocalTime();
            LocalTime horaFin = horaInicio.plusMinutes(reserva.getDuracionMinutos());

            String[] partes = bloque.split("-");
            if (partes.length != 2) {
                System.err.println("⚠️ Formato de bloque inválido: " + bloque);
                return false;
            }
            
            LocalTime inicioBloque = LocalTime.parse(partes[0].trim());
            LocalTime finBloque = LocalTime.parse(partes[1].trim());

            boolean solapa = horaInicio.isBefore(finBloque) && horaFin.isAfter(inicioBloque);
            
            // ✅ LOG DETALLADO PARA DEBUG
            System.out.println("🔍 Verificando reserva " + reserva.getId() + 
                             " contra bloque " + bloque + 
                             " - ReservaHora: " + horaInicio + "-" + horaFin + 
                             " vs BloqueHora: " + inicioBloque + "-" + finBloque + 
                             " = " + (solapa ? "✅ SÍ" : "❌ NO"));
            
            return solapa;
        } catch (Exception e) {
            System.err.println("⚠️ Error al verificar ocupación de bloque: " + bloque + 
                              " para reserva: " + reserva.getId() + " - " + e.getMessage());
            return false;
        }
    }

    /**
     * Obtener día de la semana en español
     */
    private String obtenerDiaSemana(LocalDate fecha) {
        DayOfWeek dayOfWeek = fecha.getDayOfWeek();
        switch (dayOfWeek) {
            case MONDAY: return "Lunes";
            case TUESDAY: return "Martes";
            case WEDNESDAY: return "Miércoles";
            case THURSDAY: return "Jueves";
            case FRIDAY: return "Viernes";
            case SATURDAY: return "Sábado";
            case SUNDAY: return "Domingo";
            default: throw new IllegalArgumentException("Día de semana inválido: " + dayOfWeek);
        }
    }
}