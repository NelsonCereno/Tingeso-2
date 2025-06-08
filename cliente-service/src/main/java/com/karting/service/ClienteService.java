package com.karting.service;

import com.karting.dto.ClienteRequest;
import com.karting.dto.ClienteResponse;
import com.karting.entity.ClienteEntity;
import com.karting.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    @PostConstruct
    public void inicializarDatos() {
        // Solo insertar si la tabla está vacía
        if (clienteRepository.count() == 0) {
            List<ClienteEntity> clientesIniciales = List.of(
                new ClienteEntity("Juan Pérez", LocalDate.of(1990, 6, 15), "juan.perez@email.com", "123456789"),
                new ClienteEntity("María García", LocalDate.of(1985, 3, 22), "maria.garcia@email.com", "987654321"),
                new ClienteEntity("Carlos López", LocalDate.of(1992, 12, 8), "carlos.lopez@email.com", "555123456"),
                new ClienteEntity("Ana Martínez", LocalDate.of(1988, 9, 30), "ana.martinez@email.com", "444987654"),
                new ClienteEntity("Pedro Silva", LocalDate.of(1995, 1, 12), "pedro.silva@email.com", "333555777")
            );
            
            // Asignar diferentes números de visitas para testing
            clientesIniciales.get(0).setNumeroVisitas(1);  // Cliente nuevo
            clientesIniciales.get(1).setNumeroVisitas(3);  // Cliente ocasional
            clientesIniciales.get(2).setNumeroVisitas(5);  // Cliente regular
            clientesIniciales.get(3).setNumeroVisitas(8);  // Cliente frecuente
            clientesIniciales.get(4).setNumeroVisitas(12); // Cliente VIP
            
            clienteRepository.saveAll(clientesIniciales);
            System.out.println("Clientes iniciales creados en la base de datos");
        }
    }

    // CRUD básico
    
    // Crear cliente
    public ClienteResponse crearCliente(ClienteRequest request) {
        // Validar que el email no exista
        if (clienteRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Ya existe un cliente con este email: " + request.getEmail());
        }
        
        // Verificar clientes similares por nombre
        List<ClienteEntity> clientesSimilares = clienteRepository.findClientesSimilaresPorNombre(request.getNombre());
        if (!clientesSimilares.isEmpty()) {
            System.out.println("Advertencia: Existen clientes con nombres similares: " + 
                clientesSimilares.stream().map(ClienteEntity::getNombre).collect(Collectors.joining(", ")));
        }
        
        ClienteEntity cliente = new ClienteEntity(
            request.getNombre(),
            request.getFechaNacimiento(),
            request.getEmail(),
            request.getTelefono()
        );
        
        ClienteEntity clienteGuardado = clienteRepository.save(cliente);
        return new ClienteResponse(clienteGuardado);
    }
    
    // Obtener cliente por ID
    public ClienteResponse obtenerClientePorId(Long id) {
        Optional<ClienteEntity> clienteOpt = clienteRepository.findById(id);
        if (clienteOpt.isPresent()) {
            return new ClienteResponse(clienteOpt.get());
        }
        throw new RuntimeException("Cliente no encontrado con ID: " + id);
    }
    
    // Obtener cliente por email
    public ClienteResponse obtenerClientePorEmail(String email) {
        Optional<ClienteEntity> clienteOpt = clienteRepository.findByEmail(email);
        if (clienteOpt.isPresent()) {
            return new ClienteResponse(clienteOpt.get());
        }
        throw new RuntimeException("Cliente no encontrado con email: " + email);
    }
    
    // Obtener todos los clientes activos
    public List<ClienteResponse> obtenerTodosLosClientes() {
        List<ClienteEntity> clientes = clienteRepository.findByActivoTrue();
        return clientes.stream()
                .map(ClienteResponse::new)
                .collect(Collectors.toList());
    }
    
    // Actualizar cliente
    public ClienteResponse actualizarCliente(Long id, ClienteRequest request) {
        Optional<ClienteEntity> clienteOpt = clienteRepository.findById(id);
        if (clienteOpt.isPresent()) {
            ClienteEntity cliente = clienteOpt.get();
            
            // Verificar si el email cambió y si ya existe
            if (!cliente.getEmail().equals(request.getEmail()) && 
                clienteRepository.existsByEmail(request.getEmail())) {
                throw new RuntimeException("Ya existe un cliente con este email: " + request.getEmail());
            }
            
            cliente.setNombre(request.getNombre());
            cliente.setFechaNacimiento(request.getFechaNacimiento());
            cliente.setEmail(request.getEmail());
            cliente.setTelefono(request.getTelefono());
            
            ClienteEntity clienteActualizado = clienteRepository.save(cliente);
            return new ClienteResponse(clienteActualizado);
        }
        throw new RuntimeException("Cliente no encontrado con ID: " + id);
    }
    
    // Eliminar cliente (soft delete)
    public void eliminarCliente(Long id) {
        Optional<ClienteEntity> clienteOpt = clienteRepository.findById(id);
        if (clienteOpt.isPresent()) {
            ClienteEntity cliente = clienteOpt.get();
            cliente.setActivo(false);
            clienteRepository.save(cliente);
        } else {
            throw new RuntimeException("Cliente no encontrado con ID: " + id);
        }
    }
    
    // Reactivar cliente
    public ClienteResponse reactivarCliente(Long id) {
        Optional<ClienteEntity> clienteOpt = clienteRepository.findById(id);
        if (clienteOpt.isPresent()) {
            ClienteEntity cliente = clienteOpt.get();
            cliente.setActivo(true);
            ClienteEntity clienteReactivado = clienteRepository.save(cliente);
            return new ClienteResponse(clienteReactivado);
        }
        throw new RuntimeException("Cliente no encontrado con ID: " + id);
    }

    // Métodos para el orquestador (ReservaService)
    
    // Obtener múltiples clientes por IDs (para reservas con varios clientes)
    public List<ClienteResponse> obtenerClientesPorIds(List<Long> ids) {
        List<ClienteEntity> clientes = clienteRepository.findClientesByIds(ids);
        return clientes.stream()
                .map(ClienteResponse::new)
                .collect(Collectors.toList());
    }
    
    // Incrementar número de visitas (cuando se crea una reserva)
    public ClienteResponse incrementarVisitas(Long clienteId) {
        Optional<ClienteEntity> clienteOpt = clienteRepository.findById(clienteId);
        if (clienteOpt.isPresent()) {
            ClienteEntity cliente = clienteOpt.get();
            cliente.incrementarVisitas();
            ClienteEntity clienteActualizado = clienteRepository.save(cliente);
            return new ClienteResponse(clienteActualizado);
        }
        throw new RuntimeException("Cliente no encontrado con ID: " + clienteId);
    }
    
    // Incrementar visitas para múltiples clientes
    public List<ClienteResponse> incrementarVisitasMultiples(List<Long> clientesIds) {
        return clientesIds.stream()
                .map(this::incrementarVisitas)
                .collect(Collectors.toList());
    }

    // Métodos de búsqueda
    
    // Buscar clientes por nombre
    public List<ClienteResponse> buscarClientesPorNombre(String nombre) {
        List<ClienteEntity> clientes = clienteRepository.findByNombreContainingIgnoreCase(nombre);
        return clientes.stream()
                .map(ClienteResponse::new)
                .collect(Collectors.toList());
    }
    
    // Buscar clientes por número de visitas
    public List<ClienteResponse> buscarClientesPorVisitas(Integer numeroVisitas) {
        List<ClienteEntity> clientes = clienteRepository.findByNumeroVisitas(numeroVisitas);
        return clientes.stream()
                .map(ClienteResponse::new)
                .collect(Collectors.toList());
    }
    
    // Buscar clientes por rango de visitas
    public List<ClienteResponse> buscarClientesPorRangoVisitas(Integer visitasMin, Integer visitasMax) {
        List<ClienteEntity> clientes = clienteRepository.findClientesPorRangoVisitas(visitasMin, visitasMax);
        return clientes.stream()
                .map(ClienteResponse::new)
                .collect(Collectors.toList());
    }

    // Métodos de segmentación (para marketing y descuentos)
    
    // Obtener clientes frecuentes (7+ visitas)
    public List<ClienteResponse> obtenerClientesFrecuentes() {
        List<ClienteEntity> clientes = clienteRepository.findClientesFrecuentes();
        return clientes.stream()
                .map(ClienteResponse::new)
                .collect(Collectors.toList());
    }
    
    // Obtener clientes nuevos (1-2 visitas)
    public List<ClienteResponse> obtenerClientesNuevos() {
        List<ClienteEntity> clientes = clienteRepository.findClientesNuevos();
        return clientes.stream()
                .map(ClienteResponse::new)
                .collect(Collectors.toList());
    }
    
    // Obtener clientes que cumplen años hoy
    public List<ClienteResponse> obtenerClientesQueCumplenHoy() {
        List<ClienteEntity> clientes = clienteRepository.findClientesQueCumplenHoy(LocalDate.now());
        return clientes.stream()
                .map(ClienteResponse::new)
                .collect(Collectors.toList());
    }
    
    // Obtener clientes que cumplen años en una fecha específica
    public List<ClienteResponse> obtenerClientesQueCumplenEnFecha(LocalDate fecha) {
        List<ClienteEntity> clientes = clienteRepository.findClientesQueCumplenHoy(fecha);
        return clientes.stream()
                .map(ClienteResponse::new)
                .collect(Collectors.toList());
    }
    
    // Obtener clientes por mes de cumpleaños
    public List<ClienteResponse> obtenerClientesPorMesCumpleanos(Integer mes) {
        List<ClienteEntity> clientes = clienteRepository.findClientesPorMesCumpleanos(mes);
        return clientes.stream()
                .map(ClienteResponse::new)
                .collect(Collectors.toList());
    }

    // Métodos de validación y utilidad
    
    // Verificar si un email ya existe
    public boolean existeEmail(String email) {
        return clienteRepository.existsByEmail(email);
    }
    
    // Verificar si un cliente existe
    public boolean existeCliente(Long id) {
        return clienteRepository.existsById(id);
    }
    
    // Verificar si un cliente está activo
    public boolean clienteEstaActivo(Long id) {
        Optional<ClienteEntity> clienteOpt = clienteRepository.findById(id);
        return clienteOpt.map(ClienteEntity::getActivo).orElse(false);
    }

    // Métodos de estadísticas y reportes
    
    // Obtener estadísticas generales
    public EstadisticasClientesResponse obtenerEstadisticasGenerales() {
        Long totalClientes = clienteRepository.countClientesActivos();
        Double promedioVisitas = clienteRepository.findPromedioVisitas();
        Integer maxVisitas = clienteRepository.findMaxVisitas();
        Integer minVisitas = clienteRepository.findMinVisitas();
        
        return new EstadisticasClientesResponse(totalClientes, promedioVisitas, maxVisitas, minVisitas);
    }
    
    // Obtener distribución de clientes por visitas
    public List<Object[]> obtenerDistribucionPorVisitas() {
        return clienteRepository.countClientesPorVisitas();
    }
    
    // Obtener estadísticas de registros por mes
    public List<Object[]> obtenerEstadisticasRegistrosPorMes() {
        return clienteRepository.findEstadisticasRegistrosPorMes();
    }
    
    // Obtener top clientes
    public List<ClienteResponse> obtenerTopClientes(int limite) {
        List<ClienteEntity> clientes = clienteRepository.findTopClientesPorVisitas();
        return clientes.stream()
                .limit(limite)
                .map(ClienteResponse::new)
                .collect(Collectors.toList());
    }

    // Clase para estadísticas
    public static class EstadisticasClientesResponse {
        private Long totalClientes;
        private Double promedioVisitas;
        private Integer maxVisitas;
        private Integer minVisitas;

        public EstadisticasClientesResponse(Long totalClientes, Double promedioVisitas, Integer maxVisitas, Integer minVisitas) {
            this.totalClientes = totalClientes;
            this.promedioVisitas = promedioVisitas;
            this.maxVisitas = maxVisitas;
            this.minVisitas = minVisitas;
        }

        // Getters y Setters
        public Long getTotalClientes() { return totalClientes; }
        public void setTotalClientes(Long totalClientes) { this.totalClientes = totalClientes; }

        public Double getPromedioVisitas() { return promedioVisitas; }
        public void setPromedioVisitas(Double promedioVisitas) { this.promedioVisitas = promedioVisitas; }

        public Integer getMaxVisitas() { return maxVisitas; }
        public void setMaxVisitas(Integer maxVisitas) { this.maxVisitas = maxVisitas; }

        public Integer getMinVisitas() { return minVisitas; }
        public void setMinVisitas(Integer minVisitas) { this.minVisitas = minVisitas; }
    }
}
