package com.karting.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "descuento-personas-service", path = "/api/v1/descuentos/personas")
public interface DescuentoPersonasClient {
    
    // CORREGIDO: Usar el endpoint que SÍ existe
    @GetMapping("/calcular/{numeroPersonas}/{montoBase}")
    ResponseEntity<DescuentoResponse> calcularDescuentoRapido(@PathVariable Integer numeroPersonas, @PathVariable Double montoBase);
    
    // Obtener solo el porcentaje de descuento por número de personas
    @GetMapping("/porcentaje/{numeroPersonas}")
    ResponseEntity<Double> obtenerPorcentajeDescuento(@PathVariable Integer numeroPersonas);
    
    // Health check
    @GetMapping("/health")
    ResponseEntity<String> healthCheck();
    
    // DTO Response para descuento personas
    class DescuentoResponse {
        private Double montoOriginal;
        private Double porcentajeDescuento;
        private Double montoDescuento;
        private Double montoFinal;
        private String descripcion;
        
        public DescuentoResponse() {}
        
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
    }
}