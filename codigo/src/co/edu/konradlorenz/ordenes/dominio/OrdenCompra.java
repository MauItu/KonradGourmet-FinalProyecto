package co.edu.konradlorenz.ordenes.dominio;

import java.math.BigDecimal;
import java.time.LocalDate;
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

    public static OrdenCompra desde(CotizacionOpcionada c) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    public BigDecimal total() {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

}
