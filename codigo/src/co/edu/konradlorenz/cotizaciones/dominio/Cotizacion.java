package co.edu.konradlorenz.cotizaciones.dominio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
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

    /** Una cotización nueva todavía no tiene precio del Ministerio ni PDF: nace PENDIENTE_PRECIO. */
    public Cotizacion(String numero, List<String> solicitudNumeros, Proveedor proveedor, LocalDate fechaLimite, List<ItemCotizacion> items) {
        this.numero = numero;
        this.solicitudNumeros = solicitudNumeros;
        this.proveedor = proveedor;
        this.fechaEnvio = LocalDate.now();
        this.fechaLimite = fechaLimite;
        this.estado = EstadoCotizacion.PENDIENTE_PRECIO;
        this.items = new ArrayList<>(items);
    }

    /** La misma cotización, con el PDF ya guardado en el almacén. */
    public Cotizacion(Cotizacion base, String pdfUrl) {
        this.numero = base.numero;
        this.solicitudNumeros = base.solicitudNumeros;
        this.proveedor = base.proveedor;
        this.fechaEnvio = base.fechaEnvio;
        this.fechaLimite = base.fechaLimite;
        this.pdfUrl = pdfUrl;
        this.estado = base.estado;
        this.items = new ArrayList<>(base.items);
    }

    public String numero() {
        return numero;
    }

    public List<String> solicitudNumeros() {
        return solicitudNumeros;
    }

    public Proveedor proveedor() {
        return proveedor;
    }

    public LocalDate fechaEnvio() {
        return fechaEnvio;
    }

    public LocalDate fechaLimite() {
        return fechaLimite;
    }

    public String pdfUrl() {
        return pdfUrl;
    }

    public EstadoCotizacion estado() {
        return estado;
    }

    /** La lista es modificable: quien completa los precios (la fachada) reemplaza los ítems. */
    public List<ItemCotizacion> items() {
        return items;
    }

    /** Exige al menos una solicitud (HU-09) y al menos un producto. */
    public void validarSolicitudes() {
        if (solicitudNumeros == null || solicitudNumeros.isEmpty()) {
            throw new IllegalStateException("Debe seleccionar al menos una solicitud");
        }
        if (items.isEmpty()) {
            throw new IllegalStateException("Debe seleccionar al menos un producto para cotizar");
        }
    }

    /** Suma cantidad × precio del Ministerio de todos los ítems. */
    public BigDecimal valorEstandarTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (ItemCotizacion i : items) {
            if (i.precioEstandar() == null) {
                throw new IllegalStateException("Falta el precio estándar del producto " + i.productoId());
            }
            total = total.add(i.precioEstandar().multiply(i.cantidad()));
        }
        return total;
    }

    /** Suma cantidad × precio del proveedor de todos los ítems. */
    public BigDecimal valorCotizadoTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (ItemCotizacion i : items) {
            if (i.precioProveedor() == null) {
                throw new IllegalStateException("Falta el precio del proveedor para el producto " + i.productoId());
            }
            total = total.add(i.precioProveedor().multiply(i.cantidad()));
        }
        return total;
    }

    /** (cotizado − estándar) / estándar × 100, con 2 decimales: +4,81 significa 4,81 % por encima. */
    public BigDecimal diferenciaPorcentual() {
        BigDecimal estandar = valorEstandarTotal();
        if (estandar.signum() == 0) {
            throw new IllegalStateException("El valor estándar es cero: no se puede comparar");
        }
        return valorCotizadoTotal().subtract(estandar)
                .multiply(BigDecimal.valueOf(100))
                .divide(estandar, 2, RoundingMode.HALF_UP);
    }

    /** Guarda el precio del proveedor para un producto de la cotización. */
    public void registrarValor(Long productoId, BigDecimal precio) {
        if (precio == null || precio.signum() <= 0) {
            throw new IllegalArgumentException("El precio del proveedor debe ser mayor que cero");
        }
        for (int k = 0; k < items.size(); k++) {
            ItemCotizacion i = items.get(k);
            if (i.productoId().equals(productoId)) {
                items.set(k, new ItemCotizacion(i.productoId(), i.cantidad(), i.unidad(), i.precioEstandar(), precio));
                return;
            }
        }
        throw new IllegalArgumentException("El producto " + productoId + " no está en la cotización " + numero);
    }

    /** CON_ORDEN solo se alcanza desde OPCIONADA (HU-14); los demás cambios son libres. */
    public void cambiarEstado(EstadoCotizacion e) {
        if (e == EstadoCotizacion.CON_ORDEN && estado != EstadoCotizacion.OPCIONADA) {
            throw new IllegalStateException("Solo una cotización opcionada puede pasar a CON_ORDEN");
        }
        this.estado = e;
    }

}
