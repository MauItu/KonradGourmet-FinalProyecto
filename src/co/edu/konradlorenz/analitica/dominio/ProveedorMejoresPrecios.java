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
        // la diferencia promedio contra el precio estándar, con 2 decimales: gana la más baja (PD-03);
        // el orden es estable, así que en un empate gana el proveedor que apareció primero
        Map<String, BigDecimal> promedios = new LinkedHashMap<>();
        suma.forEach((proveedor, total) -> promedios.put(proveedor,
                total.divide(BigDecimal.valueOf(cuenta.get(proveedor)), 2, RoundingMode.HALF_UP)));
        Map<String, BigDecimal> ranking = new LinkedHashMap<>();
        promedios.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .limit(ResultadoKpi.PUESTOS)
                .forEach(e -> ranking.put(e.getKey(), e.getValue()));
        return ranking.entrySet().stream().findFirst()
                .map(e -> new ResultadoKpi(nombre(), e.getKey(), e.getValue(), ranking))
                .orElse(new ResultadoKpi(nombre(), null, BigDecimal.ZERO));
    }

}
