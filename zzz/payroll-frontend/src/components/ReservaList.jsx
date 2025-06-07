import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import reservaService from "../services/reserva.service";
import httpClient from "../http-common";
import Table from "@mui/material/Table";
import TableBody from "@mui/material/TableBody";
import TableCell from "@mui/material/TableCell";
import TableContainer from "@mui/material/TableContainer";
import TableHead from "@mui/material/TableHead";
import TableRow from "@mui/material/TableRow";
import Paper from "@mui/material/Paper";
import Button from "@mui/material/Button";
import AddIcon from "@mui/icons-material/Add";

const ReservaList = () => {
  const [reservas, setReservas] = useState([]);
  const navigate = useNavigate();

  useEffect(() => {
    reservaService
      .getAll()
      .then((response) => {
        setReservas(response.data);
      })
      .catch((error) => {
        console.error("Error al cargar las reservas:", error);
      });
  }, []);

  const enviarComprobante = (reservaId) => {
    httpClient.post(`/api/reservas/${reservaId}/enviar-comprobante`)
      .then((response) => {
        const correos = response.data;
        alert(`Comprobante enviado a los siguientes correos:\n${correos.join("\n")}`);
      })
      .catch((error) => {
        alert("Error al enviar el comprobante: " + error.message);
      });
  };

  return (
    <div>
      <h2>Lista de Reservas</h2>

      {/* Botón para crear una nueva reserva */}
      <Button
        variant="contained"
        color="primary"
        startIcon={<AddIcon />}
        onClick={() => navigate("/reservas/add")}
        style={{ marginBottom: "20px" }}
      >
        Nueva Reserva
      </Button>

      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>ID</TableCell>
              <TableCell>Clientes</TableCell>
              <TableCell>Karts</TableCell>
              <TableCell>Vueltas</TableCell>
              <TableCell>Precio Base</TableCell>
              <TableCell>Descuento por Visitas</TableCell>
              <TableCell>Descuento por Cumpleaños</TableCell>
              <TableCell>Descuento por Personas</TableCell>
              <TableCell>Precio Final</TableCell>
              <TableCell>IVA</TableCell>
              <TableCell>Precio Final + IVA</TableCell>
              <TableCell>Acciones</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {reservas.map((reserva) => {
              const iva = Math.round(reserva.precioFinal * 0.19); 
              const precioConIva = Math.round(reserva.precioFinal + iva); 

              return (
                <TableRow key={reserva.id}>
                  <TableCell>{reserva.id}</TableCell>
                  <TableCell>
                    {reserva.clientes.map((cliente, index) => (
                      <div key={index}>
                        <strong>{cliente.nombre}</strong>
                      </div>
                    ))}
                  </TableCell>
                  <TableCell>
                    {reserva.karts.map((kart) => kart.codigo).join(", ")}
                  </TableCell>
                  <TableCell>{reserva.numeroVueltas}</TableCell>
                  <TableCell>${reserva.precioBase}</TableCell>
                  <TableCell>{reserva.descuentoPorVisitas || 0}%</TableCell>
                  <TableCell>{reserva.descuentoPorCumpleaños || 0}%</TableCell>
                  <TableCell>{reserva.descuentoPorPersonas || 0}%</TableCell>
                  <TableCell>${reserva.precioFinal}</TableCell>
                  <TableCell>${iva}</TableCell>
                  <TableCell>${precioConIva}</TableCell>
                  <TableCell>
                    <Button
                      variant="contained"
                      color="primary"
                      onClick={() => enviarComprobante(reserva.id)}
                    >
                      Enviar Comprobante
                    </Button>
                  </TableCell>
                </TableRow>
              );
            })}
          </TableBody>
        </Table>
      </TableContainer>
    </div>
  );
};

export default ReservaList;