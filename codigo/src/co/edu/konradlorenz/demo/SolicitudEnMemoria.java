package co.edu.konradlorenz.demo;

import co.edu.konradlorenz.solicitudes.dominio.CriteriosConsulta;
import co.edu.konradlorenz.solicitudes.dominio.ItemSolicitud;
import co.edu.konradlorenz.solicitudes.dominio.SolicitudAlimentos;
import co.edu.konradlorenz.solicitudes.puertos.salida.SolicitudRepositoryPort;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Las solicitudes en un mapa, en vez de la base de ms-solicitudes. */
public class SolicitudEnMemoria implements SolicitudRepositoryPort {

    private final Map<String, SolicitudAlimentos> datos = new LinkedHashMap<>();

    @Override
    public SolicitudAlimentos guardar(SolicitudAlimentos s) {
        datos.put(s.numero(), s);
        return s;
    }

    @Override
    public List<SolicitudAlimentos> buscar(CriteriosConsulta c) {
        return datos.values().stream()
                .filter(s -> c.desde() == null || !s.fecha().toLocalDate().isBefore(c.desde()))
                .filter(s -> c.hasta() == null || !s.fecha().toLocalDate().isAfter(c.hasta()))
                .filter(s -> c.estado() == null || s.estado() == c.estado())
                .filter(s -> c.productoId() == null || s.items().stream().anyMatch(i -> c.productoId().equals(i.productoId())))
                .filter(s -> c.marca() == null || s.items().stream().map(ItemSolicitud::marca).anyMatch(c.marca()::equals))
                .sorted(Comparator.comparing(SolicitudAlimentos::numero))
                .toList();
    }

    @Override
    public SolicitudAlimentos porNumero(String numero) {
        return datos.get(numero);
    }

}
