import React, { useEffect, useState } from "react";
import clienteService from "../services/cliente.service";
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

const ClienteList = () => {
  const [clientes, setClientes] = useState([]);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    clienteService
      .getAll()
      .then((response) => {
        setClientes(response.data);
        setLoading(false);
      })
      .catch((error) => {
        console.error("Error al cargar los clientes:", error);
        setLoading(false);
      });
  }, []);

  if (loading) {
    return <div>Cargando...</div>;
  }

  return (
    <div className="container mt-5">
      <h2>Lista de Clientes</h2>

      <Button
        variant="contained"
        color="primary"
        startIcon={<AddIcon />}
        onClick={() => navigate("/clientes/add")}
        style={{ marginBottom: "20px" }}
      >
        Nuevo Cliente
      </Button>

      <table className="table table-striped">
        <thead>
          <tr>
            <th>ID</th>
            <th>Nombre Completo</th>
            <th>RUT</th>
            <th>Email</th>
            <th>📊 Visitas</th>
            <th>🎂 Fecha Nacimiento</th>
            <th>Acciones</th>
          </tr>
        </thead>
        <tbody>
          {clientes.map((cliente) => (
            <tr key={cliente.id}>
              <td>{cliente.id}</td>
              <td>
                {cliente.nombre} {cliente.apellido}
              </td>
              <td>{cliente.rut}</td>
              <td>{cliente.email}</td>
              <td>
                <span
                  className={`badge ${
                    cliente.numeroVisitas >= 7
                      ? "bg-success"
                      : cliente.numeroVisitas >= 5
                      ? "bg-warning"
                      : cliente.numeroVisitas >= 2
                      ? "bg-info"
                      : "bg-secondary"
                  }`}
                >
                  {cliente.numeroVisitas} visitas
                </span>
                {cliente.numeroVisitas >= 7 && (
                  <small className="text-success d-block">⭐ VIP</small>
                )}
                {cliente.numeroVisitas >= 5 &&
                  cliente.numeroVisitas < 7 && (
                    <small className="text-warning d-block">
                      🔥 Frecuente
                    </small>
                  )}
                {cliente.numeroVisitas >= 2 &&
                  cliente.numeroVisitas < 5 && (
                    <small className="text-info d-block">👤 Regular</small>
                  )}
                {cliente.numeroVisitas < 2 && (
                  <small className="text-muted d-block">🆕 Nuevo</small>
                )}
              </td>
              <td>{cliente.fechaNacimiento}</td>
              <td>
                <Button
                  variant="outlined"
                  color="primary"
                  onClick={() => navigate(`/clientes/edit/${cliente.id}`)}
                >
                  Editar
                </Button>
                <Button
                  variant="outlined"
                  color="secondary"
                  onClick={() => {
                    if (window.confirm("¿Estás seguro de eliminar este cliente?")) {
                      clienteService
                        .remove(cliente.id)
                        .then(() => {
                          setClientes(clientes.filter((c) => c.id !== cliente.id));
                        })
                        .catch((error) => {
                          console.error("Error al eliminar el cliente:", error);
                        });
                    }
                  }}
                  style={{ marginLeft: "10px" }}
                >
                  Eliminar
                </Button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

export default ClienteList;