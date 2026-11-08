package co.edu.konradlorenz.cotizaciones.eventos;

import co.edu.konradlorenz.compartido.eventos.EventoDominio;
import co.edu.konradlorenz.cotizaciones.dominio.EstadoCotizacion;

import java.math.BigDecimal;

public class CotizacionValidadaEvent extends EventoDominio {

    private String numero;
    private Long proveedorId;
    private EstadoCotizacion estado;
    private BigDecimal valorCotizado;

    @Override
    public String tipo() {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

}
