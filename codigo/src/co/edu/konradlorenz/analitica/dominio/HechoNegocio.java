package co.edu.konradlorenz.analitica.dominio;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Un dato del negocio que el tablero suma o promedia. Con este esqueleto, cada KPI lee un tipo:
 * DEMANDA_PRODUCTO (clave = producto, valor = unidades), DEMANDA_SUCURSAL (clave = sucursal, valor = unidades)
 * y PRECIO_PROVEEDOR (clave = proveedor, valor = diferencia porcentual contra el precio estándar).
 */
public class HechoNegocio {

    private String tipo;
    private Long sucursalId;
    private String clave;
    private BigDecimal valor;
    private LocalDate fecha;

    public HechoNegocio(String tipo, Long sucursalId, String clave, BigDecimal valor, LocalDate fecha) {
        this.tipo = tipo;
        this.sucursalId = sucursalId;
        this.clave = clave;
        this.valor = valor;
        this.fecha = fecha;
    }

    public String tipo() {
        return tipo;
    }

    public Long sucursalId() {
        return sucursalId;
    }

    public String clave() {
        return clave;
    }

    public BigDecimal valor() {
        return valor;
    }

    public LocalDate fecha() {
        return fecha;
    }

}
