import React, { useEffect, useState } from "react";
import rackService from "../services/rack.service"; // ✅ USAR: rack.service
import { 
  Box, 
  Table, 
  TableBody, 
  TableCell, 
  TableContainer, 
  TableHead, 
  TableRow, 
  Paper, 
  Typography,
  TextField,
  Button,
  CircularProgress
} from "@mui/material";
import { format, startOfWeek, endOfWeek, addDays, parseISO } from 'date-fns';
import { es } from 'date-fns/locale';

const RackSemanal = () => {
  const [rackSemanal, setRackSemanal] = useState({});
  const [selectedDate, setSelectedDate] = useState(format(new Date(), 'yyyy-MM-dd'));
  const [loading, setLoading] = useState(true);
  const [currentWeekDates, setCurrentWeekDates] = useState([]);
  const [estadisticas, setEstadisticas] = useState({});

  const diasSemana = ["Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"];
  const bloquesHorario = ["09:00-10:00", "10:00-11:00", "11:00-12:00", "12:00-13:00", "14:00-15:00", "15:00-16:00", "16:00-17:00"];

  useEffect(() => {
    loadRackSemanal(selectedDate);
  }, []);

  const loadRackSemanal = (date) => {
    setLoading(true);
    
    // Calcular fechas de la semana
    const start = startOfWeek(new Date(date), { weekStartsOn: 1 }); // Lunes
    const end = addDays(start, 6); // Domingo
    
    const weekDates = [];
    for (let i = 0; i < 7; i++) {
      const currentDate = addDays(start, i);
      weekDates.push(currentDate);
    }
    setCurrentWeekDates(weekDates);
    
    // Formatear fechas para el backend
    const startFormatted = format(start, 'yyyy-MM-dd');
    const endFormatted = format(end, 'yyyy-MM-dd');
    
    console.log(`📅 Cargando rack semanal: ${startFormatted} - ${endFormatted}`);
    
    // ✅ USAR: rack.service con manejo de errores mejorado
    rackService.getRackSemanalPorFechas(startFormatted, endFormatted)
      .then((response) => {
        console.log("✅ Rack semanal recibido:", response.data);
        setRackSemanal(response.data.rackSemanal || response.data);
        
        // ✅ NUEVO: Cargar estadísticas también
        return rackService.getEstadisticasRack(startFormatted, endFormatted);
      })
      .then((statsResponse) => {
        console.log("📊 Estadísticas recibidas:", statsResponse.data);
        setEstadisticas(statsResponse.data);
        setLoading(false);
      })
      .catch(error => {
        console.error("❌ Error al cargar el rack semanal:", error);
        
        // ✅ FALLBACK: Mostrar rack vacío en caso de error
        const emptyRack = {};
        const dias = ['Lunes', 'Martes', 'Miércoles', 'Jueves', 'Viernes', 'Sábado', 'Domingo'];
        const bloques = ['09:00-10:00', '10:00-11:00', '11:00-12:00', '12:00-13:00', 
                        '14:00-15:00', '15:00-16:00', '16:00-17:00', '17:00-18:00', 
                        '18:00-19:00', '19:00-20:00'];
        
        dias.forEach(dia => {
          emptyRack[dia] = {};
          bloques.forEach(bloque => {
            emptyRack[dia][bloque] = [];
          });
        });
        
        setRackSemanal(emptyRack);
        setEstadisticas({ totalReservas: 0, porcentajeOcupacion: 0 });
        setLoading(false);
        
        // Mostrar error al usuario
        alert(`Error al cargar rack semanal: ${error.response?.data?.error || error.message}`);
      });
  };

  const handleDateChange = (newDate) => {
    setSelectedDate(newDate);
    loadRackSemanal(newDate);
  };

  const formatDayHeader = (date) => {
    return `${diasSemana[date.getDay() === 0 ? 6 : date.getDay() - 1]}\n${format(date, 'd MMM', { locale: es })}`;
  };

  if (loading) {
    return (
      <div className="container mt-5">
        <div className="row justify-content-center">
          <div className="col-md-6 text-center">
            <div className="spinner-border text-primary" role="status">
              <span className="visually-hidden">Cargando...</span>
            </div>
            <p className="mt-2">Cargando rack semanal...</p>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="container-fluid mt-3">
      <div className="row">
        <div className="col-md-12">
          <h2 className="text-center mb-4">🏎️ Rack Semanal de Ocupación</h2>
          
          {/* ✅ CONTROLES DE FECHA */}
          <div className="row mb-3">
            <div className="col-md-6">
              <label htmlFor="fecha-select" className="form-label">
                📅 Seleccionar semana:
              </label>
              <input
                type="date"
                id="fecha-select"
                className="form-control"
                value={selectedDate}
                onChange={(e) => handleDateChange(e.target.value)}
              />
            </div>
            <div className="col-md-6">
              {/* ✅ ESTADÍSTICAS */}
              <div className="card">
                <div className="card-body">
                  <h6 className="card-title">📊 Estadísticas</h6>
                  <p className="card-text">
                    <strong>Total Reservas:</strong> {estadisticas.totalReservas || 0}<br/>
                    <strong>Ocupación:</strong> {estadisticas.porcentajeOcupacion || 0}%
                  </p>
                </div>
              </div>
            </div>
          </div>

          {/* ✅ RACK SEMANAL */}
          <div className="table-responsive">
            <table className="table table-bordered table-hover">
              <thead className="table-dark">
                <tr>
                  <th>Horario</th>
                  {currentWeekDates.map((date, index) => {
                    const dias = ['Lunes', 'Martes', 'Miércoles', 'Jueves', 'Viernes', 'Sábado', 'Domingo'];
                    return (
                      <th key={index} className="text-center">
                        {dias[index]}<br/>
                        <small>{format(date, 'dd/MM')}</small>
                      </th>
                    );
                  })}
                </tr>
              </thead>
              <tbody>
                {Object.keys(rackSemanal.Lunes || {}).map((bloque) => (
                  <tr key={bloque}>
                    <td className="fw-bold bg-light">{bloque}</td>
                    {['Lunes', 'Martes', 'Miércoles', 'Jueves', 'Viernes', 'Sábado', 'Domingo'].map((dia) => {
                      const reservas = rackSemanal[dia]?.[bloque] || [];
                      return (
                        <td key={`${dia}-${bloque}`} className={reservas.length > 0 ? 'bg-warning' : 'bg-success'}>
                          {reservas.length > 0 ? (
                            <div>
                              {reservas.map((reserva, idx) => (
                                <div key={idx} className="small">
                                  <strong>#{reserva.id}</strong><br/>
                                  👥 {reserva.numeroPersonas} personas<br/>
                                  💰 ${reserva.precioTotal?.toLocaleString()}
                                </div>
                              ))}
                            </div>
                          ) : (
                            <div className="text-center text-muted">
                              ✅ Disponible
                            </div>
                          )}
                        </td>
                      );
                    })}
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          {/* ✅ LEYENDA */}
          <div className="row mt-3">
            <div className="col-md-12">
              <div className="alert alert-info">
                <h6>📋 Leyenda:</h6>
                <span className="badge bg-success me-2">✅ Disponible</span>
                <span className="badge bg-warning me-2">⚠️ Ocupado</span>
                <p className="mt-2 mb-0">
                  <strong>Datos en tiempo real desde microservicios:</strong> Rack Service + Reserva Service
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default RackSemanal;