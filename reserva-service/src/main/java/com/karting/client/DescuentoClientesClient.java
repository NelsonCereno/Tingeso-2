package com.karting.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "descuento-clientes-service", path = "/api/v1/descuentos/clientes")
public interface DescuentoClientesClient {
    
    // CORREGIDO: Usar el endpoint que SÍ existe
    @GetMapping("/calcular/{numeroVisitas}/{montoBase}")
    ResponseEntity<DescuentoClienteResponse> calcularDescuentoRapido(@PathVariable Integer numeroVisitas, @PathVariable Double montoBase);
    
    // Obtener solo el porcentaje de descuento por número de visitas
    @GetMapping("/porcentaje/{numeroVisitas}")
    ResponseEntity<Double> obtenerPorcentajeDescuento(@PathVariable Integer numeroVisitas);
    
    // Health check
    @GetMapping("/health")
    ResponseEntity<String> healthCheck();
    
    // DTO Response para descuento clientes
    class DescuentoClienteResponse {
        private Double montoOriginal;
        private Double porcentajeDescuento;
        private Double montoDescuento;
        private Double montoFinal;
        private String descripcion;
        private String categoriaCliente;
        private Integer numeroVisitas;
        
        public DescuentoClienteResponse() {}
        
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
        
        public String getCategoriaCliente() { return categoriaCliente; }
        public void setCategoriaCliente(String categoriaCliente) { this.categoriaCliente = categoriaCliente; }
        
        public Integer getNumeroVisitas() { return numeroVisitas; }
        public void setNumeroVisitas(Integer numeroVisitas) { this.numeroVisitas = numeroVisitas; }
    }
}