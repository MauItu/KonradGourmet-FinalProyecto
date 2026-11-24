package co.edu.konradlorenz.notificaciones.adaptadores.salida;

import co.edu.konradlorenz.notificaciones.dominio.Mensaje;
import co.edu.konradlorenz.notificaciones.puertos.salida.AlmacenArchivosPort;
import co.edu.konradlorenz.notificaciones.puertos.salida.Correo;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.MailPreparationException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * «Decorator · componente concreto (SMTP/TLS)»
 *
 * Nota del diagrama: Si el Mensaje trae adjuntoUrl, el adaptador SMTP descarga el PDF del almacén (claim
 * check) y lo adjunta al correo certificado.
 *
 * Envía por SMTP con Jakarta Mail (en local, a Mailpit sin TLS, DEC-58). Las cabeceras que pusieron los
 * decoradores (X-Certificado y X-Sello-Tiempo) van tal cual en el correo. Si el envío falla, la excepción
 * sube: el núcleo registra el fallo y el evento se reintenta.
 */
@Component
public class CorreoSmtpAdapter implements Correo {

    private final AlmacenArchivosPort almacen;
    private final JavaMailSender smtp;
    private final String remitente;

    public CorreoSmtpAdapter(AlmacenArchivosPort almacen, JavaMailSender smtp, @Value("${konrad.notificaciones.remitente}") String remitente) {
        this.almacen = almacen;
        this.smtp = smtp;
        this.remitente = remitente;
    }

    @Override
    public void enviar(Mensaje m) {
        MimeMessage correo = smtp.createMimeMessage();
        try {
            boolean conAdjunto = m.adjuntoUrl() != null && !m.adjuntoUrl().isBlank();
            MimeMessageHelper armado = new MimeMessageHelper(correo, conAdjunto, StandardCharsets.UTF_8.name());
            armado.setFrom(remitente);
            armado.setTo(m.para());
            armado.setSubject(m.asunto());
            armado.setText(m.cuerpo(), false);
            if (conAdjunto) {
                byte[] pdf = almacen.obtener(m.adjuntoUrl());
                armado.addAttachment(nombreArchivo(m.adjuntoUrl()), new ByteArrayResource(pdf), "application/pdf");
            }
            for (Map.Entry<String, String> c : m.cabeceras().entrySet()) {
                correo.setHeader(c.getKey(), c.getValue());
            }
        } catch (MessagingException e) {
            throw new MailPreparationException("No se pudo armar el correo para " + m.para(), e);
        }
        smtp.send(correo);
    }

    /** s3://documentos-pdf/ordenes/OC-0012.pdf → OC-0012.pdf */
    static String nombreArchivo(String url) {
        return url.substring(url.lastIndexOf('/') + 1);
    }

}
