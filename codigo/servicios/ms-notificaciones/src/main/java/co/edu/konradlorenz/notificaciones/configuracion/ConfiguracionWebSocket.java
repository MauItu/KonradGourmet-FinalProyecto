package co.edu.konradlorenz.notificaciones.configuracion;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * Publica el canal de alarmas en /api/notificaciones/alarmas (pasa por Nginx y el gateway, que validan el
 * JWT al abrir la conexión). No se restringe el origen: la conexión no usa cookies, solo el token.
 */
@Configuration
@EnableWebSocket
public class ConfiguracionWebSocket implements WebSocketConfigurer {

    private final CanalAlarmas canal;

    public ConfiguracionWebSocket(CanalAlarmas canal) {
        this.canal = canal;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registro) {
        registro.addHandler(canal, ConfiguracionNotificaciones.RUTA_ALARMAS).addInterceptors(canal).setAllowedOriginPatterns("*");
    }

}
