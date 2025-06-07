package edu.mtisw.KartingRM.services;

import edu.mtisw.KartingRM.entities.ClienteEntity;
import edu.mtisw.KartingRM.entities.KartEntity;
import edu.mtisw.KartingRM.entities.ReservaEntity;
import edu.mtisw.KartingRM.repositories.ClienteRepository;
import edu.mtisw.KartingRM.repositories.KartRepository;
import edu.mtisw.KartingRM.repositories.ReservaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReservaService {

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private KartRepository kartRepository;

    public List<ReservaEntity> listarReservas() {
        return reservaRepository.findAll();
    }

    public ReservaEntity crearReserva(ReservaEntity reserva) {
        // Validar la fecha de la reserva
        validarFechaReserva(reserva);

        // Validar clientes y karts
        validarClientes(reserva.getClientes());
        validarKarts(reserva.getKarts());

        // Calcular descuentos y tarifas
        calcularTarifasYDescuentos(reserva);

        // Guardar la reserva
        return reservaRepository.save(reserva);
    }

    public ReservaEntity obtenerReservaPorId(Long id) {
        return reservaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada con ID: " + id));
    }

    private void validarFechaReserva(ReservaEntity reserva) {
        if (reserva.getFechaReserva() == null) {
            throw new IllegalArgumentException("La fecha de la reserva no puede ser nula.");
        }
    }

    private void validarClientes(List<ClienteEntity> clientes) {
        if (clientes == null || clientes.isEmpty()) {
            throw new IllegalArgumentException("Debe haber al menos un cliente en la reserva");
        }

        List<ClienteEntity> clientesCompletos = new ArrayList<>();
        for (ClienteEntity cliente : clientes) {
            ClienteEntity clienteCompleto = clienteRepository.findById(cliente.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado con ID: " + cliente.getId()));
            clientesCompletos.add(clienteCompleto);
        }

        // Reemplaza la lista original con la lista completa
        clientes.clear();
        clientes.addAll(clientesCompletos);
    }

    private void validarKarts(List<KartEntity> karts) {
        if (karts == null || karts.isEmpty()) {
            throw new IllegalArgumentException("Debe haber al menos un kart en la reserva");
        }

        for (KartEntity kart : karts) {
            KartEntity kartValidado = kartRepository.findById(kart.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Kart no encontrado con ID: " + kart.getId()));
            if (!"disponible".equalsIgnoreCase(kartValidado.getEstado().trim())) {
                throw new IllegalArgumentException("El kart con ID " + kart.getId() + " no está disponible");
            }
        }
    }

    private void calcularTarifasYDescuentos(ReservaEntity reserva) {
        int precioBase = calcularPrecioBase(reserva.getNumeroVueltas());
        reserva.setPrecioBase(precioBase);

        int precioFinal = 0;
        int descuentoPorPersonas = calcularDescuentoPorPersonas(reserva.getClientes().size());
        reserva.setDescuentoPorPersonas(descuentoPorPersonas);
        reserva.setNumeroPersonas(reserva.getClientes().size());

        int totalDescuentoPorVisitas = 0;
        int totalDescuentoPorCumpleaños = 0;

        // Si no hay clientes, evitar división por cero
        if (reserva.getClientes().isEmpty()) {
            reserva.setPrecioFinal(precioBase);
            reserva.setDuracionTotal(calcularDuracionTotal(reserva.getNumeroVueltas()));
            return;
        }
        
        for (ClienteEntity cliente : reserva.getClientes()) {
            // Calcular descuentos para este cliente
            int descuentoPorVisitas = calcularDescuentoPorVisitas(cliente.getNumeroVisitas());
            int descuentoPorCumpleaños = calcularDescuentoPorCumpleaños(cliente, reserva.getFechaReserva());

            // Acumular descuentos totales para mostrar en la reserva
            totalDescuentoPorVisitas += descuentoPorVisitas;
            totalDescuentoPorCumpleaños += descuentoPorCumpleaños;

            // Calcular precio para este cliente con sus descuentos individuales
            int descuentoTotalCliente = descuentoPorVisitas + descuentoPorCumpleaños;
            int precioCliente = precioBase - (precioBase * descuentoTotalCliente / 100);
            
            // Agregar al precio final
            precioFinal += precioCliente;
            
            // Logs para depuración
            System.out.println("Cliente: " + cliente.getNombre() + 
                             " - Visitas: " + cliente.getNumeroVisitas() + 
                             " - Descuento visitas: " + descuentoPorVisitas + "%" +
                             " - Cumpleaños: " + (descuentoPorCumpleaños > 0 ? "Sí" : "No") +
                             " - Descuento cumpleaños: " + descuentoPorCumpleaños + "%" +
                             " - Precio cliente: " + precioCliente);
        }
        
        // Aplicar descuento por número de personas al total
        if (descuentoPorPersonas > 0) {
            precioFinal = precioFinal - (precioFinal * descuentoPorPersonas / 100);
            System.out.println("Aplicando descuento por número de personas: " + descuentoPorPersonas + 
                            "% al precio total: " + precioFinal);
        }

        // Guardar los descuentos totales en la reserva (para referencia)
        reserva.setDescuentoPorVisitas(totalDescuentoPorVisitas / reserva.getClientes().size()); // Promedio
        reserva.setDescuentoPorCumpleaños(Math.min(totalDescuentoPorCumpleaños, 50)); // Limitar a 50%
        reserva.setPrecioFinal(precioFinal);
        
        // Calcular la duración total en minutos
        reserva.setDuracionTotal(calcularDuracionTotal(reserva.getNumeroVueltas()));
        
        System.out.println("Reserva - Precio Base: $" + precioBase + 
                         " - Precio Final: $" + precioFinal);
    }

    private int calcularDuracionTotal(int numeroVueltas) {
        switch (numeroVueltas) {
            case 10:
                return 30;
            case 15:
                return 35;
            case 20:
                return 40;
            default:
                throw new IllegalArgumentException("Número de vueltas no válido");
        }
    }

    private int calcularPrecioBase(int numeroVueltas) {
        switch (numeroVueltas) {
            case 10:
                return 15000;
            case 15:
                return 20000;
            case 20:
                return 25000;
            default:
                throw new IllegalArgumentException("Número de vueltas no válido");
        }
    }

    private int calcularDescuentoPorPersonas(int numeroPersonas) {
        if (numeroPersonas >= 11) return 30;
        if (numeroPersonas >= 6) return 20;
        if (numeroPersonas >= 3) return 10;
        return 0;
    }

    private int calcularDescuentoPorVisitas(int numeroVisitas) {
        if (numeroVisitas >= 7) return 30;
        if (numeroVisitas >= 5) return 20;
        if (numeroVisitas >= 2) return 10;
        return 0;
    }

    private int calcularDescuentoPorCumpleaños(ClienteEntity cliente, LocalDate fechaReserva) {
        if (cliente.getFechaNacimiento() != null &&
            cliente.getFechaNacimiento().getDayOfMonth() == fechaReserva.getDayOfMonth() &&
            cliente.getFechaNacimiento().getMonth() == fechaReserva.getMonth()) {
            return 50; // 50% de descuento por cumpleaños
        }
        return 0;
    }

    public Map<String, Map<String, List<ReservaEntity>>> obtenerRackSemanal() {
        List<ReservaEntity> reservas = reservaRepository.findAll();
        Map<String, Map<String, List<ReservaEntity>>> rackSemanal = new HashMap<>();

        // Inicializa el rack semanal con días y bloques vacíos
        String[] diasSemana = {"Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"};
        String[] bloquesHorario = {"09:00-10:00", "10:00-11:00", "11:00-12:00", "12:00-13:00", "14:00-15:00", "15:00-16:00", "16:00-17:00"};

        for (String dia : diasSemana) {
            rackSemanal.put(dia, new HashMap<>());
            for (String bloque : bloquesHorario) {
                rackSemanal.get(dia).put(bloque, new ArrayList<>());
            }
        }

        // Organiza las reservas en el rack semanal
        for (ReservaEntity reserva : reservas) {
            if (reserva.getHoraReserva() == null || reserva.getDuracionTotal() == 0) {
                System.err.println("La reserva con ID " + reserva.getId() + " no tiene una hora o duración asignada.");
                continue; // Ignora esta reserva
            }

            String dia = obtenerDiaSemana(reserva.getFechaReserva());
            LocalTime horaInicio = reserva.getHoraReserva();
            LocalTime horaFin = horaInicio.plusMinutes(reserva.getDuracionTotal());

            // Asigna la reserva a todos los bloques que ocupa
            for (String bloque : bloquesHorario) {
                LocalTime inicioBloque = obtenerHoraInicioBloque(bloque);
                LocalTime finBloque = inicioBloque.plusHours(1);

                // Verifica si el bloque se solapa con la reserva
                if ((horaInicio.isBefore(finBloque) && horaFin.isAfter(inicioBloque))) {
                    rackSemanal.get(dia).get(bloque).add(reserva);
                }
            }
        }

        return rackSemanal;
    }

    public Map<String, Map<String, List<ReservaEntity>>> obtenerRackSemanalPorFechas(LocalDate inicio, LocalDate fin) {
        // Similar al método obtenerRackSemanal pero filtrando por fechas
        List<ReservaEntity> reservas = reservaRepository.findAllByFechaReservaBetween(inicio, fin);
        Map<String, Map<String, List<ReservaEntity>>> rackSemanal = new HashMap<>();

        // Inicializa el rack semanal con días y bloques vacíos
        String[] diasSemana = {"Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"};
        String[] bloquesHorario = {"09:00-10:00", "10:00-11:00", "11:00-12:00", "12:00-13:00", "14:00-15:00", "15:00-16:00", "16:00-17:00"};

        for (String dia : diasSemana) {
            rackSemanal.put(dia, new HashMap<>());
            for (String bloque : bloquesHorario) {
                rackSemanal.get(dia).put(bloque, new ArrayList<>());
            }
        }

        // Organiza las reservas en el rack semanal
        for (ReservaEntity reserva : reservas) {
            if (reserva.getHoraReserva() == null || reserva.getDuracionTotal() == 0) {
                continue;
            }

            String dia = obtenerDiaSemana(reserva.getFechaReserva());
            LocalTime horaInicio = reserva.getHoraReserva();
            LocalTime horaFin = horaInicio.plusMinutes(reserva.getDuracionTotal());

            // Asigna la reserva a todos los bloques que ocupa
            for (String bloque : bloquesHorario) {
                LocalTime inicioBloque = obtenerHoraInicioBloque(bloque);
                LocalTime finBloque = inicioBloque.plusHours(1);

                // Verifica si el bloque se solapa con la reserva
                if ((horaInicio.isBefore(finBloque) && horaFin.isAfter(inicioBloque))) {
                    rackSemanal.get(dia).get(bloque).add(reserva);
                }
            }
        }

        return rackSemanal;
    }

    private LocalTime obtenerHoraInicioBloque(String bloque) {
        String[] partes = bloque.split("-");
        return LocalTime.parse(partes[0]); // Convierte "09:00" en LocalTime
    }

    private String obtenerDiaSemana(LocalDate fecha) {
        switch (fecha.getDayOfWeek()) {
            case MONDAY: return "Lunes";
            case TUESDAY: return "Martes";
            case WEDNESDAY: return "Miércoles";
            case THURSDAY: return "Jueves";
            case FRIDAY: return "Viernes";
            case SATURDAY: return "Sábado";
            case SUNDAY: return "Domingo";
            default: throw new IllegalArgumentException("Día no válido");
        }
    }

    private String calcularBloqueHorario(LocalTime horaReserva) {
        if (horaReserva == null) {
            throw new IllegalArgumentException("La hora de la reserva no puede ser nula.");
        }

        if (horaReserva.isBefore(LocalTime.of(10, 0))) return "09:00-10:00";
        if (horaReserva.isBefore(LocalTime.of(11, 0))) return "10:00-11:00";
        if (horaReserva.isBefore(LocalTime.of(12, 0))) return "11:00-12:00";
        if (horaReserva.isBefore(LocalTime.of(13, 0))) return "12:00-13:00";
        if (horaReserva.isBefore(LocalTime.of(15, 0))) return "14:00-15:00";
        if (horaReserva.isBefore(LocalTime.of(16, 0))) return "15:00-16:00";
        return "16:00-17:00";
    }

    public Map<String, Map<String, Integer>> generarReporteIngresosPorVueltas(LocalDate inicio, LocalDate fin) {
        List<ReservaEntity> reservas = reservaRepository.findAllByFechaReservaBetween(inicio, fin);
        Map<String, Map<String, Integer>> reporte = new HashMap<>();

        System.out.println("Generando reporte de vueltas entre " + inicio + " y " + fin);
        System.out.println("Número de reservas encontradas: " + reservas.size());

        for (ReservaEntity reserva : reservas) {
            if (reserva.getFechaReserva() == null) {
                System.out.println("Reserva ID " + reserva.getId() + " no tiene fecha asignada");
                continue;
            }
            
            // Clave basada en el número de vueltas o tiempo máximo
            String key = reserva.getNumeroVueltas() + " vueltas o máx " + reserva.getDuracionTotal() + " min";
            // Obtiene el mes en formato string
            String mes = reserva.getFechaReserva().getMonth().toString();

            // Inicializa la categoría si no existe
            reporte.putIfAbsent(key, new HashMap<>());
            Map<String, Integer> ingresosPorMes = reporte.get(key);
            
            // Precio con IVA (19%)
            int precioConIVA = (int) Math.round(reserva.getPrecioFinal() * 1.19);
            
            // Suma los ingresos del mes usando el precio con IVA
            ingresosPorMes.put(mes, ingresosPorMes.getOrDefault(mes, 0) + precioConIVA);
            
            System.out.println("Agregado a reporte - Categoría: " + key + 
                               ", Mes: " + mes + 
                               ", Precio base: " + reserva.getPrecioBase() + 
                               ", Precio final: " + reserva.getPrecioFinal() + 
                               ", Con IVA: " + precioConIVA);
        }

        // Agregar totales por categoría
        reporte.forEach((key, ingresosPorMes) -> {
            // Solo sumar valores numéricos, no la cadena "TOTAL" si ya existe
            int total = ingresosPorMes.entrySet().stream()
                    .filter(entry -> !entry.getKey().equals("TOTAL"))
                    .mapToInt(Map.Entry::getValue)
                    .sum();
            ingresosPorMes.put("TOTAL", total);
        });

        return reporte;
    }

    public Map<String, Map<String, Integer>> generarReporteIngresosPorPersonas(LocalDate inicio, LocalDate fin) {
        List<ReservaEntity> reservas = reservaRepository.findAllByFechaReservaBetween(inicio, fin);
        Map<String, Map<String, Integer>> reporte = new HashMap<>();
        
        System.out.println("Generando reporte de personas entre " + inicio + " y " + fin);
        System.out.println("Número de reservas encontradas: " + reservas.size());

        for (ReservaEntity reserva : reservas) {
            if (reserva.getFechaReserva() == null) {
                System.out.println("Reserva ID " + reserva.getId() + " no tiene fecha asignada");
                continue;
            }
            
            String key;
            int numeroPersonas = reserva.getNumeroPersonas();

            if (numeroPersonas <= 2) {
                key = "1-2 personas";
            } else if (numeroPersonas <= 5) {
                key = "3-5 personas";
            } else if (numeroPersonas <= 10) {
                key = "6-10 personas";
            } else {
                key = "11-15 personas";
            }

            String mes = reserva.getFechaReserva().getMonth().toString();

            reporte.putIfAbsent(key, new HashMap<>());
            Map<String, Integer> ingresosPorMes = reporte.get(key);
            
            // Precio con IVA (19%)
            int precioConIVA = (int) Math.round(reserva.getPrecioFinal() * 1.19);
            
            // Suma los ingresos del mes usando el precio con IVA
            ingresosPorMes.put(mes, ingresosPorMes.getOrDefault(mes, 0) + precioConIVA);
            
            System.out.println("Agregado a reporte - Categoría: " + key + 
                               ", Mes: " + mes + 
                               ", Personas: " + numeroPersonas +
                               ", Precio final: " + reserva.getPrecioFinal() + 
                               ", Con IVA: " + precioConIVA);
        }

        // Agregar totales por categoría
        reporte.forEach((key, ingresosPorMes) -> {
            // Solo sumar valores numéricos, no la cadena "TOTAL" si ya existe
            int total = ingresosPorMes.entrySet().stream()
                    .filter(entry -> !entry.getKey().equals("TOTAL"))
                    .mapToInt(Map.Entry::getValue)
                    .sum();
            ingresosPorMes.put("TOTAL", total);
        });

        return reporte;
    }
}
