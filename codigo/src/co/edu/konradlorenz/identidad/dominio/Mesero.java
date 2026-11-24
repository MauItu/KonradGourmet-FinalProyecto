package co.edu.konradlorenz.identidad.dominio;

public class Mesero extends Usuario {

    public Mesero(Long id, String nombre, String correo, Sucursal sucursal) {
        super(id, nombre, correo, sucursal);
    }

    /**
     * Menú del panel izquierdo de V02–V04: Inicio, Pedidos del turno, Nuevo pedido, Mi perfil y Cerrar sesión.
     */
    @Override
    public ElementoMenu construirMenu() {
        GrupoMenu raiz = new GrupoMenu("Menú", "/");
        raiz.agregar(new OpcionMenu("Inicio", "/inicio"));
        raiz.agregar(new OpcionMenu("Pedidos del turno", "/pedidos"));
        raiz.agregar(new OpcionMenu("Nuevo pedido", "/pedidos/nuevo"));
        raiz.agregar(new OpcionMenu("Mi perfil", "/perfil"));
        raiz.agregar(new OpcionMenu("Cerrar sesión", "/cerrar-sesion"));
        return raiz;
    }

    @Override
    public String rol() {
        return "MESERO";
    }

}
