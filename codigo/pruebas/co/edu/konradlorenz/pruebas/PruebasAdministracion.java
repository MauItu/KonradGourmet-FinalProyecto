package co.edu.konradlorenz.pruebas;

import co.edu.konradlorenz.administracion.adaptadores.salida.AuditoriaJpaAdapter;
import co.edu.konradlorenz.administracion.aplicacion.AdministracionService;
import co.edu.konradlorenz.administracion.dominio.Bitacora;
import co.edu.konradlorenz.administracion.dominio.LogError;
import co.edu.konradlorenz.administracion.dominio.RegistroAuditoria;
import co.edu.konradlorenz.compartido.eventos.AuditoriaEvent;
import co.edu.konradlorenz.demo.Ensamble;
import co.edu.konradlorenz.demo.ParametrosEnMemoria;
import co.edu.konradlorenz.identidad.dominio.Administrador;
import co.edu.konradlorenz.identidad.dominio.ElementoMenu;
import co.edu.konradlorenz.identidad.dominio.Sucursal;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** ms-administracion (corte 6a): parámetros y tema (HU-20, DEC-85), auditoría (HU-21, PD-07) y logs (HU-22). */
final class PruebasAdministracion {

    private PruebasAdministracion() {
    }

    static void ejecutar() {
        parametros();
        auditoria();
        logs();
        destinatariosYMenu();
    }

    private static void parametros() {
        Prueba.grupo("ms-administracion: parámetros y tema (HU-20, DEC-85)");
        Bitacora bitacora = new Bitacora();
        ParametrosEnMemoria base = new ParametrosEnMemoria();
        AdministracionService admin = new AdministracionService(new AuditoriaJpaAdapter(bitacora), servicio -> List.of(), base);

        admin.parametrizar("  general.moneda ", " COP ");
        Prueba.igual("guarda el parámetro en la base, sin espacios", "COP", base.cargar().get("general.moneda"));
        Prueba.igual("parametros() lo devuelve", "COP", admin.parametros().get("general.moneda"));
        RegistroAuditoria r = bitacora.registros().get(bitacora.registros().size() - 1);
        Prueba.igual("el cambio queda en la bitácora, sin pasar por Kafka (n_bitacora)", "Parámetro general.moneda actualizado", r.accion());
        Prueba.igual("el núcleo pone «sistema» y el adaptador anota a quien inició sesión (DEC-64)", "sistema", r.usuario());
        Prueba.igual("con la fecha de hoy", LocalDate.now(), r.fecha());

        admin.parametrizar("tema.color-primario", "#1F7A4D");
        admin.parametrizar("tema.logo", "data:image/png;base64,iVBORw0KGgo=");
        Map<String, String> tema = admin.tema();
        Prueba.igual("tema() devuelve solo las claves tema.*", false, tema.containsKey("general.moneda"));
        Prueba.igual("con el color nuevo", "#1F7A4D", tema.get("tema.color-primario"));
        Prueba.igual("y el logo en una data URL", "data:image/png;base64,iVBORw0KGgo=", tema.get("tema.logo"));
        admin.parametrizar("tema.logo", "/assets/logo/logo.png");
        Prueba.igual("el logo de siempre es una ruta de la SPA", "/assets/logo/logo.png", admin.tema().get("tema.logo"));

        Prueba.lanza("clave vacía (PD-10)", IllegalArgumentException.class, "La clave del parámetro es obligatoria.",
                () -> admin.parametrizar(" ", "x"));
        Prueba.lanza("valor vacío (PD-10)", IllegalArgumentException.class, "El valor del parámetro general.moneda es obligatorio.",
                () -> admin.parametrizar("general.moneda", "  "));
        Prueba.lanza("clave con mayúsculas o espacios", IllegalArgumentException.class, "solo puede tener minúsculas",
                () -> admin.parametrizar("General Moneda", "COP"));
        Prueba.lanza("color que no es #RRGGBB", IllegalArgumentException.class, "El color debe tener el formato #RRGGBB",
                () -> admin.parametrizar("tema.color-primario", "naranja"));
        Prueba.lanza("logo que no es una imagen", IllegalArgumentException.class, "El logo debe ser una imagen",
                () -> admin.parametrizar("tema.logo", "javascript:alert(1)"));
        Prueba.lanza("logo de más de 200 KB", IllegalArgumentException.class, "máximo 200 KB",
                () -> admin.parametrizar("tema.logo", "data:image/png;base64," + "A".repeat(300_000)));
        Prueba.lanza("valor de más de 500 caracteres", IllegalArgumentException.class, "no puede tener más de 500 caracteres",
                () -> admin.parametrizar("general.nota", "x".repeat(501)));
        Prueba.igual("lo rechazado no se guarda", "#1F7A4D", base.cargar().get("tema.color-primario"));
    }

    private static void auditoria() {
        Prueba.grupo("ms-administracion: auditoría por rango de fechas (HU-21, PD-07)");
        Bitacora bitacora = new Bitacora();
        AdministracionService admin = new AdministracionService(new AuditoriaJpaAdapter(bitacora), servicio -> List.of(), new ParametrosEnMemoria());
        LocalDate hoy = LocalDate.now();
        admin.registrarAuditoria(new RegistroAuditoria("Pedido PED-0251 registrado", "isabella.torres@konradgourmet.com", hoy.minusDays(3), LocalTime.of(12, 0)));
        admin.registrarAuditoria(new RegistroAuditoria("Solicitud SOL-031 registrada", "carlos.mendoza@konradgourmet.com", hoy, LocalTime.of(9, 30)));
        admin.registrarAuditoria(new RegistroAuditoria("Orden OC-0012 generada", "laura.gomez@konradgourmet.com", hoy, LocalTime.of(16, 5)));

        List<RegistroAuditoria> deHoy = admin.auditoria(hoy, hoy);
        Prueba.igual("solo los del rango", 2, deHoy.size());
        Prueba.igual("lo más reciente primero", "Orden OC-0012 generada", deHoy.get(0).accion());
        Prueba.igual("con usuario, fecha y hora", "laura.gomez@konradgourmet.com " + hoy + " 16:05",
                deHoy.get(0).usuario() + " " + deHoy.get(0).fecha() + " " + deHoy.get(0).hora());
        Prueba.igual("los extremos del rango cuentan", 3, admin.auditoria(hoy.minusDays(3), hoy).size());
        Prueba.igual("un periodo sin movimientos da una lista vacía", 0, admin.auditoria(hoy.minusDays(30), hoy.minusDays(10)).size());
        Prueba.lanza("el rango es obligatorio", IllegalArgumentException.class, "Seleccione la fecha inicial y la fecha final.",
                () -> admin.auditoria(null, hoy));
        Prueba.lanza("«desde» no puede ser posterior a «hasta» (PD-10)", IllegalArgumentException.class,
                "La fecha inicial no puede ser posterior a la fecha final.", () -> admin.auditoria(hoy, hoy.minusDays(1)));

        Ensamble s = Datos.sistema();
        int antes = s.bitacora.registros().size();
        s.bus.publicar(new AuditoriaEvent("carlos.mendoza@konradgourmet.com", "Plato Sancocho creado"));
        RegistroAuditoria r = s.bitacora.registros().get(s.bitacora.registros().size() - 1);
        Prueba.igual("AuditoriaListener guarda cada auditoria.evento de los demás servicios", antes + 1, s.bitacora.registros().size());
        Prueba.igual("con la acción y el usuario del evento", "Plato Sancocho creado · carlos.mendoza@konradgourmet.com", r.accion() + " · " + r.usuario());
    }

    private static void logs() {
        Prueba.grupo("ms-administracion: logs de errores (HU-22)");
        List<String> pedidos = new ArrayList<>();
        LogError error = new LogError("ms-pedidos", "Falló la conexión con la base", LocalDateTime.now());
        AdministracionService admin = new AdministracionService(new AuditoriaJpaAdapter(new Bitacora()),
                servicio -> {
                    pedidos.add(String.valueOf(servicio));
                    return List.of(error);
                }, new ParametrosEnMemoria());
        Prueba.igual("devuelve lo que trae LogsPort", "ms-pedidos", admin.logs("ms-pedidos").get(0).servicio());
        admin.logs("  ");
        admin.logs(null);
        admin.logs(" ms-ordenes ");
        Prueba.igual("sin servicio pide todos (null) y el servicio va sin espacios", List.of("ms-pedidos", "null", "null", "ms-ordenes"), pedidos);
    }

    private static void destinatariosYMenu() {
        Prueba.grupo("Destinatarios (CU-15) y menú del administrador");
        Ensamble s = Datos.sistema();
        long antes = s.bus.cuantos("auditoria.evento");
        s.destinatariosRest.registrar(2L, "jefe.norte@konradgourmet.com");
        Prueba.igual("registrar un destinatario deja auditoría", antes + 1, s.bus.cuantos("auditoria.evento"));
        Prueba.verificar("con el correo y la sucursal", s.bus.publicados().stream().anyMatch(e -> e instanceof AuditoriaEvent a
                && a.accion().equals("Destinatario registrado: jefe.norte@konradgourmet.com (sucursal 2)")));
        Prueba.lanza("correo mal formado (PD-10)", IllegalArgumentException.class, "El formato del correo electrónico no es válido.",
                () -> s.destinatariosRest.registrar(2L, "jefe.konradgourmet.com"));

        ElementoMenu menu = new Administrador(1L, "Paula Herrera", "paula.herrera@konradgourmet.com", new Sucursal(1L, "Sucursal Centro")).construirMenu();
        ElementoMenu administracion = menu.hijos().get(1);
        Prueba.igual("Administración agrupa sus tres ventanas (Composite)", List.of("Parámetros y tema", "Auditoría", "Logs de errores"),
                administracion.hijos().stream().map(ElementoMenu::titulo).toList());
        Prueba.igual("con sus rutas", List.of("/admin/parametros", "/admin/auditoria", "/admin/logs"),
                administracion.hijos().stream().map(ElementoMenu::ruta).toList());
    }

}
