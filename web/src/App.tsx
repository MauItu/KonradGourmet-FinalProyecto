import { BrowserRouter, Route, Routes } from 'react-router-dom';
import { ProveedorAviso } from './componentes/Piezas';
import { Armazon } from './paginas/Armazon';
import { Proveedores } from './paginas/auxiliar/Proveedores';
import { Cotizaciones } from './paginas/Cotizaciones';
import { CotizacionesDirector } from './paginas/director/CotizacionesDirector';
import { Opcionadas } from './paginas/director/Opcionadas';
import { OrdenesCompra } from './paginas/director/OrdenesCompra';
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
              <Route path="/cotizaciones" element={<Cotizaciones />} />
              <Route path="/cotizaciones/nueva" element={<Cotizaciones />} />
              <Route path="/cotizaciones/todas" element={<CotizacionesDirector key="todas" soloPendientes={false} />} />
              <Route path="/cotizaciones/pendientes" element={<CotizacionesDirector key="pendientes" soloPendientes />} />
              <Route path="/cotizaciones/opcionadas" element={<Opcionadas />} />
              <Route path="/ordenes-compra" element={<OrdenesCompra />} />
              <Route path="/proveedores" element={<Proveedores />} />
              <Route path="*" element={<PorConstruir />} />
            </Route>
          </Routes>
        </BrowserRouter>
      </ProveedorAviso>
    </ProveedorSesion>
  );
}
