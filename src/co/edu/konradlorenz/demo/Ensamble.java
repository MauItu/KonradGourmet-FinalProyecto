package co.edu.konradlorenz.demo;

import co.edu.konradlorenz.administracion.adaptadores.entrada.AdminRestController;
import co.edu.konradlorenz.administracion.adaptadores.entrada.AuditoriaListener;
import co.edu.konradlorenz.administracion.adaptadores.salida.AuditoriaJpaAdapter;
import co.edu.konradlorenz.administracion.aplicacion.AdministracionService;
import co.edu.konradlorenz.administracion.dominio.Bitacora;
import co.edu.konradlorenz.analitica.adaptadores.entrada.TableroRestController;
import co.edu.konradlorenz.analitica.adaptadores.salida.KpiJpaAdapter;
import co.edu.konradlorenz.analitica.adaptadores.salida.RedesSocialesAdapter;
import co.edu.konradlorenz.analitica.adaptadores.salida.SugerenciaJpaAdapter;
import co.edu.konradlorenz.analitica.aplicacion.TableroService;
import co.edu.konradlorenz.analitica.dominio.CalculadorKpi;
import co.edu.konradlorenz.analitica.dominio.HechoNegocio;
import co.edu.konradlorenz.analitica.dominio.ProductoMayorDemanda;
import co.edu.konradlorenz.analitica.dominio.ProveedorMejoresPrecios;
import co.edu.konradlorenz.analitica.dominio.SucursalMayorDemanda;
import co.edu.konradlorenz.cotizaciones.adaptadores.entrada.CotizacionRestController;
import co.edu.konradlorenz.cotizaciones.adaptadores.entrada.OrdenGeneradaListener;
import co.edu.konradlorenz.cotizaciones.adaptadores.entrada.ProveedorRestController;
import co.edu.konradlorenz.cotizaciones.adaptadores.salida.CotizacionJpaAdapter;
import co.edu.konradlorenz.cotizaciones.aplicacion.CotizacionService;
import co.edu.konradlorenz.cotizaciones.aplicacion.GenerarCotizacionFacade;
import co.edu.konradlorenz.cotizaciones.aplicacion.ProveedorService;
import co.edu.konradlorenz.cotizaciones.dominio.Cotizacion;
import co.edu.konradlorenz.cotizaciones.dominio.ReglaValidacion;
import co.edu.konradlorenz.integracion.adaptadores.salida.MinisterioComercioAdapter;
import co.edu.konradlorenz.integracion.adaptadores.salida.PrecioReferenciaCacheProxy;
import co.edu.konradlorenz.integracion.aplicacion.ConsultarPrecioService;
import co.edu.konradlorenz.integracion.dominio.PrecioEstandar;
import co.edu.konradlorenz.inventario.adaptadores.entrada.ConsumoDiarioJob;
import co.edu.konradlorenz.inventario.adaptadores.entrada.InventarioEventsListener;
import co.edu.konradlorenz.inventario.adaptadores.entrada.InventarioRestController;
import co.edu.konradlorenz.inventario.adaptadores.entrada.PedidoRegistradoListener;
import co.edu.konradlorenz.inventario.aplicacion.InventarioService;
import co.edu.konradlorenz.inventario.dominio.UmbralStock;
import co.edu.konradlorenz.notificaciones.adaptadores.entrada.DestinatarioRestController;
import co.edu.konradlorenz.notificaciones.adaptadores.entrada.NotificacionListeners;
import co.edu.konradlorenz.notificaciones.adaptadores.salida.CorreoCertificadoDecorator;
import co.edu.konradlorenz.notificaciones.adaptadores.salida.EstampaCronologicaDecorator;
import co.edu.konradlorenz.notificaciones.aplicacion.NotificacionService;
import co.edu.konradlorenz.notificaciones.dominio.CreadorAlarmaApp;
import co.edu.konradlorenz.notificaciones.dominio.CreadorCorreo;
import co.edu.konradlorenz.notificaciones.puertos.salida.Correo;
import co.edu.konradlorenz.ordenes.adaptadores.entrada.OrdenCompraRestController;
import co.edu.konradlorenz.ordenes.aplicacion.OrdenCompraService;
import co.edu.konradlorenz.pedidos.adaptadores.entrada.MenuActualizadoListener;
import co.edu.konradlorenz.pedidos.adaptadores.entrada.PedidoRestController;
import co.edu.konradlorenz.pedidos.aplicacion.PedidoService;
import co.edu.konradlorenz.solicitudes.adaptadores.entrada.CotizacionEventsListener;
import co.edu.konradlorenz.solicitudes.adaptadores.entrada.SolicitudRestController;
import co.edu.konradlorenz.solicitudes.aplicacion.SolicitudService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Arma el sistema completo con adaptadores en memoria, como lo haría la clase de configuración del
 * proyecto real: crea cada adaptador, cada servicio con su constructor, fija el orden de los decoradores
 * del correo y suscribe cada listener a sus tópicos. Lo usan la demo y las pruebas.
 */
public class Ensamble {

    // ---- infraestructura en memoria ----
    public final BusEnMemoria bus = new BusEnMemoria();
    public final AlmacenEnMemoria almacen = new AlmacenEnMemoria();
    public final MinisterioSoapSimulado ministerio = new MinisterioSoapSimulado();
    public final Map<Long, PrecioEstandar> cachePrecios = new HashMap<>(); // el mapa que en producción respalda Redis
    public final CorreoEnMemoria correos = new CorreoEnMemoria();
    public final AlarmaEnMemoria alarmas = new AlarmaEnMemoria();
    public final EnvioEnMemoria envios = new EnvioEnMemoria();
    public final DestinatarioEnMemoria destinatarios = new DestinatarioEnMemoria();
    public final Bitacora bitacora = new Bitacora();
    public final List<HechoNegocio> hechos = new ArrayList<>();

    // ---- puntos de entrada (controladores y job) ----
    public final PedidoRestController pedidos;
    public final InventarioRestController inventario;
    public final ConsumoDiarioJob consumoDiario;
    public final SolicitudRestController solicitudes;
    public final ProveedorRestController proveedores;
    public final CotizacionRestController cotizaciones;
    public final OrdenCompraRestController ordenes;
    public final DestinatarioRestController destinatariosRest;
    public final TableroRestController tablero;
    public final AdminRestController admin;

    /** @param ultimaOrden el número de la última orden que ya existe: la próxima será el siguiente (11 → OC-0012) */
    public Ensamble(int ultimaOrden) {
        // ms-integracion: Proxy con caché sobre el Adapter del servicio web
        ConsultarPrecioService integracion = new ConsultarPrecioService(
                new PrecioReferenciaCacheProxy(new MinisterioComercioAdapter(ministerio), cachePrecios), bus);

        // ms-notificaciones: el orden de los decoradores se fija aquí
        Correo correo = new CorreoCertificadoDecorator(new EstampaCronologicaDecorator(correos));
        NotificacionService notificaciones = new NotificacionService(
                new CreadorCorreo(correo), new CreadorAlarmaApp(alarmas), envios, destinatarios, bus);
        destinatariosRest = new DestinatarioRestController(notificaciones);
        NotificacionListeners alNotificar = new NotificacionListeners(notificaciones);

        // ms-pedidos
        PedidoService pedidoService = new PedidoService(new PedidoEnMemoria(), new CatalogoEnMemoria(), bus);
        pedidos = new PedidoRestController(pedidoService);

        // ms-inventario
        InventarioService inventarioService = new InventarioService(
                new InventarioEnMemoria(), new UmbralStock(new BigDecimal("25")), bus);
        inventario = new InventarioRestController(inventarioService);
        consumoDiario = new ConsumoDiarioJob(inventarioService);
        InventarioEventsListener alInventario = new InventarioEventsListener(inventarioService);

        // ms-solicitudes
        SolicitudService solicitudService = new SolicitudService(new SolicitudEnMemoria(), bus);
        solicitudes = new SolicitudRestController(solicitudService);
        CotizacionEventsListener alCotizar = new CotizacionEventsListener(solicitudService);

        // ms-cotizaciones: la cadena de reglas se arma en un solo lugar, cadenaPorDefecto()
        CotizacionJpaAdapter repositorioCotizaciones = new CotizacionJpaAdapter(new HashMap<String, Cotizacion>());
        GenerarCotizacionFacade fachada = new GenerarCotizacionFacade(
                repositorioCotizaciones, new PrecioSugeridoLocal(integracion), new PdfSimulado(), almacen, bus);
        CotizacionService cotizacionService = new CotizacionService(
                fachada, repositorioCotizaciones, ReglaValidacion.cadenaPorDefecto(), almacen, bus);
        cotizaciones = new CotizacionRestController(cotizacionService);
        proveedores = new ProveedorRestController(new ProveedorService(new ProveedorEnMemoria(), bus));

        // ms-ordenes
        ordenes = new OrdenCompraRestController(new OrdenCompraService(
                new OrdenEnMemoria(ultimaOrden), new CotizacionConsultaLocal(cotizacionService), new PdfSimulado(), almacen, bus));

        // ms-analitica: las tres estrategias del Strategy
        List<CalculadorKpi> estrategias = List.of(new ProductoMayorDemanda(), new SucursalMayorDemanda(), new ProveedorMejoresPrecios());
        tablero = new TableroRestController(new TableroService(
                estrategias, new KpiJpaAdapter(hechos), new RedesSocialesAdapter(), bus, new SugerenciaJpaAdapter()));

        // ms-administracion
        AdministracionService administracion = new AdministracionService(
                new AuditoriaJpaAdapter(bitacora), new LogsEnMemoria(), new ParametrosEnMemoria());
        admin = new AdminRestController(administracion);

        // Publish-Subscribe: quién escucha cada tópico
        bus.suscribir("menu.actualizado", new MenuActualizadoListener(pedidoService));
        bus.suscribir("menu.actualizado", alInventario);
        bus.suscribir("pedido.registrado", new PedidoRegistradoListener(inventarioService));
        bus.suscribir("solicitud.registrada", alInventario);
        bus.suscribir("solicitud.registrada", alNotificar);
        bus.suscribir("solicitud.en-tramite", alNotificar);
        bus.suscribir("cotizacion.enviada", alCotizar);
        bus.suscribir("cotizacion.enviada", alNotificar);
        bus.suscribir("orden.generada", alCotizar);
        bus.suscribir("orden.generada", new OrdenGeneradaListener(cotizacionService));
        bus.suscribir("orden.generada", alNotificar);
        bus.suscribir("inventario.stock-bajo", alNotificar);
        bus.suscribir("auditoria.evento", new AuditoriaListener(administracion));
    }

}
