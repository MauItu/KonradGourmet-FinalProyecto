package co.edu.konradlorenz.administracion.dominio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Nota del diagrama: ms-administracion no publica auditoria.evento: registra sus propias acciones
 * directo en su bitácora con registrarAuditoria(), sin pasar por Kafka.
 */
public class Bitacora {

    private List<RegistroAuditoria> registros = new ArrayList<>();

    public List<RegistroAuditoria> registros() {
        return Collections.unmodifiableList(registros);
    }

    public void registrar(RegistroAuditoria r) {
        registros.add(r);
    }

}
