package com.karting.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "descuento-clientes-service", path = "/api/v1/descuento-clientes")
public interface DescuentoClientesClient {
    
    @GetMapping("/calcular/{numeroVisitas}/{precioBase}")
    ResponseEntity<Double> calcularDescuento(@PathVariable Integer numeroVisitas, @PathVariable Double precioBase);
    
    @GetMapping("/porcentaje/{numeroVisitas}")
    ResponseEntity<Double> obtenerPorcentajeDescuento(@PathVariable Integer numeroVisitas);
}