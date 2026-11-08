package co.edu.konradlorenz.identidad.dominio;

public class DirectorCompras extends Usuario {

    public DirectorCompras(Long id, String nombre, String correo, Sucursal sucursal) {
        super(id, nombre, correo, sucursal);
    }

    /**
     * Menú del panel izquierdo de V16–V23: Inicio, Cotizaciones (Todas, Pendientes de validar,
     * Opcionadas), Órdenes de compra, Mi perfil y Cerrar sesión.
     */
    @Override
    public ElementoMenu construirMenu() {
        GrupoMenu raiz = new GrupoMenu("Menú", "/");
        raiz.agregar(new OpcionMenu("Inicio", "/inicio"));
        GrupoMenu cotizaciones = new GrupoMenu("Cotizaciones", "/cotizaciones");
        cotizaciones.agregar(new OpcionMenu("Todas", "/cotizaciones/todas"));
        cotizaciones.agregar(new OpcionMenu("Pendientes de validar", "/cotizaciones/pendientes"));
        cotizaciones.agregar(new OpcionMenu("Opcionadas", "/cotizaciones/opcionadas"));
        raiz.agregar(cotizaciones);
        raiz.agregar(new OpcionMenu("Órdenes de compra", "/ordenes-compra"));
        raiz.agregar(new OpcionMenu("Mi perfil", "/perfil"));
        raiz.agregar(new OpcionMenu("Cerrar sesión", "/cerrar-sesion"));
        return raiz;
    }

    @Override
    public String rol() {
        return "DIRECTOR_COMPRAS";
    }

}
