package co.edu.konradlorenz.pedidos.dominio;

import java.math.BigDecimal;

public class PlatoCatalogo {

    private Long platoId;
    private Long sucursalId;
    private String nombre;
    private BigDecimal precio;
    private boolean activo;

    public PlatoCatalogo(Long platoId, Long sucursalId, String nombre, BigDecimal precio, boolean activo) {
        this.platoId = platoId;
        this.sucursalId = sucursalId;
        this.nombre = nombre;
        this.precio = precio;
        this.activo = activo;
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

    public BigDecimal precio() {
        return precio;
    }

    public boolean activo() {
        return activo;
    }

}
