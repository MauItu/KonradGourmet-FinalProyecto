package co.edu.konradlorenz.solicitudes.dominio;

import java.time.LocalDate;

/** Filtros de V11; un criterio en null no filtra. */
public class CriteriosConsulta {

    private LocalDate desde;
    private LocalDate hasta;
    private Long productoId;
    private String marca;
    private EstadoSolicitud estado;

    public CriteriosConsulta(LocalDate desde, LocalDate hasta, Long productoId, String marca, EstadoSolicitud estado) {
        this.desde = desde;
        this.hasta = hasta;
        this.productoId = productoId;
        this.marca = marca;
        this.estado = estado;
    }

    public LocalDate desde() {
        return desde;
    }

    public LocalDate hasta() {
        return hasta;
    }

    public Long productoId() {
        return productoId;
    }

    public String marca() {
        return marca;
    }

    public EstadoSolicitud estado() {
        return estado;
    }

}
