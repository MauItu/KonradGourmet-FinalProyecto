package co.edu.konradlorenz.identidad.dominio;

import java.util.ArrayList;
import java.util.List;

/**
 * {8+ caracteres, con mayúscula, minúscula y número}
 */
public class PoliticaContrasena {

    private int longitudMinima = 8;

    /**
     * Rechaza la clave que no cumple la regla (DEC-10) y dice todo lo que le falta, en el mismo orden
     * que la lista de chequeo del formulario de registro de usuarios.
     */
    public void validar(String clave) {
        String c = clave == null ? "" : clave;
        List<String> falta = new ArrayList<>();
        if (c.length() < longitudMinima) {
            falta.add(longitudMinima + " caracteres o más");
        }
        if (c.chars().noneMatch(Character::isUpperCase)) {
            falta.add("una mayúscula");
        }
        if (c.chars().noneMatch(Character::isLowerCase)) {
            falta.add("una minúscula");
        }
        if (c.chars().noneMatch(Character::isDigit)) {
            falta.add("un número");
        }
        if (!falta.isEmpty()) {
            throw new IllegalArgumentException("La contraseña debe tener " + String.join(", ", falta) + ".");
        }
    }

}
