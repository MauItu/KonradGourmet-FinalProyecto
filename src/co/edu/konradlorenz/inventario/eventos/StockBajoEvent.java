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

    @Override
    public String tipo() {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

}
