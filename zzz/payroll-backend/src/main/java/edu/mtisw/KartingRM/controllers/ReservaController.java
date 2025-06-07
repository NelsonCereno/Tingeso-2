package edu.mtisw.KartingRM.controllers;

import edu.mtisw.KartingRM.entities.ReservaEntity;
import edu.mtisw.KartingRM.entities.ClienteEntity;
import edu.mtisw.KartingRM.services.ReservaService;
import edu.mtisw.KartingRM.services.ComprobanteService;
import edu.mtisw.KartingRM.services.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reservas")
@CrossOrigin(origins = "*") 
public class ReservaController {

    @Autowired
    private ReservaService reservaService;

    @Autowired
    private ComprobanteService comprobanteService;

    @Autowired
    private EmailService emailService;

   
    @GetMapping
    public List<ReservaEntity> listarReservas() {
        List<ReservaEntity> reservas = reservaService.listarReservas();
        reservas.forEach(reserva -> {
            reserva.getClientes().size(); 
            reserva.getKarts().size();    
        });
        return reservas;
    }


    @PostMapping
    public ResponseEntity<ReservaEntity> crearReserva(@RequestBody ReservaEntity reserva) {
        try {
            if (reserva.getHoraReserva() == null) {
                throw new IllegalArgumentException("La hora de la reserva es obligatoria.");
            }
            ReservaEntity nuevaReserva = reservaService.crearReserva(reserva);
            return ResponseEntity.ok(nuevaReserva);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/rack-semanal")
    public ResponseEntity<Map<String, Map<String, List<ReservaEntity>>>> obtenerRackSemanal(
            @RequestParam(required = false) LocalDate fechaInicio,
            @RequestParam(required = false) LocalDate fechaFin) {
        
        Map<String, Map<String, List<ReservaEntity>>> rackSemanal;
        
        // Si no se proporcionan fechas, usar el método existente
        if (fechaInicio == null || fechaFin == null) {
            rackSemanal = reservaService.obtenerRackSemanal();
        } else {
            // Llamar al nuevo método con filtrado por fechas
            rackSemanal = reservaService.obtenerRackSemanalPorFechas(fechaInicio, fechaFin);
        }
        
        return ResponseEntity.ok(rackSemanal);
    }

    @PostMapping("/{id}/enviar-comprobante")
    public ResponseEntity<?> enviarComprobante(@PathVariable Long id) {
        try {
            ReservaEntity reserva = reservaService.obtenerReservaPorId(id);
            if (reserva == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Reserva no encontrada");
            }

            System.out.println("Reserva encontrada: " + reserva.getId());

            byte[] comprobantePdf = comprobanteService.generarComprobante(reserva);
            System.out.println("Comprobante generado correctamente");

            List<String> correosEnviados = new ArrayList<>();
            for (ClienteEntity cliente : reserva.getClientes()) {
                System.out.println("Enviando correo a: " + cliente.getEmail());
                emailService.enviarComprobante(cliente.getEmail(), comprobantePdf);
                correosEnviados.add(cliente.getEmail());
            }

            return ResponseEntity.ok(correosEnviados);
        } catch (Exception e) {
            e.printStackTrace(); 
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al enviar el comprobante: " + e.getMessage());
        }
    }

    @GetMapping("/reporte-ingresos-vueltas")
    public ResponseEntity<Map<String, Map<String, Integer>>> obtenerReporteIngresosPorVueltas(
            @RequestParam LocalDate inicio,
            @RequestParam LocalDate fin) {
        Map<String, Map<String, Integer>> reporte = reservaService.generarReporteIngresosPorVueltas(inicio, fin);
        return ResponseEntity.ok(reporte);
    }

    @GetMapping("/reporte-ingresos-personas")
    public ResponseEntity<Map<String, Map<String, Integer>>> obtenerReporteIngresosPorPersonas(
            @RequestParam LocalDate inicio,
            @RequestParam LocalDate fin) {
        Map<String, Map<String, Integer>> reporte = reservaService.generarReporteIngresosPorPersonas(inicio, fin);
        return ResponseEntity.ok(reporte);
    }
}
