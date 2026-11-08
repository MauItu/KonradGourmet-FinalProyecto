package co.edu.konradlorenz.menu.dominio;

import java.math.BigDecimal;
import java.util.List;

public class Plato {

    private Long id;
    private Long sucursalId;
    private Long creadoPor;
    private String nombre;
    private String descripcion;
    private BigDecimal precioVenta;
    private boolean activo;
    private List<Ingrediente> ingredientes;

    public void agregarIngrediente(Ingrediente i) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    public void validar() {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    public void editar(Plato datos) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    public void desactivar() {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

}
