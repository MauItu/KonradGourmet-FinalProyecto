package co.edu.konradlorenz.solicitudes.puertos.entrada;

import co.edu.konradlorenz.solicitudes.dominio.CriteriosConsulta;
import co.edu.konradlorenz.solicitudes.dominio.SolicitudAlimentos;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Nota del diagrama: cotizacion.enviada y orden.generada traen la lista de solicitudes de la
 * cotización. Con cotizacion.enviada, el listener llama a registrarCotizacion() por cada una con el
 * precio sugerido de cada producto; la solicitud calcula su propio valor total y pasa a EN TRÁMITE
 * cuando todos sus productos quedan cotizados. Es idempotente por número de cotización (yaAplicada),
 * así un reenvío no suma dos veces. Con orden.generada llama a completar(), y cada solicitud pasa a
 * COMPLETADA.
 */
public interface GestionarSolicitudUseCase {

    SolicitudAlimentos registrar(SolicitudAlimentos s);

    List<SolicitudAlimentos> consultar(CriteriosConsulta c);

    SolicitudAlimentos detalle(String numero);

    void registrarCotizacion(String numero, String cotizacion, Map<Long, BigDecimal> precios);

    void completar(String numero);

}
