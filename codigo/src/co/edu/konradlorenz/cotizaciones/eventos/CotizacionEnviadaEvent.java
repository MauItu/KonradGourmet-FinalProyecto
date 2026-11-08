package co.edu.konradlorenz.cotizaciones.eventos;

import co.edu.konradlorenz.compartido.eventos.EventoDominio;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class CotizacionEnviadaEvent extends EventoDominio {

    private String numero;
    private List<String> solicitudNumeros;
    private Map<Long, BigDecimal> precios;
    private String correoProveedor;
    private String pdfUrl;

    @Override
    public String tipo() {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

}
