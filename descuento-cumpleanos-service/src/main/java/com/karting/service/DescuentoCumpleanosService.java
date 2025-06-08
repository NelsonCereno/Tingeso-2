package com.karting.service;

import com.karting.dto.DescuentoCumpleanosRequest;
import com.karting.dto.DescuentoCumpleanosResponse;
import com.karting.entity.DescuentoCumpleanosEntity;
import com.karting.repository.DescuentoCumpleanosRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class DescuentoCumpleanosService {

    @Autowired
    private DescuentoCumpleanosRepository descuentoCumpleanosRepository;

    @PostConstruct
    public void inicializarDatos() {
        // Solo insertar si la tabla está vacía
        if (descuentoCumpleanosRepository.count() == 0) {
            List<DescuentoCumpleanosEntity> descuentosIniciales = Arrays.asList(
                new DescuentoCumpleanosEntity("CUMPLEANOS", 50.0, "Descuento del 50% por cumpleaños")
                // Futuras expansiones:
                // new DescuentoCumpleanosEntity("FIN_SEMANA", 25.0, "Recargo del 25% fines de semana", DayOfWeek.SATURDAY, false),
                // new DescuentoCumpleanosEntity("FERIADO", 15.0, "Recargo del 15% días feriados", null, true)
            );
            descuentoCumpleanosRepository.saveAll(descuentosIniciales);
            System.out.println("Descuentos por cumpleaños inicializados en la base de datos");
        }
    }

    // LÓGICA PRINCIPAL - Exacta del monolítico
    private boolean esCumpleanos(LocalDate fechaNacimiento, LocalDate fechaReserva) {
        if (fechaNacimiento == null || fechaReserva == null) {
            return false;
        }
        
        // Lógica original del monolítico:
        // cliente.getFechaNacimiento().getDayOfMonth() == fechaReserva.getDayOfMonth() &&
        // cliente.getFechaNacimiento().getMonth() == fechaReserva.getMonth()
        return fechaNacimiento.getDayOfMonth() == fechaReserva.getDayOfMonth() &&
               fechaNacimiento.getMonth() == fechaReserva.getMonth();
    }

    // Método principal para el orquestador
    public DescuentoCumpleanosResponse aplicarDescuento(DescuentoCumpleanosRequest request) {
        double montoBase = request.getMontoBase();
        LocalDate fechaNacimiento = request.getFechaNacimiento();
        LocalDate fechaReserva = request.getFechaReserva();

        // Verificar si es cumpleaños
        boolean esCumpleanosHoy = esCumpleanos(fechaNacimiento, fechaReserva);
        
        double porcentajeDescuento = 0.0;
        String descripcion = "Sin descuento especial";

        if (esCumpleanosHoy) {
            // Buscar descuento de cumpleaños en la base de datos
            Optional<DescuentoCumpleanosEntity> descuentoOpt = descuentoCumpleanosRepository.findDescuentoCumpleanos();
            
            if (descuentoOpt.isPresent()) {
                DescuentoCumpleanosEntity descuento = descuentoOpt.get();
                porcentajeDescuento = descuento.getPorcentajeDescuento();
                descripcion = descuento.getDescripcion();
            } else {
                // Fallback a lógica hardcodeada (del monolítico)
                porcentajeDescuento = 50.0;
                descripcion = "Descuento del 50% por cumpleaños";
            }
        }

        // Calcular montos
        double montoDescuento = montoBase * (porcentajeDescuento / 100.0);
        double montoFinal = montoBase - montoDescuento;

        return new DescuentoCumpleanosResponse(
            montoBase,
            fechaNacimiento,
            fechaReserva,
            esCumpleanosHoy,
            porcentajeDescuento,
            montoDescuento,
            montoFinal,
            descripcion
        );
    }

    // Método alternativo con lógica hardcodeada (para testing/compatibilidad)
    public DescuentoCumpleanosResponse aplicarDescuentoHardcoded(DescuentoCumpleanosRequest request) {
        double montoBase = request.getMontoBase();
        LocalDate fechaNacimiento = request.getFechaNacimiento();
        LocalDate fechaReserva = request.getFechaReserva();

        boolean esCumpleanosHoy = esCumpleanos(fechaNacimiento, fechaReserva);
        
        // Lógica original del monolítico:
        double porcentajeDescuento = esCumpleanosHoy ? 50.0 : 0.0;
        
        double montoDescuento = montoBase * (porcentajeDescuento / 100.0);
        double montoFinal = montoBase - montoDescuento;
        
        String descripcion = esCumpleanosHoy ? 
            "¡Feliz cumpleaños! Descuento del 50%" : 
            "Sin descuento - No es tu cumpleaños";

        return new DescuentoCumpleanosResponse(
            montoBase,
            fechaNacimiento,
            fechaReserva,
            esCumpleanosHoy,
            porcentajeDescuento,
            montoDescuento,
            montoFinal,
            descripcion
        );
    }

    // Verificar si una fecha específica califica para descuento
    public boolean verificarSiEsCumpleanos(LocalDate fechaNacimiento, LocalDate fechaReserva) {
        return esCumpleanos(fechaNacimiento, fechaReserva);
    }

    // Obtener porcentaje de descuento por cumpleaños (para el orquestador)
    public Double obtenerPorcentajeDescuentoCumpleanos() {
        Optional<Double> porcentaje = descuentoCumpleanosRepository.findPorcentajeDescuentoCumpleanos();
        return porcentaje.orElse(0.0);
    }

    // Verificar si el descuento de cumpleaños está activo
    public boolean descuentoCumpleanosActivo() {
        return descuentoCumpleanosRepository.existeDescuentoCumpleanosActivo();
    }

    // Obtener todos los descuentos disponibles
    public List<DescuentoCumpleanosEntity> obtenerDescuentosDisponibles() {
        return descuentoCumpleanosRepository.findAllActivosOrdenadosPorPorcentaje();
    }

    // Calcular días hasta el próximo cumpleaños (gamificación)
    public long calcularDiasHastaCumpleanos(LocalDate fechaNacimiento, LocalDate fechaActual) {
        if (fechaNacimiento == null || fechaActual == null) {
            return -1;
        }

        // Obtener cumpleaños de este año
        LocalDate cumpleanosEsteAno = fechaNacimiento.withYear(fechaActual.getYear());
        
        // Si ya pasó este año, calcular para el próximo año
        if (cumpleanosEsteAno.isBefore(fechaActual) || cumpleanosEsteAno.isEqual(fechaActual)) {
            cumpleanosEsteAno = cumpleanosEsteAno.plusYears(1);
        }
        
        return fechaActual.until(cumpleanosEsteAno).getDays();
    }

    // Simular descuento para una fecha específica (para testing)
    public DescuentoCumpleanosResponse simularDescuentoParaFecha(double montoBase, LocalDate fechaNacimiento, LocalDate fechaSimulada) {
        DescuentoCumpleanosRequest request = new DescuentoCumpleanosRequest(montoBase, fechaNacimiento, fechaSimulada);
        return aplicarDescuento(request);
    }

    // Obtener descuento específico por tipo
    public Optional<DescuentoCumpleanosEntity> obtenerDescuentoPorTipo(String tipo) {
        return descuentoCumpleanosRepository.findByTipoDescuentoAndActivoTrue(tipo);
    }

    // Obtener estadísticas de uso
    public List<Object[]> obtenerEstadisticasDescuentos() {
        return descuentoCumpleanosRepository.findEstadisticasPorTipo();
    }

    // Método para validación de edad (futuro: restricciones por edad)
    public int calcularEdad(LocalDate fechaNacimiento, LocalDate fechaActual) {
        if (fechaNacimiento == null || fechaActual == null) {
            return 0;
        }
        return fechaActual.getYear() - fechaNacimiento.getYear() - 
               (fechaActual.getDayOfYear() < fechaNacimiento.getDayOfYear() ? 1 : 0);
    }

    // Generar mensaje personalizado de cumpleaños
    public String generarMensajeCumpleanos(LocalDate fechaNacimiento, LocalDate fechaReserva) {
        if (esCumpleanos(fechaNacimiento, fechaReserva)) {
            int edad = calcularEdad(fechaNacimiento, fechaReserva);
            return String.format("¡Feliz cumpleaños número %d! Disfruta tu descuento especial del 50%%", edad);
        }
        
        long diasRestantes = calcularDiasHastaCumpleanos(fechaNacimiento, fechaReserva);
        if (diasRestantes <= 7 && diasRestantes > 0) {
            return String.format("¡Tu cumpleaños está cerca! Faltan %d días para tu descuento especial", diasRestantes);
        }
        
        return "Haz tu reserva en tu cumpleaños y obtén 50% de descuento";
    }
}
