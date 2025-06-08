package com.karting.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "cliente-service", path = "/api/v1/clientes")
public interface ClienteClient {
    
    // Verificar existencia de múltiples clientes
    @PostMapping("/verificar-existencia")
    ResponseEntity<Boolean> verificarExistenciaClientes(@RequestBody List<Long> clientesIds);
    
    // Verificar cliente individual
    @GetMapping("/verificar/{clienteId}")
    ResponseEntity<Boolean> verificarCliente(@PathVariable Long clienteId);
    
    // Obtener número de visitas de un cliente
    @GetMapping("/{clienteId}/visitas")
    ResponseEntity<Integer> obtenerNumeroVisitas(@PathVariable Long clienteId);
    
    // Verificar clientes que están de cumpleaños
    @PostMapping("/verificar-cumpleanos")
    ResponseEntity<List<Long>> verificarClientesCumpleanos(@RequestBody List<Long> clientesIds);
    
    // Health check
    @GetMapping("/health")
    ResponseEntity<String> healthCheck();

    // Obtener cliente por ID
    @GetMapping("/{clienteId}")
    ResponseEntity<Object> obtenerClientePorId(@PathVariable Long clienteId);
}