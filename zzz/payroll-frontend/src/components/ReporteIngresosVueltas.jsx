import React, { useState } from "react";
import reservaService from "../services/reserva.service";
import Table from "@mui/material/Table";
import TableBody from "@mui/material/TableBody";
import TableCell from "@mui/material/TableCell";
import TableContainer from "@mui/material/TableContainer";
import TableHead from "@mui/material/TableHead";
import TableRow from "@mui/material/TableRow";
import Paper from "@mui/material/Paper";
import TextField from "@mui/material/TextField";
import Button from "@mui/material/Button";
import Typography from "@mui/material/Typography";
import Box from "@mui/material/Box";
import Alert from "@mui/material/Alert";
import CircularProgress from "@mui/material/CircularProgress";

const ReporteIngresosVueltas = () => {
  const [reporte, setReporte] = useState({});
  const [inicio, setInicio] = useState("");
  const [fin, setFin] = useState("");
  const [meses, setMeses] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(false);

  // Función para formatear moneda
  const formatCurrency = (value) => {
    if (value === undefined || value === null) return '$0';
    return new Intl.NumberFormat('es-CL', { 
      style: 'currency', 
      currency: 'CLP',
      minimumFractionDigits: 0,
      maximumFractionDigits: 0
    }).format(value);
  };

  const generarReporte = () => {
    if (!inicio || !fin) {
      setError("Por favor selecciona un rango de fechas.");
      return;
    }

    setLoading(true);
    setError(null);
    setSuccess(false);

    // Formatear fechas para asegurar que usan el formato ISO
    const formattedInicio = inicio + 'T00:00:00.000Z';
    const formattedFin = fin + 'T23:59:59.999Z';

    console.log(`Solicitando reporte de vueltas desde ${inicio} hasta ${fin}`);

    // Llamar al backend
    reservaService
      .getReporteIngresosPorVueltas(inicio, fin)
      .then((response) => {
        console.log("Datos recibidos del reporte de vueltas:", response.data);
        setReporte(response.data);
        
        // Extraer todos los meses únicos del reporte
        const mesesSet = new Set();
        Object.values(response.data).forEach(categoria => {
          Object.keys(categoria)
            .filter(mes => mes !== 'TOTAL')
            .forEach(mes => mesesSet.add(mes));
        });
        
        // Ordenar los meses y agregar TOTAL al final
        const mesesOrdenados = Array.from(mesesSet).sort();
        mesesOrdenados.push('TOTAL');
        setMeses(mesesOrdenados);
        setSuccess(true);
      })
      .catch((error) => {
        console.error("Error al cargar el reporte:", error);
        setError("Error al cargar los datos. Por favor intente nuevamente.");
      })
      .finally(() => {
        setLoading(false);
      });
  };

  return (
    <Box sx={{ p: 2 }}>
      <Typography variant="h4" gutterBottom>
        Reporte de Ingresos por Número de Vueltas o Tiempo Máximo
      </Typography>
      
      <Box sx={{ mb: 3, display: 'flex', gap: 2, flexWrap: 'wrap', alignItems: 'center' }}>
        <TextField
          label="Fecha Inicio"
          type="date"
          value={inicio}
          onChange={(e) => setInicio(e.target.value)}
          InputLabelProps={{ shrink: true }}
          sx={{ minWidth: '200px' }}
        />
        <TextField
          label="Fecha Fin"
          type="date"
          value={fin}
          onChange={(e) => setFin(e.target.value)}
          InputLabelProps={{ shrink: true }}
          sx={{ minWidth: '200px' }}
        />
        <Button 
          variant="contained" 
          color="primary" 
          onClick={generarReporte}
          disabled={loading}
          startIcon={loading && <CircularProgress size={20} color="inherit" />}
        >
          {loading ? 'Generando...' : 'Generar Reporte'}
        </Button>
      </Box>

      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}

      {success && (
        <Alert severity="success" sx={{ mb: 2 }}>
          Reporte generado correctamente
        </Alert>
      )}

      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>
                <Typography fontWeight="bold">Opción de Reserva</Typography>
              </TableCell>
              {meses.map((mes) => (
                <TableCell key={mes} align="right">
                  <Typography fontWeight="bold">
                    {mes === 'TOTAL' ? 'TOTAL' : mes.charAt(0).toUpperCase() + mes.slice(1).toLowerCase()}
                  </Typography>
                </TableCell>
              ))}
            </TableRow>
          </TableHead>
          <TableBody>
            {Object.keys(reporte).length === 0 ? (
              <TableRow>
                <TableCell colSpan={meses.length + 1} align="center">
                  No hay datos disponibles para el período seleccionado
                </TableCell>
              </TableRow>
            ) : (
              Object.entries(reporte).map(([key, value]) => {
                return (
                  <TableRow key={key}>
                    <TableCell component="th" scope="row">
                      {key}
                    </TableCell>
                    {meses.map((mes) => (
                      <TableCell key={mes} align="right">
                        {mes === 'TOTAL' ? (
                          <Typography fontWeight="bold">
                            {formatCurrency(value[mes] || 0)}
                          </Typography>
                        ) : (
                          formatCurrency(value[mes] || 0)
                        )}
                      </TableCell>
                    ))}
                  </TableRow>
                );
              })
            )}
          </TableBody>
        </Table>
      </TableContainer>
    </Box>
  );
};

export default ReporteIngresosVueltas;