import httpClient from "../http-common";

const getAll = () => {
  return httpClient.get("/api/clientes");
};

const create = (data) => {
  return httpClient.post("/api/clientes", data);
};

const get = (id) => {
  return httpClient.get(`/api/clientes/${id}`);
};

const update = (id, data) => {
  return httpClient.put(`/api/clientes/${id}`, data);
};

const remove = (id) => {
  return httpClient.delete(`/api/clientes/${id}`);
};

export default { getAll, create, get, update, remove };