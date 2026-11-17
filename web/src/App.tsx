import { BrowserRouter, Route, Routes } from 'react-router-dom';
import { Armazon } from './paginas/Armazon';
import { Inicio } from './paginas/Inicio';
import { Login } from './paginas/Login';
import { Perfil } from './paginas/Perfil';
import { PorConstruir } from './paginas/PorConstruir';
import { ProveedorSesion } from './sesion/Sesion';

export function App() {
  return (
    <ProveedorSesion>
      <BrowserRouter future={{ v7_startTransition: true, v7_relativeSplatPath: true }}>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route element={<Armazon />}>
            <Route path="/inicio" element={<Inicio />} />
            <Route path="/perfil" element={<Perfil />} />
            <Route path="*" element={<PorConstruir />} />
          </Route>
        </Routes>
      </BrowserRouter>
    </ProveedorSesion>
  );
}
