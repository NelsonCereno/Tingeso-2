package com.karting.service;

import com.karting.client.ReservaClient;
import com.karting.dto.ReporteIngresosResponse;
import com.karting.dto.ReservaDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportsService {

    @Autowired
    private ReservaClient reservaClient;

    /**
     * RF8 - Generar reporte mensual de ingresos
     */
    public ReporteIngresosResponse generarReporteMensual(Integer anio, Integer mes) {
        try {
            System.out.println("📊 Generando reporte mensual - " + mes + "/" + anio);
            
            // Calcular fechas del mes
            LocalDate fechaInicio = LocalDate.of(anio, mes, 1);
            LocalDate fechaFin = fechaInicio.withDayOfMonth(fechaInicio.lengthOfMonth());
            
            // Obtener reservas del mes
            List<ReservaDto> reservasDelMes = obtenerReservasDelMes(fechaInicio, fechaFin);
            
            // Procesar y calcular métricas
            return procesarReporteMensual(reservasDelMes, anio, mes);
            
        } catch (Exception e) {
            System.err.println("❌ Error al generar reporte mensual: " + e.getMessage());
            throw new RuntimeException("Error al generar reporte mensual: " + e.getMessage(), e);
        }
    }

    /**
     * Generar reporte anual (resumen por meses)
     */
    public Map<String, Object> generarReporteAnual(Integer anio) {
        try {
            System.out.println("📈 Generando reporte anual - " + anio);
            
            List<Map<String, Object>> reportesPorMes = new ArrayList<>();
            Double ingresosTotalesAnio = 0.0;
            Integer totalReservasAnio = 0;
            
            // Generar reporte para cada mes
            for (int mes = 1; mes <= 12; mes++) {
                try {
                    ReporteIngresosResponse reporteMes = generarReporteMensual(anio, mes);
                    
                    Map<String, Object> resumenMes = Map.of(
                        "mes", mes,
                        "nombreMes", reporteMes.getNombreMes(),
                        "ingresosTotales", reporteMes.getIngresosTotales() != null ? reporteMes.getIngresosTotales() : 0.0,
                        "totalReservas", reporteMes.getTotalReservas() != null ? reporteMes.getTotalReservas() : 0,
                        "totalPersonas", reporteMes.getTotalPersonas() != null ? reporteMes.getTotalPersonas() : 0
                    );
                    
                    reportesPorMes.add(resumenMes);
                    ingresosTotalesAnio += (Double) resumenMes.get("ingresosTotales");
                    totalReservasAnio += (Integer) resumenMes.get("totalReservas");
                    
                } catch (Exception e) {
                    System.err.println("⚠️ Error en mes " + mes + ": " + e.getMessage());
                    // Agregar mes con datos vacíos
                    reportesPorMes.add(Map.of(
                        "mes", mes,
                        "nombreMes", obtenerNombreMes(mes),
                        "ingresosTotales", 0.0,
                        "totalReservas", 0,
                        "totalPersonas", 0
                    ));
                }
            }
            
            return Map.of(
                "anio", anio,
                "ingresosTotalesAnio", ingresosTotalesAnio,
                "totalReservasAnio", totalReservasAnio,
                "promedioIngresosMensuales", ingresosTotalesAnio / 12,
                "reportesPorMes", reportesPorMes,
                "fechaGeneracion", LocalDate.now()
            );
            
        } catch (Exception e) {
            throw new RuntimeException("Error al generar reporte anual: " + e.getMessage(), e);
        }
    }

    /**
     * Obtener estadísticas comparativas
     */
    public Map<String, Object> obtenerEstadisticasComparativas(Integer anio, Integer mes) {
        try {
            // Reporte del mes actual
            ReporteIngresosResponse reporteActual = generarReporteMensual(anio, mes);
            
            // Reporte del mes anterior
            LocalDate fechaAnterior = LocalDate.of(anio, mes, 1).minusMonths(1);
            ReporteIngresosResponse reporteAnterior = generarReporteMensual(
                fechaAnterior.getYear(), fechaAnterior.getMonthValue()
            );
            
            // Calcular comparaciones
            Double crecimientoIngresos = calcularCrecimiento(
                reporteAnterior.getIngresosTotales(), 
                reporteActual.getIngresosTotales()
            );
            
            Double crecimientoReservas = calcularCrecimiento(
                reporteAnterior.getTotalReservas().doubleValue(), 
                reporteActual.getTotalReservas().doubleValue()
            );
            
            return Map.of(
                "mesActual", reporteActual,
                "mesAnterior", reporteAnterior,
                "crecimientoIngresos", crecimientoIngresos,
                "crecimientoReservas", crecimientoReservas,
                "tendencia", crecimientoIngresos > 0 ? "CRECIMIENTO" : crecimientoIngresos < 0 ? "DECRECIMIENTO" : "ESTABLE"
            );
            
        } catch (Exception e) {
            throw new RuntimeException("Error al generar estadísticas comparativas: " + e.getMessage(), e);
        }
    }

    // ================================
    // MÉTODOS PRIVADOS DE PROCESAMIENTO
    // ================================

    /**
     * Obtener reservas del reserva-service para un rango de fechas
     */
    private List<ReservaDto> obtenerReservasDelMes(LocalDate fechaInicio, LocalDate fechaFin) {
        try {
            List<ReservaDto> reservas = reservaClient.obtenerReservasPorFechas(fechaInicio, fechaFin).getBody();
            return reservas != null ? reservas : new ArrayList<>();
        } catch (Exception e) {
            System.err.println("❌ Error al obtener reservas: " + e.getMessage());
            return new ArrayList<>(); // Fallback: lista vacía
        }
    }

    /**
     * Procesar reservas y generar reporte mensual completo
     */
    private ReporteIngresosResponse procesarReporteMensual(List<ReservaDto> reservas, Integer anio, Integer mes) {
        ReporteIngresosResponse reporte = new ReporteIngresosResponse(anio, mes);

        // Filtrar solo reservas confirmadas
        List<ReservaDto> reservasConfirmadas = reservas.stream()
            .filter(r -> "CONFIRMADA".equals(r.getEstado()))
            .collect(Collectors.toList());

        // Métricas básicas
        Double ingresosTotales = reservasConfirmadas.stream()
            .mapToDouble(r -> r.getPrecioTotal() != null ? r.getPrecioTotal() : 0.0)
            .sum();

        Double ingresosBrutos = reservasConfirmadas.stream()
            .mapToDouble(r -> r.getPrecioBase() != null ? r.getPrecioBase() : 0.0)
            .sum();

        Double descuentoPersonas = reservasConfirmadas.stream()
            .mapToDouble(r -> r.getDescuentoPersonas() != null ? r.getDescuentoPersonas() : 0.0)
            .sum();

        Double descuentoClientes = reservasConfirmadas.stream()
            .mapToDouble(r -> r.getDescuentoClientes() != null ? r.getDescuentoClientes() : 0.0)
            .sum();

        Double descuentoCumpleanos = reservasConfirmadas.stream()
            .mapToDouble(r -> r.getDescuentoCumpleanos() != null ? r.getDescuentoCumpleanos() : 0.0)
            .sum();

        Double descuentosTotales = descuentoPersonas + descuentoClientes + descuentoCumpleanos;

        Integer totalPersonas = reservasConfirmadas.stream()
            .mapToInt(r -> r.getNumeroPersonas() != null ? r.getNumeroPersonas() : 0)
            .sum();

        // Asignar métricas básicas
        reporte.setIngresosTotales(Math.round(ingresosTotales * 100.0) / 100.0);
        reporte.setIngresosBrutos(Math.round(ingresosBrutos * 100.0) / 100.0);
        reporte.setDescuentosTotales(Math.round(descuentosTotales * 100.0) / 100.0);
        reporte.setTotalReservas(reservasConfirmadas.size());
        reporte.setTotalPersonas(totalPersonas);
        reporte.setReservasConfirmadas(reservasConfirmadas.size());
        reporte.setReservasCanceladas(reservas.size() - reservasConfirmadas.size());
        
        // Desglose por descuentos
        reporte.setDescuentoPersonas(Math.round(descuentoPersonas * 100.0) / 100.0);
        reporte.setDescuentoClientes(Math.round(descuentoClientes * 100.0) / 100.0);
        reporte.setDescuentoCumpleanos(Math.round(descuentoCumpleanos * 100.0) / 100.0);

        // Promedios
        reporte.setIngresoPromedioPorReserva(
            reservasConfirmadas.size() > 0 ? Math.round((ingresosTotales / reservasConfirmadas.size()) * 100.0) / 100.0 : 0.0
        );
        reporte.setIngresoPromedioPorPersona(
            totalPersonas > 0 ? Math.round((ingresosTotales / totalPersonas) * 100.0) / 100.0 : 0.0
        );

        // Distribución por días
        reporte.setIngresosPorDia(calcularIngresosPorDia(reservasConfirmadas));
        reporte.setReservasPorDia(calcularReservasPorDia(reservasConfirmadas));

        // Análisis adicionales
        reporte.setTopClientesPorGasto(calcularTopClientesPorGasto(reservasConfirmadas));
        reporte.setDiasMasProductivos(calcularDiasMasProductivos(reservasConfirmadas));

        System.out.println("✅ Reporte mensual procesado - " + reservasConfirmadas.size() + " reservas, $" + ingresosTotales + " ingresos");
        return reporte;
    }

    /**
     * Calcular distribución de ingresos por día
     */
    private Map<String, Double> calcularIngresosPorDia(List<ReservaDto> reservas) {
        return reservas.stream()
            .filter(r -> r.getFechaHora() != null)
            .collect(Collectors.groupingBy(
                r -> r.getFechaHora().toLocalDate().toString(),
                Collectors.summingDouble(r -> r.getPrecioTotal() != null ? r.getPrecioTotal() : 0.0)
            ));
    }

    /**
     * Calcular distribución de reservas por día
     */
    private Map<String, Integer> calcularReservasPorDia(List<ReservaDto> reservas) {
        return reservas.stream()
            .filter(r -> r.getFechaHora() != null)
            .collect(Collectors.groupingBy(
                r -> r.getFechaHora().toLocalDate().toString(),
                Collectors.collectingAndThen(Collectors.counting(), Math::toIntExact)
            ));
    }

    /**
     * Calcular top clientes por gasto (simplificado)
     */
    private List<Map<String, Object>> calcularTopClientesPorGasto(List<ReservaDto> reservas) {
        Map<Long, Double> gastosPorCliente = new HashMap<>();
        
        for (ReservaDto reserva : reservas) {
            if (reserva.getClientesIds() != null && reserva.getPrecioTotal() != null) {
                Double gastoPorPersona = reserva.getPrecioTotal() / reserva.getClientesIds().size();
                for (Long clienteId : reserva.getClientesIds()) {
                    gastosPorCliente.merge(clienteId, gastoPorPersona, Double::sum);
                }
            }
        }
        
        return gastosPorCliente.entrySet().stream()
            .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
            .limit(5)
            .map(entry -> Map.of(
                "clienteId", (Object) entry.getKey(),
                "gastoTotal", Math.round(entry.getValue() * 100.0) / 100.0
            ))
            .collect(Collectors.toList());
    }

    /**
     * Calcular días más productivos
     */
    private List<Map<String, Object>> calcularDiasMasProductivos(List<ReservaDto> reservas) {
        Map<String, Double> ingresosPorDia = calcularIngresosPorDia(reservas);
        
        return ingresosPorDia.entrySet().stream()
            .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
            .limit(5)
            .map(entry -> Map.of(
                "fecha", (Object) entry.getKey(),
                "ingresos", Math.round(entry.getValue() * 100.0) / 100.0
            ))
            .collect(Collectors.toList());
    }

    /**
     * Calcular crecimiento porcentual
     */
    private Double calcularCrecimiento(Double valorAnterior, Double valorActual) {
        if (valorAnterior == null || valorAnterior == 0.0) return 0.0;
        if (valorActual == null) return -100.0;
        
        return Math.round(((valorActual - valorAnterior) / valorAnterior) * 100.0 * 100.0) / 100.0;
    }

    /**
     * Obtener nombre del mes
     */
    private String obtenerNombreMes(Integer mes) {
        String[] meses = {"", "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
                         "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
        return meses[mes];
    }
}