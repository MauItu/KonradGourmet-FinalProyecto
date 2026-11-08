package co.edu.konradlorenz.solicitudes.eventos;

import co.edu.konradlorenz.compartido.eventos.EventoDominio;

public class SolicitudEnTramiteEvent extends EventoDominio {

    private String numero;
    private String correoSolicitante;

    public SolicitudEnTramiteEvent(String usuario, String numero, String correoSolicitante) {
        super(usuario);
        this.numero = numero;
        this.correoSolicitante = correoSolicitante;
    }

    public String numero() {
        return numero;
    }

    public String correoSolicitante() {
        return correoSolicitante;
    }

    @Override
    public String tipo() {
        return "solicitud.en-tramite";
    }

}
