package co.edu.konradlorenz.notificaciones.puertos.entrada;

import co.edu.konradlorenz.compartido.eventos.EventoDominio;

import java.util.List;

public interface NotificarUseCase {

    void notificarEvento(EventoDominio evento);

    void registrarDestinatario(Long sucursalId, String correo);

    List<String> destinatarios(Long sucursalId);

}
