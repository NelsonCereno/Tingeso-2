import axios from "axios";

const httpClient = axios.create({
  // ✅ USAR: Variables de entorno para construir la URL del gateway
  baseURL: `http://${import.meta.env.VITE_PAYROLL_BACKEND_SERVER}:${import.meta.env.VITE_PAYROLL_BACKEND_PORT}`,
  headers: {
    "Content-Type": "application/json",
  },
});

// ✅ AGREGAR: Interceptor para logging (útil para debug)
httpClient.interceptors.request.use(
  (config) => {
    console.log(`🚀 ${config.method?.toUpperCase()} ${config.baseURL}${config.url}`);
    return config;
  },
  (error) => {
    console.error("❌ Error en request:", error);
    return Promise.reject(error);
  }
);

httpClient.interceptors.response.use(
  (response) => {
    console.log(`✅ ${response.status} ${response.config.url}`);
    return response;
  },
  (error) => {
    console.error(`❌ ${error.response?.status || 'Network Error'} ${error.config?.url}`);
    return Promise.reject(error);
  }
);

export default httpClient;