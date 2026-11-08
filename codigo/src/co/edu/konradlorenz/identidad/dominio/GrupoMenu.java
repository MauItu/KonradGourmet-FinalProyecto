package co.edu.konradlorenz.identidad.dominio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * «Composite · compuesto»
 */
public class GrupoMenu extends ElementoMenu {

    private List<ElementoMenu> hijos = new ArrayList<>();

    public GrupoMenu(String titulo, String ruta) {
        super(titulo, ruta);
    }

    @Override
    public void agregar(ElementoMenu e) {
        hijos.add(e);
    }

    @Override
    public List<ElementoMenu> hijos() {
        return Collections.unmodifiableList(hijos);
    }

    @Override
    public boolean esHoja() {
        return false;
    }

}
