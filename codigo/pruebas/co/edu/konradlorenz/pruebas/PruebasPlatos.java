package co.edu.konradlorenz.pruebas;

import co.edu.konradlorenz.compartido.eventos.AuditoriaEvent;
import co.edu.konradlorenz.demo.Ensamble;
import co.edu.konradlorenz.menu.dominio.Ingrediente;
import co.edu.konradlorenz.menu.dominio.Plato;
import co.edu.konradlorenz.menu.dominio.Producto;
import co.edu.konradlorenz.menu.dominio.UnidadMedida;
import co.edu.konradlorenz.menu.eventos.MenuActualizadoEvent;
import co.edu.konradlorenz.pedidos.dominio.Mesa;
import co.edu.konradlorenz.pedidos.dominio.Pedido;

import java.math.BigDecimal;

/** HU-04: ms-menu crea, edita, desactiva y reactiva platos, y por el bus actualiza el catálogo y las recetas. */
final class PruebasPlatos {

    private PruebasPlatos() {
    }

    static void ejecutar() {
        dominio();
        crear();
        editar();
        desactivar();
        imagen();
        pedidoGuardado();
    }

    private static Plato plato(String nombre, String precio) {
        return new Plato(1L, 7L, nombre, "Fuertes", "Descripción", new BigDecimal(precio));
    }

    private static Ingrediente kg(long insumo, String cantidad) {
        return new Ingrediente(new Producto(insumo, "Insumo " + insumo, null), new BigDecimal(cantidad), UnidadMedida.KG);
    }

    private static void dominio() {
        Prueba.grupo("Plato (dominio de ms-menu)");
        Prueba.lanza("el nombre es obligatorio", IllegalStateException.class, "nombre", () -> plato(" ", "1000").validar());
        Prueba.lanza("la categoría de la carta es obligatoria", IllegalStateException.class, "categoría",
                () -> new Plato(1L, 7L, "Sopa", " ", "", new BigDecimal("1000")).validar());
        Prueba.lanza("el precio debe ser mayor que cero", IllegalStateException.class, "precio", () -> plato("Sopa", "0").validar());
        Prueba.lanza("un ingrediente sin cantidad se rechaza", IllegalStateException.class, "mayor que cero", () -> plato("Sopa", "1000").agregarIngrediente(kg(1L, "0")));
        Plato p = plato("Sopa", "1000");
        p.agregarIngrediente(kg(1L, "0.5"));
        p.agregarIngrediente(new Ingrediente(new Producto(1L, "Insumo 1", null), new BigDecimal("250"), UnidadMedida.GRAMO));
        Prueba.igualValor("el mismo producto dos veces suma las cantidades (en kg)", "0.75", p.receta().get(1L));
        Prueba.verificar("un plato nuevo nace activo", p.activo());
        p.desactivar();
        Prueba.verificar("desactivar() lo deja inactivo", !p.activo());
        p.reactivar();
        Prueba.verificar("reactivar() lo vuelve a dejar activo", p.activo());
    }

    private static void crear() {
        Prueba.grupo("Plato: crear (ms-menu → ms-pedidos y ms-inventario)");
        Ensamble s = Datos.sistema();
        Plato p = plato("Sancocho", "28000");
        p.agregarIngrediente(kg(4L, "0.25"));
        Plato creado = s.platos.crear(p);
        Prueba.verificar("el repositorio le asigna un id", creado.id() != null);
        Prueba.igual("se publicó menu.actualizado", 1L, s.bus.cuantos("menu.actualizado") - 2); // los dos platos de Datos.sistema()
        Prueba.verificar("el evento lleva el plato, el precio y la receta", s.bus.publicados().stream()
                .anyMatch(e -> e instanceof MenuActualizadoEvent m && m.platoId().equals(creado.id()) && m.activo()
                        && m.precioVenta().compareTo(new BigDecimal("28000")) == 0 && m.receta().containsKey(4L)));
        Prueba.verificar("ms-pedidos lo trae en el catálogo del mesero", s.pedidos.catalogo(1L).stream().anyMatch(c -> c.nombre().equals("Sancocho")));
        Prueba.verificar("con su categoría de la carta, para filtrar V03", s.pedidos.catalogo(1L).stream()
                .anyMatch(c -> c.nombre().equals("Sancocho") && "Fuertes".equals(c.categoria())));
        Prueba.verificar("ms-inventario guardó su receta", s.repositorioInventario.recetaDe(creado.id()) != null);
        Prueba.verificar("la auditoría dice que se creó", s.bus.publicados().stream()
                .anyMatch(e -> e instanceof AuditoriaEvent a && a.accion().equals("Plato Sancocho creado")));
        Prueba.lanza("un nombre repetido en la sucursal se rechaza", IllegalArgumentException.class, "Ya existe", () -> s.platos.crear(plato("sancocho", "1000")));
        Prueba.igual("el rechazo no publica nada más", 1L, s.bus.cuantos("menu.actualizado") - 2);
        Prueba.lanza("un plato sin nombre no se crea", IllegalStateException.class, "nombre", () -> s.platos.crear(plato("", "1000")));
        Prueba.igual("consultar() trae todos los platos de la sucursal", 1, s.platos.consultar(1L).size());
    }

    private static void editar() {
        Prueba.grupo("Plato: editar");
        Ensamble s = Datos.sistema();
        Plato creado = s.platos.crear(plato("Sancocho", "28000"));
        s.platos.crear(plato("Mondongo", "25000"));
        Plato nuevo = new Plato(1L, 7L, "Sancocho de gallina", "Sopas", "Descripción", new BigDecimal("30000"));
        nuevo.agregarIngrediente(kg(4L, "0.4"));
        s.platos.editar(creado.id(), nuevo);
        Prueba.verificar("la categoría nueva llega al catálogo del mesero", s.pedidos.catalogo(1L).stream()
                .anyMatch(c -> c.platoId().equals(creado.id()) && "Sopas".equals(c.categoria())));
        Prueba.verificar("el catálogo del mesero tiene el nombre y el precio nuevos", s.pedidos.catalogo(1L).stream()
                .anyMatch(c -> c.platoId().equals(creado.id()) && c.nombre().equals("Sancocho de gallina") && c.precio().compareTo(new BigDecimal("30000")) == 0));
        Prueba.verificar("y ya no está el nombre viejo", s.pedidos.catalogo(1L).stream().noneMatch(c -> c.nombre().equals("Sancocho")));
        Prueba.igualValor("la receta del inventario se actualizó", "0.4", s.repositorioInventario.recetaDe(creado.id()).insumos().get(4L));
        Prueba.lanza("editar con el nombre de otro plato se rechaza", IllegalArgumentException.class, "Ya existe", () -> s.platos.editar(creado.id(), plato("mondongo", "1000")));
        s.platos.editar(creado.id(), plato("SANCOCHO DE GALLINA", "31000"));
        Prueba.verificar("pero el plato puede conservar su propio nombre", s.platos.consultar(1L).stream()
                .anyMatch(p -> p.id().equals(creado.id()) && p.precioVenta().compareTo(new BigDecimal("31000")) == 0));
        Prueba.lanza("editar un plato que no existe se rechaza", IllegalArgumentException.class, "no existe", () -> s.platos.editar(99L, plato("X", "1000")));
    }

    private static void desactivar() {
        Prueba.grupo("Plato: desactivar y reactivar");
        Ensamble s = Datos.sistema();
        Plato creado = s.platos.crear(plato("Sancocho", "28000"));
        Pedido antes = new Pedido("PED-0300", new Mesa(5, 1L, 4, true), 3L);
        antes.agregarPlato(creado.id(), 2, null, new BigDecimal("28000"));
        s.pedidos.registrar(antes);
        s.platos.desactivar(creado.id());
        Prueba.verificar("desactivar lo saca del catálogo del mesero", s.pedidos.catalogo(1L).stream().noneMatch(c -> c.platoId().equals(creado.id())));
        Prueba.verificar("no se borra: sigue en la lista de ms-menu, inactivo", s.platos.consultar(1L).stream()
                .anyMatch(p -> p.id().equals(creado.id()) && !p.activo()));
        Prueba.verificar("el pedido que ya lo tenía sigue intacto", s.pedidos.pedidosDelTurno(3L).stream()
                .anyMatch(p -> p.numero().equals("PED-0300") && p.detalles().size() == 1));
        Prueba.verificar("la receta se conserva en el inventario", s.repositorioInventario.recetaDe(creado.id()) != null);
        Pedido despues = new Pedido("PED-0301", new Mesa(5, 1L, 4, true), 3L);
        despues.agregarPlato(creado.id(), 1, null, new BigDecimal("28000"));
        Prueba.lanza("un pedido nuevo con el plato inactivo se rechaza", IllegalArgumentException.class, "no está disponible", () -> s.pedidos.registrar(despues));
        s.platos.reactivar(creado.id());
        Prueba.verificar("reactivar lo devuelve al catálogo", s.pedidos.catalogo(1L).stream().anyMatch(c -> c.platoId().equals(creado.id())));
        Prueba.igual("cada cambio publicó menu.actualizado (crear, desactivar, reactivar)", 3L, s.bus.cuantos("menu.actualizado") - 2);
    }

    private static void imagen() {
        Prueba.grupo("Plato: imagen en el almacén de objetos");
        Ensamble s = Datos.sistema();
        Plato creado = s.platos.crear(plato("Sancocho", "28000"));
        byte[] png = {(byte) 0x89, 'P', 'N', 'G', 1, 2, 3};
        Prueba.lanza("sin imagen, pedirla se rechaza", IllegalArgumentException.class, "no tiene imagen", () -> s.platos.imagen(creado.id()));
        Prueba.lanza("un GIF se rechaza", IllegalArgumentException.class, "JPG, PNG o WebP", () -> s.platos.subirImagen(creado.id(), png, "image/gif"));
        Prueba.lanza("más de 1 MB se rechaza", IllegalArgumentException.class, "1 MB", () -> s.platos.subirImagen(creado.id(), new byte[1024 * 1024 + 1], "image/png"));
        s.platos.subirImagen(creado.id(), png, "image/png");
        Plato guardado = s.platos.consultar(1L).get(0);
        Prueba.igual("el plato guarda la dirección del almacén", "almacen://konradgourmet/platos/" + creado.id() + ".png", guardado.imagenUrl());
        Prueba.verificar("la imagen se lee igual", java.util.Arrays.equals(png, s.platos.imagen(creado.id())));
        s.platos.editar(creado.id(), plato("Sancocho", "29000"));
        Prueba.verificar("editar el plato no le quita la imagen", s.platos.consultar(1L).get(0).imagenUrl() != null);
        Prueba.verificar("la auditoría registra el cambio de imagen", s.bus.publicados().stream()
                .anyMatch(e -> e instanceof AuditoriaEvent a && a.accion().equals("Imagen del plato Sancocho actualizada")));
    }

    private static void pedidoGuardado() {
        Prueba.grupo("Pedido leído del repositorio");
        java.time.LocalDateTime ayer = java.time.LocalDateTime.now().minusDays(1);
        Pedido p = new Pedido("PED-0251", new Mesa(5, 1L, 4, true), 3L, ayer, co.edu.konradlorenz.pedidos.dominio.EstadoPedido.SERVIDO);
        Prueba.igual("conserva la fecha con que se guardó", ayer, p.fecha());
        Prueba.igual("y su estado", co.edu.konradlorenz.pedidos.dominio.EstadoPedido.SERVIDO, p.estado());
    }

}
