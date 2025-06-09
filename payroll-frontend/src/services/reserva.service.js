import httpClient from "../http-common";

const getAll = () => {
  return httpClient.get("/reserva-service/api/v1/reservas");
};

const create = (data) => {
  console.log("📤 Reserva Service - Enviando datos:", data);
  console.log("📍 URL:", "/reserva-service/api/v1/reservas");
  
  return httpClient.post("/reserva-service/api/v1/reservas", data)
    .then(response => {
      console.log("✅ Respuesta del servidor:", response.data);
      return response;
    })
    .catch(error => {
      console.error("❌ Error en reserva service:", error.response?.data || error.message);
      throw error;
    });
};

const get = (id) => {
  return httpClient.get(`/reserva-service/api/v1/reservas/${id}`);
};

const update = (id, data) => {
  return httpClient.put(`/reserva-service/api/v1/reservas/${id}`, data);
};

const remove = (id) => {
  return httpClient.delete(`/reserva-service/api/v1/reservas/${id}`);
};

// ✅ AGREGAR: Métodos específicos para microservicios
const calcularPrecio = (data) => {
  return httpClient.post("/reserva-service/api/v1/reservas/calcular-precio", data);
};

const verificarDisponibilidad = (fechaHora, duracionMinutos, numeroPersonas) => {
  return httpClient.get("/reserva-service/api/v1/reservas/verificar-disponibilidad", {
    params: { fechaHora, duracionMinutos, numeroPersonas }
  });
};

const validarReserva = (data) => {
  return httpClient.post("/reserva-service/api/v1/reservas/validar", data);
};

const confirmarReserva = (id) => {
  return httpClient.put(`/reserva-service/api/v1/reservas/${id}/confirmar`);
};

const cancelarReserva = (id, motivo) => {
  return httpClient.put(`/reserva-service/api/v1/reservas/${id}/cancelar`, null, {
    params: { motivo }
  });
};

const enviarComprobante = (reservaId) => {
  return httpClient.post(`/reserva-service/api/v1/reservas/${reservaId}/enviar-comprobante`)
    .then((response) => {
      console.log("✅ Comprobante enviado:", response.data);
      return response;
    })
    .catch((error) => {
      console.error("❌ Error al enviar comprobante:", error);
      throw error;
    });
};

export default { 
  getAll, 
  create, 
  get, 
  update, 
  remove,
  calcularPrecio,
  verificarDisponibilidad,
  validarReserva,
  confirmarReserva,
  cancelarReserva,
  enviarComprobante
};