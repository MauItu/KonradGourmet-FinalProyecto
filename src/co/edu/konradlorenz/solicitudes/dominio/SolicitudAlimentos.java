package co.edu.konradlorenz.solicitudes.dominio;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class SolicitudAlimentos {

    private String numero;
    private Long sucursalId;
    private Long solicitanteId;
    private String correoSolicitante;
    private LocalDateTime fecha;
    private EstadoSolicitud estado;
    private BigDecimal valorSugeridoTotal;
    private Set<String> cotizacionesAplicadas;
    private List<ItemSolicitud> items;

    /** Una solicitud nueva nace PENDIENTE, sin productos y con la fecha de ahora. */
    public SolicitudAlimentos(String numero, Long sucursalId, Long solicitanteId, String correoSolicitante) {
        this.numero = numero;
        this.sucursalId = sucursalId;
        this.solicitanteId = solicitanteId;
        this.correoSolicitante = correoSolicitante;
        this.fecha = LocalDateTime.now();
        this.estado = EstadoSolicitud.PENDIENTE;
        this.valorSugeridoTotal = BigDecimal.ZERO;
        this.cotizacionesAplicadas = new LinkedHashSet<>();
        this.items = new ArrayList<>();
    }

    public String numero() {
        return numero;
    }

    public Long sucursalId() {
        return sucursalId;
    }

    public Long solicitanteId() {
        return solicitanteId;
    }

    public String correoSolicitante() {
        return correoSolicitante;
    }

    public LocalDateTime fecha() {
        return fecha;
    }

    public EstadoSolicitud estado() {
        return estado;
    }

    public BigDecimal valorSugeridoTotal() {
        return valorSugeridoTotal;
    }

    /** Las cotizaciones ya aplicadas; quien aplica una la agrega aquí (idempotencia). */
    public Set<String> cotizacionesAplicadas() {
        return cotizacionesAplicadas;
    }

    public List<ItemSolicitud> items() {
        return items;
    }

    public void agregarItem(ItemSolicitud i) {
        items.add(i);
    }

    /** Exige al menos un producto, con cantidad mayor que cero. */
    public void validarFormato() {
        if (items.isEmpty()) {
            throw new IllegalStateException("Debe agregar al menos un producto");
        }
        for (ItemSolicitud i : items) {
            if (i.productoId() == null || i.cantidad() == null || i.cantidad().signum() <= 0) {
                throw new IllegalStateException("Cada producto necesita una cantidad mayor que cero");
            }
        }
    }

    /** Guarda el precio sugerido del producto y recalcula el valor total; si la solicitud no lo pidió, no hace nada. */
    public void marcarCotizado(Long productoId, BigDecimal precio) {
        for (int k = 0; k < items.size(); k++) {
            ItemSolicitud i = items.get(k);
            if (i.productoId().equals(productoId)) {
                items.set(k, new ItemSolicitud(i.productoId(), i.cantidad(), i.marca(), i.unidad(), true, precio));
            }
        }
        valorSugeridoTotal = items.stream()
                .filter(ItemSolicitud::cotizado)
                .map(i -> i.precioSugerido().multiply(i.cantidad()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean yaAplicada(String cotizacion) {
        return cotizacionesAplicadas.contains(cotizacion);
    }

    public boolean todoCotizado() {
        return items.stream().allMatch(ItemSolicitud::cotizado);
    }

    /** PENDIENTE pasa a EN_TRAMITE; si ya lo está no hace nada; una solicitud COMPLETADA no vuelve atrás. */
    public void pasarAEnTramite() {
        if (estado == EstadoSolicitud.COMPLETADA) {
            throw new IllegalStateException("La solicitud " + numero + " ya está completada");
        }
        estado = EstadoSolicitud.EN_TRAMITE;
    }

    /** EN_TRAMITE pasa a COMPLETADA; si ya lo está no hace nada; una PENDIENTE no puede completarse. */
    public void completar() {
        if (estado == EstadoSolicitud.PENDIENTE) {
            throw new IllegalStateException("La solicitud " + numero + " sigue pendiente: no puede completarse");
        }
        estado = EstadoSolicitud.COMPLETADA;
    }

}
