package co.edu.konradlorenz.solicitudes.eventos;

import co.edu.konradlorenz.compartido.eventos.EventoDominio;

import java.util.List;

public class SolicitudRegistradaEvent extends EventoDominio {

    private String numero;
    private Long sucursalId;
    private Long solicitanteId;
    private String correoSolicitante;
    private List<Long> productoIds;

    @Override
    public String tipo() {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

}
