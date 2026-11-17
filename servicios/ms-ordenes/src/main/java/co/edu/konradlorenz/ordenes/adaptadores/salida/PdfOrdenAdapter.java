package co.edu.konradlorenz.ordenes.adaptadores.salida;

import co.edu.konradlorenz.ordenes.configuracion.NombresProductos;
import co.edu.konradlorenz.ordenes.dominio.ItemOrden;
import co.edu.konradlorenz.ordenes.dominio.OrdenCompra;
import co.edu.konradlorenz.ordenes.puertos.salida.GeneradorPdfPort;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;

/**
 * «OpenPDF»
 *
 * La orden de compra que recibe el proveedor (HU-14): el número, la fecha, el proveedor, la cotización y
 * las solicitudes de origen, y los productos con la cantidad, la unidad, el precio por unidad que cotizó y
 * el valor de cada línea, con el total. Mismo formato que la solicitud de cotización (DEC-73). Los nombres
 * de los productos salen de la copia local de ms-ordenes (NombresProductos).
 */
@Component
public class PdfOrdenAdapter implements GeneradorPdfPort {

    private static final Color NARANJA = new Color(0xF2, 0x87, 0x05);
    private static final Color DURAZNO = new Color(0xFF, 0xF4, 0xE8);
    private static final Color LINEA = new Color(0xE8, 0xE0, 0xD8);
    private static final Color TEXTO = new Color(0x2E, 0x2A, 0x26);
    private static final Color SUAVE = new Color(0x8A, 0x84, 0x7E);
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Locale CO = Locale.forLanguageTag("es-CO");

    private final NombresProductos nombres;

    public PdfOrdenAdapter(NombresProductos nombres) {
        this.nombres = nombres;
    }

    @Override
    public byte[] generar(OrdenCompra o) {
        Map<Long, String> productos = nombres.todos();
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.LETTER, 48, 48, 48, 48);
        PdfWriter.getInstance(doc, salida);
        doc.addTitle("Orden de compra " + o.numero());
        doc.addAuthor("Konrad Gourmet · Dirección de Compras");
        doc.addSubject("Orden de compra para " + o.proveedor());
        doc.open();

        // encabezado: la marca a la izquierda y el número a la derecha
        PdfPTable cabeza = new PdfPTable(new float[]{3, 2});
        cabeza.setWidthPercentage(100);
        Paragraph nombre = new Paragraph();
        nombre.add(new Chunk("Konrad ", fuente(20, Font.BOLD, TEXTO)));
        nombre.add(new Chunk("Gourmet", fuente(20, Font.BOLD, NARANJA)));
        PdfPCell marca = sinBorde(new Phrase());
        marca.addElement(nombre);
        marca.addElement(new Paragraph("Sistema de Abastecimiento · Dirección de Compras", fuente(9, Font.NORMAL, SUAVE)));
        cabeza.addCell(marca);
        PdfPCell numero = sinBorde(new Phrase());
        Paragraph titulo = new Paragraph("ORDEN DE COMPRA", fuente(10, Font.BOLD, SUAVE));
        titulo.setAlignment(Element.ALIGN_RIGHT);
        Paragraph codigo = new Paragraph(o.numero(), fuente(22, Font.BOLD, NARANJA));
        codigo.setAlignment(Element.ALIGN_RIGHT);
        numero.addElement(titulo);
        numero.addElement(codigo);
        cabeza.addCell(numero);
        doc.add(cabeza);
        doc.add(raya());

        // datos de la orden
        PdfPTable datos = new PdfPTable(new float[]{1.3f, 3, 1.4f, 2});
        datos.setWidthPercentage(100);
        datos.setSpacingBefore(10);
        dato(datos, "Proveedor:", o.proveedor());
        dato(datos, "Fecha:", o.fecha().format(FECHA));
        dato(datos, "Correo:", o.correoProveedor());
        dato(datos, "Cotización:", o.cotizacionNumero());
        dato(datos, o.solicitudNumeros().size() > 1 ? "Solicitudes:" : "Solicitud:", String.join(", ", o.solicitudNumeros()));
        dato(datos, "Estado:", o.estado() == null ? "—" : o.estado().name().replace('_', ' '));
        doc.add(datos);

        Paragraph pide = new Paragraph("Konrad Gourmet le compra los siguientes productos, a los precios por unidad de su cotización "
                + o.cotizacionNumero() + " (en pesos colombianos, con impuestos incluidos).", fuente(10, Font.NORMAL, TEXTO));
        pide.setSpacingBefore(14);
        pide.setSpacingAfter(8);
        doc.add(pide);

        PdfPTable tabla = new PdfPTable(new float[]{0.5f, 2.6f, 1.2f, 0.9f, 1.6f, 1.8f});
        tabla.setWidthPercentage(100);
        tabla.setHeaderRows(1);
        for (String h : new String[]{"#", "Producto", "Cantidad", "Unidad", "Precio por unidad", "Valor"}) {
            PdfPCell celda = new PdfPCell(new Phrase(h, fuente(9, Font.BOLD, Color.WHITE)));
            celda.setBackgroundColor(NARANJA);
            celda.setBorderColor(NARANJA);
            celda.setPadding(6);
            tabla.addCell(celda);
        }
        int k = 1;
        for (ItemOrden i : o.items()) {
            Color fondo = k % 2 == 0 ? DURAZNO : Color.WHITE;
            tabla.addCell(fila(String.valueOf(k), fondo, Element.ALIGN_CENTER));
            tabla.addCell(fila(NombresProductos.de(productos, i.productoId()), fondo, Element.ALIGN_LEFT));
            tabla.addCell(fila(cantidad(i.cantidad()), fondo, Element.ALIGN_RIGHT));
            tabla.addCell(fila(i.unidad(), fondo, Element.ALIGN_LEFT));
            tabla.addCell(fila(pesos(i.precio()) + " / " + i.unidad(), fondo, Element.ALIGN_RIGHT));
            tabla.addCell(fila(pesos(i.precio().multiply(i.cantidad())), fondo, Element.ALIGN_RIGHT));
            k++;
        }
        PdfPCell etiqueta = new PdfPCell(new Phrase("TOTAL", fuente(10, Font.BOLD, TEXTO)));
        etiqueta.setColspan(5);
        etiqueta.setHorizontalAlignment(Element.ALIGN_RIGHT);
        etiqueta.setBackgroundColor(DURAZNO);
        etiqueta.setBorderColor(LINEA);
        etiqueta.setPadding(7);
        tabla.addCell(etiqueta);
        PdfPCell total = new PdfPCell(new Phrase(pesos(o.total()), fuente(11, Font.BOLD, NARANJA)));
        total.setHorizontalAlignment(Element.ALIGN_RIGHT);
        total.setBackgroundColor(DURAZNO);
        total.setBorderColor(LINEA);
        total.setPadding(7);
        tabla.addCell(total);
        doc.add(tabla);

        Paragraph nota = new Paragraph();
        nota.setSpacingBefore(18);
        nota.add(new Chunk("Confirmación. ", fuente(10, Font.BOLD, TEXTO)));
        nota.add(new Chunk("Responda este correo citando la orden " + o.numero() + " para confirmar que la recibió y acordar la entrega "
                + "con el área de compras. La orden se generó sobre la cotización " + o.cotizacionNumero()
                + ", validada contra el precio de referencia del Ministerio de Comercio.", fuente(10, Font.NORMAL, TEXTO)));
        doc.add(nota);

        doc.add(raya());
        Paragraph pie = new Paragraph("Konrad Gourmet · Documento generado por el Sistema de Abastecimiento · " + o.numero(),
                fuente(8, Font.NORMAL, SUAVE));
        pie.setAlignment(Element.ALIGN_CENTER);
        doc.add(pie);
        doc.close();
        return salida.toByteArray();
    }

    private static Font fuente(float tamano, int estilo, Color color) {
        return new Font(Font.HELVETICA, tamano, estilo, color);
    }

    private static PdfPCell sinBorde(Phrase contenido) {
        PdfPCell celda = new PdfPCell(contenido);
        celda.setBorder(Rectangle.NO_BORDER);
        celda.setPadding(0);
        return celda;
    }

    private static void dato(PdfPTable t, String etiqueta, String valor) {
        PdfPCell e = sinBorde(new Phrase(etiqueta, fuente(9, Font.NORMAL, SUAVE)));
        e.setPaddingBottom(5);
        PdfPCell v = sinBorde(new Phrase(valor, fuente(9, Font.BOLD, TEXTO)));
        v.setPaddingBottom(5);
        t.addCell(e);
        t.addCell(v);
    }

    private static PdfPCell fila(String texto, Color fondo, int alineacion) {
        PdfPCell celda = new PdfPCell(new Phrase(texto, fuente(10, Font.NORMAL, TEXTO)));
        celda.setBackgroundColor(fondo);
        celda.setBorderColor(LINEA);
        celda.setPadding(6);
        celda.setHorizontalAlignment(alineacion);
        return celda;
    }

    /** Una raya naranja: OpenPDF pide bordes variables para colorear uno solo. */
    private static Paragraph raya() {
        PdfPTable linea = new PdfPTable(1);
        linea.setWidthPercentage(100);
        PdfPCell celda = new PdfPCell(new Phrase(" ", fuente(2, Font.NORMAL, Color.WHITE)));
        celda.setBorder(Rectangle.BOTTOM);
        celda.setUseVariableBorders(true);
        celda.setBorderColorBottom(NARANJA);
        celda.setBorderWidthBottom(1.5f);
        celda.setPadding(0);
        linea.addCell(celda);
        Paragraph p = new Paragraph();
        p.setSpacingBefore(6);
        p.add(linea);
        return p;
    }

    private static String cantidad(BigDecimal v) {
        return new DecimalFormat("#,##0.###", DecimalFormatSymbols.getInstance(CO)).format(v);
    }

    private static String pesos(BigDecimal v) {
        return "$" + new DecimalFormat("#,##0", DecimalFormatSymbols.getInstance(CO)).format(v);
    }

}
