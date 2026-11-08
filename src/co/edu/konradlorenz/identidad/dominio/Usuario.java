package co.edu.konradlorenz.identidad.dominio;

/**
 * «Factory Method · creador»
 */
public abstract class Usuario {

    protected Long id;
    protected String nombre;
    protected String correo;
    protected String passwordHash;
    protected Sucursal sucursal;

    public abstract ElementoMenu construirMenu();

    public abstract String rol();

    public void asignarPasswordHash(String hash) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

}
