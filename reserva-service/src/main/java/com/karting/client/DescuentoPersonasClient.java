package com.karting.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "descuento-personas-service", path = "/api/v1/descuento-personas")
public interface DescuentoPersonasClient {
    
    @GetMapping("/calcular/{numeroPersonas}/{precioBase}")
    ResponseEntity<Double> calcularDescuento(@PathVariable Integer numeroPersonas, @PathVariable Double precioBase);
    
    @GetMapping("/porcentaje/{numeroPersonas}")
    ResponseEntity<Double> obtenerPorcentajeDescuento(@PathVariable Integer numeroPersonas);
}