import React, { useState } from "react";
import reportsService from "../services/reports.service"; // ✅ Usar reports service
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

const ReporteIngresosPersonas = () => {
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
      console.log(`📊 Solicitando reporte por PERSONAS desde ${fechaInicio} hasta ${fechaFin}`);
      
      // ✅ LLAMAR AL ENDPOINT CORRECTO PARA PERSONAS
      const response = await reportsService.getReporteIngresosPorPersonas(fechaInicio, fechaFin);
      
      console.log("✅ Datos del reporte por personas recibidos:", response.data);
      setReporteData(response.data);

    } catch (error) {
      console.error("❌ Error al generar reporte por personas:", error);
      
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

  const formatearCategoriaPersonas = (categoria) => {
    const categorias = {
      "1-2 personas": "👥 1-2 Personas",
      "3-5 personas": "👨‍👩‍👧 3-5 Personas", 
      "6-10 personas": "👨‍👩‍👧‍👦 6-10 Personas",
      "11-15 personas": "🎉 11-15 Personas (Grupos Grandes)"
    };
    return categorias[categoria] || categoria;
  };

  return (
    <div className="container mt-5">
      <div className="row">
        <div className="col-12">
          <Typography variant="h4" component="h1" gutterBottom>
            👥 Reporte de Ingresos por Número de Personas
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
                    min: fechaInicio
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
                📈 Resultados del Reporte por Personas
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
                      <h5>👥 Total Personas</h5>
                      <h3>{reporteData.totalPersonas || 0}</h3>
                    </div>
                  </div>
                </div>
                <div className="col-md-3">
                  <div className="card bg-warning text-white">
                    <div className="card-body text-center">
                      <h5>💵 Promedio por Persona</h5>
                      <h3>{formatearMoneda(reporteData.promedioIngresosPorPersona)}</h3>
                    </div>
                  </div>
                </div>
              </div>

              {/* ✅ TABLA POR CATEGORÍAS DE PERSONAS */}
              {reporteData.detallesPorPersonas && reporteData.detallesPorPersonas.length > 0 && (
                <TableContainer component={Paper} elevation={1}>
                  <Table>
                    <TableHead>
                      <TableRow>
                        <TableCell><strong>👥 Categoría</strong></TableCell>
                        <TableCell align="right"><strong>📊 Reservas</strong></TableCell>
                        <TableCell align="right"><strong>👥 Total Personas</strong></TableCell>
                        <TableCell align="right"><strong>💰 Ingresos</strong></TableCell>
                        <TableCell align="right"><strong>📈 Promedio</strong></TableCell>
                        <TableCell align="right"><strong>📊 % del Total</strong></TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {reporteData.detallesPorPersonas.map((detalle, index) => (
                        <TableRow key={index}>
                          <TableCell>{formatearCategoriaPersonas(detalle.categoriaPersonas)}</TableCell>
                          <TableCell align="right">{detalle.cantidadReservas}</TableCell>
                          <TableCell align="right">{detalle.totalPersonas}</TableCell>
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

              {/* ✅ ANÁLISIS ADICIONAL */}
              <div className="row mt-4">
                <div className="col-md-6">
                  <div className="card">
                    <div className="card-header">
                      <h6>📊 Distribución por Tamaño de Grupo</h6>
                    </div>
                    <div className="card-body">
                      {reporteData.detallesPorPersonas && reporteData.detallesPorPersonas.map((detalle, index) => (
                        <div key={index} className="mb-2">
                          <div className="d-flex justify-content-between">
                            <span>{formatearCategoriaPersonas(detalle.categoriaPersonas)}</span>
                            <span><strong>{detalle.cantidadReservas} reservas</strong></span>
                          </div>
                          <div className="progress" style={{ height: '6px' }}>
                            <div 
                              className="progress-bar" 
                              style={{ 
                                width: `${(detalle.ingresosTotales / reporteData.ingresosTotales) * 100}%`,
                                backgroundColor: ['#007bff', '#28a745', '#ffc107', '#dc3545'][index % 4]
                              }}
                            ></div>
                          </div>
                        </div>
                      ))}
                    </div>
                  </div>
                </div>
                
                <div className="col-md-6">
                  <div className="alert alert-info">
                    <h6>💡 Insights del Reporte:</h6>
                    <ul className="mb-0">
                      <li><strong>Categoría más popular:</strong> {
                        reporteData.detallesPorPersonas && 
                        reporteData.detallesPorPersonas.length > 0 && 
                        formatearCategoriaPersonas(
                          reporteData.detallesPorPersonas
                            .sort((a, b) => b.cantidadReservas - a.cantidadReservas)[0]?.categoriaPersonas
                        )
                      }</li>
                      <li><strong>Mayor ingreso:</strong> {
                        reporteData.detallesPorPersonas && 
                        reporteData.detallesPorPersonas.length > 0 && 
                        formatearCategoriaPersonas(
                          reporteData.detallesPorPersonas
                            .sort((a, b) => b.ingresosTotales - a.ingresosTotales)[0]?.categoriaPersonas
                        )
                      }</li>
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

export default ReporteIngresosPersonas;