package inventario.estructuras;

import inventario.modelo.Producto;
import inventario.util.Formato;

import java.time.LocalDate;

/**
 * Nuestra lista enlazada simple de productos.
 * <p>
 * En el avance 1 esta era la estructura principal del programa. En el
 * avance 2 cada cliente tiene una de estas listas como su carrito, y el
 * árbol del inventario también la usa para devolver sus productos en orden.
 * <p>
 * Además de la cabeza guardamos una referencia a la cola (el último nodo).
 * Así, para insertar al final no hay que recorrer toda la lista. También
 * llevamos el tamaño en una variable para no tener que contar los nodos
 * cada vez que se necesita.
 */
public class ListaProductos {

    /** Primer nodo de la lista. Si la lista está vacía es null. */
    private NodoProducto cabeza;

    /** Último nodo de la lista. Si la lista está vacía es null. */
    private NodoProducto cola;

    /** Cuántos productos hay en la lista. */
    private int tamanio;

    /** Crea una lista vacía. */
    public ListaProductos() {
        this.cabeza = null;
        this.cola = null;
        this.tamanio = 0;
    }

    // Consultas básicas

    /**
     * @return true si la lista no tiene productos
     */
    public boolean estaVacia() {
        return cabeza == null;
    }

    /**
     * @return la cantidad de productos que hay en la lista
     */
    public int getTamanio() {
        return tamanio;
    }

    /**
     * @return el primer producto, o null si la lista está vacía
     */
    public Producto getPrimero() {
        return estaVacia() ? null : cabeza.getProducto();
    }

    /**
     * @return el último producto, o null si la lista está vacía
     */
    public Producto getUltimo() {
        return estaVacia() ? null : cola.getProducto();
    }

    // Inserción

    /**
     * Inserta un producto al inicio. El nodo nuevo apunta a la cabeza vieja
     * y pasa a ser la nueva cabeza. Enlazarlo es O(1), pero antes revisamos
     * que el nombre no esté repetido y eso sí obliga a recorrer la lista.
     *
     * @param producto producto que se quiere insertar
     * @throws IllegalArgumentException si el producto es null o ya hay uno con ese nombre
     */
    public void insertarAlInicio(Producto producto) {
        validarProductoNuevo(producto);
        NodoProducto nuevo = new NodoProducto(producto, cabeza);
        cabeza = nuevo;
        if (cola == null) {          // si la lista estaba vacía, el nuevo también es el último
            cola = nuevo;
        }
        tamanio++;
    }

    /**
     * Inserta un producto al final. Como tenemos la referencia a la cola,
     * solo hay que enganchar el nodo nuevo después de ella, sin recorrer la
     * lista (aparte de la revisión de nombres repetidos).
     *
     * @param producto producto que se quiere insertar
     * @throws IllegalArgumentException si el producto es null o ya hay uno con ese nombre
     */
    public void insertarAlFinal(Producto producto) {
        validarProductoNuevo(producto);
        NodoProducto nuevo = new NodoProducto(producto);
        if (estaVacia()) {
            cabeza = nuevo;
        } else {
            cola.setSiguiente(nuevo);
        }
        cola = nuevo;
        tamanio++;
    }

    /**
     * Inserta un producto en una posición específica (la 0 es el inicio).
     * Si la posición es 0 o es el final, reutilizamos los otros dos métodos.
     * Si es en medio, buscamos el nodo anterior y metemos el nuevo entre ese
     * nodo y el que le seguía.
     *
     * @param posicion posición donde va a quedar el producto, de 0 al tamaño
     * @param producto producto que se quiere insertar
     * @throws IndexOutOfBoundsException si la posición no existe
     * @throws IllegalArgumentException  si el producto es null o ya hay uno con ese nombre
     */
    public void insertarEnPosicion(int posicion, Producto producto) {
        if (posicion < 0 || posicion > tamanio) {
            throw new IndexOutOfBoundsException(
                    "Posición inválida: " + posicion + ". Debe estar entre 0 y " + tamanio + ".");
        }
        if (posicion == 0) {
            insertarAlInicio(producto);
        } else if (posicion == tamanio) {
            insertarAlFinal(producto);
        } else {
            validarProductoNuevo(producto);
            NodoProducto anterior = obtenerNodo(posicion - 1);
            anterior.setSiguiente(new NodoProducto(producto, anterior.getSiguiente()));
            tamanio++;
        }
    }

    // Búsqueda

    /**
     * Busca un producto por su nombre, recorriendo la lista desde la cabeza.
     *
     * @param nombre nombre del producto (no importan las mayúsculas)
     * @return el producto, o null si no está
     */
    public Producto buscar(String nombre) {
        NodoProducto nodo = buscarNodo(nombre);
        return nodo == null ? null : nodo.getProducto();
    }

    /**
     * @param nombre nombre del producto
     * @return true si hay un producto con ese nombre en la lista
     */
    public boolean existe(String nombre) {
        return buscarNodo(nombre) != null;
    }

    /**
     * Busca todos los productos cuyo nombre contiene un texto. Por ejemplo,
     * con "mouse" encuentra "Mouse inalámbrico Logitech". Tiene que revisar
     * todos los nodos, así que es O(n).
     *
     * @param texto pedazo del nombre que se busca
     * @return una lista nueva con lo que coincidió (puede quedar vacía)
     */
    public ListaProductos buscarCoincidencias(String texto) {
        ListaProductos coincidencias = new ListaProductos();
        if (texto == null || texto.isBlank()) {
            return coincidencias;
        }
        for (NodoProducto actual = cabeza; actual != null; actual = actual.getSiguiente()) {
            if (actual.getProducto().contieneEnNombre(texto)) {
                coincidencias.insertarAlFinal(actual.getProducto());
            }
        }
        return coincidencias;
    }

    /**
     * Dice en qué posición está un producto.
     *
     * @param nombre nombre del producto
     * @return la posición (empieza en 0), o -1 si no está
     */
    public int indiceDe(String nombre) {
        int indice = 0;
        for (NodoProducto actual = cabeza; actual != null; actual = actual.getSiguiente()) {
            if (actual.getProducto().tieneNombre(nombre)) {
                return indice;
            }
            indice++;
        }
        return -1;
    }

    /**
     * Devuelve el producto que está en una posición.
     *
     * @param posicion posición del producto (empieza en 0)
     * @return el producto de esa posición
     * @throws IndexOutOfBoundsException si la posición no existe
     */
    public Producto obtener(int posicion) {
        if (posicion < 0 || posicion >= tamanio) {
            throw new IndexOutOfBoundsException(
                    "Posición inválida: " + posicion + ". La lista tiene " + tamanio + " producto(s).");
        }
        return obtenerNodo(posicion).getProducto();
    }

    // Modificación (todos devuelven false si el producto no está en la lista)

    /**
     * Le cambia el nombre a un producto. Antes revisamos que el nombre nuevo
     * no lo tenga otro producto, porque el nombre funciona como identificador.
     * Sí dejamos cambiarlo por el mismo nombre con otras mayúsculas.
     *
     * @param nombreActual nombre que tiene ahora
     * @param nuevoNombre  nombre que va a tener
     * @return true si se cambió
     * @throws IllegalArgumentException si otro producto ya tiene el nombre nuevo
     */
    public boolean modificarNombre(String nombreActual, String nuevoNombre) {
        Producto producto = buscar(nombreActual);
        if (producto == null) {
            return false;
        }
        Producto otro = buscar(nuevoNombre);
        if (otro != null && otro != producto) {
            throw new IllegalArgumentException("Ya existe otro producto llamado \"" + otro.getNombre() + "\".");
        }
        producto.setNombre(nuevoNombre);
        return true;
    }

    /**
     * @param nombre      nombre del producto
     * @param nuevoPrecio precio nuevo
     * @return true si se cambió
     */
    public boolean modificarPrecio(String nombre, double nuevoPrecio) {
        Producto producto = buscar(nombre);
        if (producto == null) {
            return false;
        }
        producto.setPrecio(nuevoPrecio);
        return true;
    }

    /**
     * @param nombre         nombre del producto
     * @param nuevaCategoria categoría nueva
     * @return true si se cambió
     */
    public boolean modificarCategoria(String nombre, String nuevaCategoria) {
        Producto producto = buscar(nombre);
        if (producto == null) {
            return false;
        }
        producto.setCategoria(nuevaCategoria);
        return true;
    }

    /**
     * @param nombre     nombre del producto
     * @param nuevaFecha fecha nueva, o null para indicar que no se vence
     * @return true si se cambió
     */
    public boolean modificarFechaVencimiento(String nombre, LocalDate nuevaFecha) {
        Producto producto = buscar(nombre);
        if (producto == null) {
            return false;
        }
        producto.setFechaVencimiento(nuevaFecha);
        return true;
    }

    /**
     * @param nombre        nombre del producto
     * @param nuevaCantidad cantidad nueva
     * @return true si se cambió
     */
    public boolean modificarCantidad(String nombre, int nuevaCantidad) {
        Producto producto = buscar(nombre);
        if (producto == null) {
            return false;
        }
        producto.setCantidad(nuevaCantidad);
        return true;
    }

    /**
     * @param nombre     nombre del producto
     * @param rutaImagen imagen que se le agrega
     * @return true si se agregó (false si no existe el producto o ya tenía esa imagen)
     */
    public boolean agregarImagen(String nombre, String rutaImagen) {
        Producto producto = buscar(nombre);
        return producto != null && producto.agregarImagen(rutaImagen);
    }

    /**
     * @param nombre     nombre del producto
     * @param rutaImagen imagen que se le quita
     * @return true si se quitó
     */
    public boolean eliminarImagen(String nombre, String rutaImagen) {
        Producto producto = buscar(nombre);
        return producto != null && producto.eliminarImagen(rutaImagen);
    }

    // Eliminación

    /**
     * Elimina un producto por su nombre. Mientras recorremos vamos guardando
     * el nodo anterior, porque en una lista simple no hay forma de volver
     * atrás y lo necesitamos para "saltarnos" el nodo que se va.
     *
     * @param nombre nombre del producto
     * @return el producto que se eliminó, o null si no estaba
     */
    public Producto eliminar(String nombre) {
        NodoProducto anterior = null;
        NodoProducto actual = cabeza;

        // Avanzamos hasta encontrar el producto, siempre con el anterior a mano
        while (actual != null && !actual.getProducto().tieneNombre(nombre)) {
            anterior = actual;
            actual = actual.getSiguiente();
        }
        if (actual == null) {
            return null;                                   // llegamos al final y no estaba
        }

        if (anterior == null) {
            cabeza = actual.getSiguiente();                // era la cabeza, la cabeza pasa al siguiente
        } else {
            anterior.setSiguiente(actual.getSiguiente());  // el anterior ahora apunta al que sigue
        }
        if (actual == cola) {
            cola = anterior;                               // si era el último, el anterior pasa a ser la cola
        }
        actual.setSiguiente(null);                         // soltamos el nodo eliminado
        tamanio--;
        return actual.getProducto();
    }

    /**
     * Elimina el primer producto. Es rápido porque el producto está justo
     * en la cabeza.
     *
     * @return el producto eliminado, o null si la lista estaba vacía
     */
    public Producto eliminarAlInicio() {
        if (estaVacia()) {
            return null;
        }
        return eliminar(cabeza.getProducto().getNombre());
    }

    /**
     * Elimina el último producto. Aunque tenemos la cola, igual hay que
     * recorrer la lista para encontrar el nodo anterior a ella, así que es O(n).
     *
     * @return el producto eliminado, o null si la lista estaba vacía
     */
    public Producto eliminarAlFinal() {
        if (estaVacia()) {
            return null;
        }
        return eliminar(cola.getProducto().getNombre());
    }

    /** Deja la lista vacía. Basta con soltar la cabeza y la cola. */
    public void vaciar() {
        cabeza = null;
        cola = null;
        tamanio = 0;
    }

    // Recorridos y reportes

    /** Imprime todos los productos de la lista, uno por línea. */
    public void imprimirLista() {
        System.out.println(toString());
    }

    /**
     * Suma el costo total (precio por cantidad) de todos los productos.
     *
     * @return el costo de toda la lista
     */
    public double calcularCostoTotal() {
        double total = 0;
        for (NodoProducto actual = cabeza; actual != null; actual = actual.getSiguiente()) {
            total += actual.getProducto().calcularCostoTotal();
        }
        return total;
    }

    /**
     * Suma las unidades de todos los productos, por ejemplo cuántos artículos
     * lleva un carrito. Usamos long porque la suma de varios int muy grandes
     * se podría pasar del límite de un int.
     *
     * @return el total de unidades
     */
    public long calcularTotalUnidades() {
        long total = 0;
        for (NodoProducto actual = cabeza; actual != null; actual = actual.getSiguiente()) {
            total += actual.getProducto().getCantidad();
        }
        return total;
    }

    /**
     * Arma una tabla con cada producto, su precio, su cantidad y su costo
     * total, y al final una fila con el total acumulado. Esta misma tabla es
     * la que sale en la factura del cliente.
     *
     * @return la tabla lista para imprimir
     */
    public String generarReporteCostos() {
        final String formatoFila = "| %-3s | %-26s | %-16s | %15s | %8s | %17s |%n";
        final String separador = Formato.separador(3, 26, 16, 15, 8, 17);

        StringBuilder sb = new StringBuilder();
        sb.append(separador);
        sb.append(String.format(formatoFila, "#", "Producto", "Categoría", "Precio unitario", "Cantidad", "Costo total"));
        sb.append(separador);

        if (estaVacia()) {
            sb.append(String.format("| %-" + (separador.trim().length() - 4) + "s |%n",
                    "La lista no contiene productos."));
        } else {
            int posicion = 1;
            long totalUnidades = 0;
            double costoAcumulado = 0;
            for (NodoProducto actual = cabeza; actual != null; actual = actual.getSiguiente()) {
                Producto p = actual.getProducto();
                double costoProducto = p.calcularCostoTotal();
                sb.append(String.format(formatoFila,
                        posicion++,
                        recortar(p.getNombre(), 26),
                        recortar(p.getCategoria(), 16),
                        Producto.formatearMonto(p.getPrecio()),
                        p.getCantidad(),
                        Producto.formatearMonto(costoProducto)));
                totalUnidades += p.getCantidad();
                costoAcumulado += costoProducto;
            }
            sb.append(separador);
            sb.append(String.format(formatoFila, "", "TOTAL (" + tamanio + " producto(s))", "", "",
                    totalUnidades, Producto.formatearMonto(costoAcumulado)));
        }
        sb.append(separador);
        return sb.toString();
    }

    /** Imprime la tabla de costos en la consola. */
    public void imprimirReporteCostos() {
        System.out.print(generarReporteCostos());
    }

    /**
     * @return los productos numerados, uno por línea
     */
    @Override
    public String toString() {
        if (estaVacia()) {
            return "La lista de productos está vacía.";
        }
        StringBuilder sb = new StringBuilder();
        int posicion = 1;
        for (NodoProducto actual = cabeza; actual != null; actual = actual.getSiguiente()) {
            sb.append(String.format("%3d. %s%n", posicion++, actual.getProducto()));
        }
        sb.append("Total de productos en la lista: ").append(tamanio);
        return sb.toString();
    }

    // Métodos de apoyo (privados)

    /** Recorre la lista desde la cabeza hasta encontrar el nodo con ese nombre. */
    private NodoProducto buscarNodo(String nombre) {
        if (nombre == null) {
            return null;
        }
        for (NodoProducto actual = cabeza; actual != null; actual = actual.getSiguiente()) {
            if (actual.getProducto().tieneNombre(nombre)) {
                return actual;
            }
        }
        return null;
    }

    /** Avanza desde la cabeza tantas veces como diga la posición. Quien lo llama ya validó la posición. */
    private NodoProducto obtenerNodo(int posicion) {
        NodoProducto actual = cabeza;
        for (int i = 0; i < posicion; i++) {
            actual = actual.getSiguiente();
        }
        return actual;
    }

    /** Antes de insertar revisamos que el producto no sea null y que su nombre no esté repetido. */
    private void validarProductoNuevo(Producto producto) {
        if (producto == null) {
            throw new IllegalArgumentException("No se puede insertar un producto nulo.");
        }
        if (existe(producto.getNombre())) {
            throw new IllegalArgumentException(
                    "Ya existe un producto llamado \"" + producto.getNombre() + "\" en la lista.");
        }
    }

    private static String recortar(String texto, int maximo) {
        return Formato.recortar(texto, maximo);
    }
}
