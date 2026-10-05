package inventario.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public final class GestorImagenes {

    /** Nombre del directorio de imágenes, relativo a la raíz del proyecto. */
    public static final String DIRECTORIO_IMAGENES = "D:\\github\\ESTRUCTURAS DE DATOS\\GestionInventarios\\GestionInventarios\\imagenes";

    /** Extensiones de archivo aceptadas como imagen. */
    private static final List<String> EXTENSIONES_VALIDAS = List.of(".png", ".jpg", ".jpeg", ".gif", ".bmp", ".webp");

    /** Constructor privado: esta clase solo contiene métodos estáticos. */
    private GestorImagenes() {
    }
    
    public static boolean existeDirectorio() {
        return Files.isDirectory(Paths.get(DIRECTORIO_IMAGENES));
    }

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

    public static String construirRuta(String entrada) {
        String limpia = entrada.trim().replace('\\', '/');
        if (!limpia.startsWith(DIRECTORIO_IMAGENES + "/")) {
            limpia = DIRECTORIO_IMAGENES + "/" + limpia;
        }
        return normalizar(Paths.get(limpia));
    }


    public static String validarRuta(String ruta) {
        if (!tieneExtensionValida(ruta)) {
            return "El archivo debe ser una imagen (" + String.join(", ", EXTENSIONES_VALIDAS) + ").";
        }
        Path base = Paths.get(DIRECTORIO_IMAGENES).toAbsolutePath().normalize();
        Path archivo = Paths.get(ruta).toAbsolutePath().normalize();
        if (!archivo.startsWith(base)) {
            return "La imagen debe ubicarse dentro del directorio \"" + DIRECTORIO_IMAGENES + "/\" del proyecto.";
        }
        if (!Files.isRegularFile(archivo)) {
            return "No se encontró el archivo \"" + ruta + "\" dentro del proyecto.";
        }
        return null;
    }

    private static boolean tieneExtensionValida(String ruta) {
        String minuscula = ruta.toLowerCase(Locale.ROOT);
        return EXTENSIONES_VALIDAS.stream().anyMatch(minuscula::endsWith);
    }

    private static String normalizar(Path ruta) {
        return ruta.normalize().toString().replace('\\', '/');
    }
}
