import { useState, useEffect } from "react";
import kartService from "../services/kart.service";
import { useNavigate, useParams } from "react-router-dom";
import Button from "@mui/material/Button";
import TextField from "@mui/material/TextField";
import MenuItem from "@mui/material/MenuItem";

const AddEditKart = () => {
  const [codigo, setCodigo] = useState("");
  const [estado, setEstado] = useState("DISPONIBLE"); // ✅ CORREGIR: Usar enum del backend
  const [observaciones, setObservaciones] = useState(""); // ✅ AGREGAR: Campo observaciones
  const [loading, setLoading] = useState(false);
  const [editing, setEditing] = useState(false);
  const navigate = useNavigate();
  const { id } = useParams();

  // ✅ CORREGIR: Estados válidos según el backend
  const estadosValidos = [
    { value: "DISPONIBLE", label: "Disponible" },
    { value: "MANTENIMIENTO", label: "En Mantenimiento" },
    { value: "FUERA_SERVICIO", label: "Fuera de Servicio" }
    // ❌ NO incluir RESERVADO (se gestiona automáticamente)
  ];

  // ✅ CÓDIGOS válidos (K001-K999)
  const codigosValidos = Array.from({ length: 999 }, (_, i) =>
    `K${String(i + 1).padStart(3, "0")}`
  );

  useEffect(() => {
    if (id) {
      setEditing(true);
      loadKart(id);
    }
  }, [id]);

  const loadKart = async (kartId) => {
    try {
      const response = await kartService.get(kartId);
      const kart = response.data;
      
      setCodigo(kart.codigo || "");
      setEstado(kart.estado || "DISPONIBLE");
      setObservaciones(kart.observaciones || "");
    } catch (error) {
      console.error("Error al cargar kart:", error);
      alert("Error al cargar kart");
    }
  };

  const validateCodigo = (value) => {
    if (!value || value.trim() === "") {
      alert("El código es obligatorio");
      return false;
    }
    
    // Validar formato K### (K001, K002, etc.)
    const codigoRegex = /^K\d{3}$/;
    if (!codigoRegex.test(value)) {
      alert("El código debe tener el formato K### (ejemplo: K001, K012)");
      return false;
    }
    
    return true;
  };

  const validateEstado = (value) => {
    const estadosPermitidos = estadosValidos.map(e => e.value);
    if (!estadosPermitidos.includes(value)) {
      alert("El estado debe ser uno de los valores permitidos");
      return false;
    }
    return true;
  };

  const saveKart = async (e) => {
    e.preventDefault();
    setLoading(true);

    // ✅ VALIDACIONES
    if (!validateCodigo(codigo) || !validateEstado(estado)) {
      setLoading(false);
      return;
    }

    // ✅ ESTRUCTURA CORRECTA para el backend
    const kartRequest = {
      codigo: codigo.trim().toUpperCase(),
      estado: estado, // ✅ Ya es el enum correcto
      observaciones: observaciones.trim() || (editing ? undefined : "Kart creado desde frontend")
    };

    console.log("📤 Enviando datos del kart:", kartRequest);

    try {
      if (editing) {
        await kartService.update(id, kartRequest);
        alert("Kart actualizado exitosamente");
      } else {
        await kartService.create(kartRequest);
        alert("Kart creado exitosamente");
      }
      navigate("/karts/list");
    } catch (error) {
      console.error("❌ Error al guardar kart:", error);
      
      if (error.response?.status === 400) {
        alert("Error: Datos inválidos o código ya existe");
      } else if (error.response?.status === 409) {
        alert("Error: Ya existe un kart con este código");
      } else {
        alert("Error del servidor");
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="container mt-5">
      <div className="row justify-content-center">
        <div className="col-md-6">
          <div className="card">
            <div className="card-header">
              <h3>{editing ? "Editar Kart" : "Nuevo Kart"}</h3>
            </div>
            <div className="card-body">
              <form onSubmit={saveKart}>
                
                {/* ✅ CÓDIGO */}
                <div className="mb-3">
                  <label className="form-label">Código *</label>
                  <input
                    type="text"
                    className="form-control"
                    value={codigo}
                    onChange={(e) => setCodigo(e.target.value.toUpperCase())}
                    placeholder="K001, K012, K099..."
                    pattern="K\d{3}"
                    required
                  />
                  <small className="form-text text-muted">
                    Formato: K### (ejemplo: K001, K012, K999)
                  </small>
                </div>

                {/* ✅ ESTADO */}
                <div className="mb-3">
                  <label className="form-label">Estado *</label>
                  <select
                    className="form-select"
                    value={estado}
                    onChange={(e) => setEstado(e.target.value)}
                    required
                  >
                    {estadosValidos.map((estadoOption) => (
                      <option key={estadoOption.value} value={estadoOption.value}>
                        {estadoOption.label}
                      </option>
                    ))}
                  </select>
                  <small className="form-text text-muted">
                    💡 Estado "Reservado" se gestiona automáticamente
                  </small>
                </div>

                {/* ✅ OBSERVACIONES */}
                <div className="mb-3">
                  <label className="form-label">Observaciones</label>
                  <textarea
                    className="form-control"
                    value={observaciones}
                    onChange={(e) => setObservaciones(e.target.value)}
                    rows={3}
                    placeholder="Observaciones adicionales sobre el kart..."
                  />
                </div>

                <div className="d-grid gap-2">
                  <button 
                    type="submit" 
                    className="btn btn-primary"
                    disabled={loading}
                  >
                    {loading ? "Guardando..." : (editing ? "Actualizar Kart" : "Crear Kart")}
                  </button>
                  
                  <button 
                    type="button" 
                    className="btn btn-secondary"
                    onClick={() => navigate("/karts/list")}
                  >
                    Cancelar
                  </button>
                </div>
              </form>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default AddEditKart;