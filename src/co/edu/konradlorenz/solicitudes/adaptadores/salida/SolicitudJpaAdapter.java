package co.edu.konradlorenz.solicitudes.adaptadores.salida;

import co.edu.konradlorenz.solicitudes.dominio.CriteriosConsulta;
import co.edu.konradlorenz.solicitudes.dominio.SolicitudAlimentos;
import co.edu.konradlorenz.solicitudes.puertos.salida.SolicitudRepositoryPort;

import java.util.List;

public class SolicitudJpaAdapter implements SolicitudRepositoryPort {

    @Override
    public SolicitudAlimentos guardar(SolicitudAlimentos s) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    @Override
    public List<SolicitudAlimentos> buscar(CriteriosConsulta c) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    @Override
    public SolicitudAlimentos porNumero(String numero) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

}
