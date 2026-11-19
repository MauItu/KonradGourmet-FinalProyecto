package co.edu.konradlorenz.administracion.adaptadores.salida;

import co.edu.konradlorenz.administracion.puertos.salida.ParametrosRepositoryPort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.TreeMap;

/** Los parámetros del sistema en ms_administracion; ParametrosSistema es su caché en cada réplica (n_parametros). */
@Repository
public class ParametrosJpaAdapter implements ParametrosRepositoryPort {

    private final ParametroJpaRepository parametros;

    public ParametrosJpaAdapter(ParametroJpaRepository parametros) {
        this.parametros = parametros;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, String> cargar() {
        Map<String, String> todos = new TreeMap<>();
        parametros.findAll().forEach(p -> todos.put(p.clave, p.valor));
        return todos;
    }

    /** Crea el parámetro o reemplaza su valor. */
    @Override
    @Transactional
    public void guardar(String clave, String valor) {
        ParametroEntidad p = parametros.findById(clave).orElseGet(ParametroEntidad::new);
        p.clave = clave;
        p.valor = valor;
        p.actualizado = LocalDateTime.now();
        parametros.save(p);
    }

}
