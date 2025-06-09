import React, { useEffect, useState } from "react";
import reservaService from "../services/reserva.service";
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
  const [loading, setLoading] = useState(false);
  const [currentWeekDates, setCurrentWeekDates] = useState([]);
  const [bloquesHorario, setBloquesHorario] = useState([]);

  const diasSemana = ["Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"];

  const ordenarBloques = (bloques) => {
    return bloques.sort((a, b) => {
      try {
        const horaA = a.split('-')[0];
        const horaB = b.split('-')[0];
        return horaA.localeCompare(horaB);
      } catch (error) {
        return 0;
      }
    });
  };

  useEffect(() => {
    loadRackSemanal(selectedDate);
  }, []);

  const loadRackSemanal = (date) => {
    setLoading(true);
    
    const dateObj = parseISO(date);
    const start = startOfWeek(dateObj, { weekStartsOn: 1 });
    const end = endOfWeek(dateObj, { weekStartsOn: 1 });
    
    const weekDates = [];
    for (let i = 0; i < 7; i++) {
      const currentDate = addDays(start, i);
      weekDates.push(currentDate);
    }
    setCurrentWeekDates(weekDates);
    
    const startFormatted = format(start, 'yyyy-MM-dd');
    const endFormatted = format(end, 'yyyy-MM-dd');
    
    reservaService.getRackSemanal(startFormatted, endFormatted)
      .then((response) => {
        setRackSemanal(response.data);
        
        const bloquesUnicos = new Set();
        Object.values(response.data).forEach(dia => {
          if (dia && typeof dia === 'object') {
            Object.keys(dia).forEach(bloque => {
              bloquesUnicos.add(bloque);
            });
          }
        });
        
        const bloquesOrdenados = ordenarBloques(Array.from(bloquesUnicos));
        setBloquesHorario(bloquesOrdenados);
        
        setLoading(false);
      })
      .catch(error => {
        console.error("Error al cargar el rack semanal:", error);
        setLoading(false);
      });
  };

  const handleDateChange = (e) => {
    setSelectedDate(e.target.value);
  };

  const handleLoadWeek = () => {
    loadRackSemanal(selectedDate);
  };

  const formatDayHeader = (date) => {
    return `${diasSemana[date.getDay() === 0 ? 6 : date.getDay() - 1]}\n${format(date, 'd MMM', { locale: es })}`;
  };

  return (
    <Box sx={{ p: 2 }}>
      <Typography variant="h4" gutterBottom>
        Rack Semanal de Ocupación
      </Typography>
      
      <Box sx={{ mb: 3, display: 'flex', gap: 2, flexWrap: 'wrap', alignItems: 'center' }}>
        <TextField
          label="Seleccionar Semana"
          type="date"
          value={selectedDate}
          onChange={handleDateChange}
          InputLabelProps={{ shrink: true }}
          sx={{ minWidth: '200px' }}
        />
        <Button 
          variant="contained" 
          color="primary" 
          onClick={handleLoadWeek}
          disabled={loading}
        >
          {loading ? <CircularProgress size={24} /> : 'Ver Semana'}
        </Button>
      </Box>
      
      {currentWeekDates.length > 0 && (
        <Box sx={{ mb: 2 }}>
          <Typography variant="subtitle1">
            Semana del {format(currentWeekDates[0], 'd MMM', { locale: es })} al {format(currentWeekDates[6], 'd MMM yyyy', { locale: es })}
          </Typography>
        </Box>
      )}

      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Bloque</TableCell>
              {currentWeekDates.map((date, index) => (
                <TableCell key={index} align="center">
                  <Typography fontWeight="bold">
                    {formatDayHeader(date)}
                  </Typography>
                </TableCell>
              ))}
            </TableRow>
          </TableHead>
          <TableBody>
            {loading ? (
              <TableRow>
                <TableCell colSpan={8} align="center" sx={{ py: 5 }}>
                  <CircularProgress />
                  <Typography variant="body2" sx={{ mt: 2 }}>
                    Cargando horarios...
                  </Typography>
                </TableCell>
              </TableRow>
            ) : (
              bloquesHorario.map((bloque) => (
                <TableRow key={bloque}>
                  <TableCell component="th" scope="row">
                    {bloque}
                  </TableCell>
                  {diasSemana.map((dia, index) => (
                    <TableCell key={dia} align="center" sx={{
                      bgcolor: rackSemanal[dia]?.hasOwnProperty(bloque) && rackSemanal[dia][bloque]?.length > 0 ? '#e8f5e9' : 'inherit'
                    }}>
                      {rackSemanal[dia] && rackSemanal[dia][bloque] && rackSemanal[dia][bloque].length > 0
                        ? rackSemanal[dia][bloque].map((reserva, index) => (
                            <Typography key={index} variant="body2" sx={{ mb: 0.5 }}>
                              {reserva.clientes.map((cliente) => cliente.nombre).join(", ")}
                            </Typography>
                          ))
                        : <Typography variant="body2" color="text.secondary">Libre</Typography>}
                    </TableCell>
                  ))}
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </TableContainer>
    </Box>
  );
};

export default RackSemanal;