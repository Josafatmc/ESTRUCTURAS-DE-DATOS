package inventario.modelo;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class Producto {

    /** Formato estándar (dd/MM/yyyy) usado para mostrar fechas al usuario. */
    public static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/uuuu");

    /** Nombre del producto; funciona como identificador único dentro de la lista. */
    private String nombre;

    /** Precio unitario del producto, en colones (₡). Nunca es negativo. */
    private double precio;

    /** Categoría a la que pertenece el producto (p. ej. "Electrónica", "Lácteos"). */
    private String categoria;

    /** Fecha de vencimiento del producto. Es {@code null} cuando no aplica. */
    private LocalDate fechaVencimiento;

    /** Cantidad de unidades del producto. Nunca es negativa. */
    private int cantidad;

    /** Rutas relativas de las imágenes del producto (dentro de {@code imagenes/}). */
    private final ArrayList<String> listaImagenes;

   
    public Producto(String nombre, double precio, String categoria, LocalDate fechaVencimiento, int cantidad) {
        // Se usan los validadores estáticos (y no los setters) para no invocar
        // métodos sobrescribibles desde el constructor.
        this.nombre = validarTexto(nombre, "nombre");
        this.precio = validarPrecio(precio);
        this.categoria = validarTexto(categoria, "categoría");
        this.fechaVencimiento = fechaVencimiento;
        this.cantidad = validarCantidad(cantidad);
        this.listaImagenes = new ArrayList<>();
    }

    /**
     * Crea un producto que no tiene fecha de vencimiento (p. ej. un artículo
     * electrónico).
     */
    public Producto(String nombre, double precio, String categoria, int cantidad) {
        this(nombre, precio, categoria, null, cantidad);
    }

    // Getters

    public String getNombre() {
        return nombre;
    }

    public double getPrecio() {
        return precio;
    }

    public String getCategoria() {
        return categoria;
    }

    /**
     * @return la fecha de vencimiento, o {@code null} si el producto no vence
     */
    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    public int getCantidad() {
        return cantidad;
    }

    public List<String> getListaImagenes() {
        return Collections.unmodifiableList(listaImagenes);
    }

    // Setters con validación
   
    public void setNombre(String nombre) {
        this.nombre = validarTexto(nombre, "nombre");
    }

    /**
     * @param precio nuevo precio unitario
     * @throws IllegalArgumentException si el precio es negativo o no es un número válido
     */
    public void setPrecio(double precio) {
        this.precio = validarPrecio(precio);
    }

    /**
     * @param categoria nueva categoría; se eliminan los espacios sobrantes
     * @throws IllegalArgumentException si la categoría es nula o vacía
     */
    public void setCategoria(String categoria) {
        this.categoria = validarTexto(categoria, "categoría");
    }

    /**
     * @param fechaVencimiento nueva fecha de vencimiento, o {@code null} si no aplica
     */
    public void setFechaVencimiento(LocalDate fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    /**
     * @param cantidad nueva cantidad de unidades
     * @throws IllegalArgumentException si la cantidad es negativa
     */
    public void setCantidad(int cantidad) {
        this.cantidad = validarCantidad(cantidad);
    }

    // Gestión de la lista de imágenes

    /**
     * Agrega la ruta de una imagen a la lista de imágenes del producto.
     */
    public boolean agregarImagen(String rutaImagen) {
        String ruta = validarTexto(rutaImagen, "ruta de la imagen");
        if (listaImagenes.contains(ruta)) {
            return false;
        }
        return listaImagenes.add(ruta);
    }

    /**
     * Elimina la ruta de una imagen de la lista de imágenes del producto.
     */
    public boolean eliminarImagen(String rutaImagen) {
        return rutaImagen != null && listaImagenes.remove(rutaImagen.trim());
    }

    /**
     * @return cantidad de imágenes asociadas al producto
     */
    public int getCantidadImagenes() {
        return listaImagenes.size();
    }

    // Métodos de negocio
    /**
     * Calcula el costo total del producto en función de su cantidad
     * (precio unitario × cantidad).
     *
     * @return costo total del producto
     */
    public double calcularCostoTotal() {
        return precio * cantidad;
    }

    /**
     * @return {@code true} si el producto tiene fecha de vencimiento registrada
     */
    public boolean tieneFechaVencimiento() {
        return fechaVencimiento != null;
    }

    /**
     * @return {@code true} si el producto tiene fecha de vencimiento y esta ya pasó
     */
    public boolean estaVencido() {
        return tieneFechaVencimiento() && fechaVencimiento.isBefore(LocalDate.now());
    }

    /**
     * Compara el nombre del producto con otro nombre, sin distinguir
     * mayúsculas/minúsculas ni espacios sobrantes.
     */
    public boolean tieneNombre(String otroNombre) {
        return otroNombre != null && nombre.equalsIgnoreCase(otroNombre.trim());
    }

    /**
     * @return la fecha de vencimiento formateada, o "No aplica" si no tiene
     */
    public String getFechaVencimientoTexto() {
        if (!tieneFechaVencimiento()) {
            return "No aplica";
        }
        return fechaVencimiento.format(FORMATO_FECHA) + (estaVencido() ? " (VENCIDO)" : "");
    }

    /**
     * Da formato de moneda (colones costarricenses) a un monto.
     */
    public static String formatearMonto(double monto) {
        return String.format(Locale.US, "₡%,.2f", monto);
    }

    /**
     * Devuelve una ficha detallada del producto, apta para mostrarse en consola.
     *
     * @return descripción completa del producto en varias líneas
     */
    public String toDetalle() {
        StringBuilder sb = new StringBuilder();
        sb.append("  Nombre            : ").append(nombre).append('\n');
        sb.append("  Precio unitario   : ").append(formatearMonto(precio)).append('\n');
        sb.append("  Categoría         : ").append(categoria).append('\n');
        sb.append("  Fecha vencimiento : ").append(getFechaVencimientoTexto()).append('\n');
        sb.append("  Cantidad          : ").append(cantidad).append(" unidad(es)").append('\n');
        sb.append("  Costo total       : ").append(formatearMonto(calcularCostoTotal())).append('\n');
        sb.append("  Imágenes (").append(listaImagenes.size()).append(")");
        if (listaImagenes.isEmpty()) {
            sb.append("      : (sin imágenes)");
        } else {
            for (int i = 0; i < listaImagenes.size(); i++) {
                sb.append('\n').append("     ").append(i + 1).append(". ").append(listaImagenes.get(i));
            }
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return String.format("%s | %s | %s | Vence: %s | Cant.: %d | Imágenes: %d",
                nombre, formatearMonto(precio), categoria, getFechaVencimientoTexto(),
                cantidad, listaImagenes.size());
    }

    // Utilidades privadas


    /**
     * Verifica que un texto no sea nulo ni vacío y le elimina los espacios sobrantes.
     */
    private static String validarTexto(String texto, String campo) {
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException("El campo " + campo + " no puede estar vacío.");
        }
        return texto.trim();
    }

    private static double validarPrecio(double precio) {
        if (precio < 0 || !Double.isFinite(precio)) {
            throw new IllegalArgumentException("El precio debe ser un número mayor o igual a 0.");
        }
        return precio;
    }

    private static int validarCantidad(int cantidad) {
        if (cantidad < 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor o igual a 0.");
        }
        return cantidad;
    }
}
