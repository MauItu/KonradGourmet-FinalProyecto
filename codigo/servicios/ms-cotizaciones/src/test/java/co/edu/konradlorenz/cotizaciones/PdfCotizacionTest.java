package co.edu.konradlorenz.cotizaciones;

import co.edu.konradlorenz.compartido.infraestructura.EventosProcesados;
import co.edu.konradlorenz.cotizaciones.adaptadores.entrada.OrdenGeneradaListener;
import co.edu.konradlorenz.cotizaciones.adaptadores.salida.PdfCotizacionAdapter;
import co.edu.konradlorenz.cotizaciones.configuracion.NombresProductos;
import co.edu.konradlorenz.cotizaciones.dominio.Cotizacion;
import co.edu.konradlorenz.cotizaciones.dominio.ItemCotizacion;
import co.edu.konradlorenz.cotizaciones.dominio.Proveedor;
import co.edu.konradlorenz.cotizaciones.puertos.entrada.GestionarCotizacionUseCase;
import co.edu.konradlorenz.ordenes.eventos.OrdenGeneradaEvent;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** El PDF de OpenPDF se puede abrir y dice lo que el proveedor necesita; y el listener de orden.generada. */
class PdfCotizacionTest {

    /** Ningún evento se había procesado (DEC-104). */
    private static EventosProcesados nuevos() {
        EventosProcesados p = org.mockito.Mockito.mock(EventosProcesados.class);
        org.mockito.Mockito.when(p.marcar(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any())).thenReturn(true);
        return p;
    }

    @Test
    void elPdfAbreYLlevaLaCotizacionElProveedorYLosProductos() throws Exception {
        NombresProductos nombres = mock(NombresProductos.class);
        when(nombres.todos()).thenReturn(Map.of(1L, "Res", 2L, "Arroz"));
        Proveedor abc = new Proveedor(1L, "Proveedor ABC", "900123456-1", "ventas@proveedorabc.com", "6011234567", "Cra 7 # 10-20", List.of());
        Cotizacion c = new Cotizacion("COT-018", List.of("SOL-031", "SOL-032"), abc, LocalDate.of(2026, 11, 24),
                List.of(new ItemCotizacion(1L, new BigDecimal("20"), "kg"), new ItemCotizacion(2L, new BigDecimal("15.5"), "kg")));

        byte[] pdf = new PdfCotizacionAdapter(nombres).generar(c);

        assertTrue(new String(pdf, 0, 5, StandardCharsets.ISO_8859_1).startsWith("%PDF-"), "empieza como un PDF");
        PdfReader lector = new PdfReader(pdf);
        assertEquals(1, lector.getNumberOfPages());
        String texto = new PdfTextExtractor(lector).getTextFromPage(1);
        lector.close();
        for (String esperado : List.of("SOLICITUD DE COTIZACIÓN", "COT-018", "Proveedor ABC", "900123456-1", "SOL-031, SOL-032",
                "24/11/2026", "Res", "Arroz", "15,5")) {
            assertTrue(texto.contains(esperado), "el PDF dice «" + esperado + "»:\n" + texto);
        }
        assertTrue(!texto.contains("20000") && !texto.contains("20.000"), "no muestra el precio de referencia del Ministerio");
    }

    @Test
    void ordenGeneradaMarcaLaCotizacionConOrden() {
        GestionarCotizacionUseCase casoDeUso = mock(GestionarCotizacionUseCase.class);
        new OrdenGeneradaListener(casoDeUso, nuevos()).alRecibir(new OrdenGeneradaEvent("laura.gomez@konradgourmet.com", "OC-0012", "COT-018",
                List.of("SOL-031"), "Proveedor ABC", "ventas@proveedorabc.com", "s3://documentos-pdf/ordenes/OC-0012.pdf", new BigDecimal("545000")));
        verify(casoDeUso).marcarConOrden("COT-018");
    }

}
