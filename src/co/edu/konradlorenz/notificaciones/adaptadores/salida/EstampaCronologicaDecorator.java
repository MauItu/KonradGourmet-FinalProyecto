package co.edu.konradlorenz.notificaciones.adaptadores.salida;

import co.edu.konradlorenz.notificaciones.dominio.Mensaje;
import co.edu.konradlorenz.notificaciones.puertos.salida.Correo;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

/**
 * «Decorator concreto»
 */
public class EstampaCronologicaDecorator extends CorreoDecorator {

    public EstampaCronologicaDecorator(Correo envuelto) {
        super(envuelto);
    }

    @Override
    public void enviar(Mensaje m) {
        m.cabeceras().put("X-Sello-Tiempo", ZonedDateTime.now(ZoneId.of("America/Bogota")).truncatedTo(ChronoUnit.SECONDS).toString());
        super.enviar(m);
    }

}
