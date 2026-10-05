package inventario.estructuras;

import inventario.modelo.Producto;

public class NodoProducto {

    /** Producto almacenado en este nodo. */
    private Producto producto;

    /** Referencia al siguiente nodo de la lista ({@code null} si es el último). */
    private NodoProducto siguiente;

    /**
     * Crea un nodo que contiene el producto indicado y que aún no apunta a
     * ningún otro nodo.
     */
    public NodoProducto(Producto producto) {
        this(producto, null);
    }

    /**
     * Crea un nodo que contiene el producto indicado y que apunta al nodo
     * siguiente recibido.
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
