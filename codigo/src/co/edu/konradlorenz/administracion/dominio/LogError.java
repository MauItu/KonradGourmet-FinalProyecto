package co.edu.konradlorenz.administracion.dominio;

import java.time.LocalDateTime;

public class LogError {

    private String servicio;
    private String mensaje;
    private LocalDateTime fechaHora;

    public LogError(String servicio, String mensaje, LocalDateTime fechaHora) {
        this.servicio = servicio;
        this.mensaje = mensaje;
        this.fechaHora = fechaHora;
    }

    public String servicio() {
        return servicio;
    }

    public String mensaje() {
        return mensaje;
    }

    public LocalDateTime fechaHora() {
        return fechaHora;
    }

}
