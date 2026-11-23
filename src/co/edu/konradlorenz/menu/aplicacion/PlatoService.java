package co.edu.konradlorenz.menu.aplicacion;

import co.edu.konradlorenz.compartido.eventos.AuditoriaEvent;
import co.edu.konradlorenz.compartido.puertos.EventPublisherPort;
import co.edu.konradlorenz.menu.dominio.Plato;
import co.edu.konradlorenz.menu.dominio.Producto;
import co.edu.konradlorenz.menu.eventos.MenuActualizadoEvent;
import co.edu.konradlorenz.menu.puertos.entrada.RegistrarPlatoUseCase;
import co.edu.konradlorenz.menu.puertos.salida.AlmacenArchivosPort;
import co.edu.konradlorenz.menu.puertos.salida.PlatoRepositoryPort;

import java.util.List;
import java.util.Map;

public class PlatoService implements RegistrarPlatoUseCase {

    private static final int MAX_IMAGEN = 1024 * 1024;
    private static final Map<String, String> EXTENSIONES = Map.of("image/png", "png", "image/jpeg", "jpg", "image/webp", "webp");

    private final PlatoRepositoryPort repositorio;
    private final AlmacenArchivosPort almacen;
    private final EventPublisherPort eventos;

    public PlatoService(PlatoRepositoryPort repositorio, AlmacenArchivosPort almacen, EventPublisherPort eventos) {
        this.repositorio = repositorio;
        this.almacen = almacen;
        this.eventos = eventos;
    }

    @Override
    public Plato registrar(Plato p) {
        p.validar();
        if (repositorio.existeNombre(p.sucursalId(), p.nombre())) {
            throw new IllegalArgumentException("Ya existe un plato registrado con ese nombre");
        }
        Plato guardado = repositorio.guardar(p);
        publicar(guardado, "creado");
        return guardado;
    }

    @Override
    public List<Plato> consultar(Long sucursalId) {
        return repositorio.porSucursal(sucursalId);
    }

    @Override
    public Plato editar(Long id, Plato p) {
        Plato actual = existente(id);
        p.validar();
        // el nombre es único en la sucursal, pero el plato puede conservar el suyo
        if (!actual.nombre().equalsIgnoreCase(p.nombre()) && repositorio.existeNombre(actual.sucursalId(), p.nombre())) {
            throw new IllegalArgumentException("Ya existe un plato registrado con ese nombre");
        }
        actual.editar(p);
        Plato guardado = repositorio.guardar(actual);
        publicar(guardado, "editado");
        return guardado;
    }

    @Override
    public void desactivar(Long id) {
        Plato p = existente(id);
        p.desactivar();
        publicar(repositorio.guardar(p), "desactivado");
    }

    @Override
    public void reactivar(Long id) {
        Plato p = existente(id);
        p.reactivar();
        publicar(repositorio.guardar(p), "reactivado");
    }

    @Override
    public List<Producto> productosPorCategoria(Long categoriaId) {
        return repositorio.productosPorCategoria(categoriaId);
    }

    @Override
    public void guardarImagen(Long id, byte[] contenido, String tipo) {
        Plato p = existente(id);
        String extension = EXTENSIONES.get(tipo);
        if (extension == null) {
            throw new IllegalArgumentException("La imagen debe ser JPG, PNG o WebP");
        }
        if (contenido == null || contenido.length == 0) {
            throw new IllegalArgumentException("La imagen está vacía");
        }
        if (contenido.length > MAX_IMAGEN) {
            throw new IllegalArgumentException("La imagen supera 1 MB");
        }
        p.asignarImagen(almacen.guardar("platos/" + p.id() + "." + extension, contenido));
        repositorio.guardar(p);
        eventos.publicar(new AuditoriaEvent("sistema", "Imagen del plato " + p.nombre() + " actualizada"));
    }

    @Override
    public byte[] imagen(Long id) {
        Plato p = existente(id);
        if (p.imagenUrl() == null) {
            throw new IllegalArgumentException("El plato " + id + " no tiene imagen");
        }
        return almacen.obtener(p.imagenUrl());
    }

    private Plato existente(Long id) {
        Plato p = repositorio.porId(id);
        if (p == null) {
            throw new IllegalArgumentException("El plato " + id + " no existe");
        }
        return p;
    }

    /**
     * menu.actualizado lleva el estado completo del plato: ms-pedidos reemplaza su entrada del catálogo (con
     * activo = false sale de lo que ve el mesero) y ms-inventario guarda la receta, que se conserva aunque
     * el plato se desactive.
     */
    private void publicar(Plato p, String accion) {
        eventos.publicar(new MenuActualizadoEvent("sistema", p.id(), p.sucursalId(), p.nombre(), p.categoria(), p.precioVenta(), p.activo(), p.receta()));
        eventos.publicar(new AuditoriaEvent("sistema", "Plato " + p.nombre() + " " + accion));
    }

}
