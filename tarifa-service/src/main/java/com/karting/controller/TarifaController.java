package com.karting.controller;

import com.karting.entity.TarifaEntity;
import com.karting.service.TarifaService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/tarifas")

public class TarifaController {

    @Autowired
    TarifaService tarifaService;

    // Endpoints CRUD existentes
    @GetMapping("/")
    public ResponseEntity<List<TarifaEntity>> listTarifas() {
        List<TarifaEntity> tarifas = tarifaService.obtenerTarifas();
        return ResponseEntity.ok(tarifas);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TarifaEntity> getTarifaById(@PathVariable Long id) {
        TarifaEntity tarifa = tarifaService.obtenerPorId(id);
        return ResponseEntity.ok(tarifa);
    }

    @PostMapping("/")
    public ResponseEntity<TarifaEntity> saveTarifa(@RequestBody TarifaEntity tarifa) {
        TarifaEntity tarifaNueva = tarifaService.guardarTarifa(tarifa);
        return ResponseEntity.ok(tarifaNueva);
    }

    @PutMapping("/")
    public ResponseEntity<TarifaEntity> updateTarifa(@RequestBody TarifaEntity tarifa){
        TarifaEntity tarifaActualizada = tarifaService.actualizarTarifa(tarifa);
        return ResponseEntity.ok(tarifaActualizada);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> deleteTarifaById(@PathVariable Long id) throws Exception {
        var isDeleted = tarifaService.eliminarTarifa(id);
        return ResponseEntity.noContent().build();
    }

    // Nuevos endpoints basados en la funcionalidad del monolítico
    @PostMapping("/calcular")
    public ResponseEntity<?> calcularTarifa(@RequestBody Map<String, Integer> request) {
        try {
            // Validar que los parámetros estén presentes
            if (!request.containsKey("numeroVueltas") || !request.containsKey("numeroPersonas")) {
                return ResponseEntity.badRequest()
                    .body("Se requieren los parámetros 'numeroVueltas' y 'numeroPersonas'");
            }

            int numeroVueltas = request.get("numeroVueltas");
            int numeroPersonas = request.get("numeroPersonas");
            
            // Validar rangos
            if (numeroPersonas <= 0) {
                return ResponseEntity.badRequest()
                    .body("El número de personas debe ser mayor a 0");
            }
            
            TarifaEntity tarifa = tarifaService.calcularTarifa(numeroVueltas, numeroPersonas);
            return ResponseEntity.ok(tarifa);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error interno del servidor: " + e.getMessage());
        }
    }

    @GetMapping("/disponibles")
    public ResponseEntity<?> obtenerTarifasDisponibles() {
        try {
            List<TarifaEntity> tarifas = tarifaService.obtenerTarifasDisponibles();
            return ResponseEntity.ok(tarifas);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al obtener tarifas disponibles: " + e.getMessage());
        }
    }

    @GetMapping("/precio-base/{numeroVueltas}")
    public ResponseEntity<?> obtenerPrecioBase(@PathVariable int numeroVueltas) {
        try {
            double precioBase = tarifaService.obtenerPrecioBasePorVueltas(numeroVueltas);
            return ResponseEntity.ok(Map.of(
                "numeroVueltas", numeroVueltas,
                "precioBase", precioBase
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al obtener precio base: " + e.getMessage());
        }
    }

    @GetMapping("/duracion/{numeroVueltas}")
    public ResponseEntity<?> obtenerDuracion(@PathVariable int numeroVueltas) {
        try {
            int duracion = tarifaService.obtenerDuracionPorVueltas(numeroVueltas);
            return ResponseEntity.ok(Map.of(
                "numeroVueltas", numeroVueltas,
                "duracionMinutos", duracion
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al obtener duración: " + e.getMessage());
        }
    }

    @GetMapping("/calcular/{duracionMinutos}")
    public ResponseEntity<Double> calcularTarifaPorDuracion(@PathVariable Integer duracionMinutos) {
        try {
            // Validar duración
            if (duracionMinutos <= 0) {
                return ResponseEntity.badRequest().build();
            }
            
            // Lógica basada en la tabla de precios del negocio (PRECIO POR PERSONA)
            Double precioBasePorPersona = tarifaService.calcularTarifaPorDuracion(duracionMinutos);
            
            System.out.println("💰 Precio base POR PERSONA para " + duracionMinutos + " minutos: $" + precioBasePorPersona);
            return ResponseEntity.ok(precioBasePorPersona);
            
        } catch (Exception e) {
            System.err.println("❌ Error al calcular tarifa: " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Tarifa Service is running! 💰");
    }
}
