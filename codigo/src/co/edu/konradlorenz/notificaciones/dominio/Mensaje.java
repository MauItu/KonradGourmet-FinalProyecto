package co.edu.konradlorenz.notificaciones.dominio;

import java.util.LinkedHashMap;
import java.util.Map;

public class Mensaje {

    private String para;
    private String asunto;
    private String cuerpo;
    private Map<String, String> cabeceras;
    private String adjuntoUrl;

    public Mensaje(String para, String asunto, String cuerpo, String adjuntoUrl) {
        this.para = para;
        this.asunto = asunto;
        this.cuerpo = cuerpo;
        this.cabeceras = new LinkedHashMap<>();
        this.adjuntoUrl = adjuntoUrl;
    }

    public String para() {
        return para;
    }

    public String asunto() {
        return asunto;
    }

    public String cuerpo() {
        return cuerpo;
    }

    /** Las cabeceras del correo, en el orden en que se agregaron; los decoradores agregan las suyas aquí. */
    public Map<String, String> cabeceras() {
        return cabeceras;
    }

    public String adjuntoUrl() {
        return adjuntoUrl;
    }

}
