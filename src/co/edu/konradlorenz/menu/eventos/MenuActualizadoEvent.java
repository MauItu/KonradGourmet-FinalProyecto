package co.edu.konradlorenz.menu.eventos;

import co.edu.konradlorenz.compartido.eventos.EventoDominio;

import java.math.BigDecimal;
import java.util.Map;

public class MenuActualizadoEvent extends EventoDominio {

    private Long platoId;
    private Long sucursalId;
    private String nombre;
    private BigDecimal precioVenta;
    private boolean activo;
    private Map<Long, BigDecimal> receta;

    @Override
    public String tipo() {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

}
