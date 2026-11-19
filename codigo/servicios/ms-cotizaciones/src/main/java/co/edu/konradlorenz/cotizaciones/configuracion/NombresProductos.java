package co.edu.konradlorenz.cotizaciones.configuracion;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * La copia local del nombre de cada producto (tabla producto_nombre, DEC-74). Los nombres los pone la SPA
 * al generar la cotización, tal como los leyó de ms-menu; con ellos el PDF para el proveedor y las
 * ventanas del director (que no lee ms-menu) muestran «Res» y no «Producto 1». Un producto sin nombre
 * guardado se muestra como «Producto N».
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

    /** Todos los nombres guardados (son pocos: los productos del catálogo que ya se cotizaron). */
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
