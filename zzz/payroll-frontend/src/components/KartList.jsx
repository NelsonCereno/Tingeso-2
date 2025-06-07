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
  const navigate = useNavigate();

  useEffect(() => {
    kartService
      .getAll()
      .then((response) => {
        setKarts(response.data);
      })
      .catch((error) => {
        console.error("Error al cargar los karts:", error);
      });
  }, []);

  return (
    <div>
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

      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>ID</TableCell>
              <TableCell>Código</TableCell>
              <TableCell>Estado</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {karts.map((kart) => (
              <TableRow key={kart.id}>
                <TableCell>{kart.id}</TableCell>
                <TableCell>{kart.codigo}</TableCell>
                <TableCell>{kart.estado}</TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>
    </div>
  );
};

export default KartList;