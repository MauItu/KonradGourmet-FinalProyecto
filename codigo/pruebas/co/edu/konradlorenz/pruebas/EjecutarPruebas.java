package co.edu.konradlorenz.pruebas;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

/** Ejecuta todas las pruebas y termina con código 0 si pasaron y 1 si alguna falló. */
public final class EjecutarPruebas {

    private EjecutarPruebas() {
    }

    public static void main(String[] args) {
        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out), true, StandardCharsets.UTF_8));
        System.out.println("Konrad Gourmet - pruebas");
        PruebasCotizacion.ejecutar();
        PruebasPatrones.ejecutar();
        PruebasFlujoDeCompra.ejecutar();
        System.exit(Prueba.resumen());
    }

}
