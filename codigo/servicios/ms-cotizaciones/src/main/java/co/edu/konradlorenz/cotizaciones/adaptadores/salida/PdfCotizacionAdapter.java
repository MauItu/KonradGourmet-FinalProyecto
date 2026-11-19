package co.edu.konradlorenz.cotizaciones.adaptadores.salida;

import co.edu.konradlorenz.cotizaciones.configuracion.NombresProductos;
import co.edu.konradlorenz.cotizaciones.dominio.Cotizacion;
import co.edu.konradlorenz.cotizaciones.dominio.ItemCotizacion;
import co.edu.konradlorenz.cotizaciones.dominio.Proveedor;
import co.edu.konradlorenz.cotizaciones.puertos.salida.GeneradorPdfPort;
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
 * La solicitud de cotización que recibe el proveedor: quién la pide, el proveedor, las solicitudes de
 * origen, la fecha límite y los productos con su cantidad, y una columna vacía para el precio por unidad.
 * No lleva el precio de referencia del Ministerio: ese lo compara el director al validar (V19). Los
 * nombres de los productos salen de la copia local de ms-cotizaciones (NombresProductos, DEC-74).
 */
@Component
public class PdfCotizacionAdapter implements GeneradorPdfPort {

    private static final Color NARANJA = new Color(0xF2, 0x87, 0x05);
    private static final Color DURAZNO = new Color(0xFF, 0xF4, 0xE8);
    private static final Color LINEA = new Color(0xE8, 0xE0, 0xD8);
    private static final Color TEXTO = new Color(0x2E, 0x2A, 0x26);
    private static final Color SUAVE = new Color(0x8A, 0x84, 0x7E);
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final NombresProductos nombres;

    public PdfCotizacionAdapter(NombresProductos nombres) {
        this.nombres = nombres;
    }

    @Override
    public byte[] generar(Cotizacion c) {
        Map<Long, String> productos = nombres.todos();
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.LETTER, 48, 48, 48, 48);
        PdfWriter.getInstance(doc, salida);
        doc.addTitle("Solicitud de cotización " + c.numero());
        doc.addAuthor("Konrad Gourmet · Área de Compras");
        doc.addSubject("Solicitud de cotización para " + c.proveedor().razonSocial());
        doc.open();

        // encabezado: la marca a la izquierda y el número a la derecha
        PdfPTable cabeza = new PdfPTable(new float[]{3, 2});
        cabeza.setWidthPercentage(100);
        Paragraph nombre = new Paragraph();
        nombre.add(new Chunk("Konrad ", fuente(20, Font.BOLD, TEXTO)));
        nombre.add(new Chunk("Gourmet", fuente(20, Font.BOLD, NARANJA)));
        PdfPCell marca = sinBorde(new Phrase());
        marca.addElement(nombre);
        marca.addElement(new Paragraph("Sistema de Abastecimiento · Área de Compras", fuente(9, Font.NORMAL, SUAVE)));
        cabeza.addCell(marca);
        PdfPCell numero = sinBorde(new Phrase());
        Paragraph titulo = new Paragraph("SOLICITUD DE COTIZACIÓN", fuente(10, Font.BOLD, SUAVE));
        titulo.setAlignment(Element.ALIGN_RIGHT);
        Paragraph codigo = new Paragraph(c.numero(), fuente(22, Font.BOLD, NARANJA));
        codigo.setAlignment(Element.ALIGN_RIGHT);
        numero.addElement(titulo);
        numero.addElement(codigo);
        cabeza.addCell(numero);
        doc.add(cabeza);
        doc.add(raya());

        // datos del proveedor y de la solicitud
        Proveedor p = c.proveedor();
        PdfPTable datos = new PdfPTable(new float[]{1.3f, 3, 1.4f, 2});
        datos.setWidthPercentage(100);
        datos.setSpacingBefore(10);
        dato(datos, "Proveedor:", p.razonSocial());
        dato(datos, "Fecha de envío:", c.fechaEnvio().format(FECHA));
        dato(datos, "NIT:", p.nit());
        dato(datos, "Responder antes del:", c.fechaLimite().format(FECHA));
        dato(datos, "Correo:", p.correo());
        dato(datos, c.solicitudNumeros().size() > 1 ? "Solicitudes:" : "Solicitud:", String.join(", ", c.solicitudNumeros()));
        dato(datos, "Teléfono:", texto(p.telefono()));
        dato(datos, "Dirección:", texto(p.direccion()));
        doc.add(datos);

        Paragraph pide = new Paragraph("Le solicitamos cotizar los siguientes productos. Indique el precio por unidad de cada uno "
                + "(en pesos colombianos, con impuestos incluidos).", fuente(10, Font.NORMAL, TEXTO));
        pide.setSpacingBefore(14);
        pide.setSpacingAfter(8);
        doc.add(pide);

        // productos: la última columna la diligencia el proveedor
        PdfPTable tabla = new PdfPTable(new float[]{0.5f, 3, 1.3f, 1, 2});
        tabla.setWidthPercentage(100);
        tabla.setHeaderRows(1);
        for (String h : new String[]{"#", "Producto", "Cantidad", "Unidad", "Precio por unidad"}) {
            PdfPCell celda = new PdfPCell(new Phrase(h, fuente(9, Font.BOLD, Color.WHITE)));
            celda.setBackgroundColor(NARANJA);
            celda.setBorderColor(NARANJA);
            celda.setPadding(6);
            tabla.addCell(celda);
        }
        int k = 1;
        for (ItemCotizacion i : c.items()) {
            Color fondo = k % 2 == 0 ? DURAZNO : Color.WHITE;
            tabla.addCell(fila(String.valueOf(k), fondo, Element.ALIGN_CENTER));
            tabla.addCell(fila(NombresProductos.de(productos, i.productoId()), fondo, Element.ALIGN_LEFT));
            tabla.addCell(fila(cantidad(i.cantidad()), fondo, Element.ALIGN_RIGHT));
            tabla.addCell(fila(i.unidad(), fondo, Element.ALIGN_LEFT));
            tabla.addCell(fila("$ ______________ / " + i.unidad(), fondo, Element.ALIGN_LEFT));
            k++;
        }
        doc.add(tabla);

        Paragraph total = new Paragraph("Total de productos: " + c.items().size(), fuente(9, Font.NORMAL, SUAVE));
        total.setAlignment(Element.ALIGN_RIGHT);
        total.setSpacingBefore(4);
        doc.add(total);

        Paragraph nota = new Paragraph();
        nota.setSpacingBefore(18);
        nota.add(new Chunk("Cómo responder. ", fuente(10, Font.BOLD, TEXTO)));
        nota.add(new Chunk("Responda este correo antes del " + c.fechaLimite().format(FECHA) + " citando la cotización " + c.numero()
                + ". El área de compras registra sus precios y los compara con el precio de referencia del Ministerio de Comercio; "
                + "si la cotización queda opcionada, recibirá la orden de compra por este mismo medio.", fuente(10, Font.NORMAL, TEXTO)));
        doc.add(nota);

        doc.add(raya());
        Paragraph pie = new Paragraph("Konrad Gourmet · Documento generado por el Sistema de Abastecimiento · " + c.numero(),
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
        DecimalFormat f = new DecimalFormat("#,##0.###", DecimalFormatSymbols.getInstance(Locale.forLanguageTag("es-CO")));
        return f.format(v);
    }

    private static String texto(String v) {
        return v == null || v.isBlank() ? "—" : v;
    }

}
