package co.edu.konradlorenz.ordenes.dominio;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class OrdenCompra {

    private String numero;
    private LocalDate fecha;
    /** {único}: no puede haber dos órdenes de compra sobre la misma cotización. */
    private String cotizacionNumero;
    private List<String> solicitudNumeros;
    private String proveedor;
    private String correoProveedor;
    private String pdfUrl;
    private EstadoOrden estado;
    private List<ItemOrden> items;

    /** Orden nueva: EN_PROCESO, con la fecha de hoy y sin número ni PDF todavía. */
    private OrdenCompra(CotizacionOpcionada c) {
        this.fecha = LocalDate.now();
        this.cotizacionNumero = c.numero();
        this.solicitudNumeros = c.solicitudNumeros();
        this.proveedor = c.proveedor();
        this.correoProveedor = c.correoProveedor();
        this.estado = EstadoOrden.EN_PROCESO;
        this.items = new ArrayList<>(c.items());
    }

    /** La misma orden, ya con el número que le dio el repositorio al guardarla. */
    public OrdenCompra(String numero, OrdenCompra sinNumero) {
        this.numero = numero;
        this.fecha = sinNumero.fecha;
        this.cotizacionNumero = sinNumero.cotizacionNumero;
        this.solicitudNumeros = sinNumero.solicitudNumeros;
        this.proveedor = sinNumero.proveedor;
        this.correoProveedor = sinNumero.correoProveedor;
        this.pdfUrl = sinNumero.pdfUrl;
        this.estado = sinNumero.estado;
        this.items = sinNumero.items;
    }

    /** La misma orden, con el PDF ya guardado en el almacén. */
    public OrdenCompra(OrdenCompra base, String pdfUrl) {
        this(base.numero, base);
        this.pdfUrl = pdfUrl;
    }

    public String numero() {
        return numero;
    }

    public LocalDate fecha() {
        return fecha;
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

    public EstadoOrden estado() {
        return estado;
    }

    public List<ItemOrden> items() {
        return items;
    }

    public static OrdenCompra desde(CotizacionOpcionada c) {
        return new OrdenCompra(c);
    }

    public BigDecimal total() {
        return items.stream()
                .map(i -> i.precio().multiply(i.cantidad()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

}
