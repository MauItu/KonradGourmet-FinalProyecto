package co.edu.konradlorenz.administracion.dominio;

import java.time.LocalDate;
import java.time.LocalTime;

public class RegistroAuditoria {

    private String accion;
    private String usuario;
    private LocalDate fecha;
    private LocalTime hora;

    public RegistroAuditoria(String accion, String usuario, LocalDate fecha, LocalTime hora) {
        this.accion = accion;
        this.usuario = usuario;
        this.fecha = fecha;
        this.hora = hora;
    }

    public String accion() {
        return accion;
    }

    public String usuario() {
        return usuario;
    }

    public LocalDate fecha() {
        return fecha;
    }

    public LocalTime hora() {
        return hora;
    }

}
