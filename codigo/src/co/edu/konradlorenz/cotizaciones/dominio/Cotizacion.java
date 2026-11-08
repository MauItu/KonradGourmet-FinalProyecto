package co.edu.konradlorenz.cotizaciones.dominio;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Nota del diagrama: Una cotización agrupa una o varias solicitudes (HU-09). validarSolicitudes()
 * rechaza la lista vacía con «Debe seleccionar al menos una solicitud», y los ítems suman los
 * productos y cantidades de todas las solicitudes elegidas.
 */
public class Cotizacion {

    private String numero;
    private List<String> solicitudNumeros;
    private Proveedor proveedor;
    private LocalDate fechaEnvio;
    private LocalDate fechaLimite;
    private String pdfUrl;
    private EstadoCotizacion estado;
    private List<ItemCotizacion> items;

    public void validarSolicitudes() {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    public BigDecimal valorEstandarTotal() {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    public BigDecimal valorCotizadoTotal() {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    public BigDecimal diferenciaPorcentual() {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    public void registrarValor(Long productoId, BigDecimal precio) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    public void cambiarEstado(EstadoCotizacion e) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

}
