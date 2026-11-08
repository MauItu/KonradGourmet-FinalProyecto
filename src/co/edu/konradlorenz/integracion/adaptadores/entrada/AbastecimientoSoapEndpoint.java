package co.edu.konradlorenz.integracion.adaptadores.entrada;

import co.edu.konradlorenz.integracion.puertos.entrada.ConsultarPrecioUseCase;

import java.math.BigDecimal;

/**
 * «SOAP · @WebService · /api/integracion/** (RNF integración)»
 *
 * Endpoint SOAP que ms-integracion expone, con su WSDL, a los sistemas externos.
 * Se publica a través del API Gateway bajo /api/integracion/**.
 * En el proyecto real lleva @WebService (JAX-WS); aquí va solo en este comentario,
 * porque JAX-WS no está en el JDK y el esqueleto compila solo con javac.
 */
public class AbastecimientoSoapEndpoint {

    private final ConsultarPrecioUseCase casoDeUso;

    public AbastecimientoSoapEndpoint(ConsultarPrecioUseCase casoDeUso) {
        this.casoDeUso = casoDeUso;
    }

    public BigDecimal consultarPrecioSugerido(Long productoId) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

}
