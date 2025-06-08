package com.karting.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "descuento-cumpleanos-service", path = "/api/v1/descuentos/cumpleanos")
public interface DescuentoCumpleanosClient {
    
    // CORREGIDO: Usar el endpoint que SÍ existe
    @PostMapping("/aplicar")
    ResponseEntity<DescuentoCumpleanosResponse> aplicarDescuento(@RequestBody DescuentoCumpleanosRequest request);
    
    // Health check
    @GetMapping("/health")
    ResponseEntity<String> healthCheck();
    
    // DTO Request para descuento cumpleaños
    class DescuentoCumpleanosRequest {
        private Double montoBase;
        private Integer numeroPersonas;
        private List<Long> clientesIds;
        
        public DescuentoCumpleanosRequest() {}
        
        public DescuentoCumpleanosRequest(Double montoBase, Integer numeroPersonas, List<Long> clientesIds) {
            this.montoBase = montoBase;
            this.numeroPersonas = numeroPersonas;
            this.clientesIds = clientesIds;
        }
        
        // Getters y setters
        public Double getMontoBase() { return montoBase; }
        public void setMontoBase(Double montoBase) { this.montoBase = montoBase; }
        
        public Integer getNumeroPersonas() { return numeroPersonas; }
        public void setNumeroPersonas(Integer numeroPersonas) { this.numeroPersonas = numeroPersonas; }
        
        public List<Long> getClientesIds() { return clientesIds; }
        public void setClientesIds(List<Long> clientesIds) { this.clientesIds = clientesIds; }
    }
    
    // DTO Response para descuento cumpleaños
    class DescuentoCumpleanosResponse {
        private Double montoOriginal;
        private Double porcentajeDescuento;
        private Double montoDescuento;
        private Double montoFinal;
        private String descripcion;
        private Integer personasCumpleanos;
        private List<Long> clientesCumpleanos;
        
        public DescuentoCumpleanosResponse() {}
        
        // Getters y setters
        public Double getMontoOriginal() { return montoOriginal; }
        public void setMontoOriginal(Double montoOriginal) { this.montoOriginal = montoOriginal; }
        
        public Double getPorcentajeDescuento() { return porcentajeDescuento; }
        public void setPorcentajeDescuento(Double porcentajeDescuento) { this.porcentajeDescuento = porcentajeDescuento; }
        
        public Double getMontoDescuento() { return montoDescuento; }
        public void setMontoDescuento(Double montoDescuento) { this.montoDescuento = montoDescuento; }
        
        public Double getMontoFinal() { return montoFinal; }
        public void setMontoFinal(Double montoFinal) { this.montoFinal = montoFinal; }
        
        public String getDescripcion() { return descripcion; }
        public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
        
        public Integer getPersonasCumpleanos() { return personasCumpleanos; }
        public void setPersonasCumpleanos(Integer personasCumpleanos) { this.personasCumpleanos = personasCumpleanos; }
        
        public List<Long> getClientesCumpleanos() { return clientesCumpleanos; }
        public void setClientesCumpleanos(List<Long> clientesCumpleanos) { this.clientesCumpleanos = clientesCumpleanos; }
    }
}