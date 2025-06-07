import React, { useState, useEffect } from "react";
import reservaService from "../services/reserva.service";
import clienteService from "../services/cliente.service";
import kartService from "../services/kart.service";
import { useNavigate } from "react-router-dom";
import Button from "@mui/material/Button";
import TextField from "@mui/material/TextField";
import MenuItem from "@mui/material/MenuItem";

const AddEditReserva = () => {
  const [clientes, setClientes] = useState([]);
  const [karts, setKarts] = useState([]);
  const [selectedClientes, setSelectedClientes] = useState([]);
  const [selectedKarts, setSelectedKarts] = useState([]);
  const [numeroVueltas, setNumeroVueltas] = useState("");
  const [fechaReserva, setFechaReserva] = useState(""); // Nueva fecha de reserva
  const [horaReserva, setHoraReserva] = useState(""); // Nueva hora de reserva
  const navigate = useNavigate();

  const planes = [
    { id: "10", vueltas: 10, tiempoMaximo: 10, precio: 15000, duracionTotal: 30 },
    { id: "15", vueltas: 15, tiempoMaximo: 15, precio: 20000, duracionTotal: 35 },
    { id: "20", vueltas: 20, tiempoMaximo: 20, precio: 25000, duracionTotal: 40 },
  ];

  useEffect(() => {
    clienteService.getAll().then((response) => setClientes(response.data));
    kartService.getAll().then((response) => setKarts(response.data));
  }, []);

  const saveReserva = (e) => {
    e.preventDefault();

    const selectedPlan = planes.find((p) => p.id === numeroVueltas);

    if (selectedClientes.length !== selectedKarts.length) {
      alert("El número de clientes debe coincidir con el número de karts seleccionados.");
      return;
    }

    const reserva = {
      clientes: selectedClientes.map((id) => ({ id })),
      karts: selectedKarts.map((id) => ({ id })),
      numeroVueltas: selectedPlan.vueltas,
      tiempoMaximo: selectedPlan.tiempoMaximo,
      precioBase: selectedPlan.precio,
      duracionTotal: selectedPlan.duracionTotal,
      fechaReserva,
      horaReserva, // Agregar la hora de la reserva
    };

    reservaService
      .create(reserva)
      .then(() => {
        alert("Reserva creada exitosamente");
        navigate("/reservas/list");
      })
      .catch((error) => {
        console.error("Error al crear la reserva.", error);
      });
  };

  return (
    <form onSubmit={saveReserva}>
      <h2>Nueva Reserva</h2>

      <TextField
        select
        label="Clientes"
        value={selectedClientes}
        onChange={(e) => setSelectedClientes(e.target.value)}
        fullWidth
        margin="normal"
        SelectProps={{
          multiple: true,
        }}
      >
        {clientes.map((cliente) => (
          <MenuItem key={cliente.id} value={cliente.id}>
            {cliente.nombre}
          </MenuItem>
        ))}
      </TextField>

      <TextField
        select
        label="Karts"
        value={selectedKarts}
        onChange={(e) => setSelectedKarts(e.target.value)}
        fullWidth
        margin="normal"
        SelectProps={{
          multiple: true,
        }}
      >
        {karts.map((kart) => (
          <MenuItem key={kart.id} value={kart.id}>
            {kart.codigo}
          </MenuItem>
        ))}
      </TextField>

      <TextField
        select
        label="Plan"
        value={numeroVueltas}
        onChange={(e) => setNumeroVueltas(e.target.value)}
        fullWidth
        margin="normal"
      >
        {planes.map((p) => (
          <MenuItem key={p.id} value={p.id}>
            {p.vueltas} vueltas / {p.tiempoMaximo} min - ${p.precio}
          </MenuItem>
        ))}
      </TextField>

      <TextField
        label="Fecha de la Reserva"
        type="date"
        value={fechaReserva}
        onChange={(e) => setFechaReserva(e.target.value)}
        fullWidth
        margin="normal"
        InputLabelProps={{
          shrink: true,
        }}
      />

      <TextField
        label="Hora de la Reserva"
        type="time"
        value={horaReserva}
        onChange={(e) => setHoraReserva(e.target.value)}
        fullWidth
        margin="normal"
        InputLabelProps={{
          shrink: true,
        }}
      />

      <Button type="submit" variant="contained" color="primary">
        Guardar
      </Button>
    </form>
  );
};

export default AddEditReserva;