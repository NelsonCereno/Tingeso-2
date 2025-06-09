import httpClient from "../http-common";

const getAll = () => {
  return httpClient.get("/kart-service/api/v1/karts");
};

const create = (data) => {
  return httpClient.post("/kart-service/api/v1/karts", data);
};

const get = (id) => {
  return httpClient.get(`/kart-service/api/v1/karts/${id}`);
};

const update = (id, data) => {
  return httpClient.put(`/kart-service/api/v1/karts/${id}`, data);
};

const remove = (id) => {
  return httpClient.delete(`/kart-service/api/v1/karts/${id}`);
};

// ✅ AGREGAR: Método para obtener múltiples karts
const obtenerKartsPorIds = (ids) => {
  return httpClient.post("/kart-service/api/v1/karts/obtener-multiples", ids);
};

export default { 
  getAll, 
  create, 
  get, 
  update, 
  remove,
  obtenerKartsPorIds
};