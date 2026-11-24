package co.edu.konradlorenz.menu.configuracion;

import co.edu.konradlorenz.compartido.infraestructura.ContextoRecorrido;
import co.edu.konradlorenz.menu.dominio.Ingrediente;
import co.edu.konradlorenz.menu.dominio.Plato;
import co.edu.konradlorenz.menu.dominio.Producto;
import co.edu.konradlorenz.menu.dominio.UnidadMedida;
import co.edu.konradlorenz.menu.puertos.entrada.RegistrarPlatoUseCase;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * La carta de la demo (los mismos 8 platos y recetas de la interfaz en memoria), creada con
 * PlatoService la primera vez que arranca el servicio: cada plato publica menu.actualizado, y así
 * ms-pedidos arma el catálogo del mesero y ms-inventario guarda las recetas, como con un plato nuevo.
 * Los crea Carlos Mendoza (id 2 en ms-identidad), jefe de cocina de la Sucursal Centro.
 */
@Component
public class DatosIniciales implements ApplicationRunner {

    private static final long CENTRO = 1L;
    private static final long JEFE = 2L;
    private static final Object[][] PLATOS = {
            {"Bandeja paisa", "Fuertes", "32000", "Frijoles, arroz, chicharrón, carne, huevo, plátano y arepa.", new Object[]{1L, "0.5", 2L, "0.1"}},
            {"Ajiaco", "Sopas", "18000", "Pollo, papas criollas y guascas.", new Object[]{4L, "0.2", 3L, "0.3"}},
            {"Punta de anca", "Fuertes", "38000", "Carne a la parrilla, papas, ensalada de la casa.", new Object[]{1L, "0.35", 3L, "0.2"}},
            {"Pechuga a la plancha", "Fuertes", "26000", "Pechuga de pollo, arroz, ensalada y patacón.", new Object[]{4L, "0.3", 2L, "0.1"}},
            {"Ensalada césar", "Entradas", "20000", "Lechuga, pollo, parmesano, crutones y salsa césar.", new Object[]{4L, "0.15"}},
            {"Arepa con queso", "Entradas", "7000", "Arepa de maíz con queso derretido.", new Object[]{}},
            {"Limonada natural", "Bebidas", "6000", "Limonada de limón natural.", new Object[]{}},
            {"Jugo natural", "Bebidas", "6500", "Jugo de fruta natural en agua o en leche.", new Object[]{}},
    };

    private final RegistrarPlatoUseCase platos;
    private final JdbcTemplate db;

    public DatosIniciales(RegistrarPlatoUseCase platos, JdbcTemplate db) {
        this.platos = platos;
        this.db = db;
    }

    @Override
    public void run(ApplicationArguments args) {
        Integer hay = db.queryForObject("SELECT count(*) FROM plato", Integer.class);
        if (hay != null && hay > 0) {
            return;
        }
        ContextoRecorrido.comoRecorridoNuevo(() -> {
            for (Object[] d : PLATOS) {
                Plato p = new Plato(CENTRO, JEFE, (String) d[0], (String) d[1], (String) d[3], new BigDecimal((String) d[2]));
                Object[] receta = (Object[]) d[4];
                for (int k = 0; k < receta.length; k += 2) {
                    p.agregarIngrediente(new Ingrediente(new Producto((Long) receta[k], null, null), new BigDecimal((String) receta[k + 1]), UnidadMedida.KG));
                }
                platos.registrar(p);
            }
        });
    }

}
