import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import reservaService from "../services/reserva.service";
import clienteService from "../services/cliente.service";
import kartService from "../services/kart.service";
import Table from "@mui/material/Table";
import TableBody from "@mui/material/TableBody";
import TableCell from "@mui/material/TableCell";
import TableContainer from "@mui/material/TableContainer";
import TableHead from "@mui/material/TableHead";
import TableRow from "@mui/material/TableRow";
import Paper from "@mui/material/Paper";
import Button from "@mui/material/Button";
import AddIcon from "@mui/icons-material/Add";
import CircularProgress from "@mui/material/CircularProgress";

const ReservaList = () => {
  const [reservas, setReservas] = useState([]);
  const [clientes, setClientes] = useState({});
  const [karts, setKarts] = useState({});
  const [loading, setLoading] = useState(true);
  const [loadingComprobante, setLoadingComprobante] = useState({});
  const navigate = useNavigate();

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      setLoading(true);

      // 1. Cargar reservas
      const reservasResponse = await reservaService.getAll();
      const reservasData = reservasResponse.data || [];
      
      console.log("📋 Reservas recibidas:", reservasData);
      
      // 2. Obtener IDs únicos de clientes y karts
      const clientesIds = [...new Set(reservasData.flatMap(r => r.clientesIds || []))];
      const kartsIds = [...new Set(reservasData.flatMap(r => r.kartsIds || []))];
      
      console.log("👥 Clientes IDs:", clientesIds);
      console.log("🏎️ Karts IDs:", kartsIds);

      // 3. Cargar datos de clientes
      let clientesMap = {};
      if (clientesIds.length > 0) {
        try {
          const clientesResponse = await clienteService.obtenerClientesPorIds(clientesIds);
          clientesMap = clientesResponse.data.reduce((acc, cliente) => {
            acc[cliente.id] = cliente;
            return acc;
          }, {});
        } catch (error) {
          console.warn("⚠️ Error al cargar clientes:", error);
          // Fallback: cargar todos los clientes
          const allClientesResponse = await clienteService.getAll();
          clientesMap = allClientesResponse.data.reduce((acc, cliente) => {
            acc[cliente.id] = cliente;
            return acc;
          }, {});
        }
      }

      // 4. Cargar datos de karts
      let kartsMap = {};
      if (kartsIds.length > 0) {
        try {
          const kartsResponse = await kartService.obtenerKartsPorIds(kartsIds);
          kartsMap = kartsResponse.data.reduce((acc, kart) => {
            acc[kart.id] = kart;
            return acc;
          }, {});
        } catch (error) {
          console.warn("⚠️ Error al cargar karts:", error);
          // Fallback: cargar todos los karts
          const allKartsResponse = await kartService.getAll();
          kartsMap = allKartsResponse.data.reduce((acc, kart) => {
            acc[kart.id] = kart;
            return acc;
          }, {});
        }
      }

      setReservas(reservasData);
      setClientes(clientesMap);
      setKarts(kartsMap);

    } catch (error) {
      console.error("❌ Error al cargar datos:", error);
      alert("Error al cargar las reservas");
    } finally {
      setLoading(false);
    }
  };

  const enviarComprobante = async (reservaId) => {
    setLoadingComprobante(prev => ({ ...prev, [reservaId]: true }));
    
    try {
      const response = await reservaService.enviarComprobante(reservaId);
      const data = response.data;
      
      if (data.correosEnviados && data.correosEnviados.length > 0) {
        alert(`✅ Comprobante enviado exitosamente a:\n${data.correosEnviados.join("\n")}`);
      } else {
        alert("✅ Comprobante procesado correctamente");
      }
    } catch (error) {
      console.error("❌ Error al enviar comprobante:", error);
      alert("❌ Error al enviar el comprobante: " + (error.response?.data?.error || error.message));
    } finally {
      setLoadingComprobante(prev => ({ ...prev, [reservaId]: false }));
    }
  };

  const formatearFecha = (fechaHora) => {
    if (!fechaHora) return "No especificada";
    try {
      return new Date(fechaHora).toLocaleString('es-CL');
    } catch (error) {
      return fechaHora;
    }
  };

  const getEstadoBadge = (estado) => {
    const badgeStyles = {
      PENDIENTE: "badge bg-warning",
      CONFIRMADA: "badge bg-success", 
      EN_PROCESO: "badge bg-info",
      COMPLETADA: "badge bg-primary",
      CANCELADA: "badge bg-danger"
    };
    
    return <span className={badgeStyles[estado] || "badge bg-secondary"}>{estado}</span>;
  };

  if (loading) {
    return (
      <div className="container mt-5 text-center">
        <CircularProgress />
        <p className="mt-2">Cargando reservas...</p>
      </div>
    );
  }

  return (
    <div className="container mt-5">
      <div className="row">
        <div className="col-12">
          <h2>📋 Lista de Reservas - Microservicios</h2>

          {/* Botón para crear nueva reserva */}
          <Button
            variant="contained"
            color="primary"
            startIcon={<AddIcon />}
            onClick={() => navigate("/reservas/add")}
            style={{ marginBottom: "20px" }}
          >
            Nueva Reserva
          </Button>

          {reservas.length === 0 ? (
            <div className="alert alert-info">
              📝 No hay reservas registradas. 
              <button 
                className="btn btn-link" 
                onClick={() => navigate("/reservas/add")}
              >
                Crear la primera reserva
              </button>
            </div>
          ) : (
            <TableContainer component={Paper}>
              <Table size="small">
                <TableHead>
                  <TableRow>
                    <TableCell><strong>ID</strong></TableCell>
                    <TableCell><strong>👥 Clientes</strong></TableCell>
                    <TableCell><strong>🏎️ Karts</strong></TableCell>
                    <TableCell><strong>📅 Fecha/Hora</strong></TableCell>
                    <TableCell><strong>⏱️ Duración</strong></TableCell>
                    <TableCell><strong>👥 Personas</strong></TableCell>
                    <TableCell><strong>💰 Precio Total</strong></TableCell>
                    <TableCell><strong>📊 Estado</strong></TableCell>
                    <TableCell><strong>⚙️ Acciones</strong></TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {reservas.map((reserva) => (
                    <TableRow key={reserva.id}>
                      <TableCell>{reserva.id}</TableCell>
                      
                      {/* ✅ CLIENTES: Usar clientesIds */}
                      <TableCell>
                        {(reserva.clientesIds || []).map((clienteId, index) => {
                          const cliente = clientes[clienteId];
                          return (
                            <div key={index} className="small">
                              {cliente ? (
                                <span className="badge bg-light text-dark me-1">
                                  {cliente.nombre} {cliente.apellido}
                                </span>
                              ) : (
                                <span className="badge bg-secondary me-1">
                                  Cliente #{clienteId}
                                </span>
                              )}
                            </div>
                          );
                        })}
                      </TableCell>
                      
                      {/* ✅ KARTS: Usar kartsIds */}
                      <TableCell>
                        {(reserva.kartsIds || []).map((kartId, index) => {
                          const kart = karts[kartId];
                          return (
                            <div key={index} className="small">
                              {kart ? (
                                <span className="badge bg-info text-white me-1">
                                  {kart.codigo}
                                </span>
                              ) : (
                                <span className="badge bg-secondary me-1">
                                  Kart #{kartId}
                                </span>
                              )}
                            </div>
                          );
                        })}
                      </TableCell>
                      
                      <TableCell>{formatearFecha(reserva.fechaHora)}</TableCell>
                      <TableCell>{reserva.duracionMinutos || 0} min</TableCell>
                      <TableCell>{reserva.numeroPersonas || 0}</TableCell>
                      <TableCell>
                        <div>
                          <strong>${(reserva.precioTotal || 0).toLocaleString()}</strong>
                          {reserva.descuentoTotal > 0 && (
                            <small className="text-success d-block">
                              Descuento: ${reserva.descuentoTotal.toLocaleString()}
                            </small>
                          )}
                        </div>
                      </TableCell>
                      <TableCell>{getEstadoBadge(reserva.estado)}</TableCell>
                      
                      {/* ✅ ACCIONES */}
                      <TableCell>
                        <div className="btn-group-vertical" style={{ gap: '4px' }}>
                          <Button
                            variant="contained"
                            color="primary"
                            size="small"
                            onClick={() => enviarComprobante(reserva.id)}
                            disabled={loadingComprobante[reserva.id]}
                          >
                            {loadingComprobante[reserva.id] ? "Enviando..." : "📧 Comprobante"}
                          </Button>
                          
                          <Button
                            variant="outlined"
                            size="small"
                            onClick={() => navigate(`/reservas/edit/${reserva.id}`)}
                          >
                            ✏️ Editar
                          </Button>
                        </div>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          )}

          {/* ✅ ESTADÍSTICAS */}
          <div className="row mt-4">
            <div className="col-md-12">
              <div className="alert alert-info">
                <h6>📊 Resumen:</h6>
                <p className="mb-0">
                  <strong>Total reservas:</strong> {reservas.length} | 
                  <strong> Confirmadas:</strong> {reservas.filter(r => r.estado === 'CONFIRMADA').length} | 
                  <strong> Pendientes:</strong> {reservas.filter(r => r.estado === 'PENDIENTE').length} | 
                  <strong> Completadas:</strong> {reservas.filter(r => r.estado === 'COMPLETADA').length}
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ReservaList;