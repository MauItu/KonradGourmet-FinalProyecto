package co.edu.konradlorenz.integracion.adaptadores.salida;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * «Adaptee · generado del WSDL»
 *
 * En el proyecto real lo genera wsimport (o Apache CXF) a partir del WSDL del Ministerio. Aquí está
 * escrito a mano con el JDK (HttpClient y DOM), sin anotaciones, para que lo usen igual la versión en
 * memoria y ms-integracion: manda la operación consultarPrecioEstandar en un sobre SOAP 1.1 y lee el
 * precio de la respuesta. El contrato es el del WSDL del Ministerio simulado
 * (plataforma/infra/ministerio/precios.wsdl).
 */
public class MinisterioSoapClient {

    /** El espacio de nombres del servicio de precios del Ministerio (el targetNamespace del WSDL). */
    public static final String NAMESPACE = "http://precios.ministerio-comercio.simulado/";
    private static final String SOAP_11 = "http://schemas.xmlsoap.org/soap/envelope/";

    private final String endpoint;
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

    public MinisterioSoapClient(String endpoint) {
        this.endpoint = endpoint;
    }

    /**
     * El precio estándar del producto según el Ministerio. Si el Ministerio responde con un SOAP Fault
     * (por ejemplo, no tiene ese producto) lanza IllegalArgumentException con su mensaje; si no responde,
     * IllegalStateException.
     */
    public double consultarPrecioEstandar(String codigoProducto) {
        String sobre = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<soap:Envelope xmlns:soap=\"" + SOAP_11 + "\" xmlns:min=\"" + NAMESPACE + "\">"
                + "<soap:Body><min:consultarPrecioEstandar><codigoProducto>" + escapar(codigoProducto)
                + "</codigoProducto></min:consultarPrecioEstandar></soap:Body></soap:Envelope>";
        HttpRequest peticion = HttpRequest.newBuilder(URI.create(endpoint))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "text/xml; charset=utf-8")
                .header("SOAPAction", "\"" + NAMESPACE + "consultarPrecioEstandar\"")
                .POST(HttpRequest.BodyPublishers.ofString(sobre, StandardCharsets.UTF_8))
                .build();
        HttpResponse<String> respuesta;
        try {
            respuesta = http.send(peticion, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new IllegalStateException("El servicio web del Ministerio de Comercio no responde (" + endpoint + ")", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Se interrumpió la consulta al Ministerio de Comercio", e);
        }
        Document xml = leer(respuesta.body(), respuesta.statusCode());
        String falla = texto(xml, "faultstring");
        if (falla != null) {
            throw new IllegalArgumentException(falla);
        }
        String precio = texto(xml, "precio");
        if (respuesta.statusCode() != 200 || precio == null) {
            throw new IllegalStateException("El Ministerio de Comercio respondió " + respuesta.statusCode() + " sin precio para el producto " + codigoProducto);
        }
        try {
            return Double.parseDouble(precio.trim());
        } catch (NumberFormatException e) {
            throw new IllegalStateException("El Ministerio de Comercio respondió un precio que no es un número: " + precio, e);
        }
    }

    private static Document leer(String cuerpo, int estado) {
        try {
            DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
            f.setNamespaceAware(true);
            // un XML que llega de afuera no puede leer archivos ni otras URL (XXE)
            f.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            f.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            f.setExpandEntityReferences(false);
            return f.newDocumentBuilder().parse(new InputSource(new StringReader(cuerpo)));
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new IllegalStateException("El Ministerio de Comercio respondió " + estado + " con un XML que no se pudo leer", e);
        }
    }

    /** El texto del primer elemento con ese nombre local, en cualquier espacio de nombres; null si no está. */
    private static String texto(Document xml, String nombreLocal) {
        NodeList nodos = xml.getElementsByTagNameNS("*", nombreLocal);
        return nodos.getLength() == 0 ? null : ((Element) nodos.item(0)).getTextContent();
    }

    private static String escapar(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

}
