package inventario.modelo;

import inventario.estructuras.ListaProductos;

/**
 * Un cliente de la tienda en línea.
 * <p>
 * Cada cliente tiene su propio carrito, que es una ListaProductos con lo
 * que escogió del inventario. Lo que va en el carrito son copias de los
 * productos del inventario, y la cantidad de cada copia es lo que el
 * cliente compró. Así el total del carrito es simplemente la suma de
 * precio por cantidad de cada línea.
 * <p>
 * La prioridad decide cuándo lo atienden en la cola:
 * 1 es cliente básico, 2 es afiliado a la tienda y 3 es premium (el premium
 * se atiende primero).
 */
public class Cliente {

    /** Prioridad de un cliente básico. */
    public static final int PRIORIDAD_BASICO = 1;

    /** Prioridad de un cliente afiliado a la tienda. */
    public static final int PRIORIDAD_AFILIADO = 2;

    /** Prioridad de un cliente premium. */
    public static final int PRIORIDAD_PREMIUM = 3;

    /** La prioridad más baja que dejamos poner. */
    public static final int PRIORIDAD_MINIMA = PRIORIDAD_BASICO;

    /** La prioridad más alta que dejamos poner. */
    public static final int PRIORIDAD_MAXIMA = PRIORIDAD_PREMIUM;

    /**
     * Identificación del cliente, por ejemplo la cédula. La usamos para que
     * no se registre dos veces a la misma persona en la cola. Es final
     * porque no tiene sentido que cambie.
     */
    private final String identificacion;

    /** Nombre del cliente. */
    private String nombre;

    /** Prioridad de atención: 1 básico, 2 afiliado o 3 premium. */
    private int prioridad;

    /** El carrito del cliente: una lista enlazada con los productos que compra. */
    private final ListaProductos carrito;

    /**
     * Crea un cliente con el carrito vacío.
     *
     * @param identificacion identificación del cliente
     * @param nombre         nombre del cliente
     * @param prioridad      prioridad de atención, de 1 a 3
     * @throws IllegalArgumentException si algún dato no es válido
     */
    public Cliente(String identificacion, String nombre, int prioridad) {
        this.identificacion = validarTexto(identificacion, "identificación");
        this.nombre = validarTexto(nombre, "nombre del cliente");
        this.prioridad = validarPrioridad(prioridad);
        this.carrito = new ListaProductos();
    }

    // Getters y setters

    public String getIdentificacion() {
        return identificacion;
    }

    public String getNombre() {
        return nombre;
    }

    /**
     * @param nombre nombre nuevo del cliente
     * @throws IllegalArgumentException si el nombre viene vacío
     */
    public void setNombre(String nombre) {
        this.nombre = validarTexto(nombre, "nombre del cliente");
    }

    public int getPrioridad() {
        return prioridad;
    }

    /**
     * Cambia la prioridad. Lo ideal es hacerlo antes de meter al cliente en
     * la cola, aunque si se cambia después igual se respeta, porque la cola
     * revisa las prioridades en el momento de atender.
     *
     * @param prioridad prioridad nueva, de 1 a 3
     * @throws IllegalArgumentException si la prioridad está fuera de rango
     */
    public void setPrioridad(int prioridad) {
        this.prioridad = validarPrioridad(prioridad);
    }

    /**
     * @return el carrito del cliente
     */
    public ListaProductos getCarrito() {
        return carrito;
    }

    /**
     * @return el tipo de cliente en palabras (Básico, Afiliado o Premium)
     */
    public String getTipo() {
        return describirPrioridad(prioridad);
    }

    // Carrito

    /**
     * Mete un producto en el carrito. Si el producto ya estaba, en vez de
     * repetir la línea le sumamos las unidades a la que ya existía. Aquí no
     * revisamos existencias: de eso se encarga la Tienda, que es la que
     * descuenta del inventario.
     *
     * @param productoComprado copia del producto, con la cantidad que se compra
     * @throws IllegalArgumentException si el producto es null o trae 0 unidades
     */
    public void agregarAlCarrito(Producto productoComprado) {
        if (productoComprado == null || productoComprado.getCantidad() <= 0) {
            throw new IllegalArgumentException("Debe agregar al menos una unidad al carrito.");
        }
        Producto lineaExistente = carrito.buscar(productoComprado.getNombre());
        if (lineaExistente == null) {
            carrito.insertarAlFinal(productoComprado);
        } else {
            lineaExistente.aumentarCantidad(productoComprado.getCantidad());
        }
    }

    /**
     * Quita unidades de un producto del carrito. Si se quitan todas (o más
     * de las que hay), la línea completa sale de la lista.
     *
     * @param nombreProducto nombre del producto
     * @param unidades       unidades que se quieren quitar
     * @return cuántas unidades se quitaron de verdad (0 si el producto no estaba)
     * @throws IllegalArgumentException si las unidades no son positivas
     */
    public int quitarDelCarrito(String nombreProducto, int unidades) {
        if (unidades <= 0) {
            throw new IllegalArgumentException("Las unidades por quitar deben ser mayores que 0.");
        }
        Producto linea = carrito.buscar(nombreProducto);
        if (linea == null) {
            return 0;
        }
        if (unidades >= linea.getCantidad()) {
            carrito.eliminar(linea.getNombre());
            return linea.getCantidad();
        }
        linea.disminuirCantidad(unidades);
        return unidades;
    }

    /**
     * @return true si el carrito no tiene nada
     */
    public boolean tieneCarritoVacio() {
        return carrito.estaVacia();
    }

    /**
     * @return lo que cuesta todo el carrito
     */
    public double calcularTotalCarrito() {
        return carrito.calcularCostoTotal();
    }

    /**
     * @return cuántas unidades lleva el carrito en total
     */
    public long getTotalArticulos() {
        return carrito.calcularTotalUnidades();
    }

    /**
     * Compara la identificación sin importar mayúsculas ni espacios.
     *
     * @param identificacion identificación con la que se compara
     * @return true si es la de este cliente
     */
    public boolean tieneIdentificacion(String identificacion) {
        return identificacion != null && this.identificacion.equalsIgnoreCase(identificacion.trim());
    }

    // Otros

    /**
     * Pasa el número de prioridad a palabras, para mostrarlo en los reportes.
     *
     * @param prioridad prioridad de 1 a 3
     * @return "Básico", "Afiliado" o "Premium" ("Desconocido" si el número no es válido)
     */
    public static String describirPrioridad(int prioridad) {
        return switch (prioridad) {
            case PRIORIDAD_BASICO -> "Básico";
            case PRIORIDAD_AFILIADO -> "Afiliado";
            case PRIORIDAD_PREMIUM -> "Premium";
            default -> "Desconocido";
        };
    }

    @Override
    public String toString() {
        return String.format("%s (%s) | %s - prioridad %d | %d artículo(s) | Total: %s",
                nombre, identificacion, getTipo(), prioridad, getTotalArticulos(),
                Producto.formatearMonto(calcularTotalCarrito()));
    }

    /** Revisa que el texto no venga vacío y le quita los espacios de más. */
    private static String validarTexto(String texto, String campo) {
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException("El campo " + campo + " no puede estar vacío.");
        }
        return texto.trim();
    }

    /** Revisa que la prioridad esté entre 1 y 3. */
    private static int validarPrioridad(int prioridad) {
        if (prioridad < PRIORIDAD_MINIMA || prioridad > PRIORIDAD_MAXIMA) {
            throw new IllegalArgumentException("La prioridad debe ser un número entre "
                    + PRIORIDAD_MINIMA + " y " + PRIORIDAD_MAXIMA + ".");
        }
        return prioridad;
    }
}
