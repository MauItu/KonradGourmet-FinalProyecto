package co.edu.konradlorenz.pruebas;

import co.edu.konradlorenz.analitica.adaptadores.entrada.NegocioEventsListener;
import co.edu.konradlorenz.analitica.adaptadores.entrada.PromocionRestController;
import co.edu.konradlorenz.analitica.adaptadores.entrada.TendenciasJob;
import co.edu.konradlorenz.analitica.adaptadores.salida.KpiJpaAdapter;
import co.edu.konradlorenz.analitica.adaptadores.salida.RedesSocialesAdapter;
import co.edu.konradlorenz.analitica.aplicacion.TableroService;
import co.edu.konradlorenz.analitica.dominio.CalculadorKpi;
import co.edu.konradlorenz.analitica.dominio.EstadoSugerencia;
import co.edu.konradlorenz.analitica.dominio.HechoNegocio;
import co.edu.konradlorenz.analitica.dominio.ProductoMayorDemanda;
import co.edu.konradlorenz.analitica.dominio.ProveedorMejoresPrecios;
import co.edu.konradlorenz.analitica.dominio.ResultadoKpi;
import co.edu.konradlorenz.analitica.dominio.SucursalMayorDemanda;
import co.edu.konradlorenz.analitica.dominio.SugerenciaPromocion;
import co.edu.konradlorenz.analitica.eventos.TendenciaDetectadaEvent;
import co.edu.konradlorenz.analitica.puertos.salida.SugerenciaRepositoryPort;
import co.edu.konradlorenz.analitica.puertos.salida.TendenciasPort;
import co.edu.konradlorenz.compartido.eventos.AuditoriaEvent;
import co.edu.konradlorenz.cotizaciones.eventos.CotizacionValidadaEvent;
import co.edu.konradlorenz.demo.BusEnMemoria;
import co.edu.konradlorenz.demo.Ensamble;
import co.edu.konradlorenz.inventario.eventos.StockBajoEvent;
import co.edu.konradlorenz.notificaciones.dominio.Mensaje;
import co.edu.konradlorenz.ordenes.eventos.OrdenGeneradaEvent;
import co.edu.konradlorenz.pedidos.eventos.PedidoRegistradoEvent;
import co.edu.konradlorenz.solicitudes.eventos.SolicitudRegistradaEvent;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * ms-analitica (corte 6b): el tablero BAM con sus periodos (HU-18, PD-03, DEC-88), los hechos que arma
 * NegocioEventsListener con los 5 tópicos, las promociones de HU-19 (PD-04) con tendencia.detectada en tipos
 * simples (PD-05) y su correo, y la API de redes sociales contra un servidor simulado con el HttpServer del JDK.
 */
final class PruebasAnalitica {

    private PruebasAnalitica() {
    }

    static void ejecutar() {
        ranking();
        periodos();
        listener();
        promociones();
        aceptarYDescartar();
        correoDePromociones();
        redesSociales();
    }

    private static final LocalDate HOY = LocalDate.now();

    private static HechoNegocio hecho(String tipo, Long sucursal, String clave, String valor, int haceDias) {
        return new HechoNegocio(tipo, sucursal, clave, new BigDecimal(valor), HOY.minusDays(haceDias));
    }

    private static List<CalculadorKpi> estrategias() {
        return List.of(new ProductoMayorDemanda(), new SucursalMayorDemanda(), new ProveedorMejoresPrecios());
    }

    private static void ranking() {
        Prueba.grupo("Tablero BAM: el ranking de cada indicador (DEC-88)");
        List<HechoNegocio> hechos = new ArrayList<>();
        String[] platos = {"Ajiaco", "Bandeja paisa", "Punta de anca", "Pechuga a la plancha", "Ensalada césar", "Arepa con queso"};
        for (int i = 0; i < platos.length; i++) {
            hechos.add(hecho("DEMANDA_PRODUCTO", 1L, platos[i], String.valueOf(10 + i), 1));
        }
        hechos.add(hecho("DEMANDA_PRODUCTO", 1L, "Ajiaco", "20", 2));
        ResultadoKpi producto = new ProductoMayorDemanda().calcular(hechos);
        Prueba.igual("gana Ajiaco (10 + 20)", "Ajiaco", producto.ganador());
        Prueba.igual("el ranking tiene los 5 primeros, de mayor a menor", List.of("Ajiaco", "Arepa con queso", "Ensalada césar",
                "Pechuga a la plancha", "Punta de anca"), new ArrayList<>(producto.ranking().keySet()));
        Prueba.igualValor("con su valor", "30", producto.ranking().get("Ajiaco"));
        hechos.add(hecho("PRECIO_PROVEEDOR", null, "Proveedor ABC", "4.81", 1));
        hechos.add(hecho("PRECIO_PROVEEDOR", null, "Proveedor B", "7.69", 1));
        hechos.add(hecho("PRECIO_PROVEEDOR", null, "Proveedor B", "-2.40", 2));
        ResultadoKpi proveedor = new ProveedorMejoresPrecios().calcular(hechos);
        Prueba.igual("proveedor: gana el promedio más bajo (Proveedor B, (7,69 − 2,40) / 2)", "Proveedor B", proveedor.ganador());
        Prueba.igualValor("2,65 %", "2.65", proveedor.valor());
        Prueba.igual("el ranking del proveedor va de menor a mayor diferencia", List.of("Proveedor B", "Proveedor ABC"),
                new ArrayList<>(proveedor.ranking().keySet()));
        Prueba.verificar("sin hechos, el ranking está vacío", new SucursalMayorDemanda().calcular(List.of()).ranking().isEmpty());
        Prueba.lanza("y no se puede modificar", UnsupportedOperationException.class, "", () -> producto.ranking().clear());
    }

    private static void periodos() {
        Prueba.grupo("Tablero BAM: periodos por defecto, rango y sucursal (PD-03)");
        List<HechoNegocio> hechos = new ArrayList<>();
        hechos.add(hecho("DEMANDA_PRODUCTO", 1L, "Bandeja paisa", "10", 1));
        hechos.add(hecho("DEMANDA_SUCURSAL", 1L, "Sucursal Centro", "10", 1));
        hechos.add(hecho("DEMANDA_PRODUCTO", 2L, "Ajiaco", "8", 3));
        hechos.add(hecho("DEMANDA_SUCURSAL", 2L, "Sucursal Norte", "8", 3));
        hechos.add(hecho("DEMANDA_PRODUCTO", 2L, "Ajiaco", "50", 45));       // fuera de los 30 días
        hechos.add(hecho("DEMANDA_SUCURSAL", 2L, "Sucursal Norte", "50", 45));
        hechos.add(hecho("PRECIO_PROVEEDOR", null, "Proveedor C", "-5.00", 200)); // dentro del año
        hechos.add(hecho("PRECIO_PROVEEDOR", null, "Proveedor ABC", "4.81", 2));
        hechos.add(hecho("PRECIO_PROVEEDOR", null, "Proveedor D", "-9.00", 400)); // fuera del año
        TableroService tablero = new TableroService(estrategias(), new KpiJpaAdapter(hechos), List::of, new BusEnMemoria(), new SugerenciasEnMemoria());

        List<ResultadoKpi> sinRango = tablero.kpis(null, null, null);
        Prueba.igual("sin rango: producto del último mes (Bandeja paisa; el Ajiaco de hace 45 días no cuenta)", "Bandeja paisa", sinRango.get(0).ganador());
        Prueba.igual("sucursal del último mes: Centro", "Sucursal Centro", sinRango.get(1).ganador());
        Prueba.igual("proveedor del último año: Proveedor C (el D es de hace 400 días)", "Proveedor C", sinRango.get(2).ganador());
        Prueba.igual("la demanda dice su periodo: los últimos 30 días", List.of(HOY.minusDays(29), HOY), List.of(sinRango.get(0).desde(), sinRango.get(0).hasta()));
        Prueba.igual("el proveedor, los últimos 365", List.of(HOY.minusDays(364), HOY), List.of(sinRango.get(2).desde(), sinRango.get(2).hasta()));

        List<ResultadoKpi> conRango = tablero.kpis(HOY.minusDays(60), HOY.minusDays(40), null);
        Prueba.igual("con rango, los tres usan el rango: Ajiaco", "Ajiaco", conRango.get(0).ganador());
        Prueba.igual("y el proveedor no tiene hechos en ese rango", null, conRango.get(2).ganador());
        Prueba.igual("el periodo aplicado es el rango", List.of(HOY.minusDays(60), HOY.minusDays(40)),
                List.of(conRango.get(2).desde(), conRango.get(2).hasta()));

        List<ResultadoKpi> norte = tablero.kpis(null, null, 2L);
        Prueba.igual("la sucursal filtra la demanda: en Norte gana Ajiaco", "Ajiaco", norte.get(0).ganador());
        Prueba.igualValor("con sus 8 unidades del último mes", "8", norte.get(0).valor());
        Prueba.igual("pero no al proveedor, que sigue siendo el de todas las compras", "Proveedor C", norte.get(2).ganador());

        Prueba.lanza("rango invertido (PD-10)", IllegalArgumentException.class, "La fecha inicial no puede ser posterior a la fecha final.",
                () -> tablero.kpis(HOY, HOY.minusDays(1), null));
        Prueba.lanza("una sola fecha", IllegalArgumentException.class, "Seleccione la fecha inicial y la fecha final.",
                () -> tablero.kpis(HOY, null, null));
        Prueba.igual("un periodo sin hechos no tiene ganadores", List.of(), tablero.kpis(HOY.minusDays(1000), HOY.minusDays(900), null)
                .stream().filter(r -> r.ganador() != null).toList());
    }

    private static void listener() {
        Prueba.grupo("NegocioEventsListener: los hechos de los 5 tópicos");
        List<HechoNegocio> hechos = new ArrayList<>();
        TableroService tablero = new TableroService(estrategias(), new KpiJpaAdapter(hechos), List::of, new BusEnMemoria(), new SugerenciasEnMemoria());
        Map<Long, String> platos = Map.of(1L, "Bandeja paisa", 2L, "Ajiaco");
        NegocioEventsListener listener = new NegocioEventsListener(tablero, platos::get, id -> id == 1L ? "Sucursal Centro" : "Sucursal " + id);
        Map<Long, Integer> lineas = new LinkedHashMap<>();
        lineas.put(1L, 2);
        lineas.put(2L, 1);
        listener.alRecibir(new PedidoRegistradoEvent("isabella.torres@konradgourmet.com", "PED-0251", 1L, lineas));
        Prueba.igual("el pedido deja un hecho por plato y uno de la sucursal", 3, hechos.size());
        Prueba.igual("con el nombre del plato", "Bandeja paisa", hechos.get(0).clave());
        Prueba.igualValor("y sus unidades", "2", hechos.get(0).valor());
        Prueba.igual("la sucursal suma las unidades del pedido", "Sucursal Centro", hechos.get(2).clave());
        Prueba.igualValor("3 platos", "3", hechos.get(2).valor());
        Prueba.igual("con la fecha del evento", HOY, hechos.get(0).fecha());

        listener.alRecibir(new CotizacionValidadaEvent("laura.gomez@konradgourmet.com", "COT-018", 1L, "Proveedor ABC", "OPCIONADA",
                new BigDecimal("545000"), new BigDecimal("4.81")));
        listener.alRecibir(new CotizacionValidadaEvent("laura.gomez@konradgourmet.com", "COT-019", 3L, "Proveedor C", "RECHAZADA",
                new BigDecimal("700000"), new BigDecimal("34.62")));
        listener.alRecibir(new CotizacionValidadaEvent("laura.gomez@konradgourmet.com", "COT-020", 4L, "Proveedor D", "SOSPECHOSA",
                new BigDecimal("200000"), new BigDecimal("-61.54")));
        List<HechoNegocio> precios = hechos.stream().filter(h -> h.tipo().equals("PRECIO_PROVEEDOR")).toList();
        Prueba.igual("de las cotizaciones validadas, solo la OPCIONADA cuenta (PD-03)", 1, precios.size());
        Prueba.igual("con el nombre del proveedor (DEC-89)", "Proveedor ABC", precios.get(0).clave());
        Prueba.igualValor("y su diferencia (PD-05)", "4.81", precios.get(0).valor());
        Prueba.igual("sin sucursal: las compras son centrales", null, precios.get(0).sucursalId());

        listener.alRecibir(new StockBajoEvent("sistema", 1L, 4L, "Pollo", new BigDecimal("9.8"), "kg", new BigDecimal("24.50")));
        listener.alRecibir(new SolicitudRegistradaEvent("carlos.mendoza@konradgourmet.com", "SOL-031", 1L, 2L, "carlos.mendoza@konradgourmet.com", List.of(1L, 2L)));
        listener.alRecibir(new OrdenGeneradaEvent("laura.gomez@konradgourmet.com", "OC-0012", "COT-018", List.of("SOL-031"), "Proveedor ABC",
                "ventas@proveedorabc.com", "s3://documentos-pdf/OC-0012.pdf", new BigDecimal("545000")));
        Prueba.igual("los otros tres tópicos también quedan en el almacén",
                List.of("STOCK_BAJO", "SOLICITUD_SUCURSAL", "ORDEN_PROVEEDOR"), hechos.subList(4, 7).stream().map(HechoNegocio::tipo).toList());
        listener.alRecibir(new AuditoriaEvent("sistema", "otra cosa"));
        Prueba.igual("un evento que no es de negocio no deja hechos", 7, hechos.size());
        Prueba.igual("el tablero los usa enseguida", "Bandeja paisa", tablero.kpis(null, null, null).get(0).ganador());
        List<HechoNegocio> sinNombres = new ArrayList<>();
        new NegocioEventsListener(new TableroService(estrategias(), new KpiJpaAdapter(sinNombres), List::of, new BusEnMemoria(),
                new SugerenciasEnMemoria())).alRecibir(new PedidoRegistradoEvent("mesero", "PED-1", 2L, Map.of(7L, 1)));
        Prueba.igual("sin copia de nombres, la versión en memoria dice «Plato N» y «Sucursal N»",
                List.of("Plato 7", "Sucursal 2"), sinNombres.stream().map(HechoNegocio::clave).toList());
    }

    /** Un tablero con hechos de la semana pasada y las tendencias que se le pasen. */
    private static TableroService tableroConSemana(TendenciasPort redes, BusEnMemoria bus, SugerenciasEnMemoria sugerencias) {
        List<HechoNegocio> hechos = new ArrayList<>();
        hechos.add(hecho("DEMANDA_PRODUCTO", 1L, "Bandeja paisa", "30", 2));
        hechos.add(hecho("DEMANDA_PRODUCTO", 2L, "Ajiaco", "25", 3));
        hechos.add(hecho("DEMANDA_PRODUCTO", 3L, "Pechuga a la plancha", "20", 7));
        hechos.add(hecho("DEMANDA_PRODUCTO", 1L, "Arepa con queso", "15", 4));
        hechos.add(hecho("DEMANDA_PRODUCTO", 1L, "Punta de anca", "99", 0));  // hoy: no es de los 7 días anteriores
        hechos.add(hecho("DEMANDA_PRODUCTO", 1L, "Jugo natural", "99", 8));   // hace 8 días: tampoco
        return new TableroService(estrategias(), new KpiJpaAdapter(hechos), redes, bus, sugerencias);
    }

    private static void promociones() {
        Prueba.grupo("Promociones sugeridas: el análisis semanal (HU-19, PD-04)");
        BusEnMemoria bus = new BusEnMemoria();
        SugerenciasEnMemoria repositorio = new SugerenciasEnMemoria();
        TableroService tablero = tableroConSemana(() -> List.of("Ajiaco", "Limonada natural"), bus, repositorio);
        new TendenciasJob(tablero).ejecutar();
        List<SugerenciaPromocion> hoy = tablero.sugerencias(null, null);
        Prueba.igual("sugiere lo de las redes y los 3 más pedidos de los 7 días anteriores, sin repetir",
                List.of("Ajiaco", "Limonada natural", "Bandeja paisa", "Pechuga a la plancha"),
                hoy.stream().map(SugerenciaPromocion::producto).toList());
        Prueba.igual("con su motivo", List.of("Tendencia en redes y entre los 3 más pedidos de la semana", "Tendencia en redes",
                "Entre los 3 más pedidos de la semana", "Entre los 3 más pedidos de la semana"), hoy.stream().map(SugerenciaPromocion::motivo).toList());
        Prueba.igual("vigencia de 7 días desde hoy", List.of(HOY, HOY.plusDays(6)), List.of(hoy.get(0).vigenciaDesde(), hoy.get(0).vigenciaHasta()));
        Prueba.igual("nacen SUGERIDAS", EstadoSugerencia.SUGERIDA, hoy.get(0).estado());
        Prueba.igual("publica tendencia.detectada una vez", 1L, bus.cuantos("tendencia.detectada"));
        TendenciaDetectadaEvent evento = (TendenciaDetectadaEvent) bus.publicados().stream().filter(e -> e instanceof TendenciaDetectadaEvent).findFirst().orElseThrow();
        String del = HOY.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        Prueba.igual("con una línea de texto por sugerencia (PD-05)", "Ajiaco · Tendencia en redes y entre los 3 más pedidos de la semana · del "
                + del + " al " + HOY.plusDays(6).format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")), evento.sugerencias().get(0));
        Prueba.verificar("y la fuente con la semana analizada", evento.fuente().startsWith("Redes sociales y pedidos del "));
        Prueba.verificar("queda en la auditoría", bus.publicados().stream().anyMatch(e -> e instanceof AuditoriaEvent a
                && a.accion().equals("Promociones sugeridas: Ajiaco, Limonada natural, Bandeja paisa, Pechuga a la plancha")));

        new TendenciasJob(tablero).ejecutar();
        Prueba.igual("otra ejecución el mismo día no repite las sugerencias", 4, tablero.sugerencias(null, null).size());
        Prueba.igual("ni vuelve a notificar", 1L, bus.cuantos("tendencia.detectada"));

        TableroService otraTendencia = tableroConSemana(() -> List.of("Mazorcada"), bus, repositorio);
        otraTendencia.detectarTendencias();
        Prueba.igual("una tendencia nueva el mismo día se guarda", 5, tablero.sugerencias(null, null).size());
        Prueba.igual("pero no manda otro correo: máximo una notificación por día", 1L, bus.cuantos("tendencia.detectada"));

        BusEnMemoria otroBus = new BusEnMemoria();
        SugerenciasEnMemoria vacio = new SugerenciasEnMemoria();
        TableroService caido = tableroConSemana(() -> {
            throw new IllegalStateException("La API de tendencias de las redes sociales respondió 503");
        }, otroBus, vacio);
        TendenciasJob job = new TendenciasJob(caido);
        Prueba.lanza("si las redes fallan, detectarTendencias() lanza", IllegalStateException.class, "503", caido::detectarTendencias);
        // el log del job (System.Logger, que en el JDK escribe en java.util.logging): se captura en vez de imprimirlo
        java.util.logging.Logger log = java.util.logging.Logger.getLogger(TendenciasJob.class.getName());
        List<java.util.logging.LogRecord> registros = new ArrayList<>();
        java.util.logging.Handler captura = new java.util.logging.Handler() {
            @Override public void publish(java.util.logging.LogRecord r) { registros.add(r); }
            @Override public void flush() { }
            @Override public void close() { }
        };
        log.addHandler(captura);
        log.setUseParentHandlers(false);
        boolean sigue = true;
        try {
            job.ejecutar();
        } catch (RuntimeException e) {
            sigue = false;
        } finally {
            log.removeHandler(captura);
            log.setUseParentHandlers(true);
        }
        Prueba.verificar("TendenciasJob no deja salir el error: el resto del sistema sigue", sigue && vacio.historico(null, null).isEmpty()
                && otroBus.publicados().isEmpty());
        Prueba.verificar("y lo deja en el log como error", registros.size() == 1 && registros.get(0).getLevel() == java.util.logging.Level.SEVERE
                && registros.get(0).getMessage().equals("Falló el análisis de tendencias de promociones: La API de tendencias de las redes sociales respondió 503"));

        SugerenciasEnMemoria viejas = new SugerenciasEnMemoria();
        viejas.guardar(new SugerenciaPromocion("Ajiaco", "Tendencia en redes", HOY.minusDays(14), HOY.minusDays(8)));
        viejas.guardar(new SugerenciaPromocion("Bandeja paisa", "Entre los 3 más pedidos de la semana", HOY.minusDays(7), HOY.minusDays(1)));
        TableroService historico = new TableroService(estrategias(), new KpiJpaAdapter(new ArrayList<>()), List::of, new BusEnMemoria(), viejas);
        Prueba.igual("el histórico filtra por vigencia: del rango, las vigentes en algún día", List.of("Bandeja paisa"),
                historico.sugerencias(HOY.minusDays(3), HOY).stream().map(SugerenciaPromocion::producto).toList());
        Prueba.igual("sin rango, todo el histórico, lo más reciente primero", List.of("Bandeja paisa", "Ajiaco"),
                historico.sugerencias(null, null).stream().map(SugerenciaPromocion::producto).toList());
        Prueba.lanza("rango invertido (PD-10)", IllegalArgumentException.class, "La fecha inicial no puede ser posterior a la fecha final.",
                () -> historico.sugerencias(HOY, HOY.minusDays(1)));
        Prueba.igual("un periodo sin sugerencias da una lista vacía", 0, historico.sugerencias(HOY.minusDays(100), HOY.minusDays(90)).size());
    }

    private static void aceptarYDescartar() {
        Prueba.grupo("Promociones sugeridas: aceptar y descartar (HU-19)");
        BusEnMemoria bus = new BusEnMemoria();
        SugerenciasEnMemoria repositorio = new SugerenciasEnMemoria();
        TableroService tablero = tableroConSemana(() -> List.of("Ajiaco"), bus, repositorio);
        tablero.detectarTendencias();
        PromocionRestController rest = new PromocionRestController(tablero);
        List<SugerenciaPromocion> lista = rest.sugerencias(null, null);
        Long ajiaco = lista.get(0).id();
        Long bandeja = lista.get(1).id();
        rest.aceptar(ajiaco);
        Prueba.igual("el jefe acepta el Ajiaco", EstadoSugerencia.ACEPTADA, repositorio.porId(ajiaco).estado());
        rest.descartar(bandeja);
        Prueba.igual("y descarta la Bandeja paisa", EstadoSugerencia.DESCARTADA, repositorio.porId(bandeja).estado());
        Prueba.lanza("una aceptada no se vuelve a aceptar (PD-10)", IllegalStateException.class, "Esta sugerencia ya fue aceptada o descartada.",
                () -> rest.aceptar(ajiaco));
        Prueba.lanza("ni se descarta", IllegalStateException.class, "Esta sugerencia ya fue aceptada o descartada.", () -> rest.descartar(ajiaco));
        Prueba.igual("y sigue ACEPTADA", EstadoSugerencia.ACEPTADA, repositorio.porId(ajiaco).estado());
        Prueba.lanza("una sugerencia que no existe", IllegalArgumentException.class, "No existe la sugerencia 999", () -> rest.aceptar(999L));
        Prueba.verificar("cada decisión queda en la auditoría", bus.publicados().stream().filter(e -> e instanceof AuditoriaEvent a
                && (a.accion().equals("Promoción sugerida Ajiaco aceptada") || a.accion().equals("Promoción sugerida Bandeja paisa descartada"))).count() == 2);
        Prueba.lanza("una sugerencia sin producto no existe", IllegalArgumentException.class, "producto",
                () -> new SugerenciaPromocion(" ", "Tendencia en redes", HOY, HOY.plusDays(6)));
        Prueba.lanza("ni una con la vigencia al revés", IllegalArgumentException.class, "vigencia",
                () -> new SugerenciaPromocion("Ajiaco", "Tendencia en redes", HOY, HOY.minusDays(1)));
    }

    private static void correoDePromociones() {
        Prueba.grupo("ms-notificaciones: el correo de promociones (tendencia.detectada, PD-05)");
        Ensamble s = new Ensamble(11);
        s.destinatariosRest.registrar(1L, "carlos.mendoza@konradgourmet.com");
        s.destinatariosRest.registrar(2L, "sofia.ramirez@konradgourmet.com");
        int antes = s.correos.enviados().size();
        s.bus.publicar(new TendenciaDetectadaEvent("sistema", List.of("Ajiaco · Tendencia en redes · del 18/11/2026 al 24/11/2026",
                "Bandeja paisa · Entre los 3 más pedidos de la semana · del 18/11/2026 al 24/11/2026"), "Redes sociales y pedidos"));
        List<Mensaje> nuevos = s.correos.enviados().subList(antes, s.correos.enviados().size());
        Prueba.igual("un correo a cada jefe de la libreta, de todas las sucursales", List.of("carlos.mendoza@konradgourmet.com", "sofia.ramirez@konradgourmet.com"),
                nuevos.stream().map(Mensaje::para).toList());
        Prueba.igual("con el asunto «Promociones sugeridas»", "Promociones sugeridas", nuevos.get(0).asunto());
        Prueba.verificar("y una línea por sugerencia", nuevos.get(0).cuerpo().contains("• Ajiaco · Tendencia en redes · del 18/11/2026 al 24/11/2026")
                && nuevos.get(0).cuerpo().contains("• Bandeja paisa · Entre los 3 más pedidos"));
        Prueba.igual("certificado, como los demás correos", List.of("X-Certificado", "X-Sello-Tiempo"), new ArrayList<>(nuevos.get(0).cabeceras().keySet()));
        Prueba.igual("cada envío queda en la traza", 2L, s.envios.envios().stream().filter(e -> e.contains("tendencia.detectada")).count());
    }

    private static void redesSociales() {
        Prueba.grupo("RedesSocialesAdapter (HTTPS → API de tendencias, contra un servidor simulado)");
        AtomicInteger llamadas = new AtomicInteger();
        AtomicReference<String> autorizacion = new AtomicReference<>();
        AtomicReference<String> consulta = new AtomicReference<>();
        AtomicInteger estado = new AtomicInteger(200);
        HttpServer servidor;
        try {
            servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        } catch (IOException e) {
            Prueba.verificar("no se pudo abrir un puerto local para las redes simuladas: " + e.getMessage(), false);
            return;
        }
        servidor.createContext("/tendencias", intercambio -> {
            llamadas.incrementAndGet();
            autorizacion.set(intercambio.getRequestHeaders().getFirst("Authorization"));
            consulta.set(intercambio.getRequestURI().getQuery());
            byte[] cuerpo = ("{\"periodo\":\"semana\",\"tendencias\":[{\"producto\":\"Ajiaco\",\"menciones\":1840},"
                    + "{\"producto\":\"Limonada natural\",\"menciones\":960},{\"producto\":\"Pechuga a la plancha\",\"menciones\":512},"
                    + "{\"producto\":\"Ajiaco\",\"menciones\":3}]}").getBytes(StandardCharsets.UTF_8);
            intercambio.getResponseHeaders().add("Content-Type", "application/json");
            intercambio.sendResponseHeaders(estado.get(), cuerpo.length);
            try (OutputStream o = intercambio.getResponseBody()) {
                o.write(cuerpo);
            }
        });
        servidor.start();
        String endpoint = "http://127.0.0.1:" + servidor.getAddress().getPort() + "/tendencias";
        try {
            RedesSocialesAdapter redes = new RedesSocialesAdapter(endpoint, "token-de-prueba");
            Prueba.igual("lee los productos en tendencia, en orden y sin repetir", List.of("Ajiaco", "Limonada natural", "Pechuga a la plancha"),
                    redes.productosEnTendencia());
            Prueba.igual("con su token de acceso", "Bearer token-de-prueba", autorizacion.get());
            Prueba.igual("y la semana en Colombia", "pais=CO&categoria=gastronomia&periodo=semana", consulta.get());
            estado.set(503);
            Prueba.lanza("si la API responde un error, lo dice", IllegalStateException.class, "respondió 503", redes::productosEnTendencia);
        } finally {
            servidor.stop(0);
        }
        Prueba.lanza("si la API no responde, también", IllegalStateException.class, "No se pudo consultar las tendencias",
                () -> new RedesSocialesAdapter(endpoint, null).productosEnTendencia());
        Prueba.igual("el servidor recibió 2 llamadas", 2, llamadas.get());
    }

    /** El repositorio de sugerencias en memoria (en la plataforma, SugerenciaJpaAdapter sobre PostgreSQL). */
    static final class SugerenciasEnMemoria implements SugerenciaRepositoryPort {

        private final Map<Long, SugerenciaPromocion> porId = new LinkedHashMap<>();
        private long siguiente = 1;

        @Override
        public void guardar(SugerenciaPromocion s) {
            SugerenciaPromocion guardada = s.id() == null ? new SugerenciaPromocion(s, siguiente++) : s;
            porId.put(guardada.id(), guardada);
        }

        @Override
        public SugerenciaPromocion porId(Long id) {
            return porId.get(id);
        }

        @Override
        public List<SugerenciaPromocion> historico(LocalDate desde, LocalDate hasta) {
            return porId.values().stream()
                    .filter(s -> desde == null || !s.vigenciaHasta().isBefore(desde))
                    .filter(s -> hasta == null || !s.vigenciaDesde().isAfter(hasta))
                    .toList();
        }

        @Override
        public boolean notificadaEl(LocalDate fecha) {
            return porId.values().stream().anyMatch(s -> s.vigenciaDesde().equals(fecha));
        }

    }

}
