package inventario.modelo;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Representa un producto de la tienda. Esta clase la usamos en dos lugares:
 * en el inventario (el árbol), donde la cantidad son las unidades que hay en
 * bodega, y en el carrito de cada cliente, donde la cantidad son las
 * unidades que el cliente va a comprar.
 */
public class Producto {

    /** Formato en que mostramos las fechas: día/mes/año. */
    public static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/uuuu");

    /** Nombre del producto. Lo usamos como identificador, por eso no se puede repetir. */
    private String nombre;

    /** Precio de una unidad, en colones. No puede ser negativo. */
    private double precio;

    /** Categoría del producto, por ejemplo "Electrónica" o "Lácteos". */
    private String categoria;

    /** Fecha en que se vence. Queda en null si el producto no se vence (como una laptop). */
    private LocalDate fechaVencimiento;

    /** Cantidad de unidades. Tampoco puede ser negativa. */
    private int cantidad;

    /** Rutas de las imágenes del producto, todas dentro de la carpeta imagenes. */
    private final ArrayList<String> listaImagenes;

    /**
     * Crea un producto con todos sus datos.
     *
     * @param nombre           nombre del producto
     * @param precio           precio de una unidad
     * @param categoria        categoría del producto
     * @param fechaVencimiento fecha de vencimiento, o null si no se vence
     * @param cantidad         cantidad de unidades
     * @throws IllegalArgumentException si algún dato no es válido
     */
    public Producto(String nombre, double precio, String categoria, LocalDate fechaVencimiento, int cantidad) {
        // Aquí usamos los métodos de validación y no los setters, porque no es buena
        // práctica llamar métodos que se pueden sobrescribir desde un constructor
        this.nombre = validarTexto(nombre, "nombre");
        this.precio = validarPrecio(precio);
        this.categoria = validarTexto(categoria, "categoría");
        this.fechaVencimiento = fechaVencimiento;
        this.cantidad = validarCantidad(cantidad);
        this.listaImagenes = new ArrayList<>();
    }

    /**
     * Crea un producto que no se vence, por ejemplo algo de electrónica.
     *
     * @param nombre    nombre del producto
     * @param precio    precio de una unidad
     * @param categoria categoría del producto
     * @param cantidad  cantidad de unidades
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
     * @return la fecha de vencimiento, o null si el producto no se vence
     */
    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    public int getCantidad() {
        return cantidad;
    }

    /**
     * Devolvemos la lista de imágenes en modo solo lectura, para que nadie
     * la cambie desde afuera sin pasar por agregarImagen o eliminarImagen.
     *
     * @return las rutas de las imágenes
     */
    public List<String> getListaImagenes() {
        return Collections.unmodifiableList(listaImagenes);
    }

    // Setters (todos validan el dato antes de guardarlo)

    /**
     * @param nombre nuevo nombre del producto
     * @throws IllegalArgumentException si el nombre viene vacío
     */
    public void setNombre(String nombre) {
        this.nombre = validarTexto(nombre, "nombre");
    }

    /**
     * @param precio nuevo precio de una unidad
     * @throws IllegalArgumentException si el precio es negativo o no es un número válido
     */
    public void setPrecio(double precio) {
        this.precio = validarPrecio(precio);
    }

    /**
     * @param categoria nueva categoría
     * @throws IllegalArgumentException si la categoría viene vacía
     */
    public void setCategoria(String categoria) {
        this.categoria = validarTexto(categoria, "categoría");
    }

    /**
     * @param fechaVencimiento nueva fecha de vencimiento, o null si ya no se vence
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

    // Imágenes

    /**
     * Agrega una imagen al producto. Si la imagen ya estaba no la volvemos a
     * meter, para que no salga repetida.
     *
     * @param rutaImagen ruta de la imagen
     * @return true si se agregó, false si ya estaba
     */
    public boolean agregarImagen(String rutaImagen) {
        String ruta = validarTexto(rutaImagen, "ruta de la imagen");
        if (listaImagenes.contains(ruta)) {
            return false;
        }
        return listaImagenes.add(ruta);
    }

    /**
     * Quita una imagen del producto.
     *
     * @param rutaImagen ruta de la imagen que se quiere quitar
     * @return true si la imagen estaba y se quitó
     */
    public boolean eliminarImagen(String rutaImagen) {
        return rutaImagen != null && listaImagenes.remove(rutaImagen.trim());
    }

    /**
     * @return cuántas imágenes tiene el producto
     */
    public int getCantidadImagenes() {
        return listaImagenes.size();
    }

    // Existencias (estos métodos los agregamos en el avance 2 para el inventario)

    /**
     * @return true si todavía quedan unidades
     */
    public boolean tieneExistencias() {
        return cantidad > 0;
    }

    /**
     * Le suma unidades al producto. Lo usamos cuando se reabastece el
     * inventario o cuando un cliente devuelve algo de su carrito. Antes de
     * sumar revisamos que el resultado quepa en un int, porque si no el
     * número se desborda y queda negativo.
     *
     * @param unidades unidades que se suman, tiene que ser mayor que 0
     * @throws IllegalArgumentException si las unidades no son positivas o el total no cabe en un int
     */
    public void aumentarCantidad(int unidades) {
        if (unidades <= 0) {
            throw new IllegalArgumentException("Las unidades por agregar deben ser mayores que 0.");
        }
        if (unidades > Integer.MAX_VALUE - this.cantidad) {
            throw new IllegalArgumentException("No se pueden agregar tantas unidades: la cantidad máxima de un producto es "
                    + Integer.MAX_VALUE + ".");
        }
        this.cantidad += unidades;
    }

    /**
     * Le resta unidades al producto, por ejemplo cuando un cliente las mete
     * en su carrito. No dejamos que la cantidad quede negativa.
     *
     * @param unidades unidades que se restan, entre 1 y la cantidad que hay
     * @throws IllegalArgumentException si las unidades no son positivas o no alcanzan las existencias
     */
    public void disminuirCantidad(int unidades) {
        if (unidades <= 0) {
            throw new IllegalArgumentException("Las unidades por descontar deben ser mayores que 0.");
        }
        if (unidades > cantidad) {
            throw new IllegalArgumentException("No hay suficientes existencias de \"" + nombre
                    + "\". Disponibles: " + cantidad + ".");
        }
        this.cantidad -= unidades;
    }

    /**
     * Hace una copia del producto pero con otra cantidad. La necesitamos
     * para el carrito: si metiéramos el mismo objeto del inventario, al
     * cambiar la cantidad del carrito también cambiaría la del inventario.
     * Con la copia cada uno lleva su propia cantidad.
     *
     * @param nuevaCantidad cantidad que va a tener la copia
     * @return un producto nuevo con los mismos datos y la cantidad indicada
     */
    public Producto copiarConCantidad(int nuevaCantidad) {
        Producto copia = new Producto(nombre, precio, categoria, fechaVencimiento, nuevaCantidad);
        copia.listaImagenes.addAll(listaImagenes);
        return copia;
    }

    // Cálculos y consultas

    /**
     * Calcula cuánto vale el producto en total: precio por cantidad.
     *
     * @return el costo total
     */
    public double calcularCostoTotal() {
        return precio * cantidad;
    }

    /**
     * @return true si el producto tiene una fecha de vencimiento registrada
     */
    public boolean tieneFechaVencimiento() {
        return fechaVencimiento != null;
    }

    /**
     * Revisa si el producto ya se venció. Si se vence hoy todavía lo
     * consideramos bueno; se vence a partir de mañana.
     *
     * @return true si tiene fecha y esa fecha ya pasó
     */
    public boolean estaVencido() {
        return tieneFechaVencimiento() && fechaVencimiento.isBefore(LocalDate.now());
    }

    /**
     * Compara el nombre con otro sin importar mayúsculas ni espacios de más,
     * así "cafe britt" y "Cafe Britt " cuentan como el mismo nombre.
     *
     * @param otroNombre nombre con el que se compara
     * @return true si son el mismo nombre
     */
    public boolean tieneNombre(String otroNombre) {
        return otroNombre != null && nombre.equalsIgnoreCase(otroNombre.trim());
    }

    /**
     * Revisa si el nombre contiene un pedazo de texto. Lo usamos para la
     * búsqueda parcial, por ejemplo que "mouse" encuentre "Mouse inalámbrico".
     *
     * @param fragmento texto que se busca dentro del nombre
     * @return true si el nombre lo contiene
     */
    public boolean contieneEnNombre(String fragmento) {
        return fragmento != null && !fragmento.isBlank()
                && nombre.toLowerCase(Locale.ROOT).contains(fragmento.trim().toLowerCase(Locale.ROOT));
    }

    /**
     * @return la fecha de vencimiento en texto, con "(VENCIDO)" si ya pasó, o "No aplica"
     */
    public String getFechaVencimientoTexto() {
        if (!tieneFechaVencimiento()) {
            return "No aplica";
        }
        return fechaVencimiento.format(FORMATO_FECHA) + (estaVencido() ? " (VENCIDO)" : "");
    }

    /**
     * Le da formato de colones a un monto, por ejemplo ₡1,250.00.
     *
     * @param monto monto que se quiere mostrar
     * @return el monto con formato de moneda
     */
    public static String formatearMonto(double monto) {
        return String.format(Locale.US, "₡%,.2f", monto);
    }

    /**
     * Arma la ficha completa del producto para mostrarla en la consola.
     *
     * @return los datos del producto, uno por línea
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

    // Validaciones (son privadas porque solo las usa esta clase)

    /** Revisa que el texto no venga vacío y le quita los espacios de más. */
    private static String validarTexto(String texto, String campo) {
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException("El campo " + campo + " no puede estar vacío.");
        }
        return texto.trim();
    }

    /** Revisa que el precio no sea negativo. isFinite descarta valores raros como NaN o infinito. */
    private static double validarPrecio(double precio) {
        if (precio < 0 || !Double.isFinite(precio)) {
            throw new IllegalArgumentException("El precio debe ser un número mayor o igual a 0.");
        }
        return precio;
    }

    /** Revisa que la cantidad no sea negativa. */
    private static int validarCantidad(int cantidad) {
        if (cantidad < 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor o igual a 0.");
        }
        return cantidad;
    }
}
