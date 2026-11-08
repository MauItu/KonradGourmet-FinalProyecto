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

    public void validar() {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    public void editar(Proveedor datos) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    public void inactivar() {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

}
