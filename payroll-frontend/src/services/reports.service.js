import httpClient from "../http-common";

// ✅ REPORTES DE INGRESOS
const getReporteIngresosPorVueltas = (fechaInicio, fechaFin) => {
  console.log(`📊 Solicitando reporte por VUELTAS desde ${fechaInicio} hasta ${fechaFin}`);
  
  return httpClient.get("/reports-service/api/v1/reports/ingresos-por-vueltas", {
    params: { fechaInicio, fechaFin }
  })
  .then(response => {
    console.log("✅ Reporte por vueltas recibido:", response.data);
    return response;
  })
  .catch(error => {
    console.error("❌ Error en reporte por vueltas:", error.response?.data || error.message);
    throw error;
  });
};

const getReporteIngresosPorPersonas = (fechaInicio, fechaFin) => {
  console.log(`📊 Solicitando reporte por PERSONAS desde ${fechaInicio} hasta ${fechaFin}`);
  
  return httpClient.get("/reports-service/api/v1/reports/ingresos-por-personas", {
    params: { fechaInicio, fechaFin }
  })
  .then(response => {
    console.log("✅ Reporte por personas recibido:", response.data);
    return response;
  })
  .catch(error => {
    console.error("❌ Error en reporte por personas:", error.response?.data || error.message);
    throw error;
  });
};

// ✅ REPORTES MENSUALES/ANUALES (diferentes a los anteriores)
const getReporteIngresosMensual = (anio, mes) => {
  console.log(`📊 Solicitando reporte mensual ${mes}/${anio}`);
  
  return httpClient.get("/reports-service/api/v1/reports/ingresos/mensual", {
    params: { anio, mes }
  });
};

const getReporteIngresosAnual = (anio) => {
  return httpClient.get("/reports-service/api/v1/reports/ingresos/anual", {
    params: { anio }
  });
};

export default {
  getReporteIngresosPorVueltas,    // ✅ Por vueltas (rango de fechas)
  getReporteIngresosPorPersonas,   // ✅ Por personas (rango de fechas)  
  getReporteIngresosMensual,       // ✅ Mensual (mes/año específico)
  getReporteIngresosAnual          // ✅ Anual (año específico)
};