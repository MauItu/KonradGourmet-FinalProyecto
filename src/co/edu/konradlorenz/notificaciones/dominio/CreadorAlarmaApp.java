package co.edu.konradlorenz.notificaciones.dominio;

import co.edu.konradlorenz.notificaciones.puertos.salida.AlarmaPushPort;

public class CreadorAlarmaApp extends CreadorNotificacion {

    private final AlarmaPushPort canal;

    public CreadorAlarmaApp(AlarmaPushPort canal) {
        this.canal = canal;
    }

    @Override
    protected Notificacion crearNotificacion() {
        return new AlarmaApp(canal);
    }

}
