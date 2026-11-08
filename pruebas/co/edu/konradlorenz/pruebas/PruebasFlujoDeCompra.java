package co.edu.konradlorenz.pruebas;

import co.edu.konradlorenz.compartido.eventos.AuditoriaEvent;
import co.edu.konradlorenz.cotizaciones.adaptadores.salida.CotizacionJpaAdapter;
import co.edu.konradlorenz.cotizaciones.aplicacion.CotizacionService;
import co.edu.konradlorenz.cotizaciones.aplicacion.GenerarCotizacionFacade;
import co.edu.konradlorenz.cotizaciones.dominio.ReglaValidacion;
import co.edu.konradlorenz.cotizaciones.puertos.salida.AlmacenArchivosPort;
import co.edu.konradlorenz.demo.AlmacenEnMemoria;
import co.edu.konradlorenz.demo.BusEnMemoria;
import co.edu.konradlorenz.demo.PdfSimulado;
import co.edu.konradlorenz.cotizaciones.dominio.Cotizacion;
import co.edu.konradlorenz.cotizaciones.dominio.EstadoCotizacion;
import co.edu.konradlorenz.cotizaciones.dominio.ItemCotizacion;
import co.edu.konradlorenz.cotizaciones.dominio.Proveedor;
import co.edu.konradlorenz.demo.Ensamble;
import co.edu.konradlorenz.notificaciones.dominio.Mensaje;
import co.edu.konradlorenz.ordenes.dominio.OrdenCompra;
import co.edu.konradlorenz.pedidos.dominio.Mesa;
import co.edu.konradlorenz.pedidos.dominio.Pedido;
import co.edu.konradlorenz.solicitudes.dominio.EstadoSolicitud;
import co.edu.konradlorenz.solicitudes.dominio.SolicitudAlimentos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.HashMap;

/** El recorrido de compra de punta a punta, con todos los servicios conectados por el bus en memoria. */
final class PruebasFlujoDeCompra {

    private PruebasFlujoDeCompra() {
    }

    static void ejecutar() {
        pedido();
        inventario();
        solicitudYCotizacion();
        orden();
        variasSolicitudes();
        precioFaltante();
        pdfFaltante();
        proveedores();
    }

    private static void pedido() {
        Prueba.grupo("Pedido (ms-pedidos)");
        Ensamble s = Datos.sistema();
        Pedido registrado = s.pedidos.registrar(Datos.pedido());
        Prueba.igualValor("el total de PED-0251 es $82.000", "82000", registrado.total());
        Prueba.igual("se publicó pedido.registrado", 1L, s.bus.cuantos("pedido.registrado"));
        Prueba.igual("el catálogo vigente de la sucursal trae los 2 platos", 2, s.pedidos.catalogo(1L).size());
        Prueba.igual("las mesas activas de la sucursal", 1, s.pedidos.mesas(1L).size());

        Pedido sinPlatos = new Pedido("PED-0252", new Mesa(5, 1L, 4, true));
        Prueba.lanza("un pedido sin platos se rechaza", IllegalStateException.class, "al menos un plato", () -> s.pedidos.registrar(sinPlatos));
        Pedido mesaInactiva = new Pedido("PED-0253", new Mesa(6, 1L, 4, false));
        mesaInactiva.agregarPlato(1L, 1, null, new BigDecimal("32000"));
        Prueba.lanza("una mesa inactiva se rechaza", IllegalStateException.class, "mesa activa", () -> s.pedidos.registrar(mesaInactiva));
        Pedido platoDesconocido = new Pedido("PED-0254", new Mesa(5, 1L, 4, true));
        platoDesconocido.agregarPlato(99L, 1, null, BigDecimal.TEN);
        Prueba.lanza("un plato que no está en el catálogo se rechaza", IllegalArgumentException.class, "no está disponible", () -> s.pedidos.registrar(platoDesconocido));
        Pedido otroPrecio = new Pedido("PED-0255", new Mesa(5, 1L, 4, true));
        otroPrecio.agregarPlato(1L, 1, null, new BigDecimal("30000"));
        Prueba.lanza("un precio distinto al del catálogo se rechaza", IllegalArgumentException.class, "no coincide", () -> s.pedidos.registrar(otroPrecio));
        Prueba.lanza("la cantidad debe ser mayor que cero", IllegalArgumentException.class, "mayor que cero",
                () -> new Pedido("PED-0256", new Mesa(5, 1L, 4, true)).agregarPlato(1L, 0, null, BigDecimal.TEN));
    }

    private static void inventario() {
        Prueba.grupo("Inventario (ms-inventario) y alertas");
        Ensamble s = Datos.sistema();
        s.pedidos.registrar(Datos.pedido());
        Prueba.igual("antes del cierre no hay alertas", 0, s.inventario.alertas(1L).size());
        s.consumoDiario.ejecutar();
        Prueba.igual("después del cierre hay una alerta", 1, s.inventario.alertas(1L).size());
        Prueba.igual("es la de la res", "Res", s.inventario.alertas(1L).get(0).nombre());
        Prueba.igualValor("la res queda en 11,5 kg", "11.5", s.inventario.alertas(1L).get(0).stockActual());
        Prueba.igualValor("la res queda al 23 %", "23", s.inventario.alertas(1L).get(0).porcentajeExistencia());
        Prueba.igual("se publicó inventario.stock-bajo", 1L, s.bus.cuantos("inventario.stock-bajo"));
        Prueba.igual("la alarma dice «Res al 23 %: realice una solicitud»", "jefe.centro@konradgourmet.com: Res al 23 %: realice una solicitud",
                s.alarmas.alarmas().get(0));
        Mensaje correo = s.correos.enviados().get(0);
        Prueba.igual("el correo va al jefe de la sucursal", "jefe.centro@konradgourmet.com", correo.para());
        Prueba.verificar("el correo lleva X-Certificado", correo.cabeceras().containsKey("X-Certificado"));
        Prueba.verificar("el correo lleva X-Sello-Tiempo", correo.cabeceras().containsKey("X-Sello-Tiempo"));

        s.consumoDiario.ejecutar();
        Prueba.igualValor("ejecutar el cierre otra vez no descuenta dos veces", "11.5", s.inventario.alertas(1L).get(0).stockActual());
        Prueba.igual("ni publica otra alerta", 1L, s.bus.cuantos("inventario.stock-bajo"));

        s.solicitudes.registrar(Datos.solicitud());
        Prueba.igual("solicitud.registrada con la res atiende la alerta", 0, s.inventario.alertas(1L).size());

        // al día siguiente se vende una Bandeja paisa más: la alerta ya atendida no se repite ni se reactiva (S4-1)
        Pedido otro = new Pedido("PED-0260", new Mesa(5, 1L, 4, true));
        otro.agregarPlato(1L, 1, null, new BigDecimal("32000"));
        s.pedidos.registrar(otro);
        s.consumoDiario.ejecutar();
        Prueba.igualValor("la Bandeja paisa del día siguiente descuenta otros 0,5 kg de res", "11",
                s.inventario.insumos(1L).stream().filter(i -> i.productoId().equals(1L)).findFirst().orElseThrow().stockActual());
        Prueba.igual("el cierre del día siguiente no repite la alerta de la res", 1L, s.bus.cuantos("inventario.stock-bajo"));
        Prueba.igual("ni manda otra alarma", 1, s.alarmas.alarmas().size());
        Prueba.igual("y V05 no la vuelve a mostrar", 0, s.inventario.alertas(1L).size());
    }

    private static void variasSolicitudes() {
        Prueba.grupo("Una cotización con varias solicitudes (DEC-05)");
        Ensamble s = Datos.sistema();
        s.solicitudes.registrar(Datos.solicitud());
        SolicitudAlimentos otra = new SolicitudAlimentos("SOL-032", 1L, 8L, "ana.ruiz@konradgourmet.com");
        otra.agregarItem(new co.edu.konradlorenz.solicitudes.dominio.ItemSolicitud(3L, new BigDecimal("10"), "Marca Y", "kg")); // papa: la cotización no la cubre
        s.solicitudes.registrar(otra);
        Cotizacion c = new Cotizacion("COT-040", List.of("SOL-031", "SOL-032"), s.proveedores.detalle(1L), LocalDate.of(2026, 9, 30), List.of(
                new ItemCotizacion(1L, new BigDecimal("20"), "kg"), new ItemCotizacion(2L, new BigDecimal("15"), "kg")));
        s.cotizaciones.generar(c);
        Prueba.igual("SOL-031 queda EN_TRAMITE: todos sus productos están cotizados", EstadoSolicitud.EN_TRAMITE, s.solicitudes.detalle("SOL-031").estado());
        Prueba.igual("SOL-032 sigue PENDIENTE: la cotización no cubre la papa", EstadoSolicitud.PENDIENTE, s.solicitudes.detalle("SOL-032").estado());
        s.cotizaciones.registrarValores("COT-040", Datos.precios("21250", "8000"));
        s.cotizaciones.validar("COT-040");
        s.ordenes.generar("COT-040");
        Prueba.igual("la orden completa SOL-031", EstadoSolicitud.COMPLETADA, s.solicitudes.detalle("SOL-031").estado());
        Prueba.igual("y también SOL-032, aunque estaba pendiente", EstadoSolicitud.COMPLETADA, s.solicitudes.detalle("SOL-032").estado());
        Prueba.igual("la cotización pasa a CON_ORDEN", EstadoCotizacion.CON_ORDEN, s.cotizaciones.detalle("COT-040").estado());
        Prueba.verificar("y el proveedor recibe el correo de la orden",
                s.correos.enviados().stream().anyMatch(m -> m.asunto().contains("OC-0012")));
        Prueba.lanza("generar() con un número de cotización repetido se rechaza", IllegalStateException.class, "Ya existe la cotización COT-040",
                () -> s.cotizaciones.generar(c));
    }

    private static void solicitudYCotizacion() {
        Prueba.grupo("Solicitud y cotización (ms-solicitudes y ms-cotizaciones)");
        Ensamble s = Datos.sistema();
        Prueba.lanza("una solicitud sin productos se rechaza", IllegalStateException.class, "al menos un producto",
                () -> s.solicitudes.registrar(new SolicitudAlimentos("SOL-032", 1L, 7L, "a@b.co")));
        SolicitudAlimentos solicitud = s.solicitudes.registrar(Datos.solicitud());
        Prueba.igual("SOL-031 nace PENDIENTE", EstadoSolicitud.PENDIENTE, solicitud.estado());

        Cotizacion c18 = s.cotizaciones.generar(Datos.cotizacionNueva(s, "COT-018", 1L));
        Prueba.igual("COT-018 queda ENVIADA", EstadoCotizacion.ENVIADA, c18.estado());
        Prueba.verificar("COT-018 guarda la URL de su PDF", c18.pdfUrl() != null && c18.pdfUrl().endsWith("COT-018.pdf"));
        Prueba.verificar("el PDF se puede leer del almacén", new String(s.cotizaciones.pdf("COT-018")).contains("COT-018"));
        Prueba.igualValor("el precio estándar de la res viene del Ministerio", "20000", c18.items().get(0).precioEstandar());
        Prueba.igual("el servicio web se llamó una vez por producto", 2, s.ministerio.llamadas());
        Prueba.igual("cotizacion.enviada se publicó una vez", 1L, s.bus.cuantos("cotizacion.enviada"));
        Prueba.igual("SOL-031 pasó a EN_TRAMITE", EstadoSolicitud.EN_TRAMITE, s.solicitudes.detalle("SOL-031").estado());
        Prueba.igual("se publicó solicitud.en-tramite", 1L, s.bus.cuantos("solicitud.en-tramite"));
        Prueba.igualValor("SOL-031 calcula su valor sugerido", "520000", s.solicitudes.detalle("SOL-031").valorSugeridoTotal());
        Prueba.verificar("el proveedor recibió el correo con el PDF adjunto",
                s.correos.enviados().stream().anyMatch(m -> m.para().equals("ventas@proveedorabc.com") && m.adjuntoUrl() != null && m.adjuntoUrl().endsWith("COT-018.pdf")));
        Prueba.verificar("y los otros correos no llevan adjunto",
                s.correos.enviados().stream().filter(m -> m.para().contains("carlos")).allMatch(m -> m.adjuntoUrl() == null));

        s.cotizaciones.reenviar("COT-018");
        Prueba.igual("reenviar() vuelve a publicar cotizacion.enviada", 2L, s.bus.cuantos("cotizacion.enviada"));
        Prueba.igualValor("pero SOL-031 no suma dos veces la misma cotización", "520000", s.solicitudes.detalle("SOL-031").valorSugeridoTotal());
        Prueba.igual("ni vuelve a avisar que está en trámite", 1L, s.bus.cuantos("solicitud.en-tramite"));

        Prueba.lanza("no se valida una cotización con los valores sin registrar", IllegalStateException.class, "Falta el precio del proveedor",
                () -> s.cotizaciones.validar("COT-018"));
        s.cotizaciones.registrarValores("COT-018", Datos.precios("21250", "8000"));
        Cotizacion validada = s.cotizaciones.validar("COT-018");
        Prueba.igual("COT-018 queda OPCIONADA", EstadoCotizacion.OPCIONADA, validada.estado());
        Prueba.igualValor("con +4,81 %", "4.81", validada.diferenciaPorcentual());
        Prueba.igual("se publicó cotizacion.validada", 1L, s.bus.cuantos("cotizacion.validada"));
        Prueba.lanza("no se vuelve a validar una cotización ya validada", IllegalStateException.class, "Solo se valida una cotización enviada",
                () -> s.cotizaciones.validar("COT-018"));
        Prueba.lanza("no se registran valores en una cotización ya validada", IllegalStateException.class, "Solo se registran valores",
                () -> s.cotizaciones.registrarValores("COT-018", Datos.precios("21250", "8000")));

        s.cotizaciones.generar(Datos.cotizacionNueva(s, "COT-019", 2L));
        s.cotizaciones.registrarValores("COT-019", Datos.precios("29000", "8000"));
        Prueba.igual("COT-019 queda RECHAZADA", EstadoCotizacion.RECHAZADA, s.cotizaciones.validar("COT-019").estado());
        Prueba.igual("las opcionadas son solo COT-018", List.of("COT-018"), s.cotizaciones.opcionadas().stream().map(Cotizacion::numero).toList());
        Prueba.igual("las pendientes de validar son ninguna", 0, s.cotizaciones.pendientesDeAtencion().size());
        Prueba.igual("todas las cotizaciones, ordenadas", List.of("COT-018", "COT-019"),
                s.cotizaciones.consultar(null).stream().map(Cotizacion::numero).toList());
        Prueba.lanza("una cotización que no existe", IllegalArgumentException.class, "No existe la cotización", () -> s.cotizaciones.detalle("COT-999"));
    }

    private static void orden() {
        Prueba.grupo("Orden de compra (ms-ordenes)");
        Ensamble s = Datos.sistema();
        s.solicitudes.registrar(Datos.solicitud());
        s.cotizaciones.generar(Datos.cotizacionNueva(s, "COT-018", 1L));
        Prueba.lanza("no se genera una orden sobre una cotización sin validar", IllegalStateException.class, "Solo se pueden generar órdenes de compra sobre cotizaciones opcionadas",
                () -> s.ordenes.generar("COT-018"));
        s.cotizaciones.registrarValores("COT-018", Datos.precios("21250", "8000"));
        s.cotizaciones.validar("COT-018");

        OrdenCompra orden = s.ordenes.generar("COT-018");
        Prueba.igual("la orden es OC-0012", "OC-0012", orden.numero());
        Prueba.igualValor("por $545.000", "545000", orden.total());
        Prueba.igual("de la cotización COT-018", "COT-018", orden.cotizacionNumero());
        Prueba.verificar("con su PDF en el almacén", orden.pdfUrl() != null && orden.pdfUrl().endsWith("OC-0012.pdf"));
        Prueba.igual("se publicó orden.generada", 1L, s.bus.cuantos("orden.generada"));
        Prueba.igual("COT-018 pasó a CON_ORDEN", EstadoCotizacion.CON_ORDEN, s.cotizaciones.detalle("COT-018").estado());
        Prueba.igual("SOL-031 pasó a COMPLETADA", EstadoSolicitud.COMPLETADA, s.solicitudes.detalle("SOL-031").estado());
        Prueba.verificar("el proveedor recibió el correo de la orden",
                s.correos.enviados().stream().anyMatch(m -> m.para().equals("ventas@proveedorabc.com") && m.asunto().contains("OC-0012")
                        && m.adjuntoUrl() != null && m.adjuntoUrl().endsWith("OC-0012.pdf")));
        Prueba.lanza("una segunda orden sobre COT-018 se rechaza", IllegalStateException.class, "Ya existe una orden de compra para la cotización COT-018",
                () -> s.ordenes.generar("COT-018"));
        Prueba.igual("sigue habiendo una sola orden", 1, s.ordenes.consultar(null).size());
        Prueba.igual("y una sola orden.generada", 1L, s.bus.cuantos("orden.generada"));
    }

    private static void precioFaltante() {
        Prueba.grupo("Cotización cuando el Ministerio no responde (H18)");
        Ensamble s = Datos.sistema();
        s.solicitudes.registrar(Datos.solicitud());
        Cotizacion con3 = new Cotizacion("COT-030", List.of("SOL-031"), s.proveedores.detalle(1L), LocalDate.of(2026, 9, 30),
                List.of(new ItemCotizacion(3L, new BigDecimal("10"), "kg"))); // la papa no tiene precio en el Ministerio
        Cotizacion pendiente = s.cotizaciones.generar(con3);
        Prueba.igual("queda PENDIENTE_PRECIO, sin perderla", EstadoCotizacion.PENDIENTE_PRECIO, pendiente.estado());
        Prueba.igual("no se publicó cotizacion.enviada", 0L, s.bus.cuantos("cotizacion.enviada"));
        Prueba.igual("la cotización quedó guardada", EstadoCotizacion.PENDIENTE_PRECIO, s.cotizaciones.detalle("COT-030").estado());
        Prueba.igual("el fallo de precio deja una auditoría con su causa", 1L,
                s.bus.publicados().stream().filter(e -> e instanceof AuditoriaEvent a
                        && a.usuario().equals("sistema") && a.accion().contains("COT-030")
                        && a.accion().contains("PENDIENTE_PRECIO")
                        && a.accion().contains("El Ministerio no tiene precio para el producto 3")).count());
        s.ministerio.definirPrecio("3", 3000);
        s.cotizaciones.reenviar("COT-030");
        Prueba.igual("con el precio disponible, reenviar() la completa", EstadoCotizacion.ENVIADA, s.cotizaciones.detalle("COT-030").estado());
        Prueba.igual("y publica cotizacion.enviada", 1L, s.bus.cuantos("cotizacion.enviada"));
    }

    private static void pdfFaltante() {
        Prueba.grupo("Fallo del almacén de PDF: estado, auditoría y recuperación (S4-13)");
        Ensamble s = Datos.sistema();
        BusEnMemoria bus = new BusEnMemoria();
        CotizacionJpaAdapter repositorio = new CotizacionJpaAdapter(new HashMap<>());
        AlmacenEnMemoria archivos = new AlmacenEnMemoria();
        boolean[] falla = {true};
        AlmacenArchivosPort almacen = new AlmacenArchivosPort() {
            @Override
            public String guardar(String nombre, byte[] contenido) {
                if (falla[0]) {
                    throw new IllegalStateException("Almacén de PDF no disponible");
                }
                return archivos.guardar(nombre, contenido);
            }

            @Override
            public byte[] obtener(String url) {
                return archivos.obtener(url);
            }
        };
        GenerarCotizacionFacade fachada = new GenerarCotizacionFacade(
                repositorio, id -> BigDecimal.TEN, new PdfSimulado(), almacen, bus);
        CotizacionService servicio = new CotizacionService(
                fachada, repositorio, ReglaValidacion.cadenaPorDefecto(), almacen, bus);
        Cotizacion pendiente = servicio.generar(Datos.cotizacionNueva(s, "COT-PDF", 1L));
        Prueba.igual("el fallo de PDF queda guardado como PENDIENTE_PRECIO", EstadoCotizacion.PENDIENTE_PRECIO,
                servicio.detalle("COT-PDF").estado());
        Prueba.igual("sin PDF no se publica cotizacion.enviada", 0L, bus.cuantos("cotizacion.enviada"));
        Prueba.igual("no queda una URL de PDF inexistente", null, pendiente.pdfUrl());
        Prueba.igual("la escritura pendiente publica exactamente una auditoría", 1L, bus.cuantos("auditoria.evento"));
        Prueba.verificar("la auditoría identifica cotización, estado, usuario y causa del fallo de PDF",
                bus.publicados().stream().anyMatch(e -> e instanceof AuditoriaEvent a
                        && a.usuario().equals("sistema") && a.accion().contains("COT-PDF")
                        && a.accion().contains("PENDIENTE_PRECIO")
                        && a.accion().contains("Almacén de PDF no disponible")));
        falla[0] = false;
        servicio.reenviar("COT-PDF");
        Prueba.igual("reenviar recupera la cotización cuando vuelve el almacén", EstadoCotizacion.ENVIADA,
                servicio.detalle("COT-PDF").estado());
        Prueba.igual("la recuperación publica un solo envío", 1L, bus.cuantos("cotizacion.enviada"));
        Prueba.verificar("el PDF recuperado se puede consultar", servicio.pdf("COT-PDF").length > 0);
        Prueba.igual("el envío recuperado también deja auditoría", 2L, bus.cuantos("auditoria.evento"));
    }

    private static void proveedores() {
        Prueba.grupo("Proveedores (HU-15)");
        Ensamble s = Datos.sistema();
        Prueba.lanza("un NIT repetido se rechaza", IllegalStateException.class, "Ya existe un proveedor con el NIT",
                () -> s.proveedores.registrar(new Proveedor(9L, "Otro", "900123456-1", "otro@correo.co", "1", "d", List.of())));
        Prueba.lanza("un correo inválido se rechaza", IllegalStateException.class, "correo del proveedor no es válido",
                () -> s.proveedores.registrar(new Proveedor(9L, "Otro", "900999999-9", "correo-sin-arroba", "1", "d", List.of())));
        Prueba.lanza("el NIT es obligatorio", IllegalStateException.class, "NIT es obligatorio",
                () -> s.proveedores.registrar(new Proveedor(9L, "Otro", " ", "otro@correo.co", "1", "d", List.of())));
        s.proveedores.inactivar(2L);
        Prueba.igual("un proveedor inactivo sale del desplegable de V13", 1, s.proveedores.consultar(true).size());
        Prueba.igual("pero sigue en la lista completa", 2, s.proveedores.consultar(false).size());
        Prueba.igual("cada acción publicó auditoria.evento", true, s.bus.cuantos("auditoria.evento") >= 3);
        Prueba.lanza("un parámetro del sistema sin valor se rechaza", IllegalArgumentException.class, "obligatorio",
                () -> s.admin.parametrizar("tema.x", null));
        long antes = s.bus.cuantos("auditoria.evento");
        s.pedidos.registrar(Datos.pedido());
        Prueba.verificar("registrar un pedido también deja auditoría (S4-3)", s.bus.cuantos("auditoria.evento") > antes);
    }

}
