package co.edu.konradlorenz.inventario.dominio;

import java.util.List;

public class Inventario {

    private Long sucursalId;
    private List<InsumoInventario> insumos;

    public Inventario(Long sucursalId, List<InsumoInventario> insumos) {
        this.sucursalId = sucursalId;
        this.insumos = insumos;
    }

    public Long sucursalId() {
        return sucursalId;
    }

    public List<InsumoInventario> insumos() {
        return insumos;
    }

    public List<InsumoInventario> insumosBajoUmbral(UmbralStock umbral) {
        return insumos.stream().filter(umbral::superado).toList();
    }

}
