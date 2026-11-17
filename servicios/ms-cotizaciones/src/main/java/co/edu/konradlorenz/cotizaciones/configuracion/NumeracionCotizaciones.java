package co.edu.konradlorenz.cotizaciones.configuracion;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * El número de la cotización nueva (COT-018, COT-019…), de una secuencia de PostgreSQL. Se pide antes de
 * generarla, porque el PDF ya lo lleva; los pedidos y las solicitudes, que no tienen PDF, lo reciben al
 * guardarse (DEC-69, DEC-72). Fuera del diagrama, como los repositorios de Spring Data (DEC-74).
 */
@Component
public class NumeracionCotizaciones {

    private final JdbcTemplate jdbc;

    public NumeracionCotizaciones(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public String siguiente() {
        Long n = jdbc.queryForObject("SELECT nextval('cotizacion_numero')", Long.class);
        return String.format("COT-%03d", n);
    }

}
