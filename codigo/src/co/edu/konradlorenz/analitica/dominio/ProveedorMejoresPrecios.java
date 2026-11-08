package co.edu.konradlorenz.analitica.dominio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ProveedorMejoresPrecios implements CalculadorKpi {

    @Override
    public String nombre() {
        return "Proveedor con mejores precios";
    }

    @Override
    public ResultadoKpi calcular(List<HechoNegocio> hechos) {
        Map<String, BigDecimal> suma = new LinkedHashMap<>();
        Map<String, Integer> cuenta = new LinkedHashMap<>();
        for (HechoNegocio h : hechos) {
            if ("PRECIO_PROVEEDOR".equals(h.tipo())) {
                suma.merge(h.clave(), h.valor(), BigDecimal::add);
                cuenta.merge(h.clave(), 1, Integer::sum);
            }
        }
        // gana el proveedor con el promedio más bajo contra el precio estándar; si hay empate, el primero
        ResultadoKpi mejor = new ResultadoKpi(nombre(), null, BigDecimal.ZERO);
        for (Map.Entry<String, BigDecimal> e : suma.entrySet()) {
            BigDecimal promedio = e.getValue().divide(BigDecimal.valueOf(cuenta.get(e.getKey())), 2, RoundingMode.HALF_UP);
            if (mejor.ganador() == null || promedio.compareTo(mejor.valor()) < 0) {
                mejor = new ResultadoKpi(nombre(), e.getKey(), promedio);
            }
        }
        return mejor;
    }

}
