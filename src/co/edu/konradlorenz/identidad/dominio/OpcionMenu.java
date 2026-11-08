package co.edu.konradlorenz.identidad.dominio;

/**
 * «Composite · hoja»
 */
public class OpcionMenu extends ElementoMenu {

    public OpcionMenu(String titulo, String ruta) {
        super(titulo, ruta);
    }

    @Override
    public boolean esHoja() {
        return true;
    }

}
