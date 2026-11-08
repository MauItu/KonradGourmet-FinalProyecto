package co.edu.konradlorenz.demo;

import co.edu.konradlorenz.cotizaciones.dominio.Cotizacion;
import co.edu.konradlorenz.cotizaciones.dominio.ItemCotizacion;
import co.edu.konradlorenz.ordenes.dominio.ItemOrden;
import co.edu.konradlorenz.ordenes.dominio.OrdenCompra;

import java.nio.charset.StandardCharsets;

/**
 * Hace las veces de OpenPDF: en vez de un PDF, genera el texto del documento. Sirve para las dos
 * interfaces GeneradorPdfPort, la de cotizaciones y la de órdenes.
 */
public class PdfSimulado implements
        co.edu.konradlorenz.cotizaciones.puertos.salida.GeneradorPdfPort,
        co.edu.konradlorenz.ordenes.puertos.salida.GeneradorPdfPort {

    @Override
    public byte[] generar(Cotizacion c) {
        StringBuilder sb = new StringBuilder("SOLICITUD DE COTIZACIÓN " + c.numero() + "\n");
        sb.append("Proveedor: ").append(c.proveedor().razonSocial()).append('\n');
        sb.append("Solicitudes: ").append(String.join(", ", c.solicitudNumeros())).append('\n');
        for (ItemCotizacion i : c.items()) {
            sb.append("- producto ").append(i.productoId()).append(": ").append(i.cantidad().toPlainString())
                    .append(' ').append(i.unidad()).append('\n');
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public byte[] generar(OrdenCompra o) {
        StringBuilder sb = new StringBuilder("ORDEN DE COMPRA " + o.numero() + "\n");
        sb.append("Cotización: ").append(o.cotizacionNumero()).append('\n');
        sb.append("Proveedor: ").append(o.proveedor()).append('\n');
        for (ItemOrden i : o.items()) {
            sb.append("- producto ").append(i.productoId()).append(": ").append(i.cantidad().toPlainString())
                    .append(' ').append(i.unidad()).append(" a ").append(i.precio().toPlainString()).append('\n');
        }
        sb.append("Total: ").append(o.total().toPlainString()).append('\n');
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

}
