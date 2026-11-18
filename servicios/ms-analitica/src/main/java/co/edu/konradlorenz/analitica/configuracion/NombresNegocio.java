package co.edu.konradlorenz.analitica.configuracion;

import co.edu.konradlorenz.compartido.eventos.EventoDominio;
import co.edu.konradlorenz.menu.eventos.MenuActualizadoEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * La copia local de los nombres que los eventos de negocio no traen (DEC-90): pedido.registrado dice el id del plato
 * y de la sucursal, y el tablero muestra «Bandeja paisa» y «Sucursal Centro». Los platos se copian de
 * menu.actualizado, como hace ms-pedidos con su catálogo (tabla plato_nombre); las sucursales vienen de la carga
 * inicial (supuesto S10, tabla sucursal). Fuera del diagrama, como los repositorios de Spring Data.
 */
@Component
public class NombresNegocio {

    private static final Logger LOG = LoggerFactory.getLogger(NombresNegocio.class);

    private final JdbcTemplate jdbc;
    private final long esperaMs;

    public NombresNegocio(JdbcTemplate jdbc) {
        this(jdbc, 10_000);
    }

    NombresNegocio(JdbcTemplate jdbc, long esperaMs) {
        this.jdbc = jdbc;
        this.esperaMs = esperaMs;
    }

    @KafkaListener(topics = "${konrad.kafka.prefijo-topico:}menu.actualizado")
    public void alRecibir(EventoDominio evento) {
        if (evento instanceof MenuActualizadoEvent m && m.nombre() != null && !m.nombre().isBlank()) {
            jdbc.update("INSERT INTO plato_nombre (plato_id, nombre) VALUES (?, ?) "
                    + "ON CONFLICT (plato_id) DO UPDATE SET nombre = EXCLUDED.nombre", m.platoId(), m.nombre().trim());
        }
    }

    /**
     * El nombre del plato. Los dos tópicos se leen en paralelo: si el pedido llega antes que el plato (por ejemplo,
     * cuando el grupo de consumo es nuevo y lee todo desde el principio), espera hasta 10 s a que llegue su
     * menu.actualizado; si no llega, usa «Plato N».
     */
    public String plato(Long platoId) {
        long hasta = System.currentTimeMillis() + esperaMs;
        while (true) {
            List<String> nombre = jdbc.queryForList("SELECT nombre FROM plato_nombre WHERE plato_id = ?", String.class, platoId);
            if (!nombre.isEmpty()) {
                return nombre.get(0);
            }
            if (System.currentTimeMillis() >= hasta) {
                LOG.warn("No se conoce el nombre del plato {}: el hecho queda como «Plato {}»", platoId, platoId);
                return "Plato " + platoId;
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return "Plato " + platoId;
            }
        }
    }

    public String sucursal(Long sucursalId) {
        List<String> nombre = jdbc.queryForList("SELECT nombre FROM sucursal WHERE id = ?", String.class, sucursalId);
        return nombre.isEmpty() ? "Sucursal " + sucursalId : nombre.get(0);
    }

}
