package co.edu.konradlorenz.notificaciones.adaptadores.salida;

import co.edu.konradlorenz.notificaciones.dominio.Mensaje;
import co.edu.konradlorenz.notificaciones.puertos.salida.Correo;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * «Decorator concreto»
 */
public class CorreoCertificadoDecorator extends CorreoDecorator {

    public CorreoCertificadoDecorator(Correo envuelto) {
        super(envuelto);
    }

    @Override
    public void enviar(Mensaje m) {
        // el certificado es la huella SHA-256 del destinatario y el cuerpo: prueba que el contenido no cambió
        m.cabeceras().put("X-Certificado", "SHA-256:" + huella(m.para() + "\n" + m.cuerpo()));
        super.enviar(m);
    }

    private static String huella(String texto) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(texto.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash, 0, 8); // los primeros 8 bytes bastan para el encabezado
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no está disponible", e);
        }
    }

}
