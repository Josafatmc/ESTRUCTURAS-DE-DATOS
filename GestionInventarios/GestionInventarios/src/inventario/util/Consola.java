package inventario.util;

import inventario.modelo.Producto;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Esta clase la usamos para leer lo que el usuario escribe en la consola.
 * La idea es que cada método vuelva a pedir el dato hasta que sea válido,
 * así el programa nunca se cae porque alguien escribió letras donde iba un
 * número o dejó un campo vacío.
 */
public final class Consola {

    /** Usamos un solo Scanner para todo el programa, porque si se abren varios sobre System.in dan problemas. */
    private static final Scanner ENTRADA = new Scanner(System.in);

    /** El constructor es privado porque esta clase solo tiene métodos estáticos. */
    private Consola() {
    }

    /**
     * Muestra un mensaje y lee una línea completa. Si la entrada se cierra
     * (por ejemplo con Ctrl+Z o Ctrl+D) lanzamos nuestra propia excepción
     * para que el main pueda terminar el programa de forma ordenada.
     *
     * @param mensaje texto que se le muestra al usuario
     * @return lo que escribió el usuario, sin espacios al inicio ni al final
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
     * Lee un texto que es obligatorio, o sea, que no puede quedar vacío.
     *
     * @param mensaje texto que se le muestra al usuario
     * @return el texto que escribió el usuario
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
     * Lee un número entero y revisa que esté dentro del rango que le
     * pasamos. Si el máximo es Integer.MAX_VALUE solo mencionamos el mínimo
     * en el mensaje, porque decirle al usuario "entre 0 y 2147483647" se ve raro.
     *
     * @param mensaje texto que se le muestra al usuario
     * @param minimo  valor más pequeño permitido
     * @param maximo  valor más grande permitido
     * @return el número que escribió el usuario
     */
    public static int leerEntero(String mensaje, int minimo, int maximo) {
        while (true) {
            String texto = leerLinea(mensaje);
            if (texto.isEmpty()) {
                mostrarError("Debe escribir un número.");
                continue;
            }
            try {
                int valor = Integer.parseInt(texto);
                if (valor >= minimo && valor <= maximo) {
                    return valor;
                }
                if (maximo == Integer.MAX_VALUE) {
                    mostrarError("Debe ingresar un número mayor o igual a " + minimo + ".");
                } else {
                    mostrarError("Debe ingresar un número entre " + minimo + " y " + maximo + ".");
                }
            } catch (NumberFormatException e) {
                mostrarError("\"" + texto + "\" no es un número entero válido.");
            }
        }
    }

    /**
     * Lee un número con decimales que sea mayor o igual al mínimo. Aceptamos
     * punto o coma como separador porque en Costa Rica mucha gente escribe
     * con coma.
     *
     * @param mensaje texto que se le muestra al usuario
     * @param minimo  valor más pequeño permitido
     * @return el número que escribió el usuario
     */
    public static double leerDecimal(String mensaje, double minimo) {
        while (true) {
            String texto = leerLinea(mensaje);
            if (texto.isEmpty()) {
                mostrarError("Debe escribir un número.");
                continue;
            }
            try {
                double valor = Double.parseDouble(texto.replace(',', '.'));
                if (valor >= minimo && Double.isFinite(valor)) {
                    return valor;
                }
                mostrarError("Debe ingresar un número mayor o igual a " + mostrarSinDecimalesSobrantes(minimo) + ".");
            } catch (NumberFormatException e) {
                mostrarError("\"" + texto + "\" no es un número válido (ejemplo: 1500.50).");
            }
        }
    }

    /**
     * Lee una fecha con el formato dd/MM/aaaa. Si el usuario deja el campo
     * vacío devolvemos null, porque hay productos que no se vencen.
     *
     * @param mensaje texto que se le muestra al usuario
     * @return la fecha que escribió el usuario, o null si la dejó en blanco
     */
    public static LocalDate leerFechaOpcional(String mensaje) {
        while (true) {
            String texto = leerLinea(mensaje);
            if (texto.isEmpty()) {
                return null;
            }
            try {
                // Con STRICT una fecha que no existe, como 31/02/2026, da error en vez de "arreglarse" sola
                return LocalDate.parse(texto, Producto.FORMATO_FECHA.withResolverStyle(ResolverStyle.STRICT));
            } catch (DateTimeParseException e) {
                mostrarError("Fecha inválida. Use el formato dd/MM/aaaa (ejemplo: 25/12/2026).");
            }
        }
    }

    /**
     * Hace una pregunta de sí o no y sigue preguntando hasta que la
     * respuesta sea válida.
     *
     * @param mensaje la pregunta
     * @return true si el usuario respondió que sí
     */
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

    /** Espera a que el usuario presione Enter, para que alcance a leer lo que salió en pantalla. */
    public static void pausar() {
        leerLinea("\nPresione Enter para continuar...");
    }

    /**
     * Imprime un título con una línea de "=" arriba y abajo.
     *
     * @param titulo texto del título
     */
    public static void mostrarTitulo(String titulo) {
        String linea = "=".repeat(Math.max(titulo.length() + 4, 50));
        System.out.println();
        System.out.println(linea);
        System.out.println("  " + titulo);
        System.out.println(linea);
    }

    /** @param mensaje mensaje para cuando algo salió bien */
    public static void mostrarExito(String mensaje) {
        System.out.println("[OK] " + mensaje);
    }

    /** @param mensaje mensaje para cuando algo salió mal o hay que advertir algo */
    public static void mostrarError(String mensaje) {
        System.out.println("[X] " + mensaje);
    }

    /** @param mensaje mensaje informativo */
    public static void mostrarInfo(String mensaje) {
        System.out.println("[i] " + mensaje);
    }

    /** Para que en los mensajes salga "0" y no "0.0" cuando el número no tiene decimales. */
    private static String mostrarSinDecimalesSobrantes(double numero) {
        return numero == Math.rint(numero) ? String.valueOf((long) numero) : String.valueOf(numero);
    }

    /**
     * Excepción que lanzamos cuando ya no hay más entrada para leer. La
     * atrapamos en el main para cerrar el programa sin mostrar un error feo.
     */
    public static class EntradaFinalizadaException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public EntradaFinalizadaException() {
            super("La entrada estándar fue cerrada.");
        }
    }
}
