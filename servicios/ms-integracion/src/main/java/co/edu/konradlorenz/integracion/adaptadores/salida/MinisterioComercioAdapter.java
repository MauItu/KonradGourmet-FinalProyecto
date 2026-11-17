package co.edu.konradlorenz.integracion.adaptadores.salida;

import co.edu.konradlorenz.integracion.dominio.PrecioEstandar;
import co.edu.konradlorenz.integracion.puertos.salida.PrecioReferenciaPort;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * «Adapter»
 *
 * Traduce el servicio web SOAP del Ministerio de Comercio (MinisterioSoapClient, el Adaptee) al puerto
 * PrecioReferenciaPort. No guarda nada: la caché la pone PrecioReferenciaCacheProxy, que lo envuelve.
 */
@Component
public class MinisterioComercioAdapter implements PrecioReferenciaPort {

    private final MinisterioSoapClient cliente;

    public MinisterioComercioAdapter(MinisterioSoapClient cliente) {
        this.cliente = cliente;
    }

    @Override
    public PrecioEstandar precioEstandar(Long productoId) {
        // el Ministerio identifica el producto con un código de texto y responde un double: aquí se traduce al puerto
        double valor = cliente.consultarPrecioEstandar(String.valueOf(productoId));
        return new PrecioEstandar(productoId, BigDecimal.valueOf(valor), LocalDateTime.now());
    }

}
