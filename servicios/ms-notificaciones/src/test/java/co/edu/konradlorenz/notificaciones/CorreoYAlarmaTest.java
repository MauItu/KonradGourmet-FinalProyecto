package co.edu.konradlorenz.notificaciones;

import co.edu.konradlorenz.compartido.eventos.AuditoriaEvent;
import co.edu.konradlorenz.compartido.eventos.EventoDominio;
import co.edu.konradlorenz.compartido.puertos.EventPublisherPort;
import co.edu.konradlorenz.cotizaciones.eventos.CotizacionEnviadaEvent;
import co.edu.konradlorenz.analitica.eventos.TendenciaDetectadaEvent;
import co.edu.konradlorenz.inventario.eventos.StockBajoEvent;
import co.edu.konradlorenz.notificaciones.adaptadores.entrada.NotificacionListeners;
import co.edu.konradlorenz.notificaciones.adaptadores.salida.CorreoCertificadoDecorator;
import co.edu.konradlorenz.notificaciones.adaptadores.salida.CorreoSmtpAdapter;
import co.edu.konradlorenz.notificaciones.adaptadores.salida.EstampaCronologicaDecorator;
import co.edu.konradlorenz.notificaciones.aplicacion.NotificacionService;
import co.edu.konradlorenz.notificaciones.configuracion.CanalAlarmas;
import co.edu.konradlorenz.notificaciones.configuracion.ConfiguracionNotificaciones;
import co.edu.konradlorenz.notificaciones.dominio.CreadorAlarmaApp;
import co.edu.konradlorenz.notificaciones.dominio.CreadorCorreo;
import co.edu.konradlorenz.notificaciones.puertos.salida.AlarmaPushPort;
import co.edu.konradlorenz.notificaciones.puertos.salida.AlmacenArchivosPort;
import co.edu.konradlorenz.notificaciones.puertos.salida.DestinatarioRepositoryPort;
import co.edu.konradlorenz.notificaciones.puertos.salida.EnvioRepositoryPort;
import co.edu.konradlorenz.ordenes.eventos.OrdenGeneradaEvent;
import co.edu.konradlorenz.solicitudes.eventos.SolicitudRegistradaEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.Multipart;
import jakarta.mail.Part;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.redis.connection.DefaultMessage;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * El núcleo de notificaciones con los adaptadores Spring y los decoradores en el orden de la configuración:
 * el correo certificado al proveedor sale con el PDF del almacén adjunto y las cabeceras de los decoradores;
 * el stock bajo empuja la alarma y avisa por correo al jefe de la libreta; un fallo de SMTP queda en la traza
 * y se relanza (reintento y DLQ). Y el canal de alarmas y el token del WebSocket.
 */
class CorreoYAlarmaTest {

    private static final byte[] PDF = "%PDF-1.4 orden OC-0012".getBytes(StandardCharsets.ISO_8859_1);

    private final JavaMailSender smtp = mock(JavaMailSender.class);
    private final AlmacenArchivosPort almacen = mock(AlmacenArchivosPort.class);
    private final AlarmaPushPort alarmas = mock(AlarmaPushPort.class);
    private final EnvioRepositoryPort envios = mock(EnvioRepositoryPort.class);
    private final DestinatarioRepositoryPort destinatarios = mock(DestinatarioRepositoryPort.class);
    private final EventPublisherPort eventos = mock(EventPublisherPort.class);
    private NotificacionListeners listeners;

    @BeforeEach
    void armar() {
        when(smtp.createMimeMessage()).thenAnswer(i -> new MimeMessage(Session.getInstance(new Properties())));
        when(almacen.obtener("s3://documentos-pdf/ordenes/OC-0012.pdf")).thenReturn(PDF);
        when(destinatarios.jefesDe(1L)).thenReturn(List.of("carlos.mendoza@konradgourmet.com"));
        CorreoSmtpAdapter adaptador = new CorreoSmtpAdapter(almacen, smtp, "Konrad Gourmet <notificaciones@konradgourmet.com>");
        NotificacionService servicio = new NotificacionService(
                new CreadorCorreo(new CorreoCertificadoDecorator(new EstampaCronologicaDecorator(adaptador))),
                new CreadorAlarmaApp(alarmas), envios, destinatarios, eventos);
        listeners = new NotificacionListeners(servicio);
    }

    private MimeMessage enviado() {
        ArgumentCaptor<MimeMessage> correo = ArgumentCaptor.forClass(MimeMessage.class);
        verify(smtp).send(correo.capture());
        return correo.getValue();
    }

    @Test
    void laOrdenSaleAlProveedorCertificadaConElPdfAdjunto() throws Exception {
        listeners.alRecibir(new OrdenGeneradaEvent("laura.gomez@konradgourmet.com", "OC-0012", "COT-018", List.of("SOL-031"), "Proveedor ABC",
                "ventas@proveedorabc.com", "s3://documentos-pdf/ordenes/OC-0012.pdf", new BigDecimal("545000")));

        MimeMessage m = enviado();
        assertEquals("ventas@proveedorabc.com", m.getAllRecipients()[0].toString());
        assertEquals("Orden de compra OC-0012", m.getSubject());
        assertTrue(m.getHeader("X-Certificado")[0].startsWith("SHA-256:"), "lleva la huella del correo certificado");
        assertNotNull(m.getHeader("X-Sello-Tiempo"), "y la estampa cronológica");
        Multipart partes = (Multipart) m.getContent();
        Part adjunto = null;
        for (int i = 0; i < partes.getCount(); i++) {
            if (Part.ATTACHMENT.equalsIgnoreCase(partes.getBodyPart(i).getDisposition())) {
                adjunto = partes.getBodyPart(i);
            }
        }
        assertNotNull(adjunto, "trae un adjunto");
        assertEquals("OC-0012.pdf", adjunto.getFileName());
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        adjunto.getInputStream().transferTo(bytes);
        assertArrayEquals(PDF, bytes.toByteArray(), "el adjunto es el PDF del almacén, sin cambios");
        verify(envios).registrar(eq("ventas@proveedorabc.com"), eq("orden.generada"), any());
        ArgumentCaptor<EventoDominio> auditoria = ArgumentCaptor.forClass(EventoDominio.class);
        verify(eventos).publicar(auditoria.capture());
        assertEquals("Notificación orden.generada enviada a ventas@proveedorabc.com", ((AuditoriaEvent) auditoria.getValue()).accion());
    }

    @Test
    void elStockBajoEmpujaLaAlarmaYAvisaPorCorreoALosJefesDeLaSucursal() throws Exception {
        listeners.alRecibir(new StockBajoEvent("sistema", 1L, 1L, "Res", new BigDecimal("11.5"), "kg", new BigDecimal("23")));

        verify(alarmas).empujar("carlos.mendoza@konradgourmet.com", "Res al 23 %: realice una solicitud");
        MimeMessage m = enviado();
        assertEquals("Stock bajo: Res", m.getSubject());
        assertTrue(m.getContent().toString().contains("Quedan 11.5 kg de Res"), m.getContent().toString());
        verify(envios, times(2)).registrar(eq("carlos.mendoza@konradgourmet.com"), eq("inventario.stock-bajo"), any());
    }

    @Test
    void lasPromocionesSugeridasLleganALosJefesDeTodasLasSucursales() throws Exception {
        when(destinatarios.jefes()).thenReturn(List.of("carlos.mendoza@konradgourmet.com", "sofia.ramirez@konradgourmet.com"));
        listeners.alRecibir(new TendenciaDetectadaEvent("sistema", List.of(
                "Ajiaco · Tendencia en redes y entre los 3 más pedidos de la semana · del 18/11/2026 al 24/11/2026",
                "Limonada natural · Tendencia en redes · del 18/11/2026 al 24/11/2026"), "Redes sociales y pedidos del 11/11/2026 al 17/11/2026"));

        ArgumentCaptor<MimeMessage> correos = ArgumentCaptor.forClass(MimeMessage.class);
        verify(smtp, times(2)).send(correos.capture());
        assertEquals(List.of("carlos.mendoza@konradgourmet.com", "sofia.ramirez@konradgourmet.com"),
                correos.getAllValues().stream().map(c -> {
                    try {
                        return c.getAllRecipients()[0].toString();
                    } catch (Exception e) {
                        throw new IllegalStateException(e);
                    }
                }).toList());
        MimeMessage m = correos.getAllValues().get(0);
        assertEquals("Promociones sugeridas", m.getSubject());
        String cuerpo = m.getContent().toString();
        assertTrue(cuerpo.contains("• Ajiaco · Tendencia en redes y entre los 3 más pedidos de la semana · del 18/11/2026 al 24/11/2026"), cuerpo);
        assertTrue(cuerpo.contains("• Limonada natural · Tendencia en redes"), cuerpo);
        assertTrue(m.getHeader("X-Certificado")[0].startsWith("SHA-256:"), "certificado, como los demás");
        verify(envios, times(2)).registrar(anyString(), eq("tendencia.detectada"), any());
    }

    @Test
    void laConfirmacionDeLaSolicitudVaAQuienLaCreoSinAdjunto() throws Exception {
        listeners.alRecibir(new SolicitudRegistradaEvent("carlos.mendoza@konradgourmet.com", "SOL-031", 1L, 2L,
                "carlos.mendoza@konradgourmet.com", List.of(1L, 2L)));
        MimeMessage m = enviado();
        assertEquals("Solicitud SOL-031 registrada", m.getSubject());
        assertTrue(m.getContent() instanceof String, "un correo de texto, sin adjunto");
        verify(almacen, never()).obtener(anyString());
    }

    @Test
    void siElSmtpFallaQuedaElFalloYElEventoSeReintenta() {
        when(almacen.obtener("s3://documentos-pdf/cotizaciones/COT-022.pdf")).thenReturn(PDF);
        doThrow(new MailSendException("Connection refused: correo:1025")).when(smtp).send(any(MimeMessage.class));
        CotizacionEnviadaEvent cotizacion = new CotizacionEnviadaEvent("isabella.castro@konradgourmet.com", "COT-022", List.of("SOL-031"),
                Map.of(1L, new BigDecimal("20000")), "ventas@proveedorb.com", "s3://documentos-pdf/cotizaciones/COT-022.pdf");

        assertThrows(MailSendException.class, () -> listeners.alRecibir(cotizacion), "se relanza: Kafka lo reintenta y luego va a la DLQ");
        verify(envios).registrarFallo("ventas@proveedorb.com", "cotizacion.enviada", "Connection refused: correo:1025");
        verify(envios, never()).registrar(anyString(), anyString(), any());
    }

    @Test
    void elCanalEntregaLaAlarmaSoloALasConexionesDeSuDestinatario() throws Exception {
        CanalAlarmas canal = new CanalAlarmas(new ObjectMapper());
        WebSocketSession jefe = sesion("s1", "carlos.mendoza@konradgourmet.com");
        WebSocketSession otro = sesion("s2", "otro.jefe@konradgourmet.com");
        canal.afterConnectionEstablished(jefe);
        canal.afterConnectionEstablished(otro);
        assertEquals(1, canal.abiertas("carlos.mendoza@konradgourmet.com"));

        String alarma = "{\"destino\":\"carlos.mendoza@konradgourmet.com\",\"contenido\":\"Res al 23 %: realice una solicitud\"}";
        canal.onMessage(new DefaultMessage("alarmas:stock-bajo".getBytes(), alarma.getBytes(StandardCharsets.UTF_8)), null);

        verify(jefe).sendMessage(new TextMessage(alarma));
        verify(otro, never()).sendMessage(any());
        canal.afterConnectionClosed(jefe, org.springframework.web.socket.CloseStatus.NORMAL);
        assertEquals(0, canal.abiertas("carlos.mendoza@konradgourmet.com"));
    }

    @Test
    void elTokenEnLaUrlSoloValeParaElWebSocketDeAlarmas() {
        BearerTokenResolver resolver = new ConfiguracionNotificaciones().tokenDelWebSocket();
        MockHttpServletRequest ws = new MockHttpServletRequest("GET", "/api/notificaciones/alarmas");
        ws.setParameter("access_token", "jwt-del-jefe");
        assertEquals("jwt-del-jefe", resolver.resolve(ws));
        MockHttpServletRequest otra = new MockHttpServletRequest("GET", "/api/notificaciones/envios");
        otra.setParameter("access_token", "jwt-del-jefe");
        assertNull(resolver.resolve(otra), "en las demás rutas el token solo vale en la cabecera");
        otra.addHeader("Authorization", "Bearer jwt-de-cabecera");
        assertEquals("jwt-de-cabecera", new ConfiguracionNotificaciones().tokenDelWebSocket().resolve(conCabecera()));
    }

    private static MockHttpServletRequest conCabecera() {
        MockHttpServletRequest r = new MockHttpServletRequest("GET", "/api/notificaciones/envios");
        r.addHeader("Authorization", "Bearer jwt-de-cabecera");
        return r;
    }

    private static WebSocketSession sesion(String id, String correo) {
        WebSocketSession s = mock(WebSocketSession.class);
        Principal p = () -> correo;
        when(s.getId()).thenReturn(id);
        when(s.getPrincipal()).thenReturn(p);
        when(s.isOpen()).thenReturn(true);
        return s;
    }

}
