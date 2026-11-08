package inventario.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Aquí está todo lo que tiene que ver con las imágenes de los productos.
 * Lo que queremos es que solo se puedan asociar imágenes que de verdad
 * existan y que estén dentro de la carpeta "imagenes" del proyecto, para que
 * el proyecto no dependa de archivos que están en otra parte de la compu.
 */
public final class GestorImagenes {

    /**
     * Lugares donde puede estar la carpeta de imágenes. Al principio teníamos
     * la ruta completa del disco D:, pero así el programa solo funcionaba en
     * una computadora. Ahora probamos estas rutas relativas, porque depende
     * de desde dónde se corra el programa: desde la carpeta del módulo o
     * desde la raíz del repositorio (que es lo que hace IntelliJ).
     */
    private static final String[] UBICACIONES_POSIBLES = {
            "imagenes",
            "GestionInventarios/imagenes",
            "GestionInventarios/GestionInventarios/imagenes"
    };

    /** Carpeta de imágenes que encontramos al arrancar el programa. */
    public static final String DIRECTORIO_IMAGENES = localizarDirectorio();

    /** Solo aceptamos archivos con estas extensiones. */
    private static final List<String> EXTENSIONES_VALIDAS = List.of(".png", ".jpg", ".jpeg", ".gif", ".bmp", ".webp");

    /** El constructor es privado porque esta clase solo tiene métodos estáticos. */
    private GestorImagenes() {
    }

    /**
     * Revisa si la carpeta de imágenes existe.
     *
     * @return true si la carpeta existe
     */
    public static boolean existeDirectorio() {
        return Files.isDirectory(Paths.get(DIRECTORIO_IMAGENES));
    }

    /**
     * Busca todas las imágenes que hay dentro de la carpeta (también en
     * subcarpetas) para mostrárselas al usuario y que pueda escoger una.
     *
     * @return rutas de las imágenes ordenadas alfabéticamente, o una lista vacía si no hay carpeta
     */
    public static List<String> listarImagenesDisponibles() {
        Path directorio = Paths.get(DIRECTORIO_IMAGENES);
        if (!Files.isDirectory(directorio)) {
            return List.of();
        }
        try (Stream<Path> archivos = Files.walk(directorio)) {
            return archivos
                    .filter(Files::isRegularFile)
                    .filter(ruta -> tieneExtensionValida(ruta.toString()))
                    .map(GestorImagenes::normalizar)
                    .sorted()
                    .collect(Collectors.toList());
        } catch (IOException e) {
            return List.of();
        }
    }

    /**
     * Arma la ruta de una imagen a partir de lo que escribió el usuario. Si
     * escribió solo el nombre del archivo (por ejemplo "cafe.png") le
     * pegamos la carpeta de imágenes adelante. Si escribió la ruta completa
     * y está dentro de la carpeta, la pasamos a la misma forma corta que
     * usa la lista de imágenes, así no se puede agregar la misma imagen dos
     * veces escrita de formas distintas.
     *
     * @param entrada texto que escribió el usuario
     * @return la ruta lista para validarla con {@link #validarRuta(String)}
     */
    public static String construirRuta(String entrada) {
        String limpia = entrada.trim().replace('\\', '/');
        try {
            Path ruta = Paths.get(limpia);
            if (!ruta.isAbsolute() && !limpia.startsWith(DIRECTORIO_IMAGENES + "/")) {
                ruta = Paths.get(DIRECTORIO_IMAGENES, limpia);
            }
            Path base = Paths.get(DIRECTORIO_IMAGENES).toAbsolutePath().normalize();
            Path absoluta = ruta.toAbsolutePath().normalize();
            if (absoluta.startsWith(base)) {
                return normalizar(Paths.get(DIRECTORIO_IMAGENES).resolve(base.relativize(absoluta)));
            }
            return normalizar(ruta);
        } catch (InvalidPathException e) {
            // Si trae caracteres raros (como < o >) la devolvemos tal cual y validarRuta la rechaza
            return limpia;
        }
    }

    /**
     * Revisa que la ruta sea de una imagen que existe dentro de la carpeta
     * del proyecto.
     *
     * @param ruta ruta de la imagen
     * @return null si todo está bien, o el mensaje de error que se le muestra al usuario
     */
    public static String validarRuta(String ruta) {
        if (!tieneExtensionValida(ruta)) {
            return "El archivo debe ser una imagen (" + String.join(", ", EXTENSIONES_VALIDAS) + ").";
        }
        Path base;
        Path archivo;
        try {
            base = Paths.get(DIRECTORIO_IMAGENES).toAbsolutePath().normalize();
            archivo = Paths.get(ruta).toAbsolutePath().normalize();
        } catch (InvalidPathException e) {
            return "La ruta \"" + ruta + "\" tiene caracteres que no se permiten en un nombre de archivo.";
        }
        // Con normalize() se resuelven los ".." y así nadie se puede salir de la carpeta
        if (!archivo.startsWith(base)) {
            return "La imagen debe ubicarse dentro del directorio \"" + DIRECTORIO_IMAGENES + "/\" del proyecto.";
        }
        if (!Files.isRegularFile(archivo)) {
            return "No se encontró el archivo \"" + ruta + "\" dentro del proyecto.";
        }
        return null;
    }

    /** Busca la primera de las ubicaciones posibles que exista de verdad. */
    private static String localizarDirectorio() {
        for (String ubicacion : UBICACIONES_POSIBLES) {
            if (Files.isDirectory(Paths.get(ubicacion))) {
                return ubicacion;
            }
        }
        return UBICACIONES_POSIBLES[0];
    }

    private static boolean tieneExtensionValida(String ruta) {
        String minuscula = ruta.toLowerCase(Locale.ROOT);
        return EXTENSIONES_VALIDAS.stream().anyMatch(minuscula::endsWith);
    }

    /** Usamos siempre "/" para que las rutas se vean igual en Windows y en otros sistemas. */
    private static String normalizar(Path ruta) {
        return ruta.normalize().toString().replace('\\', '/');
    }
}
