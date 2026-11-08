package inventario.estructuras;

import inventario.modelo.Producto;

/**
 * Nodo de la lista enlazada simple (ListaProductos). Cada nodo guarda un
 * producto y una referencia al nodo que sigue, y así se van encadenando.
 * El último nodo de la lista tiene el siguiente en null.
 */
public class NodoProducto {

    /** El producto que guarda este nodo. */
    private Producto producto;

    /** El nodo que viene después; es null si este es el último. */
    private NodoProducto siguiente;

    /**
     * Crea un nodo suelto, que todavía no apunta a ningún otro.
     *
     * @param producto producto que va a guardar el nodo
     */
    public NodoProducto(Producto producto) {
        this(producto, null);
    }

    /**
     * Crea un nodo que ya apunta a otro. Lo usamos sobre todo para insertar
     * al inicio, donde el nodo nuevo tiene que apuntar a la cabeza vieja.
     *
     * @param producto  producto que va a guardar el nodo
     * @param siguiente nodo al que va a apuntar
     */
    public NodoProducto(Producto producto, NodoProducto siguiente) {
        this.producto = producto;
        this.siguiente = siguiente;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public NodoProducto getSiguiente() {
        return siguiente;
    }

    public void setSiguiente(NodoProducto siguiente) {
        this.siguiente = siguiente;
    }

    @Override
    public String toString() {
        return "NodoProducto{" + producto + "}";
    }
}
