package co.edu.konradlorenz.identidad.dominio;

public class Administrador extends Usuario {

    public Administrador(Long id, String nombre, String correo, Sucursal sucursal) {
        super(id, nombre, correo, sucursal);
    }

    /**
     * Menú del administrador (sin mockup): Inicio, Administración (parámetros, imagen corporativa,
     * auditoría y logs), Usuarios, Destinatarios de alertas, Mi perfil y Cerrar sesión.
     */
    @Override
    public ElementoMenu construirMenu() {
        GrupoMenu raiz = new GrupoMenu("Menú", "/");
        raiz.agregar(new OpcionMenu("Inicio", "/inicio"));
        raiz.agregar(new OpcionMenu("Administración", "/admin"));
        raiz.agregar(new OpcionMenu("Usuarios", "/usuarios"));
        raiz.agregar(new OpcionMenu("Destinatarios", "/destinatarios"));
        raiz.agregar(new OpcionMenu("Mi perfil", "/perfil"));
        raiz.agregar(new OpcionMenu("Cerrar sesión", "/cerrar-sesion"));
        return raiz;
    }

    @Override
    public String rol() {
        return "ADMINISTRADOR";
    }

}
