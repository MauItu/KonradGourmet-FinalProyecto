package co.edu.konradlorenz.pruebas;

import co.edu.konradlorenz.integracion.adaptadores.salida.MinisterioComercioAdapter;
import co.edu.konradlorenz.integracion.adaptadores.salida.MinisterioSoapClient;
import co.edu.konradlorenz.integracion.adaptadores.salida.PrecioReferenciaCacheProxy;
import co.edu.konradlorenz.integracion.dominio.PrecioEstandar;
import co.edu.konradlorenz.solicitudes.dominio.EstadoSolicitud;
import co.edu.konradlorenz.solicitudes.dominio.ItemSolicitud;
import co.edu.konradlorenz.solicitudes.dominio.SolicitudAlimentos;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Corte 4a: el cliente SOAP del Ministerio habla con un servicio web de verdad (aquí, uno simulado con
 * el HttpServer del JDK), el Proxy lo llama solo la primera vez, y una solicitud guardada se rearma igual.
 */
final class PruebasIntegracion {

    private PruebasIntegracion() {
    }

    static void ejecutar() {
        clienteSoap();
        solicitudGuardada();
    }

    /** El Ministerio simulado: responde el precio de la res (código 1) y un SOAP Fault para lo demás. */
    private static void clienteSoap() {
        Prueba.grupo("MinisterioSoapClient (SOAP 1.1 contra un servicio web simulado)");
        AtomicInteger llamadas = new AtomicInteger();
        AtomicReference<String> recibido = new AtomicReference<>("");
        AtomicReference<String> accion = new AtomicReference<>("");
        HttpServer servidor;
        try {
            servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        } catch (IOException e) {
            Prueba.verificar("no se pudo abrir un puerto local para el Ministerio simulado: " + e.getMessage(), false);
            return;
        }
        servidor.createContext("/precios", x -> {
            llamadas.incrementAndGet();
            String cuerpo = new String(x.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            recibido.set(cuerpo);
            accion.set(String.valueOf(x.getRequestHeaders().getFirst("SOAPAction")));
            boolean res = cuerpo.contains("<codigoProducto>1</codigoProducto>");
            String respuesta = "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\"><soap:Body>"
                    + (res ? "<min:consultarPrecioEstandarResponse xmlns:min=\"" + MinisterioSoapClient.NAMESPACE + "\"><precio>20000.0</precio></min:consultarPrecioEstandarResponse>"
                           : "<soap:Fault><faultcode>soap:Client</faultcode><faultstring>El Ministerio no tiene precio para el producto 99</faultstring></soap:Fault>")
                    + "</soap:Body></soap:Envelope>";
            byte[] bytes = respuesta.getBytes(StandardCharsets.UTF_8);
            x.getResponseHeaders().add("Content-Type", "text/xml; charset=utf-8");
            x.sendResponseHeaders(res ? 200 : 500, bytes.length);
            try (OutputStream o = x.getResponseBody()) {
                o.write(bytes);
            }
        });
        servidor.start();
        String url = "http://127.0.0.1:" + servidor.getAddress().getPort() + "/precios";
        try {
            MinisterioSoapClient cliente = new MinisterioSoapClient(url);
            Prueba.verificar("el precio de la res es $20.000 por kg", cliente.consultarPrecioEstandar("1") == 20000.0);
            Prueba.verificar("manda la operación consultarPrecioEstandar en un sobre SOAP 1.1",
                    recibido.get().contains("schemas.xmlsoap.org/soap/envelope") && recibido.get().contains("consultarPrecioEstandar"));
            Prueba.igual("con la cabecera SOAPAction del WSDL", "\"" + MinisterioSoapClient.NAMESPACE + "consultarPrecioEstandar\"", accion.get());
            Prueba.lanza("un SOAP Fault llega como IllegalArgumentException con el mensaje del Ministerio", IllegalArgumentException.class,
                    "no tiene precio para el producto 99", () -> cliente.consultarPrecioEstandar("99"));
            Prueba.lanza("un código con caracteres de XML no rompe el sobre", IllegalArgumentException.class, "",
                    () -> cliente.consultarPrecioEstandar("<1>&"));

            Prueba.grupo("Proxy + Adapter con el cliente SOAP real");
            llamadas.set(0);
            Map<Long, PrecioEstandar> cache = new HashMap<>();
            PrecioReferenciaCacheProxy proxy = new PrecioReferenciaCacheProxy(new MinisterioComercioAdapter(cliente), cache);
            BigDecimal primero = proxy.precioEstandar(1L).valor();
            Prueba.verificar("1.ª consulta: va al servicio web (" + llamadas.get() + " llamada) y responde $20.000",
                    llamadas.get() == 1 && primero.compareTo(new BigDecimal("20000")) == 0);
            proxy.precioEstandar(1L);
            Prueba.igual("2.ª consulta: la responde la caché, sin llamar al Ministerio", 1, llamadas.get());
            cache.put(1L, new PrecioEstandar(1L, new BigDecimal("19000"), LocalDateTime.now().minusDays(1).minusMinutes(1)));
            proxy.precioEstandar(1L);
            Prueba.igual("un precio de hace más de un día se vuelve a consultar", 2, llamadas.get());
        } finally {
            servidor.stop(0);
        }
        Prueba.lanza("si el Ministerio no responde, IllegalStateException", IllegalStateException.class, "no responde",
                () -> new MinisterioSoapClient(url).consultarPrecioEstandar("1"));
    }

    private static void solicitudGuardada() {
        Prueba.grupo("Solicitud leída del repositorio");
        LocalDateTime ayer = LocalDateTime.now().minusDays(1);
        SolicitudAlimentos s = new SolicitudAlimentos("SOL-031", 1L, 2L, "carlos.mendoza@konradgourmet.com", ayer,
                EstadoSolicitud.EN_TRAMITE, new BigDecimal("400000"), Set.of("COT-018"),
                List.of(new ItemSolicitud(1L, new BigDecimal("20"), "Marca X", "kg", true, new BigDecimal("20000")),
                        new ItemSolicitud(2L, new BigDecimal("15"), null, "kg")));
        Prueba.igual("conserva la fecha con que se guardó", ayer, s.fecha());
        Prueba.igual("y su estado", EstadoSolicitud.EN_TRAMITE, s.estado());
        Prueba.igualValor("y el valor sugerido", "400000", s.valorSugeridoTotal());
        Prueba.verificar("y las cotizaciones ya aplicadas (un reenvío de COT-018 no suma dos veces)", s.yaAplicada("COT-018"));
        Prueba.verificar("y sus productos, cotizados o no", s.items().size() == 2 && s.items().get(0).cotizado() && !s.todoCotizado());
        s.cotizacionesAplicadas().add("COT-019");
        Prueba.verificar("la lista de cotizaciones aplicadas sigue siendo modificable", s.yaAplicada("COT-019"));
    }

}
