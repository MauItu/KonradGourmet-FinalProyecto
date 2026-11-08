package co.edu.konradlorenz.demo;

import co.edu.konradlorenz.notificaciones.puertos.salida.EnvioRepositoryPort;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** La traza de envíos y fallos en listas de texto, en vez de la tabla de ms-notificaciones. */
public class EnvioEnMemoria implements EnvioRepositoryPort {

    private final List<String> envios = new ArrayList<>();
    private final List<String> fallos = new ArrayList<>();

    @Override
    public void registrar(String destino, String tipo, LocalDateTime fechaHora) {
        envios.add(tipo + " → " + destino);
    }

    @Override
    public void registrarFallo(String destino, String tipo, String error) {
        fallos.add(tipo + " → " + destino + ": " + error);
    }

    public List<String> envios() {
        return List.copyOf(envios);
    }

    public List<String> fallos() {
        return List.copyOf(fallos);
    }

}
