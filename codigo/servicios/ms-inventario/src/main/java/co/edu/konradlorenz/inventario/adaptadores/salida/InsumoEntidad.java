package co.edu.konradlorenz.inventario.adaptadores.salida;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

/** Fila de la tabla insumo: la llave es la sucursal y el producto. */
@Entity
@Table(name = "insumo")
@IdClass(InsumoEntidad.Llave.class)
class InsumoEntidad {

    @Id
    @Column(name = "sucursal_id")
    Long sucursalId;

    @Id
    @Column(name = "producto_id")
    Long productoId;

    @Column(nullable = false)
    String nombre;

    @Column(name = "stock_actual", nullable = false)
    BigDecimal stockActual;

    @Column(name = "stock_referencia", nullable = false)
    BigDecimal stockReferencia;

    @Column(nullable = false)
    String unidad;

    @Column(name = "alerta_atendida", nullable = false)
    boolean alertaAtendida;

    static class Llave implements Serializable {
        Long sucursalId;
        Long productoId;

        @Override
        public boolean equals(Object o) {
            return o instanceof Llave l && Objects.equals(sucursalId, l.sucursalId) && Objects.equals(productoId, l.productoId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(sucursalId, productoId);
        }
    }

}
