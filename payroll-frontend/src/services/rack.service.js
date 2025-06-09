import httpClient from "../http-common";

// Obtener rack semanal actual
const getRackSemanalActual = () => {
  return httpClient.get("/rack-service/api/v1/rack/semanal");
};

// Obtener rack semanal por fechas
const getRackSemanalPorFechas = (fechaInicio, fechaFin) => {
  return httpClient.get("/rack-service/api/v1/rack/semanal", {
    params: { fechaInicio, fechaFin }
  });
};

// Obtener estadísticas del rack
const getEstadisticasRack = (fechaInicio, fechaFin) => {
  return httpClient.get("/rack-service/api/v1/rack/semanal/estadisticas", {
    params: { fechaInicio, fechaFin }
  });
};

// Verificar disponibilidad
const verificarDisponibilidad = (fecha, bloque, numeroPersonas) => {
  return httpClient.get("/rack-service/api/v1/rack/disponibilidad", {
    params: { fecha, bloque, numeroPersonas }
  });
};

export default {
  getRackSemanalActual,
  getRackSemanalPorFechas,
  getEstadisticasRack,
  verificarDisponibilidad
};