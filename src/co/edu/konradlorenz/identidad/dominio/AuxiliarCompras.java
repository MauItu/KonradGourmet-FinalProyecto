package co.edu.konradlorenz.identidad.dominio;

public class AuxiliarCompras extends Usuario {

    public AuxiliarCompras(Long id, String nombre, String correo, Sucursal sucursal) {
        super(id, nombre, correo, sucursal);
    }

    /**
     * Menú del panel izquierdo de V11–V15: Inicio, Solicitudes (Todas las solicitudes), Cotizaciones
     * (Generar cotización, Cotizaciones enviadas), Proveedores (HU-15), Mi perfil y Cerrar sesión.
     */
    @Override
    public ElementoMenu construirMenu() {
        GrupoMenu raiz = new GrupoMenu("Menú", "/");
        raiz.agregar(new OpcionMenu("Inicio", "/inicio"));
        GrupoMenu solicitudes = new GrupoMenu("Solicitudes", "/solicitudes");
        solicitudes.agregar(new OpcionMenu("Todas las solicitudes", "/solicitudes"));
        raiz.agregar(solicitudes);
        GrupoMenu cotizaciones = new GrupoMenu("Cotizaciones", "/cotizaciones");
        cotizaciones.agregar(new OpcionMenu("Generar cotización", "/cotizaciones/nueva"));
        cotizaciones.agregar(new OpcionMenu("Cotizaciones enviadas", "/cotizaciones"));
        raiz.agregar(cotizaciones);
        raiz.agregar(new OpcionMenu("Proveedores", "/proveedores"));
        raiz.agregar(new OpcionMenu("Mi perfil", "/perfil"));
        raiz.agregar(new OpcionMenu("Cerrar sesión", "/cerrar-sesion"));
        return raiz;
    }

    @Override
    public String rol() {
        return "AUXILIAR_COMPRAS";
    }

}
