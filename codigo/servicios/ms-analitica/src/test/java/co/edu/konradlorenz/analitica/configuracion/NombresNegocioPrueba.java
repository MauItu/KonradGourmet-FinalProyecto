package co.edu.konradlorenz.analitica.configuracion;

import co.edu.konradlorenz.menu.eventos.MenuActualizadoEvent;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Arma NombresNegocio con un JdbcTemplate simulado y una espera corta (el constructor es del paquete). */
public final class NombresNegocioPrueba {

    private NombresNegocioPrueba() {
    }

    public static List<String> nombres() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        when(jdbc.queryForList("SELECT nombre FROM plato_nombre WHERE plato_id = ?", String.class, 2L)).thenReturn(List.of("Ajiaco"));
        when(jdbc.queryForList("SELECT nombre FROM plato_nombre WHERE plato_id = ?", String.class, 9L)).thenReturn(List.of());
        when(jdbc.queryForList("SELECT nombre FROM sucursal WHERE id = ?", String.class, 2L)).thenReturn(List.of("Sucursal Norte"));
        when(jdbc.queryForList("SELECT nombre FROM sucursal WHERE id = ?", String.class, 7L)).thenReturn(List.of());
        NombresNegocio nombres = new NombresNegocio(jdbc, 600);
        nombres.alRecibir(new MenuActualizadoEvent("carlos.mendoza@konradgourmet.com", 2L, 1L, "Ajiaco", "Sopas", new BigDecimal("18000"), true, Map.of()));
        verify(jdbc).update(eq("INSERT INTO plato_nombre (plato_id, nombre) VALUES (?, ?) ON CONFLICT (plato_id) DO UPDATE SET nombre = EXCLUDED.nombre"),
                eq(2L), eq("Ajiaco"));
        return List.of(nombres.plato(2L), nombres.plato(9L), nombres.sucursal(2L), nombres.sucursal(7L));
    }

}
