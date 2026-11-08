package co.edu.konradlorenz.demo;

import co.edu.konradlorenz.administracion.dominio.ParametrosSistema;
import co.edu.konradlorenz.analitica.dominio.HechoNegocio;
import co.edu.konradlorenz.analitica.dominio.ResultadoKpi;
import co.edu.konradlorenz.compartido.eventos.EventoDominio;
import co.edu.konradlorenz.cotizaciones.dominio.Cotizacion;
import co.edu.konradlorenz.cotizaciones.dominio.ItemCotizacion;
import co.edu.konradlorenz.cotizaciones.dominio.Proveedor;
import co.edu.konradlorenz.identidad.dominio.DirectorCompras;
import co.edu.konradlorenz.identidad.dominio.ElementoMenu;
import co.edu.konradlorenz.identidad.dominio.Sucursal;
import co.edu.konradlorenz.inventario.dominio.InsumoInventario;
import co.edu.konradlorenz.inventario.dominio.Inventario;
import co.edu.konradlorenz.menu.eventos.MenuActualizadoEvent;
import co.edu.konradlorenz.notificaciones.dominio.Mensaje;
import co.edu.konradlorenz.ordenes.dominio.OrdenCompra;
import co.edu.konradlorenz.pedidos.dominio.Mesa;
import co.edu.konradlorenz.pedidos.dominio.Pedido;
import co.edu.konradlorenz.solicitudes.dominio.ItemSolicitud;
import co.edu.konradlorenz.solicitudes.dominio.SolicitudAlimentos;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Ejecuta los patrones de la arquitectura sin base de datos ni Kafka.
 *
 * Cuenta un día en la sucursal Centro con adaptadores en memoria: el director de compras entra, un
 * mesero registra un pedido, el inventario se queda corto, el jefe de cocina pide alimentos, el auxiliar
 * cotiza con tres proveedores y el director genera la orden de compra. Cada sección dice qué patrón
 * muestra; las secciones numeradas del 1 al 7 son las 7 salidas del reporte (§13, p. 26).
 */
public class DemoArquitectura {

    private static final DecimalFormatSymbols COLOMBIA = simbolos();

    private final Ensamble sistema = new Ensamble(11);
    private int eventosVistos;
    private int correosVistos;
    private int alarmasVistas;

    public static void main(String[] args) {
        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out), true, StandardCharsets.UTF_8));
        new DemoArquitectura().ejecutar();
    }

    private void ejecutar() {
        System.out.println("Konrad Gourmet - demo de arquitectura");
        System.out.println("Sin base de datos, sin Kafka y sin frameworks: adaptadores en memoria.");

        Sucursal centro = new Sucursal(1L, "Sucursal Centro");
        cargaInicial(centro);
        menuDelDirectorDeCompras(centro);
        pedidoDelMesero(centro);
        cierreDelDia();
        precioConProxy();
        SolicitudAlimentos solicitud = solicitudDelJefeDeCocina(centro);
        cotizaciones(solicitud);
        ordenDeCompra();
        singleton();
        tableroDeKpi();
        resumen();
    }

    // ------------------------------------------------------------------ datos de partida

    private void cargaInicial(Sucursal centro) {
        titulo("0. Carga inicial");
        // lo que el administrador y el jefe de cocina dejaron cargado antes de que empiece el día
        sistema.destinatariosRest.registrar(1L, "jefe.centro@konradgourmet.com");
        sistema.repositorioPedidos.agregarMesa(new Mesa(5, 1L, 4, true));
        sistema.ministerio.definirPrecio("1", 20000); // res, por kg
        sistema.ministerio.definirPrecio("2", 8000);  // arroz, por kg
        sistema.repositorioInventario.guardar(new Inventario(1L, List.of(
                new InsumoInventario(1L, "Res", new BigDecimal("12.5"), new BigDecimal("50"), "kg"),
                new InsumoInventario(2L, "Arroz", new BigDecimal("40"), new BigDecimal("60"), "kg"),
                new InsumoInventario(3L, "Papa", new BigDecimal("30"), new BigDecimal("40"), "kg"),
                new InsumoInventario(4L, "Pollo", new BigDecimal("25"), new BigDecimal("40"), "kg"))));
        // ms-menu publica menu.actualizado por cada plato: ms-pedidos arma su catálogo y ms-inventario guarda la receta
        sistema.bus.publicar(new MenuActualizadoEvent("jefe.cocina", 1L, 1L, "Bandeja paisa", new BigDecimal("32000"), true,
                receta(1L, "0.5", 2L, "0.1")));
        sistema.bus.publicar(new MenuActualizadoEvent("jefe.cocina", 2L, 1L, "Ajiaco", new BigDecimal("18000"), true,
                receta(4L, "0.2", 3L, "0.3")));
        sistema.proveedores.registrar(new Proveedor(1L, "Proveedor ABC", "900123456-1", "ventas@proveedorabc.com", "6011234567", "Cra 7 # 10-20", List.of("Carnes", "Granos")));
        sistema.proveedores.registrar(new Proveedor(2L, "Proveedor B", "900234567-2", "ventas@proveedorb.com", "6012345678", "Cll 26 # 30-15", List.of("Carnes", "Granos")));
        sistema.proveedores.registrar(new Proveedor(3L, "Proveedor C", "900345678-3", "ventas@proveedorc.com", "6013456789", "Av 68 # 22-40", List.of("Carnes", "Granos")));
        sistema.proveedores.registrar(new Proveedor(4L, "Proveedor D", "900456789-4", "ventas@proveedord.com", "6014567890", "Cll 80 # 11-30", List.of("Carnes", "Granos")));
        System.out.println("Sucursal Centro: 1 mesa, 4 insumos, 2 platos con receta, 4 proveedores y 1 jefe en la libreta de correos.");
        mostrarEventos();
    }

    // ------------------------------------------------------------------ 1. Composite + Factory Method

    private void menuDelDirectorDeCompras(Sucursal centro) {
        titulo("1. Composite + Factory Method: el menú del director de compras");
        DirectorCompras director = new DirectorCompras(5L, "Laura Gómez", "laura.gomez@konradgourmet.com", centro);
        // Factory Method: cada rol decide qué menú construir; Composite: el menú es un árbol de grupos y opciones
        ElementoMenu menu = director.construirMenu();
        System.out.println("Menú " + director.rol() + " (" + director.nombre() + ")");
        imprimirMenu(menu, "");
    }

    private void imprimirMenu(ElementoMenu elemento, String prefijo) {
        List<ElementoMenu> hijos = elemento.hijos();
        for (int i = 0; i < hijos.size(); i++) {
            boolean ultimo = i == hijos.size() - 1;
            ElementoMenu h = hijos.get(i);
            System.out.println(prefijo + (ultimo ? "└─ " : "├─ ") + h.titulo() + (h.esHoja() ? "   " + h.ruta() : ""));
            imprimirMenu(h, prefijo + (ultimo ? "   " : "│  "));
        }
    }

    // ------------------------------------------------------------------ 2. Hexagonal + Observer

    private void pedidoDelMesero(Sucursal centro) {
        titulo("2. Hexagonal + Observer: el mesero registra un pedido (V03 y V04)");
        Pedido pedido = new Pedido("PED-0251", new Mesa(5, 1L, 4, true));
        pedido.agregarPlato(1L, 2, "Sin arepa", new BigDecimal("32000")); // Bandeja paisa
        pedido.agregarPlato(2L, 1, null, new BigDecimal("18000"));        // Ajiaco
        Pedido registrado = sistema.pedidos.registrar(pedido);
        System.out.println("Pedido " + registrado.numero() + " registrado · Mesa " + registrado.mesa().numero() + " · "
                + registrado.detalles().stream().mapToInt(d -> d.cantidad()).sum() + " platos · total " + pesos(registrado.total())
                + " · " + registrado.estado());
        mostrarEventos();
        System.out.println("(ms-inventario escuchó pedido.registrado y acumuló el consumo de insumos de la sucursal)");
    }

    // ------------------------------------------------------------------ 3. Factory Method + Decorator

    private void cierreDelDia() {
        titulo("3. Factory Method + Decorator: el cierre de las 23:59 deja el stock bajo");
        sistema.consumoDiario.ejecutar();
        mostrarEventos();
        for (InsumoInventario i : sistema.inventario.alertas(1L)) {
            System.out.println("Alerta activa (V05): " + i.nombre() + " " + num(i.stockActual()) + " " + i.unidad()
                    + " · " + num(i.porcentajeExistencia()) + " % del stock de referencia");
        }
        for (String alarma : desde(sistema.alarmas.alarmas(), alarmasVistas)) {
            System.out.println("Alarma en la app → " + alarma);
        }
        alarmasVistas = sistema.alarmas.alarmas().size();
        mostrarCorreos(true);
    }

    // ------------------------------------------------------------------ 4. Proxy + Adapter

    private void precioConProxy() {
        titulo("4. Proxy + Adapter: el precio de la res (V13)");
        BigDecimal primero = sistema.precios.precioSugerido(1L);
        System.out.println("1.ª consulta: res " + pesos(primero) + "/kg · llamadas al servicio web del Ministerio: " + sistema.ministerio.llamadas());
        BigDecimal segundo = sistema.precios.precioSugerido(1L);
        System.out.println("2.ª consulta: res " + pesos(segundo) + "/kg · llamadas al servicio web del Ministerio: " + sistema.ministerio.llamadas()
                + " (la respondió la caché del Proxy)");
    }

    // ------------------------------------------------------------------ solicitud (V08 y V09)

    private SolicitudAlimentos solicitudDelJefeDeCocina(Sucursal centro) {
        titulo("Solicitud de alimentos (V08 y V09)");
        SolicitudAlimentos s = new SolicitudAlimentos("SOL-031", 1L, 7L, "carlos.mendoza@konradgourmet.com");
        s.agregarItem(new ItemSolicitud(1L, new BigDecimal("20"), "Marca X", "kg"));
        s.agregarItem(new ItemSolicitud(2L, new BigDecimal("15"), "Marca X", "kg"));
        SolicitudAlimentos registrada = sistema.solicitudes.registrar(s);
        System.out.println("Solicitud " + registrada.numero() + " registrada: " + registrada.estado());
        mostrarEventos();
        System.out.println("Alertas de stock que siguen activas: " + sistema.inventario.alertas(1L).size()
                + " (solicitud.registrada atendió la alerta de la res)");
        mostrarCorreos(false);
        return registrada;
    }

    // ------------------------------------------------------------------ 5. Facade + Chain of Responsibility

    private void cotizaciones(SolicitudAlimentos solicitud) {
        titulo("5. Facade + Chain of Responsibility: COT-018, COT-019 y COT-020 (V13, V18 y V19)");
        List<Proveedor> desplegable = sistema.proveedores.consultar(true);
        System.out.println("Desplegable de proveedores de V13: " + desplegable.stream().map(Proveedor::razonSocial).toList());
        cotizar("COT-018", desplegable.get(0), solicitud, "21250", "8000");
        cotizar("COT-019", desplegable.get(2), solicitud, "29000", "8000");
        cotizar("COT-020", desplegable.get(3), solicitud, "4000", "8000");
        System.out.println("Solicitud " + solicitud.numero() + " ahora: "
                + sistema.solicitudes.detalle("SOL-031").estado() + " · valor sugerido " + pesos(sistema.solicitudes.detalle("SOL-031").valorSugeridoTotal()));
        System.out.println("Cotizaciones opcionadas (V20): " + sistema.cotizaciones.opcionadas().stream().map(Cotizacion::numero).toList());
    }

    private void cotizar(String numero, Proveedor proveedor, SolicitudAlimentos solicitud, String precioRes, String precioArroz) {
        // V13: una o varias solicitudes (DEC-05) y los productos que se cotizan
        List<ItemCotizacion> items = List.of(
                new ItemCotizacion(1L, new BigDecimal("20"), "kg"),
                new ItemCotizacion(2L, new BigDecimal("15"), "kg"));
        Cotizacion enviada = sistema.cotizaciones.generar(
                new Cotizacion(numero, List.of(solicitud.numero()), proveedor, LocalDate.of(2026, 9, 30), items));
        System.out.println();
        System.out.println(enviada.numero() + " · " + proveedor.razonSocial() + " · " + enviada.estado() + " · PDF " + enviada.pdfUrl());
        mostrarEventos();
        mostrarCorreos(false);
        // V18: el proveedor responde y el auxiliar digita sus precios por kilo
        Map<Long, BigDecimal> precios = new LinkedHashMap<>();
        precios.put(1L, new BigDecimal(precioRes));
        precios.put(2L, new BigDecimal(precioArroz));
        sistema.cotizaciones.registrarValores(numero, precios);
        // V19: «Validar cotización» recorre la cadena Rechazada → Sospechosa → Opcionada
        Cotizacion validada = sistema.cotizaciones.validar(numero);
        System.out.println("   Valor estándar total " + pesos(validada.valorEstandarTotal())
                + " · valor cotizado total " + pesos(validada.valorCotizadoTotal())
                + " · diferencia " + porcentaje(validada.diferenciaPorcentual()) + " → " + validada.estado());
        mostrarEventos();
    }

    // ------------------------------------------------------------------ orden de compra (V21 a V23)

    private void ordenDeCompra() {
        titulo("Orden de compra (V21 a V23)");
        OrdenCompra orden = sistema.ordenes.generar("COT-018");
        System.out.println("Orden " + orden.numero() + " · " + orden.proveedor() + " · cotización " + orden.cotizacionNumero()
                + " · total " + pesos(orden.total()) + " · " + orden.estado());
        mostrarEventos();
        mostrarCorreos(false);
        System.out.println("COT-018 ahora: " + sistema.cotizaciones.detalle("COT-018").estado()
                + " · SOL-031 ahora: " + sistema.solicitudes.detalle("SOL-031").estado());
        intentar("Segunda orden sobre COT-018", () -> sistema.ordenes.generar("COT-018"));
        intentar("Orden sobre COT-019 (RECHAZADA)", () -> sistema.ordenes.generar("COT-019"));
    }

    private void intentar(String que, Runnable accion) {
        try {
            accion.run();
            System.out.println(que + ": se aceptó (no debería)");
        } catch (RuntimeException e) {
            System.out.println(que + " → rechazada: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------ 6. Singleton

    private void singleton() {
        titulo("6. Singleton: los parámetros del tema (ms-administracion)");
        sistema.admin.parametrizar("tema.nombre", "Konrad Gourmet");
        sistema.admin.parametrizar("tema.color-primario", "#F26B21");
        ParametrosSistema primera = ParametrosSistema.getInstancia();
        ParametrosSistema segunda = ParametrosSistema.getInstancia();
        System.out.println("tema.color-primario = " + primera.valor("tema.color-primario")
                + " · misma instancia en dos llamadas: " + (primera == segunda));
        System.out.println("GET /api/admin/parametros/tema → " + sistema.admin.tema());
    }

    // ------------------------------------------------------------------ 7. Strategy

    private void tableroDeKpi() {
        titulo("7. Strategy: el tablero BAM (KPI)");
        // el histórico del mes, que en el sistema real saldría de los eventos de negocio
        hecho("DEMANDA_PRODUCTO", "Bandeja paisa", "22");
        hecho("DEMANDA_PRODUCTO", "Bandeja paisa", "18");
        hecho("DEMANDA_PRODUCTO", "Ajiaco", "25");
        hecho("DEMANDA_PRODUCTO", "Pechuga a la plancha", "15");
        hecho("DEMANDA_SUCURSAL", "Sucursal Centro", "120");
        hecho("DEMANDA_SUCURSAL", "Sucursal Norte", "85");
        hecho("PRECIO_PROVEEDOR", "Proveedor ABC", "6.20");
        hecho("PRECIO_PROVEEDOR", "Proveedor ABC", "3.10");
        hecho("PRECIO_PROVEEDOR", "Proveedor B", "-2.40");
        hecho("PRECIO_PROVEEDOR", "Proveedor B", "-1.80");
        hecho("PRECIO_PROVEEDOR", "Proveedor B", "-3.00");
        hecho("PRECIO_PROVEEDOR", "Proveedor C", "9.50");
        for (ResultadoKpi r : sistema.tablero.kpis(null, null, null)) {
            boolean precio = r.kpi().startsWith("Proveedor");
            System.out.println(r.kpi() + ": " + r.ganador() + " (" + (precio ? porcentaje(r.valor()) + " contra el estándar" : num(r.valor())) + ")");
        }
    }

    private void hecho(String tipo, String clave, String valor) {
        sistema.hechos.add(new HechoNegocio(tipo, 1L, clave, new BigDecimal(valor), LocalDate.now()));
    }

    // ------------------------------------------------------------------ cierre

    private void resumen() {
        titulo("Resumen");
        Map<String, Long> porTopico = new LinkedHashMap<>();
        for (EventoDominio e : sistema.bus.publicados()) {
            porTopico.merge(e.tipo(), 1L, Long::sum);
        }
        System.out.println("Eventos publicados por tópico: " + porTopico);
        System.out.println("Correos enviados: " + sistema.correos.enviados().size() + " · alarmas: " + sistema.alarmas.alarmas().size()
                + " · registros de auditoría: " + sistema.bitacora.registros().size());
    }

    // ------------------------------------------------------------------ ayudas de impresión

    private void mostrarEventos() {
        List<EventoDominio> todos = sistema.bus.publicados();
        for (EventoDominio e : todos.subList(eventosVistos, todos.size())) {
            if (!e.tipo().equals("auditoria.evento")) { // la auditoría se cuenta al final
                System.out.println("   ↳ evento " + e.tipo());
            }
        }
        eventosVistos = todos.size();
    }

    private void mostrarCorreos(boolean conEncabezados) {
        List<Mensaje> todos = sistema.correos.enviados();
        for (Mensaje m : todos.subList(correosVistos, todos.size())) {
            System.out.println("   ✉ Correo a " + m.para() + " · " + m.asunto());
            if (conEncabezados) {
                m.cabeceras().forEach((k, v) -> System.out.println("       " + k + ": " + v));
            }
        }
        correosVistos = todos.size();
    }

    private static <T> List<T> desde(List<T> lista, int indice) {
        return lista.subList(indice, lista.size());
    }

    private static void titulo(String texto) {
        System.out.println();
        System.out.println("== " + texto + " ==");
    }

    private static Map<Long, BigDecimal> receta(Long insumo1, String cantidad1, Long insumo2, String cantidad2) {
        Map<Long, BigDecimal> receta = new LinkedHashMap<>();
        receta.put(insumo1, new BigDecimal(cantidad1));
        receta.put(insumo2, new BigDecimal(cantidad2));
        return receta;
    }

    private static DecimalFormatSymbols simbolos() {
        DecimalFormatSymbols s = new DecimalFormatSymbols(Locale.ROOT);
        s.setGroupingSeparator('.');
        s.setDecimalSeparator(',');
        return s;
    }

    /** $520.000 */
    static String pesos(BigDecimal v) {
        return "$" + new DecimalFormat("#,##0", COLOMBIA).format(v);
    }

    /** +4,81 % o −61,54 % */
    static String porcentaje(BigDecimal v) {
        return (v.signum() < 0 ? "−" : "+") + new DecimalFormat("0.00", COLOMBIA).format(v.abs()) + " %";
    }

    /** 11,5 · 23 · 40 */
    static String num(BigDecimal v) {
        return new DecimalFormat("#,##0.##", COLOMBIA).format(v);
    }

}
