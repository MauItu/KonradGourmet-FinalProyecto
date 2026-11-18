package co.edu.konradlorenz.analitica.dominio;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ProductoMayorDemanda implements CalculadorKpi {

    @Override
    public String nombre() {
        return "Producto con mayor demanda";
    }

    @Override
    public ResultadoKpi calcular(List<HechoNegocio> hechos) {
        Map<String, BigDecimal> total = new LinkedHashMap<>();
        for (HechoNegocio h : hechos) {
            if ("DEMANDA_PRODUCTO".equals(h.tipo())) {
                total.merge(h.clave(), h.valor(), BigDecimal::add);
            }
        }
        // de mayor a menor demanda; el orden es estable, así que en un empate gana la clave que apareció primero
        Map<String, BigDecimal> ranking = new LinkedHashMap<>();
        total.entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .limit(ResultadoKpi.PUESTOS)
                .forEach(e -> ranking.put(e.getKey(), e.getValue()));
        return ranking.entrySet().stream().findFirst()
                .map(e -> new ResultadoKpi(nombre(), e.getKey(), e.getValue(), ranking))
                .orElse(new ResultadoKpi(nombre(), null, BigDecimal.ZERO));
    }

}
