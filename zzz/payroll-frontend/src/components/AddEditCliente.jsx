import { useState } from "react";
import clienteService from "../services/cliente.service";
import { useNavigate } from "react-router-dom";
import Button from "@mui/material/Button";
import TextField from "@mui/material/TextField";

const AddEditCliente = () => {
  const [nombre, setNombre] = useState("");
  const [numeroVisitas, setNumeroVisitas] = useState(0);
  const [fechaNacimiento, setFechaNacimiento] = useState("");
  const [email, setEmail] = useState(""); // Nuevo campo de correo
  const [errorEmail, setErrorEmail] = useState(""); // Validación de correo
  const navigate = useNavigate();

  const validateEmail = (value) => {
    const emailRegex = /^[a-zA-Z0-9._%+-]+@gmail\.com$/;
    if (!emailRegex.test(value)) {
      setErrorEmail("El correo debe ser una dirección válida de Gmail.");
      return false;
    }
    setErrorEmail("");
    return true;
  };

  const saveCliente = (e) => {
    e.preventDefault();
    if (!validateEmail(email)) {
      return;
    }

    const cliente = { nombre, numeroVisitas, fechaNacimiento, email };
    clienteService
      .create(cliente)
      .then(() => {
        alert("Cliente guardado exitosamente");
        navigate("/clientes/list");
      })
      .catch((error) => {
        console.error("Error al guardar el cliente:", error);
      });
  };

  return (
    <div>
      <form onSubmit={saveCliente}>
        <h2>Nuevo Cliente</h2>
        <TextField
          label="Nombre"
          value={nombre}
          onChange={(e) => setNombre(e.target.value)}
          fullWidth
          margin="normal"
        />
        <TextField
          label="Número de Visitas"
          type="number"
          value={numeroVisitas}
          onChange={(e) => setNumeroVisitas(e.target.value)}
          fullWidth
          margin="normal"
        />
        <TextField
          label="Fecha de Nacimiento"
          type="date"
          value={fechaNacimiento}
          onChange={(e) => setFechaNacimiento(e.target.value)}
          fullWidth
          margin="normal"
          InputLabelProps={{
            shrink: true,
          }}
        />
        <TextField
          label="Correo Electrónico"
          type="email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          onBlur={(e) => validateEmail(e.target.value)}
          error={!!errorEmail}
          helperText={errorEmail}
          fullWidth
          margin="normal"
        />
        <Button type="submit" variant="contained" color="primary">
          Guardar
        </Button>
      </form>
    </div>
  );
};

export default AddEditCliente;