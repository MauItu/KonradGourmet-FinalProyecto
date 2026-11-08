package co.edu.konradlorenz.ordenes.eventos;

import co.edu.konradlorenz.compartido.eventos.EventoDominio;

import java.math.BigDecimal;
import java.util.List;

public class OrdenGeneradaEvent extends EventoDominio {

    private String numero;
    private String cotizacionNumero;
    private List<String> solicitudNumeros;
    private String proveedor;
    private String correoProveedor;
    private String pdfUrl;
    private BigDecimal total;

    @Override
    public String tipo() {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

}
