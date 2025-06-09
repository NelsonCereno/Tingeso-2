package com.karting.controller;

import com.karting.dto.DescuentoClienteRequest;
import com.karting.dto.DescuentoClienteResponse;
import com.karting.entity.DescuentoClienteEntity;
import com.karting.service.DescuentoClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/descuentos/clientes")

public class DescuentoClienteController {

    @Autowired
    private DescuentoClienteService descuentoClienteService;

    // Obtener todos los descuentos disponibles
    @GetMapping("/disponibles")
    public ResponseEntity<List<DescuentoClienteEntity>> obtenerDescuentosDisponibles() {
        List<DescuentoClienteEntity> descuentos = descuentoClienteService.obtenerDescuentosDisponibles();
        if (descuentos.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(descuentos);
    }

    // Aplicar descuento principal (usando base de datos)
    @PostMapping("/aplicar")
    public ResponseEntity<DescuentoClienteResponse> aplicarDescuento(@RequestBody DescuentoClienteRequest request) {
        try {
            if (request.getNumeroVisitas() < 0 || request.getMontoBase() <= 0) {
                return ResponseEntity.badRequest().build();
            }
            DescuentoClienteResponse response = descuentoClienteService.aplicarDescuento(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Aplicar descuento usando lógica hardcodeada (alternativo)
    @PostMapping("/aplicar-hardcoded")
    public ResponseEntity<DescuentoClienteResponse> aplicarDescuentoHardcoded(@RequestBody DescuentoClienteRequest request) {
        try {
            if (request.getNumeroVisitas() < 0 || request.getMontoBase() <= 0) {
                return ResponseEntity.badRequest().build();
            }
            DescuentoClienteResponse response = descuentoClienteService.aplicarDescuentoHardcoded(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Obtener solo el porcentaje de descuento por número de visitas
    @GetMapping("/porcentaje/{numeroVisitas}")
    public ResponseEntity<Double> obtenerPorcentajeDescuento(@PathVariable Integer numeroVisitas) {
        if (numeroVisitas < 0) {
            return ResponseEntity.badRequest().build();
        }
        
        Double porcentaje = descuentoClienteService.obtenerPorcentajeDescuento(numeroVisitas);
        return ResponseEntity.ok(porcentaje);
    }

    // Calcular descuento simple (solo para consultas rápidas)
    @GetMapping("/calcular/{numeroVisitas}/{montoBase}")
    public ResponseEntity<DescuentoClienteResponse> calcularDescuentoRapido(
            @PathVariable Integer numeroVisitas,
            @PathVariable Double montoBase) {
        
        if (numeroVisitas < 0 || montoBase <= 0) {
            return ResponseEntity.badRequest().build();
        }

        DescuentoClienteRequest request = new DescuentoClienteRequest(montoBase, numeroVisitas);
        DescuentoClienteResponse response = descuentoClienteService.aplicarDescuento(request);
        return ResponseEntity.ok(response);
    }

    // Verificar si un cliente califica para descuento
    @GetMapping("/califica/{numeroVisitas}")
    public ResponseEntity<Boolean> clienteCalificaParaDescuento(@PathVariable Integer numeroVisitas) {
        if (numeroVisitas < 0) {
            return ResponseEntity.badRequest().build();
        }
        
        boolean califica = descuentoClienteService.clienteCalificaParaDescuento(numeroVisitas);
        return ResponseEntity.ok(califica);
    }

    // Obtener el siguiente nivel de descuento
    @GetMapping("/siguiente-nivel/{numeroVisitas}")
    public ResponseEntity<DescuentoClienteEntity> obtenerSiguienteNivelDescuento(@PathVariable Integer numeroVisitas) {
        if (numeroVisitas < 0) {
            return ResponseEntity.badRequest().build();
        }
        
        DescuentoClienteEntity siguienteNivel = descuentoClienteService.obtenerSiguienteNivelDescuento(numeroVisitas);
        if (siguienteNivel == null) {
            return ResponseEntity.noContent().build(); // Ya está en el nivel máximo
        }
        return ResponseEntity.ok(siguienteNivel);
    }

    // Calcular visitas restantes para el siguiente descuento
    @GetMapping("/visitas-restantes/{numeroVisitas}")
    public ResponseEntity<Integer> calcularVisitasParaSiguienteDescuento(@PathVariable Integer numeroVisitas) {
        if (numeroVisitas < 0) {
            return ResponseEntity.badRequest().build();
        }
        
        Integer visitasRestantes = descuentoClienteService.calcularVisitasParaSiguienteDescuento(numeroVisitas);
        return ResponseEntity.ok(visitasRestantes);
    }

    // Simular descuento para nueva reserva de cliente existente
    @PostMapping("/simular-nueva-reserva")
    public ResponseEntity<DescuentoClienteResponse> simularDescuentoParaCliente(@RequestBody SimularReservaRequest request) {
        try {
            if (request.getVisitasAnteriores() < 0 || request.getMontoBase() <= 0) {
                return ResponseEntity.badRequest().build();
            }
            
            DescuentoClienteResponse response = descuentoClienteService.simularDescuentoParaCliente(
                request.getMontoBase(), 
                request.getVisitasAnteriores()
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Obtener estadísticas de descuentos
    @GetMapping("/estadisticas")
    public ResponseEntity<List<Object[]>> obtenerEstadisticasDescuentos() {
        List<Object[]> estadisticas = descuentoClienteService.obtenerEstadisticasDescuentos();
        return ResponseEntity.ok(estadisticas);
    }

    // Health check del servicio
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Descuento Clientes Service is running!");
    }

    // DTO adicional para simular reservas
    public static class SimularReservaRequest {
        private double montoBase;
        private int visitasAnteriores;

        public SimularReservaRequest() {}

        public SimularReservaRequest(double montoBase, int visitasAnteriores) {
            this.montoBase = montoBase;
            this.visitasAnteriores = visitasAnteriores;
        }

        public double getMontoBase() { return montoBase; }
        public void setMontoBase(double montoBase) { this.montoBase = montoBase; }

        public int getVisitasAnteriores() { return visitasAnteriores; }
        public void setVisitasAnteriores(int visitasAnteriores) { this.visitasAnteriores = visitasAnteriores; }
    }
}
