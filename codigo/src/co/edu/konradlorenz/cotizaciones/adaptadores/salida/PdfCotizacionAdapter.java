package co.edu.konradlorenz.cotizaciones.adaptadores.salida;

import co.edu.konradlorenz.cotizaciones.dominio.Cotizacion;
import co.edu.konradlorenz.cotizaciones.puertos.salida.GeneradorPdfPort;

/**
 * «OpenPDF»
 *
 * En el proyecto real: @Component.
 */
public class PdfCotizacionAdapter implements GeneradorPdfPort {

    @Override
    public byte[] generar(Cotizacion c) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

}
