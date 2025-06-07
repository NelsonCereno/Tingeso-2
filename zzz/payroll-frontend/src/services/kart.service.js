import httpClient from "../http-common";

const getAll = () => {
  return httpClient.get("/api/karts");
};

const create = (data) => {
  return httpClient.post("/api/karts", data);
};

const get = (id) => {
  return httpClient.get(`/api/karts/${id}`);
};

const update = (id, data) => {
  return httpClient.put(`/api/karts/${id}`, data);
};

const remove = (id) => {
  return httpClient.delete(`/api/karts/${id}`);
};

export default { getAll, create, get, update, remove };