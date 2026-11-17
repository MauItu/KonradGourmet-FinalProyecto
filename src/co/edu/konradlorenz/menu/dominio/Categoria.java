package co.edu.konradlorenz.menu.dominio;

import java.util.List;

public class Categoria {

    private Long id;
    private String nombre;
    private List<Producto> productos;

    public Categoria(Long id, String nombre) {
        this.id = id;
        this.nombre = nombre;
        this.productos = new java.util.ArrayList<>();
    }

    public Long id() {
        return id;
    }

    public String nombre() {
        return nombre;
    }

    public List<Producto> productos() {
        return productos;
    }

}
