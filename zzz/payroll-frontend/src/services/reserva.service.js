import httpClient from "../http-common";

const getAll = () => {
  return httpClient.get("/api/reservas");
};

const create = (data) => {
  return httpClient.post("/api/reservas", data);
};

const get = (id) => {
  return httpClient.get(`/api/reservas/${id}`);
};

const update = (id, data) => {
  return httpClient.put(`/api/reservas/${id}`, data);
};

const remove = (id) => {
  return httpClient.delete(`/api/reservas/${id}`);
};

const getRackSemanal = (fechaInicio, fechaFin) => {
  return httpClient.get("/api/reservas/rack-semanal", {
    params: { fechaInicio, fechaFin }
  });
};

const enviarComprobante = (reservaId) => {
  httpClient.post(`/api/reservas/${reservaId}/enviar-comprobante`)
    .then(() => alert("Comprobante enviado correctamente"))
    .catch((error) => alert("Error al enviar el comprobante: " + error.message));
};

const getReporteIngresosPorVueltas = (inicio, fin) => {
  return httpClient.get(`/api/reservas/reporte-ingresos-vueltas`, {
    params: { inicio, fin },
  });
};

const getReporteIngresosPorPersonas = (inicio, fin) => {
  return httpClient.get(`/api/reservas/reporte-ingresos-personas`, {
    params: { inicio, fin },
  });
};

export default { getAll, create, get, update, remove, getRackSemanal, enviarComprobante, getReporteIngresosPorVueltas, getReporteIngresosPorPersonas };