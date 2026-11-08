package co.edu.konradlorenz.analitica.dominio;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SucursalMayorDemanda implements CalculadorKpi {

    @Override
    public String nombre() {
        return "Sucursal con mayor demanda";
    }

    @Override
    public ResultadoKpi calcular(List<HechoNegocio> hechos) {
        Map<String, BigDecimal> total = new LinkedHashMap<>();
        for (HechoNegocio h : hechos) {
            if ("DEMANDA_SUCURSAL".equals(h.tipo())) {
                total.merge(h.clave(), h.valor(), BigDecimal::add);
            }
        }
        // gana la clave con más demanda; si hay empate, la que apareció primero
        return total.entrySet().stream()
                .reduce((a, b) -> b.getValue().compareTo(a.getValue()) > 0 ? b : a)
                .map(e -> new ResultadoKpi(nombre(), e.getKey(), e.getValue()))
                .orElse(new ResultadoKpi(nombre(), null, BigDecimal.ZERO));
    }

}
