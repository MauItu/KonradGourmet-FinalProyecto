package co.edu.konradlorenz.notificaciones.puertos.salida;

import java.time.LocalDateTime;

public interface EnvioRepositoryPort {

    void registrar(String destino, String tipo, LocalDateTime fechaHora);

    void registrarFallo(String destino, String tipo, String error);

}
