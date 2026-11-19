package co.edu.konradlorenz.administracion.aplicacion;

import co.edu.konradlorenz.administracion.dominio.LogError;
import co.edu.konradlorenz.administracion.dominio.ParametrosSistema;
import co.edu.konradlorenz.administracion.dominio.RegistroAuditoria;
import co.edu.konradlorenz.administracion.puertos.entrada.AdministracionUseCase;
import co.edu.konradlorenz.administracion.puertos.salida.AuditoriaRepositoryPort;
import co.edu.konradlorenz.administracion.puertos.salida.LogsPort;
import co.edu.konradlorenz.administracion.puertos.salida.ParametrosRepositoryPort;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

public class AdministracionService implements AdministracionUseCase {

    /** Claves como «tema.color-primario» o «general.moneda». */
    private static final Pattern CLAVE = Pattern.compile("^[a-z][a-z0-9-]*(\\.[a-z0-9-]+)*$");
    private static final Pattern COLOR = Pattern.compile("^#[0-9A-Fa-f]{6}$");
    /** El logo es una ruta de la SPA (el logo de siempre) o la imagen misma, en una data URL. */
    private static final Pattern LOGO = Pattern.compile("^(/assets/[A-Za-z0-9_./-]+|data:image/(png|jpeg|webp|svg\\+xml);base64,[A-Za-z0-9+/=]+)$");
    /** Una imagen de 200 KB ocupa unos 273.000 caracteres en base64. */
    static final int MAXIMO_LOGO = 280_000;
    static final int MAXIMO_VALOR = 500;
    private static final String TEMA = "tema.";

    private final AuditoriaRepositoryPort repositorio;
    private final LogsPort logs;
    private final ParametrosRepositoryPort parametros;

    public AdministracionService(AuditoriaRepositoryPort repositorio, LogsPort logs, ParametrosRepositoryPort parametros) {
        this.repositorio = repositorio;
        this.logs = logs;
        this.parametros = parametros;
    }

    /**
     * Guarda el parámetro (HU-20), refresca la caché de esta réplica y deja la auditoría. Los mensajes son los
     * de PD-10. Los colores y el logo del tema se validan porque la SPA los aplica tal cual.
     */
    @Override
    public void parametrizar(String clave, String valor) {
        String c = clave == null ? "" : clave.trim();
        if (c.isEmpty()) {
            throw new IllegalArgumentException("La clave del parámetro es obligatoria.");
        }
        if (!CLAVE.matcher(c).matches() || c.length() > 80) {
            throw new IllegalArgumentException("La clave del parámetro solo puede tener minúsculas, números, guiones y puntos (por ejemplo, general.moneda).");
        }
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("El valor del parámetro " + c + " es obligatorio.");
        }
        String v = valor.trim();
        validar(c, v);
        parametros.guardar(c, v);
        ParametrosSistema.getInstancia().actualizar(c, v); // las demás réplicas la refrescan de la base cada minuto
        // ms-administracion no publica auditoria.evento: escribe directo en su bitácora (H10). Como en los demás
        // servicios, el núcleo pone «sistema» y el adaptador anota a quien inició sesión (DEC-64).
        LocalDateTime ahora = LocalDateTime.now();
        registrarAuditoria(new RegistroAuditoria("Parámetro " + c + " actualizado", "sistema", ahora.toLocalDate(), ahora.toLocalTime()));
    }

    private static void validar(String clave, String valor) {
        if (clave.startsWith(TEMA + "color") && !COLOR.matcher(valor).matches()) {
            throw new IllegalArgumentException("El color debe tener el formato #RRGGBB (por ejemplo, #F28705).");
        }
        if (clave.equals(TEMA + "logo")) {
            if (valor.length() > MAXIMO_LOGO || !LOGO.matcher(valor).matches()) {
                throw new IllegalArgumentException("El logo debe ser una imagen PNG, JPG, SVG o WebP de máximo 200 KB.");
            }
        } else if (valor.length() > MAXIMO_VALOR) {
            throw new IllegalArgumentException("El valor del parámetro " + clave + " no puede tener más de " + MAXIMO_VALOR + " caracteres.");
        }
    }

    @Override
    public void registrarAuditoria(RegistroAuditoria r) {
        repositorio.guardar(r);
    }

    /** HU-21 y PD-07: el rango es obligatorio y «desde» no puede ser posterior a «hasta»; lo más reciente primero. */
    @Override
    public List<RegistroAuditoria> auditoria(LocalDate desde, LocalDate hasta) {
        if (desde == null || hasta == null) {
            throw new IllegalArgumentException("Seleccione la fecha inicial y la fecha final.");
        }
        if (desde.isAfter(hasta)) {
            throw new IllegalArgumentException("La fecha inicial no puede ser posterior a la fecha final.");
        }
        return repositorio.entre(desde, hasta).stream()
                .sorted(Comparator.comparing(RegistroAuditoria::fecha).thenComparing(RegistroAuditoria::hora).reversed())
                .toList();
    }

    /** HU-22: el servicio es opcional; sin él, salen todos. */
    @Override
    public List<LogError> logs(String servicio) {
        return logs.logs(servicio == null || servicio.isBlank() ? null : servicio.trim());
    }

    /** Todos los parámetros, para la ventana del administrador (DEC-85). */
    @Override
    public Map<String, String> parametros() {
        return new TreeMap<>(cache().valores());
    }

    /** Solo los del tema: nombre, colores y logo, que la SPA aplica al iniciar sesión (DEC-86). */
    @Override
    public Map<String, String> tema() {
        Map<String, String> tema = new TreeMap<>();
        cache().valores().forEach((clave, valor) -> {
            if (clave.startsWith(TEMA)) {
                tema.put(clave, valor);
            }
        });
        return tema;
    }

    /**
     * Nota n_parametros: el Singleton es la caché de la base y cada réplica la refresca cada minuto. Se lee de
     * la caché; solo si está vacía (la réplica acaba de arrancar) se carga de la base.
     */
    private ParametrosSistema cache() {
        ParametrosSistema cache = ParametrosSistema.getInstancia();
        if (cache.valores().isEmpty()) {
            parametros.cargar().forEach(cache::actualizar);
        }
        return cache;
    }

}
