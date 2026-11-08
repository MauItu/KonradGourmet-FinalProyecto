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

    public OrdenGeneradaEvent(String usuario, String numero, String cotizacionNumero, List<String> solicitudNumeros, String proveedor, String correoProveedor, String pdfUrl, BigDecimal total) {
        super(usuario);
        this.numero = numero;
        this.cotizacionNumero = cotizacionNumero;
        this.solicitudNumeros = solicitudNumeros;
        this.proveedor = proveedor;
        this.correoProveedor = correoProveedor;
        this.pdfUrl = pdfUrl;
        this.total = total;
    }

    public String numero() {
        return numero;
    }

    public String cotizacionNumero() {
        return cotizacionNumero;
    }

    public List<String> solicitudNumeros() {
        return solicitudNumeros;
    }

    public String proveedor() {
        return proveedor;
    }

    public String correoProveedor() {
        return correoProveedor;
    }

    public String pdfUrl() {
        return pdfUrl;
    }

    public BigDecimal total() {
        return total;
    }

    @Override
    public String tipo() {
        return "orden.generada";
    }

}
