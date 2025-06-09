import httpClient from "../http-common";

const getAll = () => {
  return httpClient.get("/cliente-service/api/v1/clientes"); // ✅ Ruta de microservicio
};

const create = (data) => {
  return httpClient.post("/cliente-service/api/v1/clientes", data);
};

const get = (id) => {
  return httpClient.get(`/cliente-service/api/v1/clientes/${id}`);
};

const update = (id, data) => {
  return httpClient.put(`/cliente-service/api/v1/clientes/${id}`, data);
};

const remove = (id) => {
  return httpClient.delete(`/cliente-service/api/v1/clientes/${id}`);
};

// ✅ AGREGAR: Método para obtener múltiples clientes
const obtenerClientesPorIds = (ids) => {
  return httpClient.post("/cliente-service/api/v1/clientes/obtener-multiples", ids);
};

export default { 
  getAll, 
  create, 
  get, 
  update, 
  remove,
  obtenerClientesPorIds
};