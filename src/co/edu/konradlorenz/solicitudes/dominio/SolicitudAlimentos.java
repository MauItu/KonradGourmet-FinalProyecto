package co.edu.konradlorenz.solicitudes.dominio;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public class SolicitudAlimentos {

    private String numero;
    private Long sucursalId;
    private Long solicitanteId;
    private String correoSolicitante;
    private LocalDateTime fecha;
    private EstadoSolicitud estado;
    private BigDecimal valorSugeridoTotal;
    private Set<String> cotizacionesAplicadas;
    private List<ItemSolicitud> items;

    public void agregarItem(ItemSolicitud i) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    public void validarFormato() {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    public void marcarCotizado(Long productoId, BigDecimal precio) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    public boolean yaAplicada(String cotizacion) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    public boolean todoCotizado() {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    public void pasarAEnTramite() {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    public void completar() {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

}
