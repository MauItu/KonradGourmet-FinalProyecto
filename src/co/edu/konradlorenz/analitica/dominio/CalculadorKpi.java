package co.edu.konradlorenz.analitica.dominio;

import java.util.List;

/**
 * «Strategy»
 */
public interface CalculadorKpi {

    String nombre();

    ResultadoKpi calcular(List<HechoNegocio> hechos);

}
