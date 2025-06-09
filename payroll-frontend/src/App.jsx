import './App.css'
import {BrowserRouter as Router, Route, Routes, Navigate} from 'react-router-dom'
import Navbar from "./components/Navbar"
import Home from './components/Home';
import NotFound from './components/NotFound';
import ReservaList from "./components/ReservaList";
import AddEditReserva from "./components/AddEditReserva";
import ClienteList from "./components/ClienteList";
import AddEditCliente from "./components/AddEditCliente";
import KartList from "./components/KartList";
import AddEditKart from "./components/AddEditKart";
import RackSemanal from "./components/RackSemanal";
import ReporteIngresosVueltas from "./components/ReporteIngresosVueltas";
import ReporteIngresosPersonas from "./components/ReporteIngresosPersonas";

function App() {
  return (
      <Router>
          <div className="container">
          <Navbar></Navbar>
            <Routes>
              {/* Ruta raíz redirige a Home */}
              <Route path="/" element={<Navigate to="/home" />} />
              <Route path="/home" element={<Home/>} />
              <Route path="/reservas/list" element={<ReservaList />} />
              <Route path="/reservas/add" element={<AddEditReserva />} />
              <Route path="/reservas/edit/:id" element={<AddEditReserva />} />

              {/* Rutas para clientes */}
              <Route path="/clientes/list" element={<ClienteList />} />
              <Route path="/clientes/add" element={<AddEditCliente />} />

              {/* Rutas para karts */}
              <Route path="/karts/list" element={<KartList />} />
              <Route path="/karts/add" element={<AddEditKart />} />

              <Route path="/rack-semanal" element={<RackSemanal />} />
              
              <Route path="/reporte-ingresos-vueltas" element={<ReporteIngresosVueltas />} />
              <Route path="/reporte-ingresos-personas" element={<ReporteIngresosPersonas />} />
              <Route path="*" element={<NotFound/>} />
            </Routes>
          </div>
      </Router>
  );
}

export default App
