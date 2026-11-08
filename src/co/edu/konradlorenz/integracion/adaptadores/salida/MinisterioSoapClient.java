package co.edu.konradlorenz.integracion.adaptadores.salida;

/**
 * «Adaptee · generado del WSDL»
 *
 * En el proyecto real lo genera wsimport (o Apache CXF) a partir del WSDL del Ministerio;
 * aquí es un esqueleto escrito a mano.
 */
public class MinisterioSoapClient {

    private final String endpoint;

    public MinisterioSoapClient(String endpoint) {
        this.endpoint = endpoint;
    }

    public double consultarPrecioEstandar(String codigoProducto) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

}
