package co.edu.konradlorenz.identidad.dominio;

public class DirectorComercial extends Usuario {

    public DirectorComercial(Long id, String nombre, String correo, Sucursal sucursal) {
        super(id, nombre, correo, sucursal);
    }

    @Override
    public ElementoMenu construirMenu() {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    @Override
    public String rol() {
        return "DIRECTOR_COMERCIAL";
    }

}
