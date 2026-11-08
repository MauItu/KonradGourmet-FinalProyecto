package co.edu.konradlorenz.solicitudes.puertos.salida;

import co.edu.konradlorenz.solicitudes.dominio.CriteriosConsulta;
import co.edu.konradlorenz.solicitudes.dominio.SolicitudAlimentos;

import java.util.List;

public interface SolicitudRepositoryPort {

    SolicitudAlimentos guardar(SolicitudAlimentos s);

    List<SolicitudAlimentos> buscar(CriteriosConsulta c);

    SolicitudAlimentos porNumero(String numero);

}
