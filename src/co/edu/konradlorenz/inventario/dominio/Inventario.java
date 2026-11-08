package co.edu.konradlorenz.inventario.dominio;

import java.util.List;

public class Inventario {

    private Long sucursalId;
    private List<InsumoInventario> insumos;

    public List<InsumoInventario> insumosBajoUmbral(UmbralStock umbral) {
        throw new UnsupportedOperationException("Pendiente de implementar");
    }

}
