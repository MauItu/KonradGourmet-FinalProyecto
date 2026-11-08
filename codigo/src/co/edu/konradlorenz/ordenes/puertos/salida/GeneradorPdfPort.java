package co.edu.konradlorenz.ordenes.puertos.salida;

import co.edu.konradlorenz.ordenes.dominio.OrdenCompra;

public interface GeneradorPdfPort {

    byte[] generar(OrdenCompra o);

}
