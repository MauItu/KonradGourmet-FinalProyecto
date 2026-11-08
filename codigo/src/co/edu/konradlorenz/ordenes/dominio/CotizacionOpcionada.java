package co.edu.konradlorenz.ordenes.dominio;

import java.util.List;

public class CotizacionOpcionada {

    private String numero;
    private List<String> solicitudNumeros;
    private String proveedor;
    private String correoProveedor;
    private List<ItemOrden> items;

    public CotizacionOpcionada(String numero, List<String> solicitudNumeros, String proveedor, String correoProveedor, List<ItemOrden> items) {
        this.numero = numero;
        this.solicitudNumeros = solicitudNumeros;
        this.proveedor = proveedor;
        this.correoProveedor = correoProveedor;
        this.items = items;
    }

    public String numero() {
        return numero;
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

    public List<ItemOrden> items() {
        return items;
    }

}
