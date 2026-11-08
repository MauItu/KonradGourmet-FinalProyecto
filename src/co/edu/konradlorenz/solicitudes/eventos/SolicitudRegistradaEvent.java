package co.edu.konradlorenz.solicitudes.eventos;

import co.edu.konradlorenz.compartido.eventos.EventoDominio;

import java.util.List;

public class SolicitudRegistradaEvent extends EventoDominio {

    private String numero;
    private Long sucursalId;
    private Long solicitanteId;
    private String correoSolicitante;
    private List<Long> productoIds;

    public SolicitudRegistradaEvent(String usuario, String numero, Long sucursalId, Long solicitanteId, String correoSolicitante, List<Long> productoIds) {
        super(usuario);
        this.numero = numero;
        this.sucursalId = sucursalId;
        this.solicitanteId = solicitanteId;
        this.correoSolicitante = correoSolicitante;
        this.productoIds = productoIds;
    }

    public String numero() {
        return numero;
    }

    public Long sucursalId() {
        return sucursalId;
    }

    public Long solicitanteId() {
        return solicitanteId;
    }

    public String correoSolicitante() {
        return correoSolicitante;
    }

    public List<Long> productoIds() {
        return productoIds;
    }

    @Override
    public String tipo() {
        return "solicitud.registrada";
    }

}
