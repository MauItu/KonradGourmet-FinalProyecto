package co.edu.konradlorenz.administracion.dominio;

import java.util.List;

/**
 * Nota del diagrama: ms-administracion no publica auditoria.evento: registra sus propias acciones
 * directo en su bitácora con registrarAuditoria(), sin pasar por Kafka.
 */
public class Bitacora {

    private List<RegistroAuditoria> registros;

    public void registrar(RegistroAuditoria r) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

}
