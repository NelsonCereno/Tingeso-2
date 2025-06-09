import React, { useEffect, useState } from "react";
import kartService from "../services/kart.service";
import Table from "@mui/material/Table";
import TableBody from "@mui/material/TableBody";
import TableCell from "@mui/material/TableCell";
import TableContainer from "@mui/material/TableContainer";
import TableHead from "@mui/material/TableHead";
import TableRow from "@mui/material/TableRow";
import Paper from "@mui/material/Paper";
import Button from "@mui/material/Button";
import AddIcon from "@mui/icons-material/Add";
import { useNavigate } from "react-router-dom";

const KartList = () => {
  const [karts, setKarts] = useState([]);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    kartService
      .getAll()
      .then((response) => {
        setKarts(response.data);
        setLoading(false);
      })
      .catch((error) => {
        console.error("Error al cargar los karts:", error);
        setLoading(false);
      });
  }, []);

  const getEstadoBadge = (estado) => {
    switch (estado) {
      case "DISPONIBLE":
        return <span className="badge bg-success">✅ Disponible</span>;
      case "RESERVADO":
        return <span className="badge bg-warning">⚠️ Reservado</span>;
      case "MANTENIMIENTO":
        return <span className="badge bg-info">🔧 Mantenimiento</span>;
      case "FUERA_SERVICIO":
        return <span className="badge bg-danger">❌ Fuera de Servicio</span>;
      default:
        return <span className="badge bg-secondary">❓ {estado}</span>;
    }
  };

  return (
    <div className="container mt-5">
      <h2>Lista de Karts</h2>

      {/* Botón para agregar un nuevo kart */}
      <Button
        variant="contained"
        color="primary"
        startIcon={<AddIcon />}
        onClick={() => navigate("/karts/add")}
        style={{ marginBottom: "20px" }}
      >
        Nuevo Kart
      </Button>

      {loading ? (
        <p>Cargando karts...</p>
      ) : (
        <table className="table table-striped">
          <thead>
            <tr>
              <th>ID</th>
              <th>Código</th>
              <th>Estado</th>
              <th>🔢 Usos</th>
              <th>📅 Creación</th>
              <th>Acciones</th>
            </tr>
          </thead>
          <tbody>
            {karts.map((kart) => (
              <tr key={kart.id}>
                <td>{kart.id}</td>
                <td>
                  <strong>{kart.codigo}</strong>
                </td>
                <td>{getEstadoBadge(kart.estado)}</td>
                <td>
                  <span
                    className={`badge ${
                      kart.numeroUsos >= 50
                        ? "bg-danger"
                        : kart.numeroUsos >= 25
                        ? "bg-warning"
                        : "bg-success"
                    }`}
                  >
                    {kart.numeroUsos} usos
                  </span>
                  {kart.necesitaMantenimiento && (
                    <small className="text-warning d-block">
                      ⚠️ Necesita mantenimiento
                    </small>
                  )}
                </td>
                <td>
                  {new Date(kart.fechaCreacion).toLocaleDateString()}
                </td>
                <td>
                  {/* ...existing action buttons... */}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
};

export default KartList;