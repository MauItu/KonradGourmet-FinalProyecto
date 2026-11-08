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

    public MenuActualizadoEvent(String usuario, Long platoId, Long sucursalId, String nombre, BigDecimal precioVenta, boolean activo, Map<Long, BigDecimal> receta) {
        super(usuario);
        this.platoId = platoId;
        this.sucursalId = sucursalId;
        this.nombre = nombre;
        this.precioVenta = precioVenta;
        this.activo = activo;
        this.receta = receta;
    }

    public Long platoId() {
        return platoId;
    }

    public Long sucursalId() {
        return sucursalId;
    }

    public String nombre() {
        return nombre;
    }

    public BigDecimal precioVenta() {
        return precioVenta;
    }

    public boolean activo() {
        return activo;
    }

    public Map<Long, BigDecimal> receta() {
        return receta;
    }

    @Override
    public String tipo() {
        return "menu.actualizado";
    }

}
