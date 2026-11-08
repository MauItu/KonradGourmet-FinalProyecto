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

    public CotizacionEnviadaEvent(String usuario, String numero, List<String> solicitudNumeros, Map<Long, BigDecimal> precios, String correoProveedor, String pdfUrl) {
        super(usuario);
        this.numero = numero;
        this.solicitudNumeros = solicitudNumeros;
        this.precios = precios;
        this.correoProveedor = correoProveedor;
        this.pdfUrl = pdfUrl;
    }

    public String numero() {
        return numero;
    }

    public List<String> solicitudNumeros() {
        return solicitudNumeros;
    }

    public Map<Long, BigDecimal> precios() {
        return precios;
    }

    public String correoProveedor() {
        return correoProveedor;
    }

    public String pdfUrl() {
        return pdfUrl;
    }

    @Override
    public String tipo() {
        return "cotizacion.enviada";
    }

}
