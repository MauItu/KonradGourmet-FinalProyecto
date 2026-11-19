package co.edu.konradlorenz.identidad.dominio;

public class JefeCocina extends Usuario {

    public JefeCocina(Long id, String nombre, String correo, Sucursal sucursal) {
        super(id, nombre, correo, sucursal);
    }

    /**
     * Menú del panel izquierdo de V05–V10: Inicio, Platos (Mis platos, Nuevo plato), Inventario,
     * Solicitudes (Nueva solicitud, Mis solicitudes), Promociones (HU-19), Mi perfil y Cerrar sesión.
     */
    @Override
    public ElementoMenu construirMenu() {
        GrupoMenu raiz = new GrupoMenu("Menú", "/");
        raiz.agregar(new OpcionMenu("Inicio", "/inicio"));
        GrupoMenu platos = new GrupoMenu("Platos", "/platos");
        platos.agregar(new OpcionMenu("Mis platos", "/platos"));
        platos.agregar(new OpcionMenu("Nuevo plato", "/platos/nuevo"));
        raiz.agregar(platos);
        raiz.agregar(new OpcionMenu("Inventario", "/inventario"));
        GrupoMenu solicitudes = new GrupoMenu("Solicitudes", "/solicitudes");
        solicitudes.agregar(new OpcionMenu("Nueva solicitud", "/solicitudes/nueva"));
        solicitudes.agregar(new OpcionMenu("Mis solicitudes", "/solicitudes"));
        raiz.agregar(solicitudes);
        raiz.agregar(new OpcionMenu("Promociones", "/promociones"));
        raiz.agregar(new OpcionMenu("Mi perfil", "/perfil"));
        raiz.agregar(new OpcionMenu("Cerrar sesión", "/cerrar-sesion"));
        return raiz;
    }

    @Override
    public String rol() {
        return "JEFE_COCINA";
    }

}
