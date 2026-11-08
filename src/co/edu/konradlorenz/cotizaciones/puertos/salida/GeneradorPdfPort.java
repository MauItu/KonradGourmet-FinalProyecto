package co.edu.konradlorenz.cotizaciones.puertos.salida;

import co.edu.konradlorenz.cotizaciones.dominio.Cotizacion;

public interface GeneradorPdfPort {

    byte[] generar(Cotizacion c);

}
