package co.edu.konradlorenz.menu.dominio;

public class Producto {

    private Long id;
    private String nombre;
    private Categoria categoria;

    public Producto(Long id, String nombre, Categoria categoria) {
        this.id = id;
        this.nombre = nombre;
        this.categoria = categoria;
    }

    public Long id() {
        return id;
    }

    public String nombre() {
        return nombre;
    }

    public Categoria categoria() {
        return categoria;
    }

}
