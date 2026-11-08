package co.edu.konradlorenz.notificaciones.aplicacion;

import co.edu.konradlorenz.compartido.eventos.EventoDominio;
import co.edu.konradlorenz.compartido.puertos.EventPublisherPort;
import co.edu.konradlorenz.notificaciones.dominio.CreadorNotificacion;
import co.edu.konradlorenz.notificaciones.puertos.entrada.NotificarUseCase;
import co.edu.konradlorenz.notificaciones.puertos.salida.DestinatarioRepositoryPort;
import co.edu.konradlorenz.notificaciones.puertos.salida.EnvioRepositoryPort;

import java.util.List;

public class NotificacionService implements NotificarUseCase {

    private final CreadorNotificacion creadorCorreo;
    private final CreadorNotificacion creadorAlarma;
    private final EnvioRepositoryPort envios;
    private final DestinatarioRepositoryPort destinatarios;
    private final EventPublisherPort eventos;

    public NotificacionService(CreadorNotificacion creadorCorreo, CreadorNotificacion creadorAlarma, EnvioRepositoryPort envios, DestinatarioRepositoryPort destinatarios, EventPublisherPort eventos) {
        this.creadorCorreo = creadorCorreo;
        this.creadorAlarma = creadorAlarma;
        this.envios = envios;
        this.destinatarios = destinatarios;
        this.eventos = eventos;
    }

    @Override
    public void notificarEvento(EventoDominio evento) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    @Override
    public void registrarDestinatario(Long sucursalId, String correo) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    @Override
    public List<String> destinatarios(Long sucursalId) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

}
