package com.karting.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(name = "cliente-service", path = "/api/v1/clientes")
public interface ClienteClient {
    
    // Obtener cliente por ID
    @GetMapping("/{id}")
    ResponseEntity<Map<String, Object>> obtenerClientePorId(@PathVariable Long id);
    
    // Verificar si clientes existen
    @PostMapping("/verificar-existencia")
    ResponseEntity<List<Long>> verificarExistenciaClientes(@RequestBody List<Long> clientesIds);
    
    // Incrementar visitas de clientes
    @PostMapping("/incrementar-visitas")
    ResponseEntity<List<Map<String, Object>>> incrementarVisitas(@RequestBody List<Long> clientesIds);
    
    // Obtener información básica de múltiples clientes
    @PostMapping("/informacion-basica")
    ResponseEntity<List<Map<String, Object>>> obtenerInformacionBasica(@RequestBody List<Long> clientesIds);
    
    // Verificar si hay clientes de cumpleaños
    @PostMapping("/verificar-cumpleanos")
    ResponseEntity<List<Long>> verificarClientesCumpleanos(@RequestBody List<Long> clientesIds);
    
    // Obtener número total de visitas de un cliente
    @GetMapping("/{id}/visitas")
    ResponseEntity<Integer> obtenerNumeroVisitas(@PathVariable Long id);
}