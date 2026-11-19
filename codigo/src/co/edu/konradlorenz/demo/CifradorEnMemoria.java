package co.edu.konradlorenz.demo;

import co.edu.konradlorenz.identidad.puertos.salida.CifradorPort;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Cifrador de la demo: SHA-256 del JDK en vez de BCrypt, para no depender de una librería. Sirve para
 * probar que el servicio nunca guarda la clave en claro; en ms-identidad se usa BCrypt.
 */
public class CifradorEnMemoria implements CifradorPort {

    @Override
    public String cifrar(String clave) {
        try {
            byte[] h = MessageDigest.getInstance("SHA-256").digest(clave.getBytes(StandardCharsets.UTF_8));
            return "sha256:" + HexFormat.of().formatHex(h);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public boolean coincide(String clave, String hash) {
        return cifrar(clave).equals(hash);
    }

}
