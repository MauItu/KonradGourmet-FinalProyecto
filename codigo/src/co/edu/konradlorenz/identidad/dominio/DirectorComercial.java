package co.edu.konradlorenz.identidad.dominio;

public class DirectorComercial extends Usuario {

    public DirectorComercial(Long id, String nombre, String correo, Sucursal sucursal) {
        super(id, nombre, correo, sucursal);
    }

    /**
     * Menú del director comercial (sin mockup, HU-18): Tablero BAM, Mi perfil y Cerrar sesión.
     */
    @Override
    public ElementoMenu construirMenu() {
        GrupoMenu raiz = new GrupoMenu("Menú", "/");
        raiz.agregar(new OpcionMenu("Tablero BAM", "/bam"));
        raiz.agregar(new OpcionMenu("Mi perfil", "/perfil"));
        raiz.agregar(new OpcionMenu("Cerrar sesión", "/cerrar-sesion"));
        return raiz;
    }

    @Override
    public String rol() {
        return "DIRECTOR_COMERCIAL";
    }

}
