package co.edu.konradlorenz.demo;

import co.edu.konradlorenz.notificaciones.puertos.salida.DestinatarioRepositoryPort;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** La libreta de jefes por sucursal, en vez de la tabla de ms-notificaciones. */
public class DestinatarioEnMemoria implements DestinatarioRepositoryPort {

    private final Map<Long, List<String>> porSucursal = new LinkedHashMap<>();

    @Override
    public List<String> jefesDe(Long sucursalId) {
        return List.copyOf(porSucursal.getOrDefault(sucursalId, List.of()));
    }

    @Override
    public List<String> jefes() {
        return porSucursal.values().stream().flatMap(List::stream).distinct().toList();
    }

    @Override
    public void guardar(Long sucursalId, String correo) {
        List<String> jefes = porSucursal.computeIfAbsent(sucursalId, id -> new ArrayList<>());
        if (!jefes.contains(correo)) {
            jefes.add(correo);
        }
    }

}
