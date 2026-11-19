import { useEffect, useState } from 'react';
import { administracion } from '../../api/servicios';
import { Encabezado } from '../../componentes/Encabezado';
import { Icono } from '../../componentes/Icono';
import { ErrorCarga, Tabla, useAviso, useDatos, usePagina } from '../../componentes/Piezas';
import { Tarjeta } from '../../componentes/Tarjeta';
import { TEMA_INICIAL, useTema } from '../../sesion/Tema';

const MAXIMO_LOGO = 200 * 1024;
const TIPOS_LOGO = ['image/png', 'image/jpeg', 'image/svg+xml', 'image/webp'];
const MENSAJE_LOGO = 'El logo debe ser una imagen PNG, JPG, SVG o WebP de máximo 200 KB.';

const leerArchivo = (archivo: File) => new Promise<string>((listo, falla) => {
  const lector = new FileReader();
  lector.onload = () => listo(String(lector.result));
  lector.onerror = () => falla(new Error('No se pudo leer el archivo.'));
  lector.readAsDataURL(archivo);
});

/**
 * Parámetros y tema (HU-20, sin mockup): a la izquierda la imagen corporativa (nombre, color principal y logo, con
 * su vista previa); a la derecha los parámetros generales, que se crean o se cambian uno a uno (PUT
 * /api/admin/parametros/{clave}, ms-administracion). Cada cambio queda en la auditoría a nombre del administrador y
 * la SPA lo toma al iniciar sesión, sin redesplegar. Las listas maestras no se mantienen aquí (supuesto S10).
 */
export function ParametrosTema() {
  const avisar = useAviso();
  const { aplicar } = useTema();
  const lista = useDatos(() => administracion.parametros());
  const todos = lista.datos ?? {};

  // ── Imagen corporativa ──
  const [nombre, setNombre] = useState('');
  const [color, setColor] = useState(TEMA_INICIAL.color);
  const [logo, setLogo] = useState(TEMA_INICIAL.logo);
  const [errorTema, setErrorTema] = useState('');
  const [guardandoTema, setGuardandoTema] = useState(false);
  useEffect(() => {
    if (!lista.datos) return;
    setNombre(lista.datos['tema.nombre'] ?? TEMA_INICIAL.nombre);
    setColor(lista.datos['tema.color-primario'] ?? TEMA_INICIAL.color);
    setLogo(lista.datos['tema.logo'] ?? TEMA_INICIAL.logo);
  }, [lista.datos]);
  const [primera, ...demas] = (nombre || TEMA_INICIAL.nombre).split(' ');

  const escogerLogo = async (archivo: File | undefined) => {
    if (!archivo) return;
    if (!TIPOS_LOGO.includes(archivo.type) || archivo.size > MAXIMO_LOGO) {
      setErrorTema(MENSAJE_LOGO);
      return;
    }
    setErrorTema('');
    setLogo(await leerArchivo(archivo));
  };

  const guardarTema = async () => {
    const nuevos: Record<string, string> = { 'tema.nombre': nombre.trim(), 'tema.color-primario': color.toUpperCase(), 'tema.logo': logo };
    const cambios = Object.entries(nuevos).filter(([k, v]) => todos[k] !== v);
    if (!cambios.length) { avisar('No hay cambios en la imagen corporativa.'); return; }
    setGuardandoTema(true);
    try {
      for (const [clave, valor] of cambios) await administracion.parametrizar(clave, valor);
      setErrorTema('');
      aplicar({ ...todos, ...nuevos });
      lista.recargar();
      avisar('Imagen corporativa actualizada: la verán todos los usuarios al iniciar sesión.');
    } catch (e) {
      setErrorTema(e instanceof Error ? e.message : 'No se pudo guardar la imagen corporativa.');
      lista.recargar();
    } finally {
      setGuardandoTema(false);
    }
  };

  // ── Parámetros generales ──
  const generales = Object.entries(todos).filter(([k]) => !k.startsWith('tema.')).sort(([a], [b]) => a.localeCompare(b));
  const { filas, pie } = usePagina(generales, 6);
  const [clave, setClave] = useState('');
  const [valor, setValor] = useState('');
  const [editando, setEditando] = useState<string | null>(null);
  const [errorParametro, setErrorParametro] = useState('');
  const [guardando, setGuardando] = useState(false);
  const editar = (k: string | null) => { setEditando(k); setClave(k ?? ''); setValor(k ? todos[k] : ''); setErrorParametro(''); };

  const guardarParametro = async () => {
    setGuardando(true);
    try {
      const r = await administracion.parametrizar(clave.trim(), valor);
      setErrorParametro('');
      lista.recargar();
      setEditando(clave.trim());
      avisar(r.mensaje);
    } catch (e) {
      setErrorParametro(e instanceof Error ? e.message : 'No se pudo guardar el parámetro.');
    } finally {
      setGuardando(false);
    }
  };

  return (
    <>
      <Encabezado titulo="Parámetros y tema" sub="Imagen corporativa y parámetros generales del sistema" icono="settings" />
      <div className="page">
        <ErrorCarga mensaje={lista.error} />
        <div className="grid halves">
          <Tarjeta titulo="Imagen corporativa" icono="image" sub="Nombre, color principal y logo que ven todos los usuarios">
            <div className="tema-vista" aria-label="Vista previa del tema">
              <img src={logo} alt="Logo actual" />
              <div><b>{primera}{demas.length > 0 && <> <span style={{ color }}>{demas.join(' ')}</span></>}</b><small>Sistema de Abastecimiento</small></div>
              <span className="tema-boton" style={{ background: color }}>Botón principal</span>
            </div>
            <div className="form-grid">
              <label className="fl span2">Nombre del sistema *<input value={nombre} onChange={(e) => setNombre(e.target.value)} maxLength={60} aria-label="Nombre del sistema" /></label>
              <label className="fl">Color principal *
                <span className="tema-color">
                  <input type="color" value={/^#[0-9A-Fa-f]{6}$/.test(color) ? color : TEMA_INICIAL.color} onChange={(e) => setColor(e.target.value.toUpperCase())} aria-label="Escoger color" />
                  <input value={color} onChange={(e) => setColor(e.target.value)} maxLength={7} aria-label="Color principal" />
                </span>
              </label>
              <label className="fl">Logo (PNG, JPG, SVG o WebP, máx. 200 KB)
                <span className="tema-logo">
                  <span className="btn outline sm"><Icono nombre="upload" /> Subir logo
                    <input type="file" accept={TIPOS_LOGO.join(',')} onChange={(e) => void escogerLogo(e.target.files?.[0])} aria-label="Subir logo" /></span>
                  <button type="button" className="btn ghost sm" onClick={() => { setLogo(TEMA_INICIAL.logo); setErrorTema(''); }}>Restaurar</button>
                </span>
              </label>
            </div>
            {errorTema && <p className="form-error" role="alert">{errorTema}</p>}
            <div className="m-actions">
              <button type="button" className="btn primary" disabled={guardandoTema || !lista.datos} onClick={() => void guardarTema()}>
                <Icono nombre="check" /> Guardar imagen corporativa</button>
            </div>
          </Tarjeta>

          <Tarjeta titulo="Parámetros generales" icono="settings" sub={`${generales.length} parámetros`}
            extra={<button type="button" className="btn soft sm" onClick={() => editar(null)}><Icono nombre="plus" /> Nuevo parámetro</button>}>
            <Tabla cabeceras={['Clave', 'Valor', 'Acción']} vacio="Aún no hay parámetros generales.">
              {filas.map(([k, v]) => (
                <tr key={k} className={`clickable ${editando === k ? 'selected' : ''}`} onClick={() => editar(k)}>
                  <td><b>{k}</b></td><td className="valor-parametro">{v}</td>
                  <td><span className="link-btn">Editar <Icono nombre="chevR" /></span></td>
                </tr>
              ))}
            </Tabla>
            {pie}
            <div className="form-grid mt">
              <label className="fl">Clave *<input value={clave} onChange={(e) => setClave(e.target.value)} readOnly={editando !== null} maxLength={80}
                placeholder="general.moneda" aria-label="Clave del parámetro" /></label>
              <label className="fl">Valor *<input value={valor} onChange={(e) => setValor(e.target.value)} maxLength={500} aria-label="Valor del parámetro" /></label>
            </div>
            {errorParametro && <p className="form-error" role="alert">{errorParametro}</p>}
            <div className="m-actions">
              <button type="button" className="btn primary" disabled={guardando} onClick={() => void guardarParametro()}>
                <Icono nombre="check" /> {editando ? 'Guardar cambios' : 'Crear parámetro'}</button>
            </div>
          </Tarjeta>
        </div>
      </div>
    </>
  );
}
