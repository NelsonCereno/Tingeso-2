package com.karting.client;

import com.karting.dto.ReservaDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@FeignClient(name = "reserva-service", path = "/api/v1/reservas")
public interface ReservaClient {

    // Obtener todas las reservas
    @GetMapping
    ResponseEntity<List<ReservaDto>> obtenerTodasLasReservas();

    // Obtener reservas por rango de fechas (para reportes)
    @GetMapping("/por-fechas")
    ResponseEntity<List<ReservaDto>> obtenerReservasPorFechas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin
    );

    // Health check
    @GetMapping("/health")
    ResponseEntity<String> healthCheck();
}