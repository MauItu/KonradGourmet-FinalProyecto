package co.edu.konradlorenz.solicitudes.adaptadores.entrada;

import co.edu.konradlorenz.solicitudes.dominio.CriteriosConsulta;
import co.edu.konradlorenz.solicitudes.dominio.SolicitudAlimentos;
import co.edu.konradlorenz.solicitudes.puertos.entrada.GestionarSolicitudUseCase;

import java.util.List;

/**
 * «REST · /api/solicitudes»
 */
public class SolicitudRestController {

    private final GestionarSolicitudUseCase casoDeUso;

    public SolicitudRestController(GestionarSolicitudUseCase casoDeUso) {
        this.casoDeUso = casoDeUso;
    }

    // Atiende la ventana V08 de los mockups
    // Atiende la ventana V09 de los mockups
    // POST /api/solicitudes → 201 Created
    public SolicitudAlimentos registrar(SolicitudAlimentos s) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    // Atiende la ventana V10 de los mockups
    // GET /api/solicitudes?solicitante=yo
    public List<SolicitudAlimentos> misSolicitudes(CriteriosConsulta c) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    // Atiende la ventana V11 de los mockups
    // GET /api/solicitudes?estado=PENDIENTE (y los demás criterios)
    public List<SolicitudAlimentos> consultar(CriteriosConsulta c) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    // Atiende la ventana V12 de los mockups
    // GET /api/solicitudes/{id}
    public SolicitudAlimentos detalle(String numero) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

}
