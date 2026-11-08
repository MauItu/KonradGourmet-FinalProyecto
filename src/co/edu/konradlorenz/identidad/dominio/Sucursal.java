package co.edu.konradlorenz.identidad.dominio;

import java.util.ArrayList;
import java.util.List;

public class Sucursal {

    private Long id;
    private String nombre;
    private List<Usuario> usuarios = new ArrayList<>();

    public Sucursal(Long id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public Long id() {
        return id;
    }

    public String nombre() {
        return nombre;
    }

    public List<Usuario> usuarios() {
        return usuarios;
    }

}
