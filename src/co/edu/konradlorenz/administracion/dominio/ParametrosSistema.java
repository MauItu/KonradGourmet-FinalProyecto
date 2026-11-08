package co.edu.konradlorenz.administracion.dominio;

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
    private Map<String, String> valores;

    private ParametrosSistema() {
        // Singleton: solo getInstancia() crea la instancia.
    }

    public static ParametrosSistema getInstancia() {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    public String valor(String clave) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    public void actualizar(String clave, String valor) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

}
