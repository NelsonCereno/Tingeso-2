import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import reservaService from "../services/reserva.service";
import clienteService from "../services/cliente.service";
import kartService from "../services/kart.service";
import TextField from "@mui/material/TextField";
import Button from "@mui/material/Button";
import Select from "@mui/material/Select";
import MenuItem from "@mui/material/MenuItem";
import FormControl from "@mui/material/FormControl";
import InputLabel from "@mui/material/InputLabel";
import Chip from "@mui/material/Chip";
import Box from "@mui/material/Box";

const AddEditReserva = () => {
  const [clientes, setClientes] = useState([]);
  const [karts, setKarts] = useState([]);
  const [selectedClientes, setSelectedClientes] = useState([]);
  const [selectedKarts, setSelectedKarts] = useState([]);
  const [planSeleccionado, setPlanSeleccionado] = useState("");
  const [fechaReserva, setFechaReserva] = useState("");
  const [horaReserva, setHoraReserva] = useState("");
  const [observaciones, setObservaciones] = useState("");
  const [loading, setLoading] = useState(false);
  
  const navigate = useNavigate();

  // ✅ ACTUALIZAR: Planes con duración en minutos (no vueltas)
  const planesDisponibles = [
    { 
      id: "basico", 
      nombre: "Plan Básico", 
      duracionMinutos: 30, 
      descripcion: "30 minutos de karting",
      precio: 15000 
    },
    { 
      id: "intermedio", 
      nombre: "Plan Intermedio", 
      duracionMinutos: 60, 
      descripcion: "1 hora de karting",
      precio: 25000 
    },
    { 
      id: "premium", 
      nombre: "Plan Premium", 
      duracionMinutos: 90, 
      descripcion: "1.5 horas de karting",
      precio: 35000 
    }
  ];

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      const [clientesResponse, kartsResponse] = await Promise.all([
        clienteService.getAll(),
        kartService.getAll()
      ]);
      
      setClientes(clientesResponse.data || []);
      setKarts(kartsResponse.data || []);
    } catch (error) {
      console.error("Error al cargar datos:", error);
      alert("Error al cargar clientes y karts");
    }
  };

  const validarFormulario = () => {
    if (selectedClientes.length === 0) {
      alert("Debe seleccionar al menos un cliente");
      return false;
    }

    if (!planSeleccionado) {
      alert("Debe seleccionar un plan");
      return false;
    }

    if (!fechaReserva) {
      alert("Debe seleccionar una fecha");
      return false;
    }

    if (!horaReserva) {
      alert("Debe seleccionar una hora");
      return false;
    }

    // Validar que la fecha sea futura
    const fechaHoraSeleccionada = new Date(`${fechaReserva}T${horaReserva}`);
    const ahora = new Date();
    
    if (fechaHoraSeleccionada <= ahora) {
      alert("La fecha y hora deben ser futuras");
      return false;
    }

    return true;
  };

  const saveReserva = async (e) => {
    e.preventDefault();
    
    if (!validarFormulario()) {
      return;
    }

    setLoading(true);

    try {
      const planElegido = planesDisponibles.find(p => p.id === planSeleccionado);
      
      // ✅ FORMATO CORRECTO para microservicio
      const reservaRequest = {
        fechaHora: `${fechaReserva}T${horaReserva}:00`, // ISO 8601 format
        duracionMinutos: planElegido.duracionMinutos,
        numeroPersonas: selectedClientes.length,
        clientesIds: selectedClientes, // ✅ Solo IDs, no objetos
        kartsIds: selectedKarts.length > 0 ? selectedKarts : null, // ✅ Asignación automática si está vacío
        observaciones: observaciones || `Reserva ${planElegido.nombre} desde frontend`
      };

      console.log("📤 Enviando reserva:", reservaRequest);

      const response = await reservaService.create(reservaRequest);
      
      console.log("✅ Reserva creada:", response.data);
      alert("Reserva creada exitosamente");
      navigate("/reservas/list");

    } catch (error) {
      console.error("❌ Error al crear reserva:", error);
      
      if (error.response?.status === 400) {
        alert("Error: Datos inválidos o conflicto de horarios");
      } else if (error.response?.status === 409) {
        alert("Error: No hay karts disponibles para el horario seleccionado");
      } else {
        alert("Error del servidor: " + (error.response?.data?.mensaje || error.message));
      }
    } finally {
      setLoading(false);
    }
  };

  const generarHorariosDisponibles = () => {
    const horarios = [];
    for (let hora = 8; hora <= 20; hora++) {
      horarios.push(`${hora.toString().padStart(2, '0')}:00`);
      horarios.push(`${hora.toString().padStart(2, '0')}:30`);
    }
    return horarios;
  };

  return (
    <div className="container mt-5">
      <div className="row justify-content-center">
        <div className="col-md-8">
          <div className="card">
            <div className="card-header">
              <h3>📋 Nueva Reserva - Microservicios</h3>
            </div>
            <div className="card-body">
              <form onSubmit={saveReserva}>
                
                {/* ✅ SELECCIÓN DE CLIENTES */}
                <FormControl fullWidth margin="normal">
                  <InputLabel>👥 Clientes *</InputLabel>
                  <Select
                    multiple
                    value={selectedClientes}
                    onChange={(e) => setSelectedClientes(e.target.value)}
                    renderValue={(selected) => (
                      <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5 }}>
                        {selected.map((clienteId) => {
                          const cliente = clientes.find(c => c.id === clienteId);
                          return (
                            <Chip 
                              key={clienteId} 
                              label={cliente ? `${cliente.nombre} ${cliente.apellido}` : `Cliente #${clienteId}`}
                              size="small"
                            />
                          );
                        })}
                      </Box>
                    )}
                  >
                    {clientes.map((cliente) => (
                      <MenuItem key={cliente.id} value={cliente.id}>
                        {cliente.nombre} {cliente.apellido} - {cliente.email}
                        {cliente.numeroVisitas >= 5 && (
                          <span className="badge bg-warning ms-2">Frecuente</span>
                        )}
                      </MenuItem>
                    ))}
                  </Select>
                  <small className="form-text text-muted">
                    Selecciona uno o más clientes para la reserva
                  </small>
                </FormControl>

                {/* ✅ SELECCIÓN DE KARTS (OPCIONAL) */}
                <FormControl fullWidth margin="normal">
                  <InputLabel>🏎️ Karts (Opcional)</InputLabel>
                  <Select
                    multiple
                    value={selectedKarts}
                    onChange={(e) => setSelectedKarts(e.target.value)}
                    renderValue={(selected) => (
                      <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5 }}>
                        {selected.map((kartId) => {
                          const kart = karts.find(k => k.id === kartId);
                          return (
                            <Chip 
                              key={kartId} 
                              label={kart ? kart.codigo : `Kart #${kartId}`}
                              size="small"
                              color="info"
                            />
                          );
                        })}
                      </Box>
                    )}
                  >
                    {karts.filter(kart => kart.estado === 'DISPONIBLE').map((kart) => (
                      <MenuItem key={kart.id} value={kart.id}>
                        {kart.codigo} - {kart.estado}
                        <small className="text-muted ms-2">
                          ({kart.numeroUsos} usos)
                        </small>
                      </MenuItem>
                    ))}
                  </Select>
                  <small className="form-text text-muted">
                    Deja vacío para asignación automática de karts disponibles
                  </small>
                </FormControl>

                {/* ✅ SELECCIÓN DE PLAN */}
                <FormControl fullWidth margin="normal">
                  <InputLabel>📦 Plan *</InputLabel>
                  <Select
                    value={planSeleccionado}
                    onChange={(e) => setPlanSeleccionado(e.target.value)}
                  >
                    {planesDisponibles.map((plan) => (
                      <MenuItem key={plan.id} value={plan.id}>
                        <div>
                          <strong>{plan.nombre}</strong>
                          <br />
                          <small>{plan.descripcion} - ${plan.precio.toLocaleString()}</small>
                        </div>
                      </MenuItem>
                    ))}
                  </Select>
                </FormControl>

                {/* ✅ FECHA */}
                <TextField
                  label="📅 Fecha de Reserva *"
                  type="date"
                  value={fechaReserva}
                  onChange={(e) => setFechaReserva(e.target.value)}
                  fullWidth
                  margin="normal"
                  InputLabelProps={{ shrink: true }}
                  inputProps={{
                    min: new Date().toISOString().split('T')[0] // No fechas pasadas
                  }}
                />

                {/* ✅ HORA */}
                <FormControl fullWidth margin="normal">
                  <InputLabel>⏰ Hora de Reserva *</InputLabel>
                  <Select
                    value={horaReserva}
                    onChange={(e) => setHoraReserva(e.target.value)}
                  >
                    {generarHorariosDisponibles().map((hora) => (
                      <MenuItem key={hora} value={hora}>
                        {hora}
                      </MenuItem>
                    ))}
                  </Select>
                </FormControl>

                {/* ✅ OBSERVACIONES */}
                <TextField
                  label="📝 Observaciones"
                  multiline
                  rows={3}
                  value={observaciones}
                  onChange={(e) => setObservaciones(e.target.value)}
                  fullWidth
                  margin="normal"
                  placeholder="Observaciones adicionales para la reserva..."
                />

                {/* ✅ RESUMEN */}
                {selectedClientes.length > 0 && planSeleccionado && (
                  <div className="alert alert-info mt-3">
                    <h6>📊 Resumen de la Reserva:</h6>
                    <p><strong>Clientes:</strong> {selectedClientes.length}</p>
                    <p><strong>Plan:</strong> {planesDisponibles.find(p => p.id === planSeleccionado)?.nombre}</p>
                    <p><strong>Duración:</strong> {planesDisponibles.find(p => p.id === planSeleccionado)?.duracionMinutos} minutos</p>
                    {fechaReserva && horaReserva && (
                      <p><strong>Fecha/Hora:</strong> {fechaReserva} a las {horaReserva}</p>
                    )}
                    {selectedKarts.length > 0 ? (
                      <p><strong>Karts:</strong> {selectedKarts.length} específicos</p>
                    ) : (
                      <p><strong>Karts:</strong> Asignación automática</p>
                    )}
                  </div>
                )}

                {/* ✅ BOTONES */}
                <div className="d-grid gap-2 mt-4">
                  <Button 
                    type="submit" 
                    variant="contained" 
                    color="primary"
                    disabled={loading}
                    size="large"
                  >
                    {loading ? "Creando Reserva..." : "🎯 Crear Reserva"}
                  </Button>
                  
                  <Button 
                    type="button" 
                    variant="outlined"
                    onClick={() => navigate("/reservas/list")}
                  >
                    Cancelar
                  </Button>
                </div>
              </form>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default AddEditReserva;