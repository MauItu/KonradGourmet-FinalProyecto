package co.edu.konradlorenz.notificaciones.configuracion;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Las conexiones WebSocket de V05 en esta réplica (PD-01). AlarmaPushAdapter publica cada alarma en el
 * canal de Redis; todas las réplicas la reciben aquí y cada una la entrega a las conexiones abiertas de su
 * destinatario (el correo del JWT con que el jefe abrió la conexión). Si el jefe no estaba conectado, no se
 * pierde nada: V05 lee las alertas de ms-inventario al abrirse. Fuera del diagrama: es la parte de
 * infraestructura de AlarmaPushAdapter «WebSocket → V05».
 */
@Component
public class CanalAlarmas extends TextWebSocketHandler implements MessageListener, HandshakeInterceptor {

    private static final Logger LOG = LoggerFactory.getLogger(CanalAlarmas.class);

    private final ObjectMapper json;
    private final Map<String, Set<WebSocketSession>> conexiones = new ConcurrentHashMap<>();

    public CanalAlarmas(ObjectMapper json) {
        this.json = json;
    }

    /** Solo el jefe de cocina abre el canal (el JWT ya lo validó Spring Security). */
    @Override
    public boolean beforeHandshake(ServerHttpRequest peticion, ServerHttpResponse respuesta, WebSocketHandler manejador,
                                   Map<String, Object> atributos) {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        boolean jefe = a != null && a.getAuthorities().stream().anyMatch(r -> "ROLE_JEFE_COCINA".equals(r.getAuthority()));
        if (!jefe) {
            respuesta.setStatusCode(HttpStatus.FORBIDDEN);
        }
        return jefe;
    }

    @Override
    public void afterHandshake(ServerHttpRequest peticion, ServerHttpResponse respuesta, WebSocketHandler manejador, Exception error) {
        // nada: la conexión se registra al establecerse
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession sesion) {
        String correo = sesion.getPrincipal() == null ? null : sesion.getPrincipal().getName();
        if (correo == null) {
            cerrar(sesion);
            return;
        }
        conexiones.computeIfAbsent(correo, c -> ConcurrentHashMap.newKeySet())
                .add(new ConcurrentWebSocketSessionDecorator(sesion, 5_000, 64 * 1024));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession sesion, CloseStatus estado) {
        conexiones.values().forEach(s -> s.removeIf(d -> d.getId().equals(sesion.getId())));
    }

    /** Llega del canal de Redis: {"destino": correo, "contenido": texto, "fechaHora": …}. */
    @Override
    public void onMessage(Message mensaje, byte[] patron) {
        String texto = new String(mensaje.getBody(), StandardCharsets.UTF_8);
        try {
            JsonNode alarma = json.readTree(texto);
            Set<WebSocketSession> abiertas = conexiones.getOrDefault(alarma.path("destino").asText(), Set.of());
            for (WebSocketSession s : abiertas) {
                try {
                    s.sendMessage(new TextMessage(texto));
                } catch (IOException | IllegalStateException e) {
                    LOG.warn("No se pudo entregar la alarma a una conexión de {}: {}", alarma.path("destino").asText(), e.getMessage());
                }
            }
        } catch (IOException e) {
            LOG.warn("Alarma ilegible en el canal: {}", e.getMessage());
        }
    }

    /** Cuántas conexiones abiertas tiene un destinatario en esta réplica. */
    public int abiertas(String correo) {
        return conexiones.getOrDefault(correo, Set.of()).size();
    }

    private static void cerrar(WebSocketSession sesion) {
        try {
            sesion.close(CloseStatus.POLICY_VIOLATION);
        } catch (IOException e) {
            // ya estaba cerrada
        }
    }

}
