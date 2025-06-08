package com.karting.controller;

import com.karting.dto.ClienteRequest;
import com.karting.dto.ClienteResponse;
import com.karting.service.ClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/clientes")
@CrossOrigin("*")
public class ClienteController {

    @Autowired
    private ClienteService clienteService;

    // Health check del servicio
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Cliente Service is running!");
    }

    // CRUD básico
    
    // Crear cliente
    @PostMapping
    public ResponseEntity<ClienteResponse> crearCliente(@RequestBody ClienteRequest request) {
        try {
            if (request.getNombre() == null || request.getNombre().trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            
            ClienteResponse response = clienteService.crearCliente(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Obtener todos los clientes
    @GetMapping
    public ResponseEntity<List<ClienteResponse>> obtenerTodosLosClientes() {
        List<ClienteResponse> clientes = clienteService.obtenerTodosLosClientes();
        if (clientes.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(clientes);
    }
    
    // Obtener cliente por ID
    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponse> obtenerClientePorId(@PathVariable Long id) {
        try {
            ClienteResponse response = clienteService.obtenerClientePorId(id);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // Obtener cliente por email
    @GetMapping("/email/{email}")
    public ResponseEntity<ClienteResponse> obtenerClientePorEmail(@PathVariable String email) {
        try {
            ClienteResponse response = clienteService.obtenerClientePorEmail(email);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // Actualizar cliente
    @PutMapping("/{id}")
    public ResponseEntity<ClienteResponse> actualizarCliente(@PathVariable Long id, @RequestBody ClienteRequest request) {
        try {
            if (request.getNombre() == null || request.getNombre().trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            
            ClienteResponse response = clienteService.actualizarCliente(id, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Eliminar cliente (soft delete)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCliente(@PathVariable Long id) {
        try {
            clienteService.eliminarCliente(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // Reactivar cliente
    @PutMapping("/{id}/reactivar")
    public ResponseEntity<ClienteResponse> reactivarCliente(@PathVariable Long id) {
        try {
            ClienteResponse response = clienteService.reactivarCliente(id);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Métodos para el orquestador (ReservaService)
    
    // Obtener múltiples clientes por IDs (PRINCIPAL para el orquestador)
    @PostMapping("/obtener-multiples")
    public ResponseEntity<List<ClienteResponse>> obtenerClientesPorIds(@RequestBody List<Long> ids) {
        try {
            if (ids == null || ids.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            
            List<ClienteResponse> clientes = clienteService.obtenerClientesPorIds(ids);
            if (clientes.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(clientes);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Incrementar visitas de un cliente (cuando confirma reserva)
    @PutMapping("/{id}/incrementar-visitas")
    public ResponseEntity<ClienteResponse> incrementarVisitas(@PathVariable Long id) {
        try {
            ClienteResponse response = clienteService.incrementarVisitas(id);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // Incrementar visitas de múltiples clientes (PRINCIPAL para el orquestador)
    @PutMapping("/incrementar-visitas-multiples")
    public ResponseEntity<List<ClienteResponse>> incrementarVisitasMultiples(@RequestBody List<Long> clientesIds) {
        try {
            if (clientesIds == null || clientesIds.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            
            List<ClienteResponse> clientes = clienteService.incrementarVisitasMultiples(clientesIds);
            return ResponseEntity.ok(clientes);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Métodos de búsqueda
    
    // Buscar clientes por nombre
    @GetMapping("/buscar/nombre/{nombre}")
    public ResponseEntity<List<ClienteResponse>> buscarClientesPorNombre(@PathVariable String nombre) {
        List<ClienteResponse> clientes = clienteService.buscarClientesPorNombre(nombre);
        if (clientes.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(clientes);
    }
    
    // Buscar clientes por número de visitas exacto
    @GetMapping("/buscar/visitas/{numeroVisitas}")
    public ResponseEntity<List<ClienteResponse>> buscarClientesPorVisitas(@PathVariable Integer numeroVisitas) {
        if (numeroVisitas < 0) {
            return ResponseEntity.badRequest().build();
        }
        
        List<ClienteResponse> clientes = clienteService.buscarClientesPorVisitas(numeroVisitas);
        if (clientes.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(clientes);
    }
    
    // Buscar clientes por rango de visitas
    @GetMapping("/buscar/visitas/{visitasMin}/{visitasMax}")
    public ResponseEntity<List<ClienteResponse>> buscarClientesPorRangoVisitas(
            @PathVariable Integer visitasMin, 
            @PathVariable Integer visitasMax) {
        
        if (visitasMin < 0 || visitasMax < 0 || visitasMin > visitasMax) {
            return ResponseEntity.badRequest().build();
        }
        
        List<ClienteResponse> clientes = clienteService.buscarClientesPorRangoVisitas(visitasMin, visitasMax);
        if (clientes.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(clientes);
    }

    // Métodos de segmentación (para marketing y descuentos)
    
    // Obtener clientes frecuentes (7+ visitas)
    @GetMapping("/segmentos/frecuentes")
    public ResponseEntity<List<ClienteResponse>> obtenerClientesFrecuentes() {
        List<ClienteResponse> clientes = clienteService.obtenerClientesFrecuentes();
        if (clientes.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(clientes);
    }
    
    // Obtener clientes nuevos (1-2 visitas)
    @GetMapping("/segmentos/nuevos")
    public ResponseEntity<List<ClienteResponse>> obtenerClientesNuevos() {
        List<ClienteResponse> clientes = clienteService.obtenerClientesNuevos();
        if (clientes.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(clientes);
    }

    // Métodos de cumpleaños (para descuento-cumpleanos-service)
    
    // Obtener clientes que cumplen años hoy
    @GetMapping("/cumpleanos/hoy")
    public ResponseEntity<List<ClienteResponse>> obtenerClientesQueCumplenHoy() {
        List<ClienteResponse> clientes = clienteService.obtenerClientesQueCumplenHoy();
        if (clientes.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(clientes);
    }
    
    // Obtener clientes que cumplen años en fecha específica (PRINCIPAL para orquestador)
    @GetMapping("/cumpleanos/fecha/{fecha}")
    public ResponseEntity<List<ClienteResponse>> obtenerClientesQueCumplenEnFecha(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        
        List<ClienteResponse> clientes = clienteService.obtenerClientesQueCumplenEnFecha(fecha);
        if (clientes.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(clientes);
    }
    
    // Obtener clientes por mes de cumpleaños
    @GetMapping("/cumpleanos/mes/{mes}")
    public ResponseEntity<List<ClienteResponse>> obtenerClientesPorMesCumpleanos(@PathVariable Integer mes) {
        if (mes < 1 || mes > 12) {
            return ResponseEntity.badRequest().build();
        }
        
        List<ClienteResponse> clientes = clienteService.obtenerClientesPorMesCumpleanos(mes);
        if (clientes.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(clientes);
    }

    // Métodos de validación (para el orquestador)
    
    // Verificar si un email ya existe
    @GetMapping("/existe/email/{email}")
    public ResponseEntity<Boolean> existeEmail(@PathVariable String email) {
        boolean existe = clienteService.existeEmail(email);
        return ResponseEntity.ok(existe);
    }
    
    // Verificar si un cliente existe
    @GetMapping("/existe/{id}")
    public ResponseEntity<Boolean> existeCliente(@PathVariable Long id) {
        boolean existe = clienteService.existeCliente(id);
        return ResponseEntity.ok(existe);
    }
    
    // Verificar si un cliente está activo
    @GetMapping("/activo/{id}")
    public ResponseEntity<Boolean> clienteEstaActivo(@PathVariable Long id) {
        boolean activo = clienteService.clienteEstaActivo(id);
        return ResponseEntity.ok(activo);
    }

    // Métodos de estadísticas y reportes
    
    // Obtener estadísticas generales
    @GetMapping("/estadisticas/generales")
    public ResponseEntity<ClienteService.EstadisticasClientesResponse> obtenerEstadisticasGenerales() {
        ClienteService.EstadisticasClientesResponse estadisticas = clienteService.obtenerEstadisticasGenerales();
        return ResponseEntity.ok(estadisticas);
    }
    
    // Obtener distribución de clientes por visitas
    @GetMapping("/estadisticas/distribucion-visitas")
    public ResponseEntity<List<Object[]>> obtenerDistribucionPorVisitas() {
        List<Object[]> distribucion = clienteService.obtenerDistribucionPorVisitas();
        return ResponseEntity.ok(distribucion);
    }
    
    // Obtener estadísticas de registros por mes
    @GetMapping("/estadisticas/registros-por-mes")
    public ResponseEntity<List<Object[]>> obtenerEstadisticasRegistrosPorMes() {
        List<Object[]> estadisticas = clienteService.obtenerEstadisticasRegistrosPorMes();
        return ResponseEntity.ok(estadisticas);
    }
    
    // Obtener top clientes
    @GetMapping("/top/{limite}")
    public ResponseEntity<List<ClienteResponse>> obtenerTopClientes(@PathVariable int limite) {
        if (limite <= 0) {
            return ResponseEntity.badRequest().build();
        }
        
        List<ClienteResponse> topClientes = clienteService.obtenerTopClientes(limite);
        if (topClientes.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(topClientes);
    }
}
