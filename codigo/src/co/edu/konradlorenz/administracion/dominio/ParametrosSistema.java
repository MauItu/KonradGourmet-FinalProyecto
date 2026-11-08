package co.edu.konradlorenz.administracion.dominio;

import java.util.HashMap;
import java.util.Map;

/**
 * «Singleton»
 *
 * Nota del diagrama: Singleton por réplica: es la caché de los parámetros que se guardan en la base
 * (ParametrosRepositoryPort), y cada réplica la refresca de la base cada minuto. Guarda el tema, los
 * logos y demás parámetros generales; la SPA los lee al iniciar con GET /api/admin/parametros/tema. El
 * umbral del 25 % no vive aquí: es de ms-inventario (4b).
 */
public class ParametrosSistema {

    private static ParametrosSistema instancia;
    private Map<String, String> valores = new HashMap<>();

    private ParametrosSistema() {
        // Singleton: solo getInstancia() crea la instancia.
    }

    public static synchronized ParametrosSistema getInstancia() {
        if (instancia == null) {
            instancia = new ParametrosSistema();
        }
        return instancia;
    }

    /** Copia de solo lectura de todos los parámetros. */
    public synchronized Map<String, String> valores() {
        return Map.copyOf(valores);
    }

    public synchronized String valor(String clave) {
        return valores.get(clave);
    }

    public synchronized void actualizar(String clave, String valor) {
        valores.put(clave, valor);
    }

}
