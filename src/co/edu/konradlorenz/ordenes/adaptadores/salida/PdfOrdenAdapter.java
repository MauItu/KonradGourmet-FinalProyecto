package co.edu.konradlorenz.ordenes.adaptadores.salida;

import co.edu.konradlorenz.ordenes.dominio.OrdenCompra;
import co.edu.konradlorenz.ordenes.puertos.salida.GeneradorPdfPort;

/**
 * «OpenPDF»
 *
 * Nota del diagrama: El PDF de la orden (HU-14) se genera en ms-ordenes y se guarda en el almacén de
 * objetos. orden.generada lleva pdfUrl, correoProveedor y cotizacionNumero: ms-notificaciones lo envía
 * por correo certificado al proveedor, y ms-cotizaciones pasa la cotización a CON_ORDEN.
 */
public class PdfOrdenAdapter implements GeneradorPdfPort {

    @Override
    public byte[] generar(OrdenCompra o) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

}
