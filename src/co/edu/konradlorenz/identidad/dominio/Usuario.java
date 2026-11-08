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

    protected Usuario(Long id, String nombre, String correo, Sucursal sucursal) {
        this.id = id;
        this.nombre = nombre;
        this.correo = correo;
        this.sucursal = sucursal;
    }

    public Long id() {
        return id;
    }

    public String nombre() {
        return nombre;
    }

    public String correo() {
        return correo;
    }

    public String passwordHash() {
        return passwordHash;
    }

    public Sucursal sucursal() {
        return sucursal;
    }

    public abstract ElementoMenu construirMenu();

    public abstract String rol();

    public void asignarPasswordHash(String hash) {
        this.passwordHash = hash;
    }

}
