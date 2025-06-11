import React, { useState } from "react";
import reportsService from "../services/reports.service"; // ✅ CAMBIAR: Usar reports.service
import Button from "@mui/material/Button";
import TextField from "@mui/material/TextField";
import Table from "@mui/material/Table";
import TableBody from "@mui/material/TableBody";
import TableCell from "@mui/material/TableCell";
import TableContainer from "@mui/material/TableContainer";
import TableHead from "@mui/material/TableHead";
import TableRow from "@mui/material/TableRow";
import Paper from "@mui/material/Paper";
import Typography from "@mui/material/Typography";
import CircularProgress from "@mui/material/CircularProgress";

const ReporteIngresosVueltas = () => {
  const [fechaInicio, setFechaInicio] = useState("");
  const [fechaFin, setFechaFin] = useState("");
  const [reporteData, setReporteData] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const validarFechas = () => {
    if (!fechaInicio || !fechaFin) {
      alert("Debe seleccionar ambas fechas");
      return false;
    }

    if (new Date(fechaInicio) > new Date(fechaFin)) {
      alert("La fecha de inicio debe ser anterior a la fecha de fin");
      return false;
    }

    // Validar que no sea más de 1 año
    const diffTime = new Date(fechaFin) - new Date(fechaInicio);
    const diffDays = diffTime / (1000 * 60 * 60 * 24);
    
    if (diffDays > 365) {
      alert("El rango de fechas no puede ser mayor a 1 año");
      return false;
    }

    return true;
  };

  const generarReporte = async () => {
    if (!validarFechas()) {
      return;
    }

    setLoading(true);
    setError(null);
    setReporteData(null);

    try {
      console.log(`📊 Solicitando reporte de vueltas desde ${fechaInicio} hasta ${fechaFin}`);
      
      // ✅ CAMBIAR: Usar reportsService
      const response = await reportsService.getReporteIngresosPorVueltas(fechaInicio, fechaFin);
      
      console.log("✅ Datos del reporte recibidos:", response.data);
      console.log("🔍 Detalles por vueltas:", response.data.detallesPorVueltas); // ⭐ AGREGAR ESTA LÍNEA
      setReporteData(response.data);

    } catch (error) {
      console.error("❌ Error al generar reporte:", error);
      
      if (error.response?.status === 404) {
        setError("No se encontraron datos para el período seleccionado");
      } else if (error.response?.status === 400) {
        setError("Parámetros de fecha inválidos");
      } else {
        setError("Error del servidor: " + (error.response?.data?.mensaje || error.message));
      }
    } finally {
      setLoading(false);
    }
  };

  const formatearMoneda = (valor) => {
    if (valor == null || isNaN(valor)) return "$0";
    return `$${Number(valor).toLocaleString('es-CL')}`;
  };

  const formatearFecha = (fecha) => {
    if (!fecha) return "No especificada";
    try {
      return new Date(fecha).toLocaleDateString('es-CL');
    } catch (error) {
      return fecha;
    }
  };

  return (
    <div className="container mt-5">
      <div className="row">
        <div className="col-12">
          <Typography variant="h4" component="h1" gutterBottom>
            📊 Reporte de Ingresos por Vueltas
          </Typography>

          {/* ✅ FORMULARIO DE FILTROS */}
          <Paper elevation={3} sx={{ p: 3, mb: 3 }}>
            <Typography variant="h6" gutterBottom>
              🗓️ Seleccionar Período
            </Typography>
            
            <div className="row">
              <div className="col-md-4">
                <TextField
                  label="Fecha Inicio"
                  type="date"
                  value={fechaInicio}
                  onChange={(e) => setFechaInicio(e.target.value)}
                  fullWidth
                  InputLabelProps={{ shrink: true }}
                  inputProps={{
                    max: new Date().toISOString().split('T')[0] // No fechas futuras
                  }}
                />
              </div>
              
              <div className="col-md-4">
                <TextField
                  label="Fecha Fin"
                  type="date"
                  value={fechaFin}
                  onChange={(e) => setFechaFin(e.target.value)}
                  fullWidth
                  InputLabelProps={{ shrink: true }}
                  inputProps={{
                    min: fechaInicio, // No anterior a fecha inicio
                    max: new Date().toISOString().split('T')[0]
                  }}
                />
              </div>
              
              <div className="col-md-4 d-flex align-items-end">
                <Button
                  variant="contained"
                  color="primary"
                  onClick={generarReporte}
                  disabled={loading}
                  fullWidth
                  size="large"
                >
                  {loading ? <CircularProgress size={24} /> : "📈 Generar Reporte"}
                </Button>
              </div>
            </div>

            {/* ✅ ACCESOS RÁPIDOS */}
            <div className="row mt-3">
              <div className="col-12">
                <Typography variant="subtitle2" gutterBottom>
                  🚀 Accesos Rápidos:
                </Typography>
                <div className="btn-group" role="group">
                  <Button
                    variant="outlined"
                    size="small"
                    onClick={() => {
                      const hoy = new Date();
                      const inicioMes = new Date(hoy.getFullYear(), hoy.getMonth(), 1);
                      setFechaInicio(inicioMes.toISOString().split('T')[0]);
                      setFechaFin(hoy.toISOString().split('T')[0]);
                    }}
                  >
                    Este Mes
                  </Button>
                  <Button
                    variant="outlined"
                    size="small"
                    onClick={() => {
                      const hoy = new Date();
                      const hace30Dias = new Date(hoy);
                      hace30Dias.setDate(hoy.getDate() - 30);
                      setFechaInicio(hace30Dias.toISOString().split('T')[0]);
                      setFechaFin(hoy.toISOString().split('T')[0]);
                    }}
                  >
                    Últimos 30 días
                  </Button>
                  <Button
                    variant="outlined"
                    size="small"
                    onClick={() => {
                      const hoy = new Date();
                      const hace7Dias = new Date(hoy);
                      hace7Dias.setDate(hoy.getDate() - 7);
                      setFechaInicio(hace7Dias.toISOString().split('T')[0]);
                      setFechaFin(hoy.toISOString().split('T')[0]);
                    }}
                  >
                    Última Semana
                  </Button>
                </div>
              </div>
            </div>
          </Paper>

          {/* ✅ MOSTRAR ERRORES */}
          {error && (
            <div className="alert alert-danger">
              <strong>❌ Error:</strong> {error}
            </div>
          )}

          {/* ✅ MOSTRAR RESULTADOS */}
          {reporteData && (
            <Paper elevation={3} sx={{ p: 3 }}>
              <Typography variant="h5" gutterBottom>
                📈 Resultados del Reporte
              </Typography>
              
              {/* ✅ RESUMEN GENERAL */}
              <div className="row mb-4">
                <div className="col-md-3">
                  <div className="card bg-primary text-white">
                    <div className="card-body text-center">
                      <h5>💰 Ingresos Totales</h5>
                      <h3>{formatearMoneda(reporteData.ingresosTotales)}</h3>
                    </div>
                  </div>
                </div>
                <div className="col-md-3">
                  <div className="card bg-success text-white">
                    <div className="card-body text-center">
                      <h5>📊 Total Reservas</h5>
                      <h3>{reporteData.totalReservas || 0}</h3>
                    </div>
                  </div>
                </div>
                <div className="col-md-3">
                  <div className="card bg-info text-white">
                    <div className="card-body text-center">
                      <h5>🎯 Promedio por Reserva</h5>
                      <h3>{formatearMoneda(reporteData.promedioIngresosPorReserva)}</h3>
                    </div>
                  </div>
                </div>
                <div className="col-md-3">
                  <div className="card bg-warning text-white">
                    <div className="card-body text-center">
                      <h5>🔥 Vueltas Totales</h5>
                      <h3>{reporteData.totalVueltas || 0}</h3>
                    </div>
                  </div>
                </div>
              </div>

              {/* ✅ TABLA DETALLADA */}
              {reporteData.detallesPorVueltas && reporteData.detallesPorVueltas.length > 0 && (
                <TableContainer component={Paper} elevation={1}>
                  <Table size="small">
                    <TableHead>
                      <TableRow>
                        <TableCell><strong>🎯 Vueltas</strong></TableCell>
                        <TableCell align="right"><strong>📊 Cantidad Reservas</strong></TableCell>
                        <TableCell align="right"><strong>💰 Ingresos Totales</strong></TableCell>
                        <TableCell align="right"><strong>📈 Promedio</strong></TableCell>
                        <TableCell align="right"><strong>📊 Porcentaje</strong></TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {reporteData.detallesPorVueltas.map((detalle, index) => (
                        <TableRow key={index}>
                          <TableCell>{detalle.numeroVueltas} vueltas</TableCell>
                          <TableCell align="right">{detalle.cantidadReservas}</TableCell>
                          <TableCell align="right">{formatearMoneda(detalle.ingresosTotales)}</TableCell>
                          <TableCell align="right">{formatearMoneda(detalle.promedioIngresos)}</TableCell>
                          <TableCell align="right">
                            {((detalle.ingresosTotales / reporteData.ingresosTotales) * 100).toFixed(1)}%
                          </TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </TableContainer>
              )}

              {/* ✅ INFORMACIÓN ADICIONAL */}
              <div className="row mt-4">
                <div className="col-12">
                  <div className="alert alert-info">
                    <strong>ℹ️ Información del Reporte:</strong>
                    <ul className="mb-0 mt-2">
                      <li><strong>Período:</strong> {formatearFecha(fechaInicio)} - {formatearFecha(fechaFin)}</li>
                      <li><strong>Generado:</strong> {new Date().toLocaleString('es-CL')}</li>
                      <li><strong>Fuente:</strong> reports-service (microservicio)</li>
                    </ul>
                  </div>
                </div>
              </div>
            </Paper>
          )}

          {/* ✅ ESTADO VACÍO */}
          {!loading && !error && !reporteData && (
            <div className="text-center">
              <Typography variant="h6" color="textSecondary">
                📋 Selecciona un período y haz clic en "Generar Reporte"
              </Typography>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default ReporteIngresosVueltas;