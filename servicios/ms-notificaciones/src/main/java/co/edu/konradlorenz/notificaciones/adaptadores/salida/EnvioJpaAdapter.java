package co.edu.konradlorenz.notificaciones.adaptadores.salida;

import co.edu.konradlorenz.compartido.infraestructura.ContextoRecorrido;
import co.edu.konradlorenz.notificaciones.puertos.salida.EnvioRepositoryPort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Nota del diagrama: Si el correo falla, registrarFallo() deja la traza y el evento pasa a una cola de
 * errores (DLQ) para reintentarlo; el auxiliar también puede reenviar la cotización (HU-12).
 *
 * Cada fila guarda además la correlación del recorrido (DEC-64), la del evento que originó el envío: con
 * ella V14 sabe si el correo de su cotización ya salió (DEC-76). La firma del puerto no cambia.
 */
@Repository
public class EnvioJpaAdapter implements EnvioRepositoryPort {

    private static final int LARGO_ERROR = 500;

    private final EnvioJpaRepository envios;

    public EnvioJpaAdapter(EnvioJpaRepository envios) {
        this.envios = envios;
    }

    @Override
    @Transactional
    public void registrar(String destino, String tipo, LocalDateTime fechaHora) {
        guardar(destino, tipo, fechaHora, true, null);
    }

    @Override
    @Transactional
    public void registrarFallo(String destino, String tipo, String error) {
        String texto = error == null ? "Error sin mensaje" : error;
        guardar(destino, tipo, LocalDateTime.now(), false, texto.length() > LARGO_ERROR ? texto.substring(0, LARGO_ERROR) : texto);
    }

    private void guardar(String destino, String tipo, LocalDateTime fechaHora, boolean exito, String error) {
        EnvioEntidad e = new EnvioEntidad();
        e.destino = destino == null ? "—" : destino;
        e.tipo = tipo;
        e.fechaHora = fechaHora;
        e.exito = exito;
        e.error = error;
        ContextoRecorrido.Datos contexto = ContextoRecorrido.actual();
        UUID correlacion = contexto == null ? null : contexto.correlacionId();
        e.correlacionId = correlacion;
        envios.save(e);
    }

}
