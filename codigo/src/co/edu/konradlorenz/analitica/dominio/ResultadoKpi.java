package co.edu.konradlorenz.analitica.dominio;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Un indicador del tablero BAM (HU-18): el ganador con su valor numérico, los primeros puestos para la gráfica
 * (el ganador primero) y el periodo con que se calculó (PD-03, DEC-88).
 */
public class ResultadoKpi {

    /** Cuántos puestos lleva el ranking de la gráfica. */
    public static final int PUESTOS = 5;

    private String kpi;
    private String ganador;
    private BigDecimal valor;
    private Map<String, BigDecimal> ranking;
    private LocalDate desde;
    private LocalDate hasta;

    public ResultadoKpi(String kpi, String ganador, BigDecimal valor) {
        this(kpi, ganador, valor, Map.of());
    }

    public ResultadoKpi(String kpi, String ganador, BigDecimal valor, Map<String, BigDecimal> ranking) {
        this.kpi = kpi;
        this.ganador = ganador;
        this.valor = valor;
        this.ranking = Collections.unmodifiableMap(new LinkedHashMap<>(ranking));
    }

    /** El mismo resultado con el periodo que se aplicó (lo pone TableroService, que conoce el rango). */
    public ResultadoKpi(ResultadoKpi base, LocalDate desde, LocalDate hasta) {
        this(base.kpi, base.ganador, base.valor, base.ranking);
        this.desde = desde;
        this.hasta = hasta;
    }

    public String kpi() {
        return kpi;
    }

    /** Null cuando no hay hechos suficientes. */
    public String ganador() {
        return ganador;
    }

    public BigDecimal valor() {
        return valor;
    }

    /** Los primeros puestos, en orden (el ganador primero); vacío si no hay hechos. */
    public Map<String, BigDecimal> ranking() {
        return ranking;
    }

    public LocalDate desde() {
        return desde;
    }

    public LocalDate hasta() {
        return hasta;
    }

}
