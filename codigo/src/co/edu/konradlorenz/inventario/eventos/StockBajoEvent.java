package co.edu.konradlorenz.inventario.eventos;

import co.edu.konradlorenz.compartido.eventos.EventoDominio;

import java.math.BigDecimal;

public class StockBajoEvent extends EventoDominio {

    private Long sucursalId;
    private Long productoId;
    private String nombreProducto;
    private BigDecimal stockActual;
    private String unidad;
    private BigDecimal porcentaje;

    public StockBajoEvent(String usuario, Long sucursalId, Long productoId, String nombreProducto, BigDecimal stockActual, String unidad, BigDecimal porcentaje) {
        super(usuario);
        this.sucursalId = sucursalId;
        this.productoId = productoId;
        this.nombreProducto = nombreProducto;
        this.stockActual = stockActual;
        this.unidad = unidad;
        this.porcentaje = porcentaje;
    }

    public Long sucursalId() {
        return sucursalId;
    }

    public Long productoId() {
        return productoId;
    }

    public String nombreProducto() {
        return nombreProducto;
    }

    public BigDecimal stockActual() {
        return stockActual;
    }

    public String unidad() {
        return unidad;
    }

    public BigDecimal porcentaje() {
        return porcentaje;
    }

    @Override
    public String tipo() {
        return "inventario.stock-bajo";
    }

}
