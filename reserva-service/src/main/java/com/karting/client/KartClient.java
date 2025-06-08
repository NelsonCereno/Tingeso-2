package com.karting.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(name = "kart-service", path = "/api/v1/karts")
public interface KartClient {
    
    // Obtener karts disponibles
    @GetMapping("/disponibles")
    ResponseEntity<List<Map<String, Object>>> obtenerKartsDisponibles();
    
    // Verificar disponibilidad de karts específicos
    @PostMapping("/verificar-disponibilidad")
    ResponseEntity<List<Map<String, Object>>> verificarDisponibilidadKarts(@RequestBody List<Long> kartsIds);
    
    // Reservar karts
    @PostMapping("/reservar")
    ResponseEntity<List<Map<String, Object>>> reservarKarts(@RequestBody List<Long> kartsIds);
    
    // Liberar karts
    @PostMapping("/liberar")
    ResponseEntity<List<Map<String, Object>>> liberarKarts(@RequestBody List<Long> kartsIds);
    
    // Obtener capacidad disponible
    @GetMapping("/capacidad-disponible")
    ResponseEntity<Long> obtenerCapacidadDisponible();
    
    // Obtener karts optimizados (menos usados primero)
    @GetMapping("/disponibles/optimizados/{cantidad}")
    ResponseEntity<List<Map<String, Object>>> obtenerKartsDisponiblesOptimizados(@PathVariable int cantidad);
    
    // Verificar si karts están disponibles (validación rápida)
    @PostMapping("/estan-disponibles")
    ResponseEntity<Boolean> kartsEstanDisponibles(@RequestBody List<Long> kartsIds);
}