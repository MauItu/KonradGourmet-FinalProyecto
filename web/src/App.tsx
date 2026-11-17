import { BrowserRouter, Route, Routes } from 'react-router-dom';
import { ProveedorAviso } from './componentes/Piezas';
import { Armazon } from './paginas/Armazon';
import { Inventario } from './paginas/jefe/Inventario';
import { Platos } from './paginas/jefe/Platos';
import { NuevoPedido } from './paginas/mesero/NuevoPedido';
import { PedidosTurno } from './paginas/mesero/PedidosTurno';
import { Inicio } from './paginas/Inicio';
import { Login } from './paginas/Login';
import { Perfil } from './paginas/Perfil';
import { PorConstruir } from './paginas/PorConstruir';
import { Solicitudes } from './paginas/Solicitudes';
import { ProveedorSesion } from './sesion/Sesion';

export function App() {
  return (
    <ProveedorSesion>
      <ProveedorAviso>
        <BrowserRouter future={{ v7_startTransition: true, v7_relativeSplatPath: true }}>
          <Routes>
            <Route path="/login" element={<Login />} />
            <Route element={<Armazon />}>
              <Route path="/inicio" element={<Inicio />} />
              <Route path="/perfil" element={<Perfil />} />
              <Route path="/pedidos" element={<PedidosTurno />} />
              <Route path="/pedidos/nuevo" element={<NuevoPedido />} />
              <Route path="/platos" element={<Platos />} />
              <Route path="/platos/nuevo" element={<Platos />} />
              <Route path="/inventario" element={<Inventario />} />
              <Route path="/solicitudes" element={<Solicitudes />} />
              <Route path="/solicitudes/nueva" element={<Solicitudes />} />
              <Route path="*" element={<PorConstruir />} />
            </Route>
          </Routes>
        </BrowserRouter>
      </ProveedorAviso>
    </ProveedorSesion>
  );
}
