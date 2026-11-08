package co.edu.konradlorenz.integracion.dominio;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PrecioEstandar {

    private Long productoId;
    private BigDecimal valor;
    private LocalDateTime consultadoEn;

    public PrecioEstandar(Long productoId, BigDecimal valor, LocalDateTime consultadoEn) {
        this.productoId = productoId;
        this.valor = valor;
        this.consultadoEn = consultadoEn;
    }

    public Long productoId() {
        return productoId;
    }

    public BigDecimal valor() {
        return valor;
    }

    public LocalDateTime consultadoEn() {
        return consultadoEn;
    }

}
