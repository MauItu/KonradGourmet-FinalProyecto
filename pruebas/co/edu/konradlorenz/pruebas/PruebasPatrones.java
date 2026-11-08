package co.edu.konradlorenz.pruebas;

import co.edu.konradlorenz.administracion.dominio.ParametrosSistema;
import co.edu.konradlorenz.analitica.adaptadores.salida.KpiJpaAdapter;
import co.edu.konradlorenz.analitica.dominio.HechoNegocio;
import co.edu.konradlorenz.analitica.dominio.ProductoMayorDemanda;
import co.edu.konradlorenz.analitica.dominio.ProveedorMejoresPrecios;
import co.edu.konradlorenz.analitica.dominio.ResultadoKpi;
import co.edu.konradlorenz.analitica.dominio.SucursalMayorDemanda;
import co.edu.konradlorenz.compartido.eventos.EventoDominio;
import co.edu.konradlorenz.demo.AlarmaEnMemoria;
import co.edu.konradlorenz.demo.BusEnMemoria;
import co.edu.konradlorenz.demo.CorreoEnMemoria;
import co.edu.konradlorenz.demo.MinisterioSoapSimulado;
import co.edu.konradlorenz.identidad.dominio.DirectorCompras;
import co.edu.konradlorenz.identidad.dominio.ElementoMenu;
import co.edu.konradlorenz.identidad.dominio.GrupoMenu;
import co.edu.konradlorenz.identidad.dominio.Mesero;
import co.edu.konradlorenz.identidad.dominio.OpcionMenu;
import co.edu.konradlorenz.identidad.dominio.Sucursal;
import co.edu.konradlorenz.integracion.adaptadores.salida.MinisterioComercioAdapter;
import co.edu.konradlorenz.integracion.adaptadores.salida.PrecioReferenciaCacheProxy;
import co.edu.konradlorenz.integracion.dominio.PrecioEstandar;
import co.edu.konradlorenz.integracion.puertos.salida.PrecioReferenciaPort;
import co.edu.konradlorenz.notificaciones.adaptadores.salida.CorreoCertificadoDecorator;
import co.edu.konradlorenz.notificaciones.adaptadores.salida.EstampaCronologicaDecorator;
import co.edu.konradlorenz.notificaciones.dominio.CreadorAlarmaApp;
import co.edu.konradlorenz.notificaciones.dominio.CreadorCorreo;
import co.edu.konradlorenz.notificaciones.dominio.Mensaje;
import co.edu.konradlorenz.notificaciones.puertos.salida.Correo;
import co.edu.konradlorenz.pedidos.eventos.PedidoRegistradoEvent;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Los patrones de diseño, uno por uno y sin el resto del sistema. */
final class PruebasPatrones {

    private PruebasPatrones() {
    }

    static void ejecutar() {
        composite();
        factoryMethod();
        decorator();
        proxyYAdapter();
        singleton();
        strategy();
        observer();
    }

    private static void composite() {
        Prueba.grupo("Composite + Factory Method: el menú por rol");
        Sucursal centro = new Sucursal(1L, "Centro");
        DirectorCompras director = new DirectorCompras(5L, "Laura Gómez", "laura@kg.co", centro);
        ElementoMenu menu = director.construirMenu();
        Prueba.igual("el rol", "DIRECTOR_COMPRAS", director.rol());
        Prueba.igual("las opciones del primer nivel", List.of("Inicio", "Cotizaciones", "Órdenes de compra", "Mi perfil", "Cerrar sesión"),
                menu.hijos().stream().map(ElementoMenu::titulo).toList());
        ElementoMenu cotizaciones = menu.hijos().get(1);
        Prueba.igual("Cotizaciones es un grupo con 3 opciones", List.of("Todas", "Pendientes de validar", "Opcionadas"),
                cotizaciones.hijos().stream().map(ElementoMenu::titulo).toList());
        Prueba.verificar("un grupo no es hoja", !cotizaciones.esHoja());
        Prueba.verificar("una opción es hoja", menu.hijos().get(0).esHoja());
        Prueba.igual("una hoja no tiene hijos", 0, menu.hijos().get(0).hijos().size());
        Prueba.lanza("a una opción no se le agregan hijos", UnsupportedOperationException.class, "no tiene hijos",
                () -> menu.hijos().get(0).agregar(new OpcionMenu("x", "/x")));
        GrupoMenu grupo = new GrupoMenu("Grupo", "/g");
        grupo.agregar(new OpcionMenu("Uno", "/g/1"));
        Prueba.igual("un grupo guarda lo que se le agrega", 1, grupo.hijos().size());
        Prueba.lanza("los hijos de un grupo no se modifican desde afuera", UnsupportedOperationException.class, "",
                () -> grupo.hijos().add(new OpcionMenu("Dos", "/g/2")));
        Prueba.igual("el Mesero todavía no tiene menú (pendiente)", "MESERO", new Mesero(1L, "Isabella", "i@kg.co", centro).rol());
    }

    private static void factoryMethod() {
        Prueba.grupo("Factory Method: CreadorNotificacion");
        CorreoEnMemoria correos = new CorreoEnMemoria();
        new CreadorCorreo(correos).notificar("jefe@kg.co", "Asunto de prueba\nCuerpo de prueba");
        Prueba.igual("CreadorCorreo crea un correo con el asunto de la primera línea", "Asunto de prueba", correos.enviados().get(0).asunto());
        Prueba.igual("y el resto como cuerpo", "Cuerpo de prueba", correos.enviados().get(0).cuerpo());
        new CreadorCorreo(correos).notificar("jefe@kg.co", "Sin salto de línea");
        Prueba.igual("sin salto de línea el asunto es genérico", "Konrad Gourmet", correos.enviados().get(1).asunto());
        AlarmaEnMemoria alarmas = new AlarmaEnMemoria();
        new CreadorAlarmaApp(alarmas).notificar("jefe@kg.co", "Res al 23 %");
        Prueba.igual("CreadorAlarmaApp empuja la alarma", List.of("jefe@kg.co: Res al 23 %"), alarmas.alarmas());
    }

    private static void decorator() {
        Prueba.grupo("Decorator: cabeceras del correo");
        CorreoEnMemoria base = new CorreoEnMemoria();
        Correo correo = new CorreoCertificadoDecorator(new EstampaCronologicaDecorator(base));
        Mensaje m = new Mensaje("jefe@kg.co", "Asunto", "Cuerpo", null);
        correo.enviar(m);
        Prueba.igual("el correo llegó al componente concreto", 1, base.enviados().size());
        Prueba.igual("las cabeceras, en el orden de los decoradores", List.of("X-Certificado", "X-Sello-Tiempo"), new ArrayList<>(m.cabeceras().keySet()));
        Prueba.verificar("el certificado es una huella SHA-256", m.cabeceras().get("X-Certificado").startsWith("SHA-256:"));
        Prueba.verificar("el sello es una fecha con zona horaria", m.cabeceras().get("X-Sello-Tiempo").matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}-05:00"));
        Mensaje igual = new Mensaje("jefe@kg.co", "Otro asunto", "Cuerpo", null);
        correo.enviar(igual);
        Prueba.igual("el mismo destinatario y cuerpo dan el mismo certificado", m.cabeceras().get("X-Certificado"), igual.cabeceras().get("X-Certificado"));
        Mensaje distinto = new Mensaje("jefe@kg.co", "Asunto", "Cuerpo cambiado", null);
        correo.enviar(distinto);
        Prueba.verificar("un cuerpo distinto da otro certificado", !m.cabeceras().get("X-Certificado").equals(distinto.cabeceras().get("X-Certificado")));
        CorreoEnMemoria soloSello = new CorreoEnMemoria();
        Mensaje m2 = new Mensaje("a@kg.co", "A", "B", null);
        new EstampaCronologicaDecorator(soloSello).enviar(m2);
        Prueba.igual("cada decorador se puede usar solo", List.of("X-Sello-Tiempo"), new ArrayList<>(m2.cabeceras().keySet()));
    }

    private static void proxyYAdapter() {
        Prueba.grupo("Proxy + Adapter: el precio estándar");
        MinisterioSoapSimulado ministerio = new MinisterioSoapSimulado();
        ministerio.definirPrecio("1", 20000);
        MinisterioComercioAdapter adaptador = new MinisterioComercioAdapter(ministerio);
        Prueba.igualValor("el Adapter traduce el double del servicio web a PrecioEstandar", "20000", adaptador.precioEstandar(1L).valor());
        Prueba.igual("y recuerda de qué producto es", 1L, adaptador.precioEstandar(1L).productoId());

        Map<Long, PrecioEstandar> cache = new HashMap<>();
        MinisterioSoapSimulado otro = new MinisterioSoapSimulado();
        otro.definirPrecio("1", 20000);
        PrecioReferenciaPort proxy = new PrecioReferenciaCacheProxy(new MinisterioComercioAdapter(otro), cache);
        proxy.precioEstandar(1L);
        Prueba.igual("la primera consulta llama al servicio web", 1, otro.llamadas());
        proxy.precioEstandar(1L);
        Prueba.igual("la segunda la responde la caché", 1, otro.llamadas());
        Prueba.verificar("el precio quedó en la caché", cache.containsKey(1L));

        cache.put(1L, new PrecioEstandar(1L, new BigDecimal("999"), LocalDateTime.now().minusHours(23)));
        Prueba.igualValor("un precio de hace 23 horas todavía vale", "999", proxy.precioEstandar(1L).valor());
        cache.put(1L, new PrecioEstandar(1L, new BigDecimal("999"), LocalDateTime.now().minusDays(2)));
        Prueba.igualValor("uno de hace 2 días venció y se vuelve a pedir", "20000", proxy.precioEstandar(1L).valor());
        Prueba.igual("con una llamada más al servicio web", 2, otro.llamadas());
        Prueba.lanza("un producto que el Ministerio no conoce", IllegalArgumentException.class, "no tiene precio", () -> proxy.precioEstandar(77L));
    }

    private static void singleton() {
        Prueba.grupo("Singleton: ParametrosSistema");
        ParametrosSistema a = ParametrosSistema.getInstancia();
        ParametrosSistema b = ParametrosSistema.getInstancia();
        Prueba.verificar("dos llamadas dan la misma instancia", a == b);
        a.actualizar("tema.prueba", "azul");
        Prueba.igual("lo que se actualiza en una se ve en la otra", "azul", b.valor("tema.prueba"));
        Prueba.igual("un parámetro que no existe es null", null, b.valor("tema.no-existe"));
        Prueba.verificar("valores() es una copia de solo lectura", b.valores().containsKey("tema.prueba"));
        Prueba.lanza("y no se puede modificar", UnsupportedOperationException.class, "", () -> b.valores().put("x", "y"));
    }

    private static void strategy() {
        Prueba.grupo("Strategy: los KPI del tablero");
        List<HechoNegocio> hechos = new ArrayList<>();
        hechos.add(hecho("DEMANDA_PRODUCTO", 1L, "Bandeja paisa", "22", LocalDate.of(2026, 9, 1)));
        hechos.add(hecho("DEMANDA_PRODUCTO", 1L, "Bandeja paisa", "18", LocalDate.of(2026, 9, 2)));
        hechos.add(hecho("DEMANDA_PRODUCTO", 1L, "Ajiaco", "25", LocalDate.of(2026, 9, 2)));
        hechos.add(hecho("DEMANDA_SUCURSAL", 1L, "Sucursal Centro", "120", LocalDate.of(2026, 9, 1)));
        hechos.add(hecho("DEMANDA_SUCURSAL", 2L, "Sucursal Norte", "85", LocalDate.of(2026, 9, 1)));
        hechos.add(hecho("PRECIO_PROVEEDOR", 1L, "Proveedor ABC", "6.20", LocalDate.of(2026, 9, 1)));
        hechos.add(hecho("PRECIO_PROVEEDOR", 1L, "Proveedor B", "-2.40", LocalDate.of(2026, 9, 1)));
        hechos.add(hecho("PRECIO_PROVEEDOR", 1L, "Proveedor B", "-1.80", LocalDate.of(2026, 9, 2)));
        ResultadoKpi producto = new ProductoMayorDemanda().calcular(hechos);
        Prueba.igual("producto con mayor demanda: Bandeja paisa", "Bandeja paisa", producto.ganador());
        Prueba.igualValor("con 40 unidades (22 + 18)", "40", producto.valor());
        Prueba.igual("sucursal con mayor demanda", "Sucursal Centro", new SucursalMayorDemanda().calcular(hechos).ganador());
        ResultadoKpi proveedor = new ProveedorMejoresPrecios().calcular(hechos);
        Prueba.igual("proveedor con mejores precios: Proveedor B", "Proveedor B", proveedor.ganador());
        Prueba.igualValor("con el promedio más bajo (−2,10 %)", "-2.10", proveedor.valor());
        Prueba.igual("sin hechos no hay ganador", null, new ProductoMayorDemanda().calcular(List.of()).ganador());
        Prueba.igual("cada KPI tiene su nombre", "Producto con mayor demanda", new ProductoMayorDemanda().nombre());

        KpiJpaAdapter repositorio = new KpiJpaAdapter(new ArrayList<>());
        hechos.forEach(repositorio::guardar);
        Prueba.igual("el repositorio filtra por fecha", 8, repositorio.hechos(LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30), null).size());
        Prueba.igual("el del 2 de septiembre", 3, repositorio.hechos(LocalDate.of(2026, 9, 2), LocalDate.of(2026, 9, 2), null).size());
        Prueba.igual("y por sucursal", 1, repositorio.hechos(null, null, 2L).size());
    }

    private static HechoNegocio hecho(String tipo, Long sucursalId, String clave, String valor, LocalDate fecha) {
        return new HechoNegocio(tipo, sucursalId, clave, new BigDecimal(valor), fecha);
    }

    private static void observer() {
        Prueba.grupo("Observer (Publish-Subscribe): el bus en memoria");
        BusEnMemoria bus = new BusEnMemoria();
        List<String> recibidos = new ArrayList<>();
        bus.suscribir("pedido.registrado", e -> recibidos.add("A:" + e.tipo()));
        bus.suscribir("pedido.registrado", e -> recibidos.add("B:" + e.tipo()));
        bus.suscribir("*", e -> recibidos.add("todos:" + e.tipo()));
        EventoDominio evento = new PedidoRegistradoEvent("mesero", "PED-0251", 1L, Map.of(1L, 2));
        bus.publicar(evento);
        Prueba.igual("cada suscriptor del tópico recibe el evento, y «*» recibe todos",
                List.of("todos:pedido.registrado", "A:pedido.registrado", "B:pedido.registrado"), recibidos);
        Prueba.igual("el bus guarda lo publicado", 1, bus.publicados().size());
        Prueba.igual("el evento lleva su usuario", "mesero", evento.usuario());
        Prueba.verificar("y un id, una correlación y una hora", evento.id() != null && evento.correlacionId() != null && evento.ocurridoEn() != null);
        Prueba.verificar("dos eventos tienen ids distintos", !evento.id().equals(new PedidoRegistradoEvent("mesero", "PED-0252", 1L, Map.of()).id()));
        bus.publicar(new PedidoRegistradoEvent("mesero", "PED-0252", 1L, Map.of()));
        Prueba.igual("cuantos() cuenta por tópico", 2L, bus.cuantos("pedido.registrado"));
        Prueba.igual("un tópico sin suscriptores no falla", 0L, bus.cuantos("orden.generada"));
    }

}
