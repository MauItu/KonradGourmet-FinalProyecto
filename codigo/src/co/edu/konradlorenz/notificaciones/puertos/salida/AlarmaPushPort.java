package co.edu.konradlorenz.notificaciones.puertos.salida;

public interface AlarmaPushPort {

    void empujar(String destino, String contenido);

}
