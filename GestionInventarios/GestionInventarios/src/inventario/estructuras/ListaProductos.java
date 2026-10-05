package inventario.estructuras;

import inventario.modelo.Producto;

import java.time.LocalDate;


public class ListaProductos {

    /** Primer nodo de la lista ({@code null} si la lista está vacía). */
    private NodoProducto cabeza;

    /** Último nodo de la lista ({@code null} si la lista está vacía). */
    private NodoProducto cola;

    /** Cantidad de nodos (productos) que contiene la lista. */
    private int tamanio;

    /** Crea una lista de productos vacía. */
    public ListaProductos() {
        this.cabeza = null;
        this.cola = null;
        this.tamanio = 0;
    }

    // Consultas básicas
    

    /**
     * @return {@code true} si la lista no contiene productos. Complejidad O(1).
     */
    public boolean estaVacia() {
        return cabeza == null;
    }

    /**
     * @return cantidad de productos en la lista. Complejidad O(1).
     */
    public int getTamanio() {
        return tamanio;
    }

    /**
     * @return el primer producto de la lista, o {@code null} si está vacía
     */
    public Producto getPrimero() {
        return estaVacia() ? null : cabeza.getProducto();
    }

    /**
     * @return el último producto de la lista, o {@code null} si está vacía
     */
    public Producto getUltimo() {
        return estaVacia() ? null : cola.getProducto();
    }

    // Inserción
    
    /**
     * Inserta un producto al inicio de la lista. Complejidad O(n) por la
     * verificación de duplicados; el enlace en sí es O(1).
     *
     * @param producto producto que se desea insertar
     * @throws IllegalArgumentException si el producto es nulo o su nombre ya existe en la lista
     */
    public void insertarAlInicio(Producto producto) {
        validarProductoNuevo(producto);
        NodoProducto nuevo = new NodoProducto(producto, cabeza);
        cabeza = nuevo;
        if (cola == null) {          // la lista estaba vacía
            cola = nuevo;
        }
        tamanio++;
    }

    /**
     * Inserta un producto al final de la lista. Gracias a la referencia
     * {@code cola}, el enlace es O(1) (la verificación de duplicados es O(n)).
     *
     * @param producto producto que se desea insertar
     * @throws IllegalArgumentException si el producto es nulo o su nombre ya existe en la lista
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
    
    public Producto buscar(String nombre) {
        NodoProducto nodo = buscarNodo(nombre);
        return nodo == null ? null : nodo.getProducto();
    }

    public boolean existe(String nombre) {
        return buscarNodo(nombre) != null;
    }

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

    public Producto obtener(int posicion) {
        if (posicion < 0 || posicion >= tamanio) {
            throw new IndexOutOfBoundsException(
                    "Posición inválida: " + posicion + ". La lista tiene " + tamanio + " producto(s).");
        }
        return obtenerNodo(posicion).getProducto();
    }

    
    // Modificación
    
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

    public boolean modificarPrecio(String nombre, double nuevoPrecio) {
        Producto producto = buscar(nombre);
        if (producto == null) {
            return false;
        }
        producto.setPrecio(nuevoPrecio);
        return true;
    }

    public boolean modificarCategoria(String nombre, String nuevaCategoria) {
        Producto producto = buscar(nombre);
        if (producto == null) {
            return false;
        }
        producto.setCategoria(nuevaCategoria);
        return true;
    }

    public boolean modificarFechaVencimiento(String nombre, LocalDate nuevaFecha) {
        Producto producto = buscar(nombre);
        if (producto == null) {
            return false;
        }
        producto.setFechaVencimiento(nuevaFecha);
        return true;
    }

    public boolean modificarCantidad(String nombre, int nuevaCantidad) {
        Producto producto = buscar(nombre);
        if (producto == null) {
            return false;
        }
        producto.setCantidad(nuevaCantidad);
        return true;
    }

    public boolean agregarImagen(String nombre, String rutaImagen) {
        Producto producto = buscar(nombre);
        return producto != null && producto.agregarImagen(rutaImagen);
    }

    public boolean eliminarImagen(String nombre, String rutaImagen) {
        Producto producto = buscar(nombre);
        return producto != null && producto.eliminarImagen(rutaImagen);
    }

   
    // Eliminación
    
    public Producto eliminar(String nombre) {
        NodoProducto anterior = null;
        NodoProducto actual = cabeza;

        // Recorrer la lista conservando la referencia al nodo anterior
        while (actual != null && !actual.getProducto().tieneNombre(nombre)) {
            anterior = actual;
            actual = actual.getSiguiente();
        }
        if (actual == null) {
            return null;                                   // no se encontró
        }

        if (anterior == null) {
            cabeza = actual.getSiguiente();                // se elimina la cabeza
        } else {
            anterior.setSiguiente(actual.getSiguiente());  // se "salta" el nodo
        }
        if (actual == cola) {
            cola = anterior;                               // se eliminó el último
        }
        actual.setSiguiente(null);                         // desenlazar el nodo eliminado
        tamanio--;
        return actual.getProducto();
    }

    /**
     * Elimina el primer producto de la lista. Complejidad O(1).
     *
     * @return el producto eliminado, o {@code null} si la lista estaba vacía
     */
    public Producto eliminarAlInicio() {
        if (estaVacia()) {
            return null;
        }
        return eliminar(cabeza.getProducto().getNombre());
    }

    public Producto eliminarAlFinal() {
        if (estaVacia()) {
            return null;
        }
        return eliminar(cola.getProducto().getNombre());
    }

    /** Elimina todos los productos de la lista. Complejidad O(1). */
    public void vaciar() {
        cabeza = null;
        cola = null;
        tamanio = 0;
    }

    // Recorrido y reportes
    
    public void imprimirLista() {
        System.out.println(toString());
    }

    public double calcularCostoTotal() {
        double total = 0;
        for (NodoProducto actual = cabeza; actual != null; actual = actual.getSiguiente()) {
            total += actual.getProducto().calcularCostoTotal();
        }
        return total;
    }

    public String generarReporteCostos() {
        final String formatoFila = "| %-3s | %-24s | %-14s | %15s | %8s | %17s |%n";
        final String separador = "+-----+--------------------------+----------------+-----------------+----------+-------------------+"
                + System.lineSeparator();

        StringBuilder sb = new StringBuilder();
        sb.append(separador);
        sb.append(String.format(formatoFila, "#", "Producto", "Categoría", "Precio unitario", "Cantidad", "Costo total"));
        sb.append(separador);

        if (estaVacia()) {
            sb.append(String.format("| %-95s |%n", "La lista no contiene productos."));
        } else {
            int posicion = 1;
            int totalUnidades = 0;
            double costoAcumulado = 0;
            for (NodoProducto actual = cabeza; actual != null; actual = actual.getSiguiente()) {
                Producto p = actual.getProducto();
                double costoProducto = p.calcularCostoTotal();
                sb.append(String.format(formatoFila,
                        posicion++,
                        recortar(p.getNombre(), 24),
                        recortar(p.getCategoria(), 14),
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

    /**
     * Recorre la lista e imprime en consola el reporte de costos totales de
     * cada producto (según su cantidad) y el costo total acumulado de la
     * lista completa.
     */
    public void imprimirReporteCostos() {
        System.out.print(generarReporteCostos());
    }

    /**
     * @return representación de la lista con un producto por línea
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

    // Métodos auxiliares privados
    
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

    private NodoProducto obtenerNodo(int posicion) {
        NodoProducto actual = cabeza;
        for (int i = 0; i < posicion; i++) {
            actual = actual.getSiguiente();
        }
        return actual;
    }

    /**
     * Verifica que un producto se pueda insertar: no debe ser nulo ni repetir
     * el nombre de otro producto ya existente en la lista.
     */
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
        return texto.length() <= maximo ? texto : texto.substring(0, maximo - 1) + "…";
    }
}
