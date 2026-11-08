package co.edu.konradlorenz.notificaciones.puertos.salida;

/**
 * Nota del diagrama: Si el Mensaje trae adjuntoUrl, el adaptador SMTP descarga el PDF del almacén
 * (claim check) y lo adjunta al correo certificado.
 */
public interface AlmacenArchivosPort {

    byte[] obtener(String url);

}
