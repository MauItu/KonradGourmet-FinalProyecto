package co.edu.konradlorenz.integracion.adaptadores.entrada;

import co.edu.konradlorenz.integracion.puertos.entrada.ConsultarPrecioUseCase;
import jakarta.jws.WebParam;
import jakarta.jws.WebResult;
import jakarta.jws.WebService;

import java.math.BigDecimal;

/**
 * «SOAP · @WebService · /api/integracion/** (RNF integración)»
 *
 * Endpoint SOAP que ms-integracion expone, con su WSDL, a los sistemas externos. Lo publica Apache CXF
 * en /api/integracion/abastecimiento (el WSDL en …?wsdl) y el API Gateway lo deja pasar bajo
 * /api/integracion/**, con el JWT como cualquier otra ruta.
 */
@WebService(serviceName = "AbastecimientoService", portName = "AbastecimientoPort",
        targetNamespace = AbastecimientoSoapEndpoint.NAMESPACE)
public class AbastecimientoSoapEndpoint {

    public static final String NAMESPACE = "http://abastecimiento.konradgourmet.com/";

    private final ConsultarPrecioUseCase casoDeUso;

    public AbastecimientoSoapEndpoint(ConsultarPrecioUseCase casoDeUso) {
        this.casoDeUso = casoDeUso;
    }

    @WebResult(name = "precioSugerido")
    public BigDecimal consultarPrecioSugerido(@WebParam(name = "productoId") Long productoId) {
        if (productoId == null) {
            throw new IllegalArgumentException("Falta el productoId");
        }
        return casoDeUso.precioSugerido(productoId);
    }

}
