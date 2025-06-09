package com.karting.controller;

import com.karting.dto.DescuentoCumpleanosRequest;
import com.karting.dto.DescuentoCumpleanosResponse;
import com.karting.entity.DescuentoCumpleanosEntity;
import com.karting.service.DescuentoCumpleanosService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/descuentos/cumpleanos")

public class DescuentoCumpleanosController {

    @Autowired
    private DescuentoCumpleanosService descuentoCumpleanosService;

    // Health check del servicio
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Descuento Cumpleanos Service is running!");
    }

    // Obtener todos los descuentos disponibles
    @GetMapping("/disponibles")
    public ResponseEntity<List<DescuentoCumpleanosEntity>> obtenerDescuentosDisponibles() {
        List<DescuentoCumpleanosEntity> descuentos = descuentoCumpleanosService.obtenerDescuentosDisponibles();
        if (descuentos.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(descuentos);
    }

    // MÉTODO PRINCIPAL - Aplicar descuento (para el orquestador)
    @PostMapping("/aplicar")
    public ResponseEntity<DescuentoCumpleanosResponse> aplicarDescuento(@RequestBody DescuentoCumpleanosRequest request) {
        try {
            if (request.getMontoBase() <= 0) {
                return ResponseEntity.badRequest().build();
            }
            
            DescuentoCumpleanosResponse response = descuentoCumpleanosService.aplicarDescuento(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Método alternativo con lógica hardcodeada
    @PostMapping("/aplicar-hardcoded")
    public ResponseEntity<DescuentoCumpleanosResponse> aplicarDescuentoHardcoded(@RequestBody DescuentoCumpleanosRequest request) {
        try {
            if (request.getMontoBase() <= 0) {
                return ResponseEntity.badRequest().build();
            }
            
            DescuentoCumpleanosResponse response = descuentoCumpleanosService.aplicarDescuentoHardcoded(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Verificar si una fecha es cumpleaños (para el orquestador)
    @GetMapping("/verificar/{fechaNacimiento}/{fechaReserva}")
    public ResponseEntity<Boolean> verificarSiEsCumpleanos(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaNacimiento,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaReserva) {
        
        boolean esCumpleanos = descuentoCumpleanosService.verificarSiEsCumpleanos(fechaNacimiento, fechaReserva);
        return ResponseEntity.ok(esCumpleanos);
    }

    // Obtener solo el porcentaje de descuento por cumpleaños
    @GetMapping("/porcentaje")
    public ResponseEntity<Double> obtenerPorcentajeDescuentoCumpleanos() {
        Double porcentaje = descuentoCumpleanosService.obtenerPorcentajeDescuentoCumpleanos();
        return ResponseEntity.ok(porcentaje);
    }

    // Verificar si el descuento de cumpleaños está activo
    @GetMapping("/activo")
    public ResponseEntity<Boolean> descuentoCumpleanosActivo() {
        boolean activo = descuentoCumpleanosService.descuentoCumpleanosActivo();
        return ResponseEntity.ok(activo);
    }

    // Calcular descuento rápido por parámetros de URL
    @GetMapping("/calcular/{montoBase}/{fechaNacimiento}/{fechaReserva}")
    public ResponseEntity<DescuentoCumpleanosResponse> calcularDescuentoRapido(
            @PathVariable Double montoBase,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaNacimiento,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaReserva) {
        
        if (montoBase <= 0) {
            return ResponseEntity.badRequest().build();
        }

        DescuentoCumpleanosRequest request = new DescuentoCumpleanosRequest(montoBase, fechaNacimiento, fechaReserva);
        DescuentoCumpleanosResponse response = descuentoCumpleanosService.aplicarDescuento(request);
        return ResponseEntity.ok(response);
    }

    // Calcular días hasta el próximo cumpleaños
    @GetMapping("/dias-hasta-cumpleanos/{fechaNacimiento}")
    public ResponseEntity<Long> calcularDiasHastaCumpleanos(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaNacimiento) {
        
        LocalDate fechaActual = LocalDate.now();
        long dias = descuentoCumpleanosService.calcularDiasHastaCumpleanos(fechaNacimiento, fechaActual);
        return ResponseEntity.ok(dias);
    }

    // Calcular días hasta cumpleaños con fecha específica
    @GetMapping("/dias-hasta-cumpleanos/{fechaNacimiento}/{fechaActual}")
    public ResponseEntity<Long> calcularDiasHastaCumpleanosEspecifico(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaNacimiento,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaActual) {
        
        long dias = descuentoCumpleanosService.calcularDiasHastaCumpleanos(fechaNacimiento, fechaActual);
        return ResponseEntity.ok(dias);
    }

    // Simular descuento para una fecha específica
    @PostMapping("/simular")
    public ResponseEntity<DescuentoCumpleanosResponse> simularDescuentoParaFecha(@RequestBody SimularDescuentoRequest request) {
        try {
            if (request.getMontoBase() <= 0) {
                return ResponseEntity.badRequest().build();
            }
            
            DescuentoCumpleanosResponse response = descuentoCumpleanosService.simularDescuentoParaFecha(
                request.getMontoBase(), 
                request.getFechaNacimiento(), 
                request.getFechaSimulada()
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Obtener descuento por tipo específico
    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<DescuentoCumpleanosEntity> obtenerDescuentoPorTipo(@PathVariable String tipo) {
        Optional<DescuentoCumpleanosEntity> descuento = descuentoCumpleanosService.obtenerDescuentoPorTipo(tipo);
        if (descuento.isPresent()) {
            return ResponseEntity.ok(descuento.get());
        }
        return ResponseEntity.notFound().build();
    }

    // Calcular edad de una persona
    @GetMapping("/edad/{fechaNacimiento}")
    public ResponseEntity<Integer> calcularEdad(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaNacimiento) {
        
        LocalDate fechaActual = LocalDate.now();
        int edad = descuentoCumpleanosService.calcularEdad(fechaNacimiento, fechaActual);
        return ResponseEntity.ok(edad);
    }

    // Calcular edad en fecha específica
    @GetMapping("/edad/{fechaNacimiento}/{fechaActual}")
    public ResponseEntity<Integer> calcularEdadEspecifica(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaNacimiento,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaActual) {
        
        int edad = descuentoCumpleanosService.calcularEdad(fechaNacimiento, fechaActual);
        return ResponseEntity.ok(edad);
    }

    // Generar mensaje personalizado de cumpleaños
    @GetMapping("/mensaje/{fechaNacimiento}/{fechaReserva}")
    public ResponseEntity<String> generarMensajeCumpleanos(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaNacimiento,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaReserva) {
        
        String mensaje = descuentoCumpleanosService.generarMensajeCumpleanos(fechaNacimiento, fechaReserva);
        return ResponseEntity.ok(mensaje);
    }

    // Obtener estadísticas de descuentos
    @GetMapping("/estadisticas")
    public ResponseEntity<List<Object[]>> obtenerEstadisticasDescuentos() {
        List<Object[]> estadisticas = descuentoCumpleanosService.obtenerEstadisticasDescuentos();
        return ResponseEntity.ok(estadisticas);
    }

    // DTO adicional para simulaciones
    public static class SimularDescuentoRequest {
        private double montoBase;
        private LocalDate fechaNacimiento;
        private LocalDate fechaSimulada;

        public SimularDescuentoRequest() {}

        public SimularDescuentoRequest(double montoBase, LocalDate fechaNacimiento, LocalDate fechaSimulada) {
            this.montoBase = montoBase;
            this.fechaNacimiento = fechaNacimiento;
            this.fechaSimulada = fechaSimulada;
        }

        public double getMontoBase() { return montoBase; }
        public void setMontoBase(double montoBase) { this.montoBase = montoBase; }

        public LocalDate getFechaNacimiento() { return fechaNacimiento; }
        public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }

        public LocalDate getFechaSimulada() { return fechaSimulada; }
        public void setFechaSimulada(LocalDate fechaSimulada) { this.fechaSimulada = fechaSimulada; }
    }
}
