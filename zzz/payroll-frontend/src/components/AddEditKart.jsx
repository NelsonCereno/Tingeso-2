import { useState } from "react";
import kartService from "../services/kart.service";
import { useNavigate } from "react-router-dom";
import Button from "@mui/material/Button";
import TextField from "@mui/material/TextField";
import MenuItem from "@mui/material/MenuItem";

const AddEditKart = () => {
  const [codigo, setCodigo] = useState("");
  const [estado, setEstado] = useState("disponible");
  const [errorCodigo, setErrorCodigo] = useState("");
  const [errorEstado, setErrorEstado] = useState("");
  const navigate = useNavigate();

  const estadosValidos = ["disponible", "no disponible"];
  const codigosValidos = Array.from({ length: 15 }, (_, i) =>
    `K${String(i + 1).padStart(3, "0")}`
  );

  const validateCodigo = (value) => {
    if (!codigosValidos.includes(value)) {
      setErrorCodigo("El código debe ser uno de los siguientes: " + codigosValidos.join(", "));
      return false;
    }
    setErrorCodigo("");
    return true;
  };

  const validateEstado = (value) => {
    if (!estadosValidos.includes(value)) {
      setErrorEstado("El estado debe ser 'disponible' o 'no disponible'.");
      return false;
    }
    setErrorEstado("");
    return true;
  };

  const saveKart = (e) => {
    e.preventDefault();

    const isCodigoValid = validateCodigo(codigo);
    const isEstadoValid = validateEstado(estado);

    if (!isCodigoValid || !isEstadoValid) {
      return;
    }

    const kart = { codigo, estado };
    kartService
      .create(kart)
      .then(() => {
        alert("Kart guardado exitosamente");
        navigate("/karts/list");
      })
      .catch((error) => {
        console.error("Error al guardar el kart:", error);
      });
  };

  return (
    <form onSubmit={saveKart}>
      <h2>Nuevo Kart</h2>
      <TextField
        label="Código"
        value={codigo}
        onChange={(e) => setCodigo(e.target.value)}
        onBlur={(e) => validateCodigo(e.target.value)}
        error={!!errorCodigo}
        helperText={errorCodigo}
        fullWidth
        margin="normal"
      />
      <TextField
        select
        label="Estado"
        value={estado}
        onChange={(e) => setEstado(e.target.value)}
        onBlur={(e) => validateEstado(e.target.value)}
        error={!!errorEstado}
        helperText={errorEstado}
        fullWidth
        margin="normal"
      >
        {estadosValidos.map((estado) => (
          <MenuItem key={estado} value={estado}>
            {estado}
          </MenuItem>
        ))}
      </TextField>
      <Button type="submit" variant="contained" color="primary">
        Guardar
      </Button>
    </form>
  );
};

export default AddEditKart;