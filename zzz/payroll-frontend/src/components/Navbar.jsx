import * as React from "react";
import AppBar from "@mui/material/AppBar";
import Box from "@mui/material/Box";
import Toolbar from "@mui/material/Toolbar";
import Typography from "@mui/material/Typography";
import Button from "@mui/material/Button";
import IconButton from "@mui/material/IconButton";
import MenuIcon from "@mui/icons-material/Menu";
import { Link } from "react-router-dom";
import { useState } from "react";
import MenuItem from "@mui/material/MenuItem";
import Menu from "@mui/material/Menu";
import HomeIcon from "@mui/icons-material/Home";

export default function Navbar() {
  const [drawerOpen, setDrawerOpen] = useState(false);
  
  // Estados para los menús desplegables
  const [clientesAnchorEl, setClientesAnchorEl] = useState(null);
  const [kartsAnchorEl, setKartsAnchorEl] = useState(null);
  const [reservasAnchorEl, setReservasAnchorEl] = useState(null);
  const [reportesAnchorEl, setReportesAnchorEl] = useState(null);

  // Handlers para los menús desplegables
  const handleClientesMenuOpen = (event) => {
    setClientesAnchorEl(event.currentTarget);
  };

  const handleKartsMenuOpen = (event) => {
    setKartsAnchorEl(event.currentTarget);
  };

  const handleReservasMenuOpen = (event) => {
    setReservasAnchorEl(event.currentTarget);
  };

  const handleReportesMenuOpen = (event) => {
    setReportesAnchorEl(event.currentTarget);
  };

  const handleMenuClose = () => {
    setClientesAnchorEl(null);
    setKartsAnchorEl(null);
    setReservasAnchorEl(null);
    setReportesAnchorEl(null);
  };

  const toggleDrawer = (open) => (event) => {
    if (
      event.type === "keydown" &&
      (event.key === "Tab" || event.key === "Shift")
    ) {
      return;
    }
    setDrawerOpen(open);
  };

  return (
    <Box sx={{ flexGrow: 1 }}>
      <AppBar position="static">
        <Toolbar>
          <Typography 
            variant="h6" 
            component={Link} 
            to="/home"
            sx={{ 
              flexGrow: 1, 
              textDecoration: 'none',
              color: 'white',
              fontWeight: 'bold'
            }}
          >
            Karting RM
          </Typography>

          {/* Enlaces de navegación */}
          <Button 
            color="inherit" 
            component={Link} 
            to="/home" 
            startIcon={<HomeIcon />}
          >
            Home
          </Button>

          {/* Clientes */}
          <Button 
            color="inherit" 
            aria-controls="clientes-menu"
            aria-haspopup="true"
            onClick={handleClientesMenuOpen}
          >
            Clientes
          </Button>
          <Menu
            id="clientes-menu"
            anchorEl={clientesAnchorEl}
            keepMounted
            open={Boolean(clientesAnchorEl)}
            onClose={handleMenuClose}
          >
            <MenuItem onClick={handleMenuClose} component={Link} to="/clientes/list">Ver clientes</MenuItem>
            <MenuItem onClick={handleMenuClose} component={Link} to="/clientes/add">Agregar cliente</MenuItem>
          </Menu>

          {/* Karts */}
          <Button 
            color="inherit"
            aria-controls="karts-menu"
            aria-haspopup="true"
            onClick={handleKartsMenuOpen}
          >
            Karts
          </Button>
          <Menu
            id="karts-menu"
            anchorEl={kartsAnchorEl}
            keepMounted
            open={Boolean(kartsAnchorEl)}
            onClose={handleMenuClose}
          >
            <MenuItem onClick={handleMenuClose} component={Link} to="/karts/list">Ver karts</MenuItem>
            <MenuItem onClick={handleMenuClose} component={Link} to="/karts/add">Agregar kart</MenuItem>
          </Menu>

          {/* Reservas */}
          <Button 
            color="inherit"
            aria-controls="reservas-menu"
            aria-haspopup="true"
            onClick={handleReservasMenuOpen}
          >
            Reservas
          </Button>
          <Menu
            id="reservas-menu"
            anchorEl={reservasAnchorEl}
            keepMounted
            open={Boolean(reservasAnchorEl)}
            onClose={handleMenuClose}
          >
            <MenuItem onClick={handleMenuClose} component={Link} to="/reservas/list">Ver reservas</MenuItem>
            <MenuItem onClick={handleMenuClose} component={Link} to="/reservas/add">Crear reserva</MenuItem>
          </Menu>

          {/* Rack Semanal */}
          <Button color="inherit" component={Link} to="/rack-semanal">
            Rack Semanal
          </Button>

          {/* Reportes */}
          <Button 
            color="inherit"
            aria-controls="reportes-menu"
            aria-haspopup="true"
            onClick={handleReportesMenuOpen}
          >
            Reportes
          </Button>
          <Menu
            id="reportes-menu"
            anchorEl={reportesAnchorEl}
            keepMounted
            open={Boolean(reportesAnchorEl)}
            onClose={handleMenuClose}
          >
            <MenuItem onClick={handleMenuClose} component={Link} to="/reporte-ingresos-vueltas">
              Ingresos por vueltas
            </MenuItem>
            <MenuItem onClick={handleMenuClose} component={Link} to="/reporte-ingresos-personas">
              Ingresos por personas
            </MenuItem>
          </Menu>
        </Toolbar>
      </AppBar>
    </Box>
  );
}
