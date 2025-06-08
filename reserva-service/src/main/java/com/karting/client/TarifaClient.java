package com.karting.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "tarifa-service", path = "/api/v1/tarifas")
public interface TarifaClient {
    
    @GetMapping("/calcular/{duracionMinutos}")
    ResponseEntity<Double> calcularTarifa(@PathVariable Integer duracionMinutos);
    
    @GetMapping("/tarifa-base")
    ResponseEntity<Double> obtenerTarifaBase();
    
    @GetMapping("/duracion/{duracion}")
    ResponseEntity<Double> obtenerTarifaPorDuracion(@PathVariable Integer duracion);
}