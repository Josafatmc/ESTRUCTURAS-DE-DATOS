package inventario.modelo;

import inventario.estructuras.ArbolProductos;
import inventario.estructuras.ColaClientes;
import inventario.estructuras.ListaProductos;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * La tienda en línea. Esta es la clase que une todas las estructuras:
 * el inventario es un ArbolProductos (ABB por nombre), los clientes que
 * esperan están en una ColaClientes (cola de prioridad) y cada cliente
 * lleva su carrito, que es una ListaProductos.
 * <p>
 * Así funciona una compra:
 * 1) El cliente escoge productos del inventario. Cuando los mete al
 *    carrito, las unidades se "apartan", o sea, se restan del inventario
 *    de una vez. Lo hicimos así para que otro cliente no pueda comprar
 *    unidades que ya están en un carrito.
 * 2) Si el cliente quita algo del carrito o cancela su registro, las
 *    unidades vuelven al inventario.
 * 3) Cuando termina, el cliente entra a la cola de prioridad.
 * 4) Al atenderlo se le hace la factura con todo lo de su carrito y el
 *    costo total acumulado.
 */
public class Tienda {

    /** Formato de fecha y hora que sale en las facturas. */
    private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm:ss");

    /** Nombre de la tienda, sale en el encabezado de la factura. */
    private String nombre;

    /** El inventario: un árbol binario de búsqueda de productos. */
    private final ArbolProductos inventario;

    /** Los clientes que están esperando: una cola de prioridad. */
    private final ColaClientes colaClientes;

    /** Cuántas facturas se han hecho. Lo usamos también como número de la siguiente factura. */
    private int facturasEmitidas;

    /** Cuánto se ha vendido en total desde que se abrió el programa. */
    private double totalVendido;

    /**
     * Crea una tienda con el inventario y la cola vacíos.
     *
     * @param nombre nombre de la tienda
     * @throws IllegalArgumentException si el nombre viene vacío
     */
    public Tienda(String nombre) {
        setNombre(nombre);
        this.inventario = new ArbolProductos();
        this.colaClientes = new ColaClientes();
        this.facturasEmitidas = 0;
        this.totalVendido = 0;
    }

    // Getters y setters

    public String getNombre() {
        return nombre;
    }

    /**
     * Lo dejamos final porque lo llamamos desde el constructor, y así nadie
     * lo puede sobrescribir en una subclase.
     *
     * @param nombre nombre nuevo de la tienda
     * @throws IllegalArgumentException si el nombre viene vacío
     */
    public final void setNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre de la tienda no puede estar vacío.");
        }
        this.nombre = nombre.trim();
    }

    public ArbolProductos getInventario() {
        return inventario;
    }

    public ColaClientes getColaClientes() {
        return colaClientes;
    }

    public int getFacturasEmitidas() {
        return facturasEmitidas;
    }

    public double getTotalVendido() {
        return totalVendido;
    }

    // Inventario

    /**
     * Mete un producto nuevo al inventario.
     *
     * @param producto producto que se agrega
     * @throws IllegalArgumentException si es null o ya hay un producto con ese nombre
     */
    public void agregarProductoAlInventario(Producto producto) {
        inventario.insertar(producto);
    }

    /**
     * Le suma unidades a un producto que ya está en el inventario.
     *
     * @param nombreProducto nombre del producto
     * @param unidades       unidades que llegan
     * @throws IllegalArgumentException si el producto no existe o las unidades no son válidas
     */
    public void reabastecerProducto(String nombreProducto, int unidades) {
        buscarProductoObligatorio(nombreProducto).aumentarCantidad(unidades);
    }

    /**
     * @param nombreProducto nombre del producto
     * @return el producto, o null si no está en el inventario
     */
    public Producto buscarProductoEnInventario(String nombreProducto) {
        return inventario.buscar(nombreProducto);
    }

    /**
     * Saca un producto del inventario. A los clientes que ya están en la
     * cola no les pasa nada, porque su carrito tiene copias de los productos
     * y no los mismos objetos del inventario.
     *
     * @param nombreProducto nombre del producto
     * @return el producto eliminado, o null si no estaba
     */
    public Producto eliminarProductoDelInventario(String nombreProducto) {
        return inventario.eliminar(nombreProducto);
    }

    // Compra (carrito del cliente)

    /**
     * Pasa unidades de un producto del inventario al carrito de un cliente,
     * y en ese mismo momento se descuentan del inventario.
     *
     * @param cliente        cliente que compra
     * @param nombreProducto nombre del producto
     * @param cantidad       unidades que compra
     * @throws IllegalArgumentException si falta el cliente, el producto no existe, está vencido o no alcanzan las unidades
     */
    public void agregarAlCarrito(Cliente cliente, String nombreProducto, int cantidad) {
        validarCliente(cliente);
        Producto productoInventario = buscarProductoObligatorio(nombreProducto);
        if (productoInventario.estaVencido()) {
            throw new IllegalArgumentException("El producto \"" + productoInventario.getNombre()
                    + "\" está vencido y no se puede vender.");
        }
        // Primero descontamos, porque eso revisa si hay suficientes unidades. Si no hay, el carrito queda igual
        productoInventario.disminuirCantidad(cantidad);
        try {
            cliente.agregarAlCarrito(productoInventario.copiarConCantidad(cantidad));
        } catch (IllegalArgumentException e) {
            productoInventario.aumentarCantidad(cantidad);       // si no se pudo agregar, devolvemos lo descontado
            throw e;
        }
    }

    /**
     * Quita unidades del carrito de un cliente y las devuelve al inventario.
     *
     * @param cliente        cliente dueño del carrito
     * @param nombreProducto nombre del producto
     * @param unidades       unidades que se quitan
     * @return cuántas unidades volvieron al inventario
     */
    public int quitarDelCarrito(Cliente cliente, String nombreProducto, int unidades) {
        validarCliente(cliente);
        Producto linea = cliente.getCarrito().buscar(nombreProducto);
        if (linea == null) {
            return 0;
        }
        int quitadas = cliente.quitarDelCarrito(nombreProducto, unidades);
        devolverAlInventario(linea, quitadas);
        return quitadas;
    }

    /**
     * Vacía el carrito y devuelve todo al inventario. Lo usamos cuando se
     * cancela el registro de un cliente. Vamos sacando el primero de la
     * lista hasta que quede vacía.
     *
     * @param cliente cliente al que se le vacía el carrito
     */
    public void vaciarCarrito(Cliente cliente) {
        validarCliente(cliente);
        ListaProductos carrito = cliente.getCarrito();
        while (!carrito.estaVacia()) {
            Producto linea = carrito.eliminarAlInicio();
            devolverAlInventario(linea, linea.getCantidad());
        }
    }

    // Cola de clientes

    /**
     * Mete en la cola a un cliente que ya llenó su carrito. No dejamos
     * entrar a nadie con el carrito vacío, porque no habría nada que facturar.
     *
     * @param cliente cliente que entra a la cola
     * @throws IllegalArgumentException si el carrito está vacío o la identificación ya está en la cola
     */
    public void registrarClienteEnCola(Cliente cliente) {
        if (cliente == null || cliente.tieneCarritoVacio()) {
            throw new IllegalArgumentException("El cliente debe tener al menos un producto en su carrito.");
        }
        colaClientes.encolar(cliente);
    }

    /**
     * @return true si hay clientes esperando
     */
    public boolean hayClientesEnEspera() {
        return !colaClientes.estaVacia();
    }

    /**
     * @return el próximo cliente que se va a atender (sin sacarlo de la cola), o null si no hay
     */
    public Cliente verSiguienteCliente() {
        return colaClientes.verSiguiente();
    }

    /**
     * Atiende al cliente que sigue según la prioridad y le hace la factura.
     *
     * @return el texto de la factura
     * @throws IllegalStateException si no hay clientes en la cola
     */
    public String atenderSiguienteCliente() {
        Cliente cliente = colaClientes.atender();
        if (cliente == null) {
            throw new IllegalStateException("No hay clientes en espera.");
        }
        facturasEmitidas++;
        double total = cliente.calcularTotalCarrito();
        totalVendido += total;
        return generarFactura(cliente, facturasEmitidas, total);
    }

    // Métodos de apoyo (privados)

    /**
     * Arma la factura: arriba los datos de la tienda y del cliente, en medio
     * la tabla del carrito (la misma de ListaProductos) y abajo el total.
     * Las líneas de la factura miden lo mismo que la tabla del carrito, así
     * todo queda alineado.
     */
    private String generarFactura(Cliente cliente, int numeroFactura, double total) {
        String detalleCarrito = cliente.getCarrito().generarReporteCostos();
        int ancho = detalleCarrito.indexOf(System.lineSeparator());     // largo de la primera línea de la tabla
        String lineaDoble = "=".repeat(ancho) + System.lineSeparator();
        String lineaSimple = "-".repeat(ancho) + System.lineSeparator();

        StringBuilder sb = new StringBuilder();
        sb.append(lineaDoble);
        sb.append(centrar(nombre.toUpperCase(), ancho)).append(System.lineSeparator());
        sb.append(centrar("FACTURA DE VENTA", ancho)).append(System.lineSeparator());
        sb.append(lineaDoble);
        sb.append(String.format("  Factura N.°        : F-%06d%n", numeroFactura));
        sb.append(String.format("  Fecha de emisión   : %s%n", LocalDateTime.now().format(FORMATO_FECHA_HORA)));
        sb.append(String.format("  Cliente            : %s%n", cliente.getNombre()));
        sb.append(String.format("  Identificación     : %s%n", cliente.getIdentificacion()));
        sb.append(String.format("  Tipo de cliente    : %s (prioridad %d)%n", cliente.getTipo(), cliente.getPrioridad()));
        sb.append(lineaSimple);
        sb.append("  DETALLE DEL CARRITO").append(System.lineSeparator());
        sb.append(detalleCarrito);
        sb.append(String.format("  Artículos comprados: %d%n", cliente.getTotalArticulos()));
        sb.append(String.format("  TOTAL A PAGAR      : %s%n", Producto.formatearMonto(total)));
        sb.append(lineaDoble);
        sb.append(centrar("¡Gracias por su compra!", ancho)).append(System.lineSeparator());
        sb.append(lineaDoble);
        return sb.toString();
    }

    /**
     * Devuelve unidades al inventario. Si mientras tanto alguien eliminó el
     * producto del inventario, lo volvemos a crear con esas unidades, para
     * que no se pierdan.
     */
    private void devolverAlInventario(Producto lineaCarrito, int unidades) {
        if (unidades <= 0) {
            return;
        }
        Producto productoInventario = inventario.buscar(lineaCarrito.getNombre());
        if (productoInventario != null) {
            productoInventario.aumentarCantidad(unidades);
        } else {
            inventario.insertar(lineaCarrito.copiarConCantidad(unidades));
        }
    }

    /** Todas las operaciones del carrito necesitan un cliente, así que revisamos que no venga null. */
    private static void validarCliente(Cliente cliente) {
        if (cliente == null) {
            throw new IllegalArgumentException("Debe indicar el cliente que realiza la compra.");
        }
    }

    /** Busca el producto y, si no existe, lanza el error de una vez para no repetir el if en cada método. */
    private Producto buscarProductoObligatorio(String nombreProducto) {
        Producto producto = inventario.buscar(nombreProducto);
        if (producto == null) {
            throw new IllegalArgumentException("No existe el producto \"" + nombreProducto + "\" en el inventario.");
        }
        return producto;
    }

    /** Le pone espacios adelante al texto para que quede centrado en la factura. */
    private static String centrar(String texto, int ancho) {
        int relleno = Math.max(0, (ancho - texto.length()) / 2);
        return " ".repeat(relleno) + texto;
    }

    @Override
    public String toString() {
        return String.format("%s | Productos en inventario: %d | Clientes en cola: %d | Facturas emitidas: %d",
                nombre, inventario.getTamanio(), colaClientes.getTamanio(), facturasEmitidas);
    }
}
