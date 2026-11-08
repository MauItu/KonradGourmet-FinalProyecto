package co.edu.konradlorenz.identidad.dominio;

import java.util.List;

/**
 * «Composite · componente»
 */
public abstract class ElementoMenu {

    protected String titulo;
    protected String ruta;

    protected ElementoMenu(String titulo, String ruta) {
        this.titulo = titulo;
        this.ruta = ruta;
    }

    public String titulo() {
        return titulo;
    }

    public String ruta() {
        return ruta;
    }

    /** Solo los grupos aceptan hijos; una opción lanza UnsupportedOperationException. */
    public void agregar(ElementoMenu e) {
        throw new UnsupportedOperationException("Una opción de menú no tiene hijos: " + titulo);
    }

    /** Una hoja no tiene hijos. */
    public List<ElementoMenu> hijos() {
        return List.of();
    }

    public abstract boolean esHoja();

}
