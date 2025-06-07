package com.karting.controller;

import com.karting.entity.Tarifa;
import com.karting.service.TarifaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/tarifas")
public class TarifaController {
    
    @Autowired
    private TarifaService tarifaService;
    
    @PostMapping("/inicializar")
    public ResponseEntity<String> inicializarTarifas() {
        tarifaService.inicializarTarifasPredeterminadas();
        return ResponseEntity.ok("Tarifas predeterminadas inicializadas correctamente");
    }

    @GetMapping
    public ResponseEntity<List<Tarifa>> listarTarifas() {
        List<Tarifa> tarifas = tarifaService.obtenerTodasLasTarifas();
        if (tarifas.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(tarifas);
    }

    @GetMapping("/activas")
    public ResponseEntity<List<Tarifa>> listarTarifasActivas() {
        List<Tarifa> tarifas = tarifaService.obtenerTarifasActivas();
        if (tarifas.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(tarifas);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Tarifa> obtenerTarifaPorId(@PathVariable("id") Long id) {
        Optional<Tarifa> tarifaOpt = tarifaService.obtenerTarifaPorId(id);
        return tarifaOpt.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/vueltas/{numeroVueltas}")
    public ResponseEntity<Tarifa> obtenerTarifaPorVueltas(@PathVariable("numeroVueltas") Integer numeroVueltas) {
        Optional<Tarifa> tarifaOpt = tarifaService.obtenerTarifaPorNumeroVueltas(numeroVueltas);
        return tarifaOpt.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/tipo/{tipoTarifa}")
    public ResponseEntity<Tarifa> obtenerTarifaPorTipo(@PathVariable("tipoTarifa") String tipoTarifa) {
        Optional<Tarifa> tarifaOpt = tarifaService.obtenerTarifaPorTipo(tipoTarifa);
        return tarifaOpt.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/precio-base/{numeroVueltas}")
    public ResponseEntity<Double> obtenerPrecioBasePorVueltas(@PathVariable("numeroVueltas") Integer numeroVueltas) {
        try {
            Double precio = tarifaService.obtenerPrecioBasePorNumeroVueltas(numeroVueltas);
            return ResponseEntity.ok(precio);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/precio-iva/{numeroVueltas}")
    public ResponseEntity<Double> obtenerPrecioIVAPorVueltas(@PathVariable("numeroVueltas") Integer numeroVueltas) {
        try {
            Double precio = tarifaService.obtenerPrecioIVAPorNumeroVueltas(numeroVueltas);
            return ResponseEntity.ok(precio);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/duracion/{numeroVueltas}")
    public ResponseEntity<Integer> obtenerDuracionPorVueltas(@PathVariable("numeroVueltas") Integer numeroVueltas) {
        try {
            Integer duracion = tarifaService.obtenerDuracionMinutosPorNumeroVueltas(numeroVueltas);
            return ResponseEntity.ok(duracion);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/informacion/{numeroVueltas}")
    public ResponseEntity<Map<String, Object>> obtenerInformacionTarifa(@PathVariable("numeroVueltas") Integer numeroVueltas) {
        try {
            Optional<Tarifa> tarifaOpt = tarifaService.obtenerTarifaPorNumeroVueltas(numeroVueltas);
            if (tarifaOpt.isPresent()) {
                Tarifa tarifa = tarifaOpt.get();
                Map<String, Object> response = new HashMap<>();
                response.put("tipoTarifa", tarifa.getTipoTarifa());
                response.put("numeroVueltas", tarifa.getNumeroVueltas());
                response.put("duracionMinutos", tarifa.getDuracionMinutos());
                response.put("precioBase", tarifa.getPrecioBase());
                response.put("precioIVA", tarifa.getPrecioIVA());
                return ResponseEntity.ok(response);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    public ResponseEntity<Tarifa> crearTarifa(@RequestBody Tarifa tarifa) {
        Tarifa nuevaTarifa = tarifaService.guardarTarifa(tarifa);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevaTarifa);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Tarifa> actualizarTarifa(@PathVariable("id") Long id, @RequestBody Tarifa tarifa) {
        Optional<Tarifa> tarifaOpt = tarifaService.obtenerTarifaPorId(id);
        if (tarifaOpt.isPresent()) {
            tarifa.setId(id);
            Tarifa updatedTarifa = tarifaService.guardarTarifa(tarifa);
            return ResponseEntity.ok(updatedTarifa);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarTarifa(@PathVariable("id") Long id) {
        Optional<Tarifa> tarifaOpt = tarifaService.obtenerTarifaPorId(id);
        if (tarifaOpt.isPresent()) {
            tarifaService.eliminarTarifa(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
