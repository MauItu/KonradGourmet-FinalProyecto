package co.edu.konradlorenz.analitica.dominio;

import java.math.BigDecimal;

public class ResultadoKpi {

    private String kpi;
    private String ganador;
    private BigDecimal valor;

    public ResultadoKpi(String kpi, String ganador, BigDecimal valor) {
        this.kpi = kpi;
        this.ganador = ganador;
        this.valor = valor;
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

}
