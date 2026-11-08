package co.edu.konradlorenz.identidad.dominio;

import java.util.List;

/**
 * «Composite · componente»
 */
public abstract class ElementoMenu {

    protected String titulo;
    protected String ruta;

    public void agregar(ElementoMenu e) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    public List<ElementoMenu> hijos() {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    public abstract boolean esHoja();

}
