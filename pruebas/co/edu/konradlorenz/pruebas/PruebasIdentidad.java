package co.edu.konradlorenz.pruebas;

import co.edu.konradlorenz.compartido.eventos.AuditoriaEvent;
import co.edu.konradlorenz.demo.BusEnMemoria;
import co.edu.konradlorenz.demo.CifradorEnMemoria;
import co.edu.konradlorenz.demo.EmisorTokenEnMemoria;
import co.edu.konradlorenz.demo.UsuarioEnMemoria;
import co.edu.konradlorenz.identidad.aplicacion.IniciarSesionService;
import co.edu.konradlorenz.identidad.aplicacion.RegistrarUsuarioService;
import co.edu.konradlorenz.identidad.dominio.ElementoMenu;
import co.edu.konradlorenz.identidad.dominio.PoliticaContrasena;
import co.edu.konradlorenz.identidad.dominio.TokenAcceso;
import co.edu.konradlorenz.identidad.dominio.Usuario;

import java.util.List;

/** V01 y el alta de usuarios (DEC-10, DEC-62): política de contraseña, Factory Method por rol e inicio de sesión. */
final class PruebasIdentidad {

    private PruebasIdentidad() {
    }

    static void ejecutar() {
        politica();
        registroEInicio();
    }

    private static void politica() {
        Prueba.grupo("ms-identidad: política de contraseña (DEC-10)");
        PoliticaContrasena p = new PoliticaContrasena();
        p.validar("Konrad2026");
        Prueba.verificar("Konrad2026 cumple la regla", true);
        Prueba.lanza("7 caracteres no alcanzan", IllegalArgumentException.class, "8 caracteres o más", () -> p.validar("Konr4d1"));
        Prueba.lanza("sin mayúscula", IllegalArgumentException.class, "una mayúscula", () -> p.validar("konrad2026"));
        Prueba.lanza("sin minúscula", IllegalArgumentException.class, "una minúscula", () -> p.validar("KONRAD2026"));
        Prueba.lanza("sin número", IllegalArgumentException.class, "un número", () -> p.validar("KonradGourmet"));
        Prueba.lanza("vacía: dice todo lo que falta", IllegalArgumentException.class,
                "8 caracteres o más, una mayúscula, una minúscula, un número", () -> p.validar(null));
    }

    private static void registroEInicio() {
        Prueba.grupo("ms-identidad: alta de usuarios e inicio de sesión (V01)");
        UsuarioEnMemoria usuarios = new UsuarioEnMemoria();
        CifradorEnMemoria cifrador = new CifradorEnMemoria();
        BusEnMemoria bus = new BusEnMemoria();
        RegistrarUsuarioService registro = new RegistrarUsuarioService(usuarios, cifrador, new PoliticaContrasena(), bus);
        IniciarSesionService sesion = new IniciarSesionService(usuarios, cifrador, new EmisorTokenEnMemoria(), bus);

        List<String> roles = List.of("MESERO", "JEFE_COCINA", "AUXILIAR_COMPRAS", "DIRECTOR_COMPRAS", "DIRECTOR_COMERCIAL", "ADMINISTRADOR");
        for (int i = 0; i < roles.size(); i++) {
            registro.registrar("Usuario " + i, "u" + i + "@konradgourmet.com", roles.get(i), 1L, "Konrad2026");
        }
        Prueba.igual("se registran los 6 roles", 6, usuarios.todos().size());
        Prueba.igual("cada rol crea su subclase (Factory Method)",
                List.of("Mesero", "JefeCocina", "AuxiliarCompras", "DirectorCompras", "DirectorComercial", "Administrador"),
                usuarios.todos().stream().map(u -> u.getClass().getSimpleName()).toList());
        Usuario mesero = usuarios.buscarPorCorreo("u0@konradgourmet.com");
        Prueba.igual("el id lo asigna el repositorio", 1L, mesero.id());
        Prueba.verificar("no se guarda la clave en claro", !mesero.passwordHash().contains("Konrad2026"));
        Prueba.igual("el alta queda en la auditoría", 6L, bus.cuantos("auditoria.evento"));

        Long id = registro.registrar("Ana", "  Ana.Perez@KonradGourmet.com ", "mesero", 2L, "Konrad2026");
        Prueba.igual("el correo se guarda en minúsculas y sin espacios", "ana.perez@konradgourmet.com",
                usuarios.buscarPorCorreo("ana.perez@konradgourmet.com").correo());
        Prueba.igual("devuelve el id nuevo", 7L, id);
        Prueba.lanza("correo repetido", IllegalArgumentException.class, "Ya existe un usuario con el correo",
                () -> registro.registrar("Otra", "ana.perez@konradgourmet.com", "MESERO", 1L, "Konrad2026"));
        Prueba.lanza("correo mal escrito", IllegalArgumentException.class, "El formato del correo electrónico no es válido.",
                () -> registro.registrar("Otra", "ana.konradgourmet.com", "MESERO", 1L, "Konrad2026"));
        Prueba.lanza("rol que no existe", IllegalArgumentException.class, "El rol CHEF no existe.",
                () -> registro.registrar("Otra", "otra@konradgourmet.com", "CHEF", 1L, "Konrad2026"));
        Prueba.lanza("clave que no cumple la política", IllegalArgumentException.class, "La contraseña debe tener",
                () -> registro.registrar("Otra", "otra@konradgourmet.com", "MESERO", 1L, "corta"));
        Prueba.lanza("sin sucursal", IllegalArgumentException.class, "La sucursal es obligatoria.",
                () -> registro.registrar("Otra", "otra@konradgourmet.com", "MESERO", null, "Konrad2026"));

        long antes = bus.cuantos("auditoria.evento");
        TokenAcceso t = sesion.iniciarSesion(" U3@KonradGourmet.com", "Konrad2026");
        Prueba.igual("el token lo firma el emisor (EmisorTokenPort)", "demo.DIRECTOR_COMPRAS.u3@konradgourmet.com", t.jwt());
        Prueba.igual("el menú del token es el del rol (Composite)", List.of("Inicio", "Cotizaciones", "Órdenes de compra", "Mi perfil", "Cerrar sesión"),
                t.menu().hijos().stream().map(ElementoMenu::titulo).toList());
        Prueba.igual("el inicio de sesión queda en la auditoría", antes + 1, bus.cuantos("auditoria.evento"));
        AuditoriaEvent ultimo = (AuditoriaEvent) bus.publicados().get(bus.publicados().size() - 1);
        Prueba.igual("con el correo de quien entró", "u3@konradgourmet.com", ultimo.usuario());

        Prueba.lanza("clave equivocada", IllegalArgumentException.class, "Correo o contraseña incorrectos.",
                () -> sesion.iniciarSesion("u3@konradgourmet.com", "Konrad2027"));
        Prueba.lanza("correo que no existe: el mismo mensaje", IllegalArgumentException.class, "Correo o contraseña incorrectos.",
                () -> sesion.iniciarSesion("nadie@konradgourmet.com", "Konrad2026"));
        Prueba.igual("los intentos fallidos también se auditan", antes + 3, bus.cuantos("auditoria.evento"));

        for (Usuario u : usuarios.todos()) {
            ElementoMenu m = u.construirMenu();
            List<String> titulos = m.hijos().stream().map(ElementoMenu::titulo).toList();
            Prueba.verificar("el menú de " + u.rol() + " termina en Mi perfil y Cerrar sesión",
                    titulos.size() >= 3 && titulos.get(titulos.size() - 2).equals("Mi perfil") && titulos.get(titulos.size() - 1).equals("Cerrar sesión"));
        }
        Prueba.igual("el director comercial ve el tablero BAM", "Tablero BAM",
                usuarios.buscarPorCorreo("u4@konradgourmet.com").construirMenu().hijos().get(0).titulo());
    }

}
