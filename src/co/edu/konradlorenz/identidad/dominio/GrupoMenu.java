package co.edu.konradlorenz.identidad.dominio;

import java.util.List;

/**
 * «Composite · compuesto»
 */
public class GrupoMenu extends ElementoMenu {

    private List<ElementoMenu> hijos;

    @Override
    public void agregar(ElementoMenu e) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    @Override
    public List<ElementoMenu> hijos() {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    @Override
    public boolean esHoja() {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

}
