package co.edu.konradlorenz.solicitudes.eventos;

import co.edu.konradlorenz.compartido.eventos.EventoDominio;

public class SolicitudEnTramiteEvent extends EventoDominio {

    private String numero;
    private String correoSolicitante;

    @Override
    public String tipo() {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

}
