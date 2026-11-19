package co.edu.konradlorenz.demo;

import co.edu.konradlorenz.cotizaciones.dominio.*;
import co.edu.konradlorenz.identidad.dominio.DirectorCompras;
import co.edu.konradlorenz.identidad.dominio.ElementoMenu;
import co.edu.konradlorenz.identidad.dominio.Sucursal;
import co.edu.konradlorenz.inventario.dominio.*;
import co.edu.konradlorenz.menu.dominio.Ingrediente;
import co.edu.konradlorenz.menu.dominio.Plato;
import co.edu.konradlorenz.menu.dominio.Producto;
import co.edu.konradlorenz.menu.dominio.UnidadMedida;
import co.edu.konradlorenz.ordenes.dominio.*;
import co.edu.konradlorenz.pedidos.dominio.*;
import co.edu.konradlorenz.solicitudes.dominio.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;

/**
 * Servidor local de la interfaz web (V01 a V23): usa las mismas clases del {@link Ensamble} que la demo
 * de terminal, con los datos en memoria. Las pantallas del mesero, el jefe de cocina, el auxiliar y el
 * director de compras llaman a estos endpoints; al detener el servidor se pierden los datos.
 */
public class ServidorWeb {
    private static final Long CENTRO = 1L, MESERO = 3L, JEFE = 7L;
    private static final String CORREO_JEFE = "carlos.mendoza@konradgourmet.com";

    private final Ensamble sistema = new Ensamble(11);
    private int pedidos = 250, solicitudes = 30, cotizaciones = 17;
    private final Path web = Path.of(".").toAbsolutePath().normalize();

    private static final Set<String> PUBLICOS = Set.of("/index.html", "/styles.css", "/app.html", "/app.css", "/app.js");
    private static final int MAX_CUERPO = 2 * 1024 * 1024;
    private static final Map<Long, String> CATEGORIA_INSUMO = Map.of(1L, "Proteína", 2L, "Cereal", 3L, "Tubérculo", 4L, "Proteína");

    public static void main(String[] args) throws IOException {
        ServidorWeb app = new ServidorWeb();
        app.sembrar();
        int puerto = args.length == 0 ? 8000 : Integer.parseInt(args[0]);
        HttpServer servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", puerto), 0);
        servidor.createContext("/", app::atender);
        servidor.start();
        System.out.println("Interfaz Konrad Gourmet: http://localhost:" + puerto);
    }

    /** La carga inicial de la demo de terminal, más las mesas y los platos que muestran V02 y V03. */
    private void sembrar() {
        sistema.destinatariosRest.registrar(CENTRO, "jefe.centro@konradgourmet.com");
        for (int mesa : new int[]{2, 3, 5, 6, 8}) sistema.repositorioPedidos.agregarMesa(new Mesa(mesa, CENTRO, 4, true));
        sistema.ministerio.definirPrecio("1", 20000); // res, por kg
        sistema.ministerio.definirPrecio("2", 8000);  // arroz, por kg
        sistema.ministerio.definirPrecio("3", 3500);  // papa, por kg
        sistema.ministerio.definirPrecio("4", 14000); // pollo, por kg
        sistema.repositorioInventario.guardar(new Inventario(CENTRO, List.of(
            new InsumoInventario(1L, "Res", new BigDecimal("12.5"), new BigDecimal("50"), "kg"),
            new InsumoInventario(2L, "Arroz", new BigDecimal("40"), new BigDecimal("60"), "kg"),
            new InsumoInventario(3L, "Papa", new BigDecimal("30"), new BigDecimal("40"), "kg"),
            new InsumoInventario(4L, "Pollo", new BigDecimal("25"), new BigDecimal("40"), "kg"))));
        // los dos platos de la demo de terminal, con la misma receta, y el resto de la carta de V03
        plato("Bandeja paisa", "32000", "Fuertes", "Frijoles, arroz, chicharrón, carne, huevo, plátano y arepa.", receta(1L, "0.5", 2L, "0.1"));
        plato("Ajiaco", "18000", "Sopas", "Pollo, papas criollas y guascas.", receta(4L, "0.2", 3L, "0.3"));
        plato("Punta de anca", "38000", "Fuertes", "Carne a la parrilla, papas, ensalada de la casa.", receta(1L, "0.35", 3L, "0.2"));
        plato("Pechuga a la plancha", "26000", "Fuertes", "Pechuga de pollo, arroz, ensalada y patacón.", receta(4L, "0.3", 2L, "0.1"));
        plato("Ensalada césar", "20000", "Entradas", "Lechuga, pollo, parmesano, crutones y salsa césar.", receta(4L, "0.15", null, null));
        plato("Arepa con queso", "7000", "Entradas", "Arepa de maíz con queso derretido.", Map.of());
        plato("Limonada natural", "6000", "Bebidas", "Limonada de limón natural.", Map.of());
        plato("Jugo natural", "6500", "Bebidas", "Jugo de fruta natural en agua o en leche.", Map.of());
        sistema.proveedores.registrar(new Proveedor(1L, "Proveedor ABC", "900123456-1", "ventas@proveedorabc.com", "6011234567", "Cra 7 # 10-20", List.of("Carnes", "Granos")));
        sistema.proveedores.registrar(new Proveedor(2L, "Proveedor B", "900234567-2", "ventas@proveedorb.com", "6012345678", "Cll 26 # 30-15", List.of("Carnes", "Granos")));
        sistema.proveedores.registrar(new Proveedor(3L, "Proveedor C", "900345678-3", "ventas@proveedorc.com", "6013456789", "Av 68 # 22-40", List.of("Carnes", "Granos")));
        sistema.proveedores.registrar(new Proveedor(4L, "Proveedor D", "900456789-4", "ventas@proveedord.com", "6014567890", "Cll 80 # 11-30", List.of("Carnes", "Granos")));
    }

    /** ms-menu crea el plato y publica menu.actualizado: ms-pedidos arma su catálogo y ms-inventario guarda la receta. */
    private long plato(String nombre, String precio, String categoria, String descripcion, Map<Long, BigDecimal> receta) {
        Plato p = new Plato(CENTRO, JEFE, nombre, categoria, descripcion, new BigDecimal(precio));
        receta.forEach((insumo, kg) -> p.agregarIngrediente(new Ingrediente(new Producto(insumo, producto(insumo), null), kg, UnidadMedida.KG)));
        return sistema.platos.crear(p).id();
    }

    private static Map<Long, BigDecimal> receta(Long a, String ca, Long b, String cb) {
        Map<Long, BigDecimal> r = new LinkedHashMap<>();
        r.put(a, new BigDecimal(ca));
        if (b != null) r.put(b, new BigDecimal(cb));
        return r;
    }

    // ------------------------------------------------------------------ HTTP

    private synchronized void atender(HttpExchange x) throws IOException {
        try {
            String ruta = x.getRequestURI().getPath();
            if (!ruta.startsWith("/api/")) { archivo(x, ruta); return; }
            boolean get = x.getRequestMethod().equals("GET");
            if (get && ruta.equals("/api/state")) { responder(x, 200, estado(null)); return; }
            if (get && ruta.equals("/api/menu/director")) { responder(x, 200, menu(new DirectorCompras(5L, "Laura Gómez", "laura.gomez@konradgourmet.com", new Sucursal(CENTRO, "Sucursal Centro")).construirMenu())); return; }
            if (get && ruta.equals("/api/solicitudes")) { responder(x, 200, filtrarSolicitudes(consulta(x.getRequestURI().getRawQuery()))); return; }
            if (get && ruta.startsWith("/api/pdf/")) { texto(x, sistema.cotizaciones.pdf(ruta.substring("/api/pdf/".length()))); return; }
            if (get && ruta.startsWith("/api/imagen/")) { imagen(x, Long.parseLong(ruta.substring("/api/imagen/".length()))); return; }
            if (get) { responder(x, 404, "{\"error\":\"Ruta desconocida\"}"); return; }
            if (!x.getRequestMethod().equals("POST")) { responder(x, 405, "{\"error\":\"Método no permitido\"}"); return; }
            Map<String, String> p = parametros(x);
            String creado = switch (ruta) {
                case "/api/pedidos" -> registrarPedido(p);
                case "/api/platos" -> registrarPlato(p);
                case "/api/platos/editar" -> editarPlato(p);
                case "/api/platos/estado" -> estadoPlato(p);
                case "/api/cierre" -> { sistema.consumoDiario.ejecutar(); yield null; }
                case "/api/solicitudes" -> registrarSolicitud(p);
                case "/api/cotizaciones" -> generarCotizacion(p);
                case "/api/valores" -> registrarValores(p);
                case "/api/ordenes" -> sistema.ordenes.generar(requerido(p, "cotizacion")).numero();
                default -> throw new NoSuchElementException("Ruta desconocida");
            };
            responder(x, 200, estado(creado));
        } catch (NoSuchElementException e) {
            responder(x, 404, "{\"error\":" + json(e.getMessage()) + "}");
        } catch (Exception e) {
            responder(x, 400, "{\"error\":" + json(e.getMessage() == null ? "Error de solicitud" : e.getMessage()) + "}");
        }
    }

    // ------------------------------------------------------------------ casos de uso

    /** V03: mesa, cantidad y nota por plato (plato_{id}, nota_{id}). */
    private String registrarPedido(Map<String, String> p) {
        int numeroMesa = entero(p, "mesa");
        Mesa mesa = sistema.pedidos.mesas(CENTRO).stream().filter(m -> m.numero() == numeroMesa).findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Mesa inexistente"));
        Pedido pedido = new Pedido("PED-" + String.format("%04d", pedidos + 1), mesa, MESERO);
        for (PlatoCatalogo plato : sistema.pedidos.catalogo(CENTRO).stream().sorted(Comparator.comparing(PlatoCatalogo::platoId)).toList()) {
            String clave = "plato_" + plato.platoId();
            int cantidad = p.containsKey(clave) ? entero(p, clave) : 0;
            if (cantidad > 0) pedido.agregarPlato(plato.platoId(), cantidad, p.getOrDefault("nota_" + plato.platoId(), "").trim(), plato.precio());
        }
        if (pedido.detalles().isEmpty()) throw new IllegalArgumentException("Agrega al menos un plato al pedido");
        String numero = sistema.pedidos.registrar(pedido).numero();
        pedidos++;
        return numero;
    }

    /** V06: nombre, descripción, categoría, precio, imagen e ingredientes (ing_{insumo} en kg). */
    private String registrarPlato(Map<String, String> p) {
        long id = sistema.platos.crear(platoDe(p)).id();
        guardarImagen(id, p);
        return String.valueOf(id);
    }

    /** V06 · Editar plato: la misma ventana; la imagen solo cambia si llega una nueva. */
    private String editarPlato(Map<String, String> p) {
        long id = Long.parseLong(requerido(p, "id"));
        sistema.platos.editar(id, platoDe(p));
        guardarImagen(id, p);
        return String.valueOf(id);
    }

    /** V06 · Desactivar / Activar: el plato no se borra; solo entra o sale del catálogo del mesero. */
    private String estadoPlato(Map<String, String> p) {
        long id = Long.parseLong(requerido(p, "id"));
        if ("1".equals(requerido(p, "activo"))) sistema.platos.reactivar(id); else sistema.platos.desactivar(id);
        return String.valueOf(id);
    }

    private Plato platoDe(Map<String, String> p) {
        Plato plato = new Plato(CENTRO, JEFE, requerido(p, "nombre"), requerido(p, "categoria"), p.getOrDefault("descripcion", ""), decimal(p, "precio"));
        for (InsumoInventario i : sistema.inventario.insumos(CENTRO)) {
            String clave = "ing_" + i.productoId();
            if (p.containsKey(clave)) {
                plato.agregarIngrediente(new Ingrediente(new Producto(i.productoId(), i.nombre(), null), decimal(p, clave), UnidadMedida.KG));
            }
        }
        return plato;
    }

    private void guardarImagen(long id, Map<String, String> p) {
        String datos = p.getOrDefault("imagen", "");
        int coma = datos.indexOf(',');
        if (datos.startsWith("data:image/") && coma > 0) {
            sistema.platos.subirImagen(id, Base64.getDecoder().decode(datos.substring(coma + 1)), datos.substring(5, datos.indexOf(';')));
        }
    }

    /** V08: una fila por producto (producto_{k}, cantidad_{k}, marca_{k}, unidad_{k}). */
    private String registrarSolicitud(Map<String, String> p) {
        SolicitudAlimentos s = new SolicitudAlimentos("SOL-" + String.format("%03d", solicitudes + 1), CENTRO, JEFE, CORREO_JEFE);
        for (int k = 0; p.containsKey("producto_" + k); k++) {
            String marca = p.getOrDefault("marca_" + k, "").trim();
            s.agregarItem(new ItemSolicitud(Long.parseLong(requerido(p, "producto_" + k)), decimal(p, "cantidad_" + k),
                marca.isEmpty() ? null : marca, requerido(p, "unidad_" + k)));
        }
        String numero = sistema.solicitudes.registrar(s).numero();
        solicitudes++;
        return numero;
    }

    /** V13 (DEC-05): una o varias solicitudes, los productos marcados y la suma de sus cantidades. */
    private String generarCotizacion(Map<String, String> p) {
        List<String> numeros = new ArrayList<>();
        for (int k = 0; p.containsKey("solicitud_" + k); k++) numeros.add(p.get("solicitud_" + k));
        if (numeros.isEmpty()) throw new IllegalArgumentException("Debe seleccionar al menos una solicitud");
        Map<Long, BigDecimal> cantidades = new LinkedHashMap<>();
        Map<Long, String> unidades = new HashMap<>();
        for (String numero : numeros) {
            for (ItemSolicitud i : sistema.solicitudes.detalle(numero).items()) {
                if (!p.containsKey("producto_" + i.productoId())) continue;
                cantidades.merge(i.productoId(), i.cantidad(), BigDecimal::add);
                unidades.put(i.productoId(), i.unidad());
            }
        }
        List<ItemCotizacion> items = cantidades.entrySet().stream()
            .map(e -> new ItemCotizacion(e.getKey(), e.getValue(), unidades.get(e.getKey()))).toList();
        Proveedor proveedor = sistema.proveedores.detalle(Long.parseLong(requerido(p, "proveedor")));
        if (proveedor == null) throw new IllegalArgumentException("Proveedor inexistente");
        Cotizacion c = new Cotizacion("COT-" + String.format("%03d", cotizaciones + 1), numeros, proveedor,
            LocalDate.parse(requerido(p, "fechaLimite")), items);
        String numero = sistema.cotizaciones.generar(c).numero();
        cotizaciones++;
        return numero;
    }

    /** V18: precio del proveedor por unidad (precio_{producto}); «Validar cotización» además recorre la cadena (V19). */
    private String registrarValores(Map<String, String> p) {
        String numero = requerido(p, "cotizacion");
        Map<Long, BigDecimal> valores = new LinkedHashMap<>();
        for (ItemCotizacion i : sistema.cotizaciones.detalle(numero).items()) valores.put(i.productoId(), decimal(p, "precio_" + i.productoId()));
        sistema.cotizaciones.registrarValores(numero, valores);
        if ("1".equals(p.get("validar"))) sistema.cotizaciones.validar(numero);
        return numero;
    }

    /** HU-07: los cinco filtros de CriteriosConsulta; un filtro vacío no filtra. Devuelve los números. */
    private String filtrarSolicitudes(Map<String, String> p) {
        String desde = p.getOrDefault("desde", ""), hasta = p.getOrDefault("hasta", ""), producto = p.getOrDefault("producto", "");
        String marca = p.getOrDefault("marca", "").trim(), estado = p.getOrDefault("estado", "");
        LocalDate d = desde.isEmpty() ? null : LocalDate.parse(desde), h = hasta.isEmpty() ? null : LocalDate.parse(hasta);
        if (d != null && h != null && d.isAfter(h)) throw new IllegalArgumentException("La fecha «desde» es posterior a «hasta»");
        CriteriosConsulta c = new CriteriosConsulta(d, h, producto.isEmpty() ? null : Long.valueOf(producto),
            marca.isEmpty() ? null : marca, estado.isEmpty() ? null : EstadoSolicitud.valueOf(estado));
        return lista(sistema.solicitudes.consultar(c), s -> json(s.numero()));
    }

    // ------------------------------------------------------------------ estado para la interfaz

    private String estado(String creado) {
        // un plato desactivado sale del catálogo del mesero, pero sus pedidos viejos siguen mostrando su nombre
        List<Plato> menu = sistema.platos.consultar(CENTRO);
        Map<Long, String> nombres = new HashMap<>();
        menu.forEach(m -> nombres.put(m.id(), m.nombre()));
        Set<Long> enCatalogo = new HashSet<>();
        sistema.pedidos.catalogo(CENTRO).forEach(c -> enCatalogo.add(c.platoId()));
        Set<Long> enAlerta = new HashSet<>();
        sistema.inventario.alertas(CENTRO).forEach(i -> enAlerta.add(i.productoId()));
        StringBuilder b = new StringBuilder("{");
        campo(b, "creado", json(creado));
        campo(b, "pedidos", lista(sistema.pedidos.pedidosDelTurno(MESERO), p -> obj(
            "numero", json(p.numero()), "mesa", p.mesa().numero(), "fecha", json(p.fecha().toString()),
            "estado", json(p.estado().toString()), "total", n(p.total()),
            "platos", p.detalles().stream().mapToInt(DetallePedido::cantidad).sum(),
            "detalles", lista(p.detalles(), d -> obj("plato", json(nombres.get(d.platoId())), "cantidad", d.cantidad(),
                "nota", json(d.nota()), "precio", n(d.precioUnitario()), "subtotal", n(d.subtotal()))))));
        campo(b, "mesas", lista(sistema.pedidos.mesas(CENTRO), m -> obj("numero", m.numero(), "capacidad", m.capacidad())));
        campo(b, "platos", lista(menu, m -> obj(
            "id", m.id(), "nombre", json(m.nombre()), "precio", n(m.precioVenta()), "activo", m.activo(), "enCatalogo", enCatalogo.contains(m.id()),
            "categoria", json(m.categoria()), "descripcion", json(m.descripcion()),
            "imagen", m.imagenUrl() != null,
            "receta", lista(m.ingredientes(), g -> obj("id", g.producto().id(), "insumo", json(producto(g.producto().id())),
                "cantidad", n(g.enKg()), "unidad", json("kg"))))));
        campo(b, "insumos", lista(sistema.inventario.insumos(CENTRO), i -> obj(
            "id", i.productoId(), "nombre", json(i.nombre()), "categoria", json(CATEGORIA_INSUMO.get(i.productoId())),
            "stock", n(i.stockActual()), "referencia", n(i.stockReferencia()),
            "minimo", n(i.stockReferencia().multiply(new BigDecimal("0.25")).setScale(2, RoundingMode.HALF_UP)),
            "porcentaje", n(i.porcentajeExistencia()), "unidad", json(i.unidad()), "alerta", enAlerta.contains(i.productoId()))));
        campo(b, "solicitudes", lista(sistema.solicitudes.consultar(new CriteriosConsulta(null, null, null, null, null)), s -> obj(
            "numero", json(s.numero()), "fecha", json(s.fecha().toString()), "estado", json(s.estado().toString()),
            "sucursal", json("Sede Centro"), "solicitante", json("Carlos Mendoza"),
            "items", lista(s.items(), i -> obj("productoId", i.productoId(), "producto", json(producto(i.productoId())),
                "categoria", json(CATEGORIA_INSUMO.get(i.productoId())), "cantidad", n(i.cantidad()),
                "marca", json(i.marca()), "unidad", json(i.unidad()), "cotizado", i.cotizado())))));
        campo(b, "proveedores", lista(sistema.proveedores.consultar(true), p -> obj("id", p.id(), "nombre", json(p.razonSocial()))));
        campo(b, "cotizaciones", lista(sistema.cotizaciones.consultar(null), c -> obj(
            "numero", json(c.numero()), "solicitudes", lista(c.solicitudNumeros(), ServidorWeb::json),
            "proveedor", json(c.proveedor().razonSocial()), "fechaEnvio", json(String.valueOf(c.fechaEnvio())),
            "fechaLimite", json(String.valueOf(c.fechaLimite())), "estado", json(c.estado().toString()), "pdf", c.pdfUrl() != null,
            "estandar", total(c::valorEstandarTotal), "cotizado", total(c::valorCotizadoTotal), "diferencia", total(c::diferenciaPorcentual),
            "items", lista(c.items(), i -> obj("productoId", i.productoId(), "producto", json(producto(i.productoId())),
                "cantidad", n(i.cantidad()), "unidad", json(i.unidad()),
                "precioEstandar", n(i.precioEstandar()), "precioProveedor", n(i.precioProveedor()))))));
        campo(b, "ordenes", lista(sistema.ordenes.consultar(null), o -> obj(
            "numero", json(o.numero()), "fecha", json(String.valueOf(o.fecha())), "cotizacion", json(o.cotizacionNumero()),
            "solicitudes", lista(o.solicitudNumeros(), ServidorWeb::json), "proveedor", json(o.proveedor()),
            "estado", json(o.estado().toString()), "total", n(o.total()),
            "items", lista(o.items(), i -> obj("producto", json(producto(i.productoId())), "cantidad", n(i.cantidad()),
                "unidad", json(i.unidad()), "precio", n(i.precio()))))));
        campo(b, "alarmas", lista(sistema.alarmas.alarmas(), ServidorWeb::json));
        campo(b, "correos", lista(sistema.correos.enviados(), m -> obj("para", json(m.para()), "asunto", json(m.asunto()))));
        b.append("\"eventos\":").append(sistema.bus.publicados().size()).append('}');
        return b.toString();
    }

    private String producto(Long id) {
        return sistema.inventario.insumos(CENTRO).stream().filter(i -> i.productoId().equals(id)).map(InsumoInventario::nombre)
            .findFirst().orElse("Producto " + id);
    }

    private static String menu(ElementoMenu e) {
        return obj("titulo", json(e.titulo()), "ruta", json(e.ruta()), "hijos", lista(e.hijos(), ServidorWeb::menu));
    }

    /** Los totales de una cotización enviada sin precios todavía no existen: van en null. */
    private static String total(java.util.function.Supplier<BigDecimal> s) {
        try { return n(s.get()); } catch (IllegalStateException e) { return "null"; }
    }

    // ------------------------------------------------------------------ JSON y parámetros

    private static String obj(Object... kv) {
        StringBuilder b = new StringBuilder("{");
        for (int k = 0; k < kv.length; k += 2) {
            if (k > 0) b.append(',');
            b.append('"').append(kv[k]).append("\":").append(kv[k + 1]);
        }
        return b.append('}').toString();
    }
    private static void campo(StringBuilder b, String nombre, String valor) { b.append('"').append(nombre).append("\":").append(valor).append(','); }
    private static <T> String lista(List<T> v, Function<T, String> f) { return "[" + String.join(",", v.stream().map(f).toList()) + "]"; }
    private static String n(BigDecimal v) { return v == null ? "null" : v.stripTrailingZeros().toPlainString(); }
    private static String json(String s) {
        if (s == null) return "null";
        StringBuilder b = new StringBuilder("\"");
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"' -> b.append("\\\"");
                case '\\' -> b.append("\\\\");
                case '\n' -> b.append("\\n");
                case '\r' -> { }
                case '\t' -> b.append("\\t");
                default -> { if (c < 0x20) b.append(String.format("\\u%04x", (int) c)); else b.append(c); }
            }
        }
        return b.append('"').toString();
    }
    private static String requerido(Map<String, String> p, String k) {
        String v = p.getOrDefault(k, "").trim();
        if (v.isEmpty()) throw new IllegalArgumentException("Falta el campo " + k);
        return v;
    }
    private static int entero(Map<String, String> p, String k) {
        int v = Integer.parseInt(requerido(p, k));
        if (v < 0 || v > 1000) throw new IllegalArgumentException("Cantidad inválida");
        return v;
    }
    private static BigDecimal decimal(Map<String, String> p, String k) {
        BigDecimal v;
        try { v = new BigDecimal(requerido(p, k)); } catch (NumberFormatException e) { throw new IllegalArgumentException("Valor numérico inválido"); }
        if (v.signum() < 0) throw new IllegalArgumentException("El valor no puede ser negativo");
        return v;
    }
    private static Map<String, String> parametros(HttpExchange x) throws IOException {
        byte[] cuerpo = x.getRequestBody().readNBytes(MAX_CUERPO + 1);
        if (cuerpo.length > MAX_CUERPO) throw new IllegalArgumentException("La petición supera 2 MB");
        return consulta(new String(cuerpo, StandardCharsets.UTF_8));
    }
    private static Map<String, String> consulta(String body) {
        Map<String, String> p = new HashMap<>();
        if (body == null) return p;
        for (String parte : body.split("&")) {
            String[] kv = parte.split("=", 2);
            if (kv.length == 2) p.put(URLDecoder.decode(kv[0], StandardCharsets.UTF_8), URLDecoder.decode(kv[1], StandardCharsets.UTF_8));
        }
        return p;
    }

    // ------------------------------------------------------------------ respuestas

    private void archivo(HttpExchange x, String ruta) throws IOException {
        if (ruta.equals("/")) ruta = "/index.html";
        Path f = web.resolve(ruta.substring(1)).normalize();
        // solo la interfaz: nada del código, las pruebas, .git ni otros archivos del proyecto
        boolean publico = PUBLICOS.contains(ruta) || f.startsWith(web.resolve("assets"));
        if (!publico || !f.startsWith(web) || !Files.isRegularFile(f)) {
            responder(x, 404, "{\"error\":\"No encontrado\"}");
            return;
        }
        String tipo = ruta.endsWith(".html") ? "text/html; charset=utf-8" : ruta.endsWith(".css") ? "text/css; charset=utf-8"
            : ruta.endsWith(".js") ? "text/javascript; charset=utf-8" : ruta.endsWith(".png") ? "image/png"
            : ruta.endsWith(".mp4") ? "video/mp4" : "application/octet-stream";
        enviar(x, 200, tipo, Files.readAllBytes(f));
    }
    private void imagen(HttpExchange x, long id) throws IOException {
        String url = sistema.platos.consultar(CENTRO).stream().filter(p -> p.id() == id).map(Plato::imagenUrl).findFirst().orElse(null);
        if (url == null) { responder(x, 404, "{\"error\":\"Sin imagen\"}"); return; }
        String tipo = url.endsWith(".png") ? "image/png" : url.endsWith(".webp") ? "image/webp" : "image/jpeg";
        enviar(x, 200, tipo, sistema.platos.imagen(id));
    }
    private static void texto(HttpExchange x, byte[] b) throws IOException { enviar(x, 200, "text/plain; charset=utf-8", b); }
    private static void responder(HttpExchange x, int codigo, String s) throws IOException {
        enviar(x, codigo, "application/json; charset=utf-8", s.getBytes(StandardCharsets.UTF_8));
    }
    private static void enviar(HttpExchange x, int codigo, String tipo, byte[] b) throws IOException {
        x.getResponseHeaders().set("Content-Type", tipo);
        x.getResponseHeaders().set("Cache-Control", "no-store");
        x.sendResponseHeaders(codigo, b.length);
        x.getResponseBody().write(b);
        x.close();
    }
}
