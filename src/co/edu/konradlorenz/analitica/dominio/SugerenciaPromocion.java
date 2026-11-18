package co.edu.konradlorenz.analitica.dominio;

import java.time.LocalDate;

/**
 * Una promoción sugerida al jefe de cocina (HU-19): el producto, el motivo de la recomendación y su vigencia.
 * Nace SUGERIDA y el jefe la acepta o la descarta una sola vez.
 */
public class SugerenciaPromocion {

    /** El texto de PD-10 cuando la sugerencia ya no está SUGERIDA. */
    public static final String YA_RESUELTA = "Esta sugerencia ya fue aceptada o descartada.";

    private Long id;
    private String producto;
    private String motivo;
    private LocalDate vigenciaDesde;
    private LocalDate vigenciaHasta;
    private EstadoSugerencia estado;

    /** Una sugerencia nueva, todavía sin id (lo pone el repositorio al guardarla). */
    public SugerenciaPromocion(String producto, String motivo, LocalDate vigenciaDesde, LocalDate vigenciaHasta) {
        this(null, producto, motivo, vigenciaDesde, vigenciaHasta, EstadoSugerencia.SUGERIDA);
    }

    /** La sugerencia rearmada desde la base, con su id y su estado. */
    public SugerenciaPromocion(Long id, String producto, String motivo, LocalDate vigenciaDesde, LocalDate vigenciaHasta,
                               EstadoSugerencia estado) {
        if (producto == null || producto.isBlank()) {
            throw new IllegalArgumentException("La sugerencia necesita un producto.");
        }
        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("La sugerencia necesita el motivo de la recomendación.");
        }
        if (vigenciaDesde == null || vigenciaHasta == null || vigenciaHasta.isBefore(vigenciaDesde)) {
            throw new IllegalArgumentException("La vigencia de la sugerencia no es válida.");
        }
        this.id = id;
        this.producto = producto.trim();
        this.motivo = motivo.trim();
        this.vigenciaDesde = vigenciaDesde;
        this.vigenciaHasta = vigenciaHasta;
        this.estado = estado == null ? EstadoSugerencia.SUGERIDA : estado;
    }

    /** La misma sugerencia con el id que le dio el repositorio. */
    public SugerenciaPromocion(SugerenciaPromocion base, Long id) {
        this(id, base.producto, base.motivo, base.vigenciaDesde, base.vigenciaHasta, base.estado);
    }

    public void aceptar() {
        resolver(EstadoSugerencia.ACEPTADA);
    }

    public void descartar() {
        resolver(EstadoSugerencia.DESCARTADA);
    }

    private void resolver(EstadoSugerencia nuevo) {
        if (estado != EstadoSugerencia.SUGERIDA) {
            throw new IllegalStateException(YA_RESUELTA);
        }
        estado = nuevo;
    }

    public Long id() {
        return id;
    }

    public String producto() {
        return producto;
    }

    public String motivo() {
        return motivo;
    }

    public LocalDate vigenciaDesde() {
        return vigenciaDesde;
    }

    public LocalDate vigenciaHasta() {
        return vigenciaHasta;
    }

    public EstadoSugerencia estado() {
        return estado;
    }

}
