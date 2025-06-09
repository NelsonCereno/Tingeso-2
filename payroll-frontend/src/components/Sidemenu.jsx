import React from "react";
import { useNavigate } from "react-router-dom";
import List from "@mui/material/List";
import ListItemButton from "@mui/material/ListItemButton";
import ListItemIcon from "@mui/material/ListItemIcon";
import ListItemText from "@mui/material/ListItemText";
import Drawer from "@mui/material/Drawer";
import PeopleAltIcon from "@mui/icons-material/PeopleAlt";
import DirectionsCarIcon from "@mui/icons-material/DirectionsCar";
import AssignmentIcon from "@mui/icons-material/Assignment";
import CalendarViewWeekIcon from "@mui/icons-material/CalendarViewWeek"; // Icono para el Rack Semanal
import BarChartIcon from "@mui/icons-material/BarChart"; // Icono para reportes

const Sidemenu = ({ open, toggleDrawer }) => {
  const navigate = useNavigate();

  return (
    <Drawer anchor="left" open={open} onClose={toggleDrawer(false)}>
      <List>
        {/* Opción para gestionar reservas */}
        <ListItemButton onClick={() => navigate("/reservas/list")}>
          <ListItemIcon>
            <AssignmentIcon />
          </ListItemIcon>
          <ListItemText primary="Reservas" />
        </ListItemButton>

        {/* Opción para gestionar clientes */}
        <ListItemButton onClick={() => navigate("/clientes/list")}>
          <ListItemIcon>
            <PeopleAltIcon />
          </ListItemIcon>
          <ListItemText primary="Clientes" />
        </ListItemButton>

        {/* Opción para gestionar karts */}
        <ListItemButton onClick={() => navigate("/karts/list")}>
          <ListItemIcon>
            <DirectionsCarIcon />
          </ListItemIcon>
          <ListItemText primary="Karts" />
        </ListItemButton>

        {/* Opción para el Rack Semanal */}
        <ListItemButton onClick={() => navigate("/rack-semanal")}>
          <ListItemIcon>
            <CalendarViewWeekIcon />
          </ListItemIcon>
          <ListItemText primary="Rack Semanal" />
        </ListItemButton>

        {/* Opción para Reporte de Ingresos por Vueltas */}
        <ListItemButton onClick={() => navigate("/reporte-ingresos-vueltas")}>
          <ListItemIcon>
            <BarChartIcon />
          </ListItemIcon>
          <ListItemText primary="Reporte Ingresos por Vueltas" />
        </ListItemButton>

        {/* Opción para Reporte de Ingresos por Personas */}
        <ListItemButton onClick={() => navigate("/reporte-ingresos-personas")}>
          <ListItemIcon>
            <BarChartIcon />
          </ListItemIcon>
          <ListItemText primary="Reporte Ingresos por Personas" />
        </ListItemButton>
      </List>
    </Drawer>
  );
};

export default Sidemenu;
