import { useState, useEffect } from "react";
import clienteService from "../services/cliente.service";
import { useNavigate, useParams } from "react-router-dom";
import Button from "@mui/material/Button";
import TextField from "@mui/material/TextField";

const AddEditCliente = () => {
  const [nombre, setNombre] = useState("");
  const [fechaNacimiento, setFechaNacimiento] = useState("");
  const [email, setEmail] = useState("");
  const [telefono, setTelefono] = useState(""); // ✅ AGREGAR
  const [rut, setRut] = useState(""); // ✅ AGREGAR
  const [apellido, setApellido] = useState(""); // ✅ AGREGAR
  
  const [loading, setLoading] = useState(false);
  const [editing, setEditing] = useState(false);
  const navigate = useNavigate();
  const { id } = useParams();

  useEffect(() => {
    if (id) {
      setEditing(true);
      loadCliente(id);
    }
  }, [id]);

  const loadCliente = async (clienteId) => {
    try {
      const response = await clienteService.get(clienteId);
      const cliente = response.data;
      
      setNombre(cliente.nombre || "");
      setApellido(cliente.apellido || "");
      setRut(cliente.rut || "");
      setEmail(cliente.email || "");
      setTelefono(cliente.telefono || "");
      setFechaNacimiento(cliente.fechaNacimiento || "");
      // ❌ NO cargar numeroVisitas - es readonly
    } catch (error) {
      console.error("Error al cargar cliente:", error);
      alert("Error al cargar cliente");
    }
  };

  const saveCliente = async (e) => {
    e.preventDefault();
    setLoading(true);

    // ✅ VALIDACIONES
    if (!nombre.trim()) {
      alert("El nombre es obligatorio");
      setLoading(false);
      return;
    }

    if (!apellido.trim()) {
      alert("El apellido es obligatorio");
      setLoading(false);
      return;
    }

    if (!rut.trim()) {
      alert("El RUT es obligatorio");
      setLoading(false);
      return;
    }

    if (!fechaNacimiento) {
      alert("La fecha de nacimiento es obligatoria");
      setLoading(false);
      return;
    }

    if (!email.trim()) {
      alert("El email es obligatorio");
      setLoading(false);
      return;
    }

    // ✅ DATOS CORRECTOS para el backend
    const clienteData = {
      nombre: nombre.trim(),
      apellido: apellido.trim(),
      rut: rut.trim(),
      email: email.trim(),
      telefono: telefono.trim() || "Sin teléfono",
      fechaNacimiento: fechaNacimiento
      // ❌ NO enviar numeroVisitas - se maneja automáticamente
    };

    try {
      if (editing) {
        await clienteService.update(id, clienteData);
        alert("Cliente actualizado exitosamente");
      } else {
        await clienteService.create(clienteData);
        alert("Cliente creado exitosamente");
      }
      navigate("/clientes/list");
    } catch (error) {
      console.error("Error al guardar cliente:", error);
      
      if (error.response?.status === 400) {
        alert("Error: Datos inválidos o RUT/email ya existe");
      } else if (error.response?.status === 409) {
        alert("Error: Ya existe un cliente con este RUT o email");
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
              <h3>{editing ? "Editar Cliente" : "Nuevo Cliente"}</h3>
            </div>
            <div className="card-body">
              <form onSubmit={saveCliente}>
                
                {/* ✅ NOMBRE */}
                <div className="mb-3">
                  <label className="form-label">Nombre *</label>
                  <input
                    type="text"
                    className="form-control"
                    value={nombre}
                    onChange={(e) => setNombre(e.target.value)}
                    required
                  />
                </div>

                {/* ✅ APELLIDO */}
                <div className="mb-3">
                  <label className="form-label">Apellido *</label>
                  <input
                    type="text"
                    className="form-control"
                    value={apellido}
                    onChange={(e) => setApellido(e.target.value)}
                    required
                  />
                </div>

                {/* ✅ RUT */}
                <div className="mb-3">
                  <label className="form-label">RUT *</label>
                  <input
                    type="text"
                    className="form-control"
                    value={rut}
                    onChange={(e) => setRut(e.target.value)}
                    placeholder="12345678-9"
                    required
                  />
                </div>

                {/* ✅ FECHA DE NACIMIENTO */}
                <div className="mb-3">
                  <label className="form-label">Fecha de Nacimiento *</label>
                  <input
                    type="date"
                    className="form-control"
                    value={fechaNacimiento}
                    onChange={(e) => setFechaNacimiento(e.target.value)}
                    required
                  />
                </div>

                {/* ✅ EMAIL */}
                <div className="mb-3">
                  <label className="form-label">Email *</label>
                  <input
                    type="email"
                    className="form-control"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    required
                  />
                </div>

                {/* ✅ TELÉFONO */}
                <div className="mb-3">
                  <label className="form-label">Teléfono</label>
                  <input
                    type="tel"
                    className="form-control"
                    value={telefono}
                    onChange={(e) => setTelefono(e.target.value)}
                    placeholder="+56 9 1234 5678"
                  />
                </div>

                {/* ✅ MOSTRAR VISITAS SOLO EN EDICIÓN (READONLY) */}
                {editing && (
                  <div className="mb-3">
                    <label className="form-label">Número de Visitas</label>
                    <input
                      type="number"
                      className="form-control"
                      value="Cargando..."
                      disabled
                      style={{ backgroundColor: '#f8f9fa' }}
                    />
                    <small className="form-text text-muted">
                      📊 Las visitas se incrementan automáticamente con cada reserva confirmada
                    </small>
                  </div>
                )}

                <div className="d-grid gap-2">
                  <button 
                    type="submit" 
                    className="btn btn-primary"
                    disabled={loading}
                  >
                    {loading ? "Guardando..." : (editing ? "Actualizar Cliente" : "Crear Cliente")}
                  </button>
                  
                  <button 
                    type="button" 
                    className="btn btn-secondary"
                    onClick={() => navigate("/clientes/list")}
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

export default AddEditCliente;