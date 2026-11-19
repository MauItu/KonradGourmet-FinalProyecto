package co.edu.konradlorenz.ordenes.configuracion;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * La copia local del nombre de cada producto (tabla producto_nombre), como en ms-cotizaciones (DEC-74).
 * ItemOrden guarda el id del producto; CotizacionRestClient trae el nombre con la cotización y lo deja aquí,
 * y con él el PDF de la orden y V23 dicen «Res» y no «Producto 1». Fuera del diagrama, como los
 * repositorios de Spring Data.
 */
@Component
public class NombresProductos {

    private final JdbcTemplate jdbc;

    public NombresProductos(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void recordar(Map<Long, String> nombres) {
        nombres.forEach((id, nombre) -> {
            if (id != null && nombre != null && !nombre.isBlank()) {
                String limpio = nombre.trim();
                jdbc.update("INSERT INTO producto_nombre (producto_id, nombre) VALUES (?, ?) "
                        + "ON CONFLICT (producto_id) DO UPDATE SET nombre = EXCLUDED.nombre",
                        id, limpio.length() > 80 ? limpio.substring(0, 80) : limpio);
            }
        });
    }

    /** Todos los nombres guardados (son pocos: los productos que ya se compraron). */
    public Map<Long, String> todos() {
        Map<Long, String> nombres = new HashMap<>();
        jdbc.query("SELECT producto_id, nombre FROM producto_nombre", r -> {
            nombres.put(r.getLong(1), r.getString(2));
        });
        return nombres;
    }

    public static String de(Map<Long, String> nombres, Long productoId) {
        return nombres.getOrDefault(productoId, "Producto " + productoId);
    }

}
