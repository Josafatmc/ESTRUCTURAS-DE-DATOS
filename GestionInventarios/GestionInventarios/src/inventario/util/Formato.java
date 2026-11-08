package inventario.util;

/**
 * Métodos pequeños que usamos para armar las tablas de los reportes
 * (inventario, carrito, factura y cola). Los pusimos aquí para no repetir
 * el mismo código en cada clase.
 */
public final class Formato {

    /** El constructor es privado porque esta clase solo tiene métodos estáticos. */
    private Formato() {
    }

    /**
     * Corta un texto que no cabe en una columna y le pone "…" al final, para
     * que la tabla no se desacomode cuando un nombre es muy largo.
     *
     * @param texto  texto que queremos mostrar
     * @param maximo cantidad de caracteres que caben en la columna
     * @return el mismo texto si cabe, o el texto recortado si no
     */
    public static String recortar(String texto, int maximo) {
        if (texto == null) {
            return "";
        }
        return texto.length() <= maximo ? texto : texto.substring(0, maximo - 1) + "…";
    }

    /**
     * Arma la línea que separa las filas de una tabla, por ejemplo
     * "+-----+------+". Le sumamos 2 a cada ancho por el espacio que dejamos
     * a cada lado del texto dentro de la columna.
     *
     * @param anchos ancho de cada columna
     * @return la línea separadora con su salto de línea
     */
    public static String separador(int... anchos) {
        StringBuilder sb = new StringBuilder("+");
        for (int ancho : anchos) {
            sb.append("-".repeat(ancho + 2)).append('+');
        }
        return sb.append(System.lineSeparator()).toString();
    }
}
