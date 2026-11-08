package co.edu.konradlorenz.pruebas;

import java.util.Objects;

/** Un marco de pruebas mínimo, sin JUnit ni Maven: cuenta los aciertos y los fallos y los imprime. */
public final class Prueba {

    private static int aciertos;
    private static int fallos;

    private Prueba() {
    }

    public static void grupo(String nombre) {
        System.out.println();
        System.out.println(nombre);
    }

    public static void verificar(String descripcion, boolean condicion) {
        if (condicion) {
            aciertos++;
            System.out.println("  ✓ " + descripcion);
        } else {
            fallos++;
            System.out.println("  ✗ " + descripcion);
        }
    }

    public static void igual(String descripcion, Object esperado, Object actual) {
        if (Objects.equals(esperado, actual)) {
            aciertos++;
            System.out.println("  ✓ " + descripcion);
        } else {
            fallos++;
            System.out.println("  ✗ " + descripcion + " — esperado: " + esperado + " · obtenido: " + actual);
        }
    }

    /** Compara dos BigDecimal por valor, sin importar los ceros a la derecha (23 es igual a 23,00). */
    public static void igualValor(String descripcion, String esperado, java.math.BigDecimal actual) {
        verificar(descripcion + " (" + actual + ")", actual != null && actual.compareTo(new java.math.BigDecimal(esperado)) == 0);
    }

    /** Verifica que la acción lance esa excepción y que su mensaje contenga el fragmento (si el fragmento es "", solo mira el tipo). */
    public static void lanza(String descripcion, Class<? extends RuntimeException> tipo, String fragmento, Runnable accion) {
        try {
            accion.run();
            fallos++;
            System.out.println("  ✗ " + descripcion + " — no lanzó nada");
        } catch (RuntimeException e) {
            boolean bien = tipo.isInstance(e) && (fragmento.isEmpty() || (e.getMessage() != null && e.getMessage().contains(fragmento)));
            if (bien) {
                aciertos++;
                System.out.println("  ✓ " + descripcion);
            } else {
                fallos++;
                System.out.println("  ✗ " + descripcion + " — lanzó " + e.getClass().getSimpleName() + ": " + e.getMessage());
            }
        }
    }

    /** Imprime el total y devuelve el código de salida: 0 si todo pasó, 1 si algo falló. */
    public static int resumen() {
        System.out.println();
        System.out.println((aciertos + fallos) + " verificaciones: " + aciertos + " bien, " + fallos + " mal.");
        return fallos == 0 ? 0 : 1;
    }

}
