package co.edu.konradlorenz.analitica.adaptadores.salida;

import co.edu.konradlorenz.analitica.puertos.salida.TendenciasPort;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * «HTTPS → APIs de redes»
 *
 * En el proyecto real: @Component.
 *
 * Pide a la API de tendencias de la red social (supuesto S11: la red concreta no la fija el enunciado) los productos
 * gastronómicos en tendencia de la última semana en Colombia, con su token de acceso:
 * GET {endpoint}?pais=CO&amp;categoria=gastronomia&amp;periodo=semana → {"tendencias": [{"producto": "Ajiaco",
 * "menciones": 1840}, …]}, de más a menos menciones. Está escrito con el JDK (HttpClient), sin anotaciones, para
 * que lo usen igual la versión en memoria y ms-analitica, como MinisterioSoapClient. Si la API no responde o
 * responde un error, lanza IllegalStateException: TendenciasJob la deja en el log (HU-19).
 */
public class RedesSocialesAdapter implements TendenciasPort {

    private static final Pattern PRODUCTO = Pattern.compile("\"producto\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"");

    private final String endpoint;
    private final String token;
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

    public RedesSocialesAdapter(String endpoint, String token) {
        this.endpoint = endpoint;
        this.token = token;
    }

    @Override
    public List<String> productosEnTendencia() {
        HttpRequest.Builder peticion = HttpRequest.newBuilder(URI.create(endpoint + "?pais=CO&categoria=gastronomia&periodo=semana"))
                .timeout(Duration.ofSeconds(10))
                .header("Accept", "application/json")
                .GET();
        if (token != null && !token.isBlank()) {
            peticion.header("Authorization", "Bearer " + token);
        }
        HttpResponse<String> respuesta;
        try {
            respuesta = http.send(peticion.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo consultar las tendencias de las redes sociales: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Se interrumpió la consulta de tendencias de las redes sociales", e);
        }
        if (respuesta.statusCode() != 200) {
            throw new IllegalStateException("La API de tendencias de las redes sociales respondió " + respuesta.statusCode());
        }
        List<String> productos = new ArrayList<>();
        Matcher m = PRODUCTO.matcher(respuesta.body());
        while (m.find()) {
            String p = texto(m.group(1)).trim();
            if (!p.isEmpty() && !productos.contains(p)) {
                productos.add(p);
            }
        }
        return productos;
    }

    /** El texto de una cadena JSON (las secuencias \", \\, \/ y \\uXXXX). */
    private static String texto(String json) {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c != '\\' || i + 1 >= json.length()) {
                s.append(c);
                continue;
            }
            char e = json.charAt(++i);
            switch (e) {
                case 'u' -> {
                    s.append((char) Integer.parseInt(json.substring(i + 1, i + 5), 16));
                    i += 4;
                }
                case 'n' -> s.append('\n');
                case 't' -> s.append('\t');
                default -> s.append(e);
            }
        }
        return s.toString();
    }

}
