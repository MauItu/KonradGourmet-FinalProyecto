package co.edu.konradlorenz.menu.aplicacion;

import co.edu.konradlorenz.compartido.puertos.EventPublisherPort;
import co.edu.konradlorenz.menu.dominio.Plato;
import co.edu.konradlorenz.menu.dominio.Producto;
import co.edu.konradlorenz.menu.puertos.entrada.RegistrarPlatoUseCase;
import co.edu.konradlorenz.menu.puertos.salida.PlatoRepositoryPort;

import java.util.List;

public class PlatoService implements RegistrarPlatoUseCase {

    private final PlatoRepositoryPort repositorio;
    private final EventPublisherPort eventos;

    public PlatoService(PlatoRepositoryPort repositorio, EventPublisherPort eventos) {
        this.repositorio = repositorio;
        this.eventos = eventos;
    }

    @Override
    public Plato registrar(Plato p) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    @Override
    public List<Plato> consultar(Long sucursalId) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    @Override
    public Plato editar(Long id, Plato p) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    @Override
    public void desactivar(Long id) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

    @Override
    public List<Producto> productosPorCategoria(Long categoriaId) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

}
