package com.karting.controller;

import com.karting.dto.DescuentoRequest;
import com.karting.dto.DescuentoResponse;
import com.karting.entity.DescuentoEntity;
import com.karting.service.DescuentoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/descuentos/personas")

public class DescuentoController {

    @Autowired
    private DescuentoService descuentoService;

    // Obtener todos los descuentos disponibles
    @GetMapping("/disponibles")
    public ResponseEntity<List<DescuentoEntity>> obtenerDescuentosDisponibles() {
        List<DescuentoEntity> descuentos = descuentoService.obtenerDescuentosDisponibles();
        if (descuentos.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(descuentos);
    }

    // Aplicar descuento principal (usando base de datos)
    @PostMapping("/aplicar")
    public ResponseEntity<DescuentoResponse> aplicarDescuento(@RequestBody DescuentoRequest request) {
        try {
            DescuentoResponse response = descuentoService.aplicarDescuento(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Aplicar descuento usando lógica hardcodeada (alternativo)
    @PostMapping("/aplicar-hardcoded")
    public ResponseEntity<DescuentoResponse> aplicarDescuentoHardcoded(@RequestBody DescuentoRequest request) {
        try {
            DescuentoResponse response = descuentoService.aplicarDescuentoHardcoded(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Obtener solo el porcentaje de descuento por número de personas
    @GetMapping("/porcentaje/{numeroPersonas}")
    public ResponseEntity<Double> obtenerPorcentajeDescuento(@PathVariable Integer numeroPersonas) {
        if (numeroPersonas <= 0) {
            return ResponseEntity.badRequest().build();
        }

        Double porcentaje = descuentoService.obtenerPorcentajeDescuento(numeroPersonas);
        return ResponseEntity.ok(porcentaje);
    }

    // Calcular descuento simple (solo para consultas rápidas)
    @GetMapping("/calcular/{numeroPersonas}/{montoBase}")
    public ResponseEntity<DescuentoResponse> calcularDescuentoRapido(
            @PathVariable Integer numeroPersonas,
            @PathVariable Double montoBase) {

        if (numeroPersonas <= 0 || montoBase <= 0) {
            return ResponseEntity.badRequest().build();
        }

        DescuentoRequest request = new DescuentoRequest(montoBase, numeroPersonas);
        DescuentoResponse response = descuentoService.aplicarDescuento(request);
        return ResponseEntity.ok(response);
    }

    // Health check del servicio
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Descuento Personas Service is running!");
    }
}
