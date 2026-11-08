package co.edu.konradlorenz.notificaciones.dominio;

import co.edu.konradlorenz.notificaciones.puertos.salida.AlarmaPushPort;

public class AlarmaApp implements Notificacion {

    private final AlarmaPushPort canal;

    public AlarmaApp(AlarmaPushPort canal) {
        this.canal = canal;
    }

    @Override
    public void enviar(String destino, String contenido) {
        canal.empujar(destino, contenido);
    }

}
