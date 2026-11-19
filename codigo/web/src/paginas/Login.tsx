import { useState, type FormEvent } from 'react';
import { Navigate } from 'react-router-dom';
import { useSesion } from '../sesion/Sesion';
import '../estilos/login.css';

// Accesos de demostración: un usuario por rol (DatosIniciales de ms-identidad, DEC-63).
const DEMO: [string, string][] = [
  ['Mesero', 'isabella.torres@konradgourmet.com'],
  ['Jefe de cocina', 'carlos.mendoza@konradgourmet.com'],
  ['Auxiliar', 'isabella.castro@konradgourmet.com'],
  ['Directora', 'laura.gomez@konradgourmet.com'],
  ['Comercial', 'andres.rojas@konradgourmet.com'],
  ['Administradora', 'paula.herrera@konradgourmet.com'],
];
const CLAVE_DEMO = 'Konrad2026';

/** V01 · Inicio de sesión: POST /api/auth/token a ms-identidad. */
export function Login() {
  const { usuario, vencida, iniciar } = useSesion();
  const [correo, setCorreo] = useState('');
  const [clave, setClave] = useState('');
  const [ver, setVer] = useState(false);
  const [recordar, setRecordar] = useState(true);
  const [error, setError] = useState(vencida ? 'Tu sesión venció. Vuelve a iniciar sesión.' : '');
  const [enviando, setEnviando] = useState(false);

  if (usuario) return <Navigate to="/" replace />;

  const entrar = async (e: FormEvent) => {
    e.preventDefault();
    setError('');
    setEnviando(true);
    try {
      await iniciar(correo, clave, recordar);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'No se pudo iniciar sesión.');
    } finally {
      setEnviando(false);
    }
  };

  return (
    <div className="pantalla-login">
      <div className="stage">
        <video className="bg-video" autoPlay muted loop playsInline poster="/assets/logo/logo.png">
          <source src="/assets/video/3DVideo.mp4" type="video/mp4" />
        </video>
        <div className="bg-overlay"></div>
        <span className="deco deco--square"></span>
        <span className="deco deco--glow"></span>

        <main className="login-card">
          <header className="brand">
            <img className="brand__logo" src="/assets/logo/logo.png" alt="Konrad Gourmet" />
            <div className="brand__text">
              <h1 className="brand__name"><span>Konrad</span> <strong>Gourmet</strong></h1>
              <p className="brand__tagline">Sistema de Abastecimiento</p>
            </div>
          </header>

          <h2 className="welcome">¡Bien<span>venido!</span></h2>
          <p className="welcome__sub">Inicia sesión para continuar</p>

          <form className="form" onSubmit={entrar} autoComplete="on">
            <div className="field">
              <label className="field__label" htmlFor="email">Correo electrónico</label>
              <div className="input-wrap">
                <span className="input-icon" aria-hidden="true">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
                    <rect x="2.5" y="4.5" width="19" height="15" rx="3" /><path d="m3.5 7 7.4 5.3a2 2 0 0 0 2.2 0L20.5 7" />
                  </svg>
                </span>
                <input id="email" type="email" placeholder="tu@konradgourmet.com" autoComplete="email" required
                  value={correo} onChange={(e) => setCorreo(e.target.value)} />
              </div>
            </div>

            <div className="field">
              <label className="field__label" htmlFor="password">Contraseña</label>
              <div className="input-wrap">
                <span className="input-icon" aria-hidden="true">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
                    <rect x="4" y="10" width="16" height="11" rx="3" /><path d="M8 10V7.5a4 4 0 0 1 8 0V10" /><circle cx="12" cy="15.5" r="1.4" />
                  </svg>
                </span>
                <input id="password" type={ver ? 'text' : 'password'} placeholder="Ingresa tu contraseña" autoComplete="current-password"
                  required value={clave} onChange={(e) => setClave(e.target.value)} />
                <button className={`input-toggle ${ver ? 'is-visible' : ''}`} type="button" onClick={() => setVer(!ver)}
                  aria-label={ver ? 'Ocultar contraseña' : 'Mostrar contraseña'}>
                  <svg className="icon-eye-off" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
                    <path d="M3 3 21 21" /><path d="M10.6 6.3A8.9 8.9 0 0 1 12 6.2c4.6 0 8.1 3.6 9.3 5.3a1 1 0 0 1 0 1.1 17 17 0 0 1-2.7 2.9" />
                    <path d="M16.2 16.5A9.5 9.5 0 0 1 12 17.8c-4.6 0-8.1-3.6-9.3-5.3a1 1 0 0 1 0-1.1A17.6 17.6 0 0 1 6 7.9" /><path d="M9.9 9.9a3 3 0 0 0 4.2 4.2" />
                  </svg>
                  <svg className="icon-eye" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
                    <path d="M2.7 12.4a1 1 0 0 1 0-1.1C3.9 9.6 7.4 6 12 6s8.1 3.6 9.3 5.3a1 1 0 0 1 0 1.1C20.1 14.2 16.6 17.8 12 17.8s-8.1-3.6-9.3-5.4Z" />
                    <circle cx="12" cy="11.9" r="3" />
                  </svg>
                </button>
              </div>
            </div>

            <div className="form__row">
              <label className="check">
                <input type="checkbox" checked={recordar} onChange={(e) => setRecordar(e.target.checked)} />
                <span className="check__box">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="3.2" strokeLinecap="round" strokeLinejoin="round"><path d="m5 12.5 4.5 4.5L19 7" /></svg>
                </span>
                <span className="check__label">Recordarme</span>
              </label>
              <button type="button" className="forgot" onClick={() => setError('Pide al administrador que te asigne una contraseña nueva.')}>
                ¿Olvidaste tu contraseña?
              </button>
            </div>

            <button className="btn-login" type="submit" disabled={enviando}>
              <span>{enviando ? 'Ingresando…' : 'Iniciar sesión'}</span>
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round"><path d="M4 12h15" /><path d="m13 6 6 6-6 6" /></svg>
            </button>

            {error && <p className="login-error" role="alert">{error}</p>}

            <div className="demo-access" aria-label="Accesos de demostración">
              <span className="demo-access__title">Accesos de demostración · clave {CLAVE_DEMO}</span>
              <div className="demo-access__roles">
                {DEMO.map(([rol, c]) => (
                  <button key={c} type="button" onClick={() => { setCorreo(c); setClave(CLAVE_DEMO); setError(''); }}>{rol}</button>
                ))}
              </div>
            </div>
          </form>
        </main>
      </div>
    </div>
  );
}
