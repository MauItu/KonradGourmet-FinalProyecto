package co.edu.konradlorenz.ordenes;

import co.edu.konradlorenz.ordenes.adaptadores.salida.PdfOrdenAdapter;
import co.edu.konradlorenz.ordenes.configuracion.NombresProductos;
import co.edu.konradlorenz.ordenes.dominio.EstadoOrden;
import co.edu.konradlorenz.ordenes.dominio.ItemOrden;
import co.edu.konradlorenz.ordenes.dominio.OrdenCompra;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** El PDF de la orden se puede abrir y dice lo que el proveedor necesita: qué se le compra, a qué precio y el total. */
class PdfOrdenTest {

    @Test
    void elPdfAbreYLlevaLaOrdenElProveedorLosProductosYElTotal() throws Exception {
        NombresProductos nombres = mock(NombresProductos.class);
        when(nombres.todos()).thenReturn(Map.of(1L, "Res", 2L, "Arroz"));
        OrdenCompra o = new OrdenCompra("OC-0012", LocalDate.of(2026, 11, 17), "COT-018", List.of("SOL-031"), "Proveedor ABC",
                "ventas@proveedorabc.com", null, EstadoOrden.EN_PROCESO,
                List.of(new ItemOrden(1L, new BigDecimal("20"), "kg", new BigDecimal("21250")),
                        new ItemOrden(2L, new BigDecimal("15"), "kg", new BigDecimal("8000"))));

        byte[] pdf = new PdfOrdenAdapter(nombres).generar(o);

        assertTrue(new String(pdf, 0, 5, StandardCharsets.ISO_8859_1).startsWith("%PDF-"), "empieza como un PDF");
        PdfReader lector = new PdfReader(pdf);
        assertEquals(1, lector.getNumberOfPages());
        String texto = new PdfTextExtractor(lector).getTextFromPage(1);
        lector.close();
        for (String esperado : List.of("ORDEN DE COMPRA", "OC-0012", "Proveedor ABC", "ventas@proveedorabc.com", "COT-018", "SOL-031",
                "17/11/2026", "Res", "Arroz", "$21.250", "$425.000", "$120.000", "$545.000")) {
            assertTrue(texto.contains(esperado), "el PDF dice «" + esperado + "»:\n" + texto);
        }
    }

}
