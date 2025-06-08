package com.karting.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "descuento-cumpleanos-service", path = "/api/v1/descuento-cumpleanos")
public interface DescuentoCumpleanosClient {
    
    @GetMapping("/calcular/{precioBase}")
    ResponseEntity<Double> calcularDescuento(@PathVariable Double precioBase);
    
    @GetMapping("/porcentaje")
    ResponseEntity<Double> obtenerPorcentajeDescuento();
    
    @GetMapping("/verificar-cumpleanos/{fechaNacimiento}")
    ResponseEntity<Boolean> esCumpleanos(@PathVariable String fechaNacimiento);
}