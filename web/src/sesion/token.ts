// Lee los datos del JWT que firmó ms-identidad. La firma la valida el gateway, no el navegador.
export interface ElementoMenu {
  titulo: string;
  ruta: string;
  hijos: ElementoMenu[];
}

export type Rol = 'MESERO' | 'JEFE_COCINA' | 'AUXILIAR_COMPRAS' | 'DIRECTOR_COMPRAS' | 'DIRECTOR_COMERCIAL' | 'ADMINISTRADOR';

export interface Usuario {
  id: number;
  correo: string;
  nombre: string;
  rol: Rol;
  sucursalId: number;
  sucursal: string;
  menu: ElementoMenu;
  vence: number;
}

export function leerUsuario(jwt: string): Usuario | null {
  try {
    const parte = jwt.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
    const binario = atob(parte.padEnd(parte.length + ((4 - (parte.length % 4)) % 4), '='));
    const bytes = Uint8Array.from(binario, (c) => c.charCodeAt(0));
    const c = JSON.parse(new TextDecoder().decode(bytes));
    const u: Usuario = {
      id: Number(c.sub), correo: c.correo, nombre: c.nombre, rol: c.rol,
      sucursalId: c.sucursalId, sucursal: c.sucursal, menu: c.menu, vence: c.exp * 1000,
    };
    return u.vence > Date.now() ? u : null;
  } catch {
    return null;
  }
}

/** Los nombres de los 6 roles del proyecto (reporte y casos de uso), no el cargo de una persona (DEC-65). */
export const NOMBRE_ROL: Record<Rol, string> = {
  MESERO: 'Mesero',
  JEFE_COCINA: 'Jefe de cocina',
  AUXILIAR_COMPRAS: 'Auxiliar de compras',
  DIRECTOR_COMPRAS: 'Director de compras',
  DIRECTOR_COMERCIAL: 'Director comercial',
  ADMINISTRADOR: 'Administrador',
};

/** La clase del body que usa app.css para los matices de cada rol. */
export const CLASE_ROL: Record<Rol, string> = {
  MESERO: 'rol-mesero',
  JEFE_COCINA: 'rol-jefe',
  AUXILIAR_COMPRAS: 'rol-auxiliar',
  DIRECTOR_COMPRAS: 'rol-director',
  DIRECTOR_COMERCIAL: 'rol-comercial',
  ADMINISTRADOR: 'rol-admin',
};
