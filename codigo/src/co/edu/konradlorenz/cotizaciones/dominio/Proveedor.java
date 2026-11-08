package co.edu.konradlorenz.cotizaciones.dominio;

import java.util.List;

public class Proveedor {

    private Long id;
    private String razonSocial;
    private String nit;
    private String correo;
    private String telefono;
    private String direccion;
    private List<String> categorias;
    private boolean activo;

    /** Un proveedor nuevo nace activo. */
    public Proveedor(Long id, String razonSocial, String nit, String correo, String telefono, String direccion, List<String> categorias) {
        this.id = id;
        this.razonSocial = razonSocial;
        this.nit = nit;
        this.correo = correo;
        this.telefono = telefono;
        this.direccion = direccion;
        this.categorias = categorias;
        this.activo = true;
    }

    public Long id() {
        return id;
    }

    public String razonSocial() {
        return razonSocial;
    }

    public String nit() {
        return nit;
    }

    public String correo() {
        return correo;
    }

    public String telefono() {
        return telefono;
    }

    public String direccion() {
        return direccion;
    }

    public List<String> categorias() {
        return categorias;
    }

    public boolean activo() {
        return activo;
    }

    /** Exige razón social, NIT y un correo con forma de correo (HU-15). */
    public void validar() {
        if (razonSocial == null || razonSocial.isBlank()) {
            throw new IllegalStateException("La razón social es obligatoria");
        }
        if (nit == null || nit.isBlank()) {
            throw new IllegalStateException("El NIT es obligatorio");
        }
        if (correo == null || !correo.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
            throw new IllegalStateException("El correo del proveedor no es válido");
        }
    }

    /** Copia los datos editables; el id y el estado no cambian. */
    public void editar(Proveedor datos) {
        this.razonSocial = datos.razonSocial;
        this.nit = datos.nit;
        this.correo = datos.correo;
        this.telefono = datos.telefono;
        this.direccion = datos.direccion;
        this.categorias = datos.categorias;
    }

    /** Inactivar no borra: el proveedor deja de salir en el desplegable de V13. */
    public void inactivar() {
        this.activo = false;
    }

}
