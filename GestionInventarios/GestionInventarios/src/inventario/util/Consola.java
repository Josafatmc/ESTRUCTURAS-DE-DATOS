package inventario.util;

import inventario.modelo.Producto;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Clase utilitaria para leer y validar datos ingresados por el usuario en la
 * consola. Cada método vuelve a solicitar el dato hasta que sea válido, de
 * modo que el programa nunca se detiene por una entrada incorrecta.
 */
public final class Consola {

    /** Único lector de la entrada estándar que usa toda la aplicación. */
    private static final Scanner ENTRADA = new Scanner(System.in);

    /** Constructor privado: esta clase solo contiene métodos estáticos. */
    private Consola() {
    }

    /**
     * Lee una línea de texto. Si la entrada estándar se cierra (p. ej. con
     * Ctrl+D / Ctrl+Z) se lanza una excepción controlada para finalizar el programa.
     */
    public static String leerLinea(String mensaje) {
        System.out.print(mensaje);
        try {
            return ENTRADA.nextLine().trim();
        } catch (NoSuchElementException e) {
            throw new EntradaFinalizadaException();
        }
    }

    /**
     * Lee un texto obligatorio (no vacío).
     */
    public static String leerTexto(String mensaje) {
        while (true) {
            String texto = leerLinea(mensaje);
            if (!texto.isEmpty()) {
                return texto;
            }
            mostrarError("Este dato es obligatorio. Intente de nuevo.");
        }
    }

    /**
     * Lee un número entero dentro de un rango.
     */
    public static int leerEntero(String mensaje, int minimo, int maximo) {
        while (true) {
            String texto = leerLinea(mensaje);
            try {
                int valor = Integer.parseInt(texto);
                if (valor >= minimo && valor <= maximo) {
                    return valor;
                }
                mostrarError("Debe ingresar un número entre " + minimo + " y " + maximo + ".");
            } catch (NumberFormatException e) {
                mostrarError("\"" + texto + "\" no es un número entero válido.");
            }
        }
    }

    /**
     * Lee un número decimal mayor o igual a un mínimo. Acepta tanto el punto
     * como la coma como separador decimal.
     */
    public static double leerDecimal(String mensaje, double minimo) {
        while (true) {
            String texto = leerLinea(mensaje);
            try {
                double valor = Double.parseDouble(texto.replace(',', '.'));
                if (valor >= minimo && Double.isFinite(valor)) {
                    return valor;
                }
                mostrarError("Debe ingresar un número mayor o igual a " + minimo + ".");
            } catch (NumberFormatException e) {
                mostrarError("\"" + texto + "\" no es un número válido (ejemplo: 1500.50).");
            }
        }
    }

    public static LocalDate leerFechaOpcional(String mensaje) {
        while (true) {
            String texto = leerLinea(mensaje);
            if (texto.isEmpty()) {
                return null;
            }
            try {
                // STRICT evita que fechas inexistentes (p. ej. 31/02/2026) se "corrijan" solas
                return LocalDate.parse(texto, Producto.FORMATO_FECHA.withResolverStyle(ResolverStyle.STRICT));
            } catch (DateTimeParseException e) {
                mostrarError("Fecha inválida. Use el formato dd/MM/aaaa (ejemplo: 25/12/2026).");
            }
        }
    }

    public static boolean confirmar(String mensaje) {
        while (true) {
            String respuesta = leerLinea(mensaje + " (s/n): ").toLowerCase();
            switch (respuesta) {
                case "s", "si", "sí" -> {
                    return true;
                }
                case "n", "no" -> {
                    return false;
                }
                default -> mostrarError("Responda con 's' (sí) o 'n' (no).");
            }
        }
    }

    /** Detiene la ejecución hasta que el usuario presione Enter. */
    public static void pausar() {
        leerLinea("\nPresione Enter para continuar...");
    }

    public static void mostrarTitulo(String titulo) {
        String linea = "=".repeat(Math.max(titulo.length() + 4, 50));
        System.out.println();
        System.out.println(linea);
        System.out.println("  " + titulo);
        System.out.println(linea);
    }

    /** @param mensaje mensaje de operación exitosa */
    public static void mostrarExito(String mensaje) {
        System.out.println("[OK] " + mensaje);
    }

    /** @param mensaje mensaje de error o advertencia */
    public static void mostrarError(String mensaje) {
        System.out.println("[X] " + mensaje);
    }

    /** @param mensaje mensaje informativo */
    public static void mostrarInfo(String mensaje) {
        System.out.println("[i] " + mensaje);
    }

    public static class EntradaFinalizadaException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public EntradaFinalizadaException() {
            super("La entrada estándar fue cerrada.");
        }
    }
}
