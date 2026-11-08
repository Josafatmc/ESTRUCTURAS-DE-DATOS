package inventario.estructuras;

import inventario.modelo.Producto;

/**
 * Nodo del árbol binario de búsqueda (ArbolProductos). A diferencia del nodo
 * de la lista, este tiene dos hijos en vez de un "siguiente":
 * a la izquierda van los productos cuyo nombre es menor alfabéticamente y a
 * la derecha los que son mayores. El nombre del producto es la llave.
 */
public class NodoArbolProducto {

    /** El producto que guarda el nodo. Su nombre es la llave del árbol. */
    private Producto producto;

    /** Hijo izquierdo: aquí empiezan los nombres menores. Es null si no tiene. */
    private NodoArbolProducto izquierdo;

    /** Hijo derecho: aquí empiezan los nombres mayores. Es null si no tiene. */
    private NodoArbolProducto derecho;

    /**
     * Crea un nodo hoja, o sea, sin hijos. Todo nodo nuevo entra al árbol
     * así, porque en un ABB siempre se inserta en una hoja.
     *
     * @param producto producto que va a guardar el nodo
     */
    public NodoArbolProducto(Producto producto) {
        this.producto = producto;
        this.izquierdo = null;
        this.derecho = null;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public NodoArbolProducto getIzquierdo() {
        return izquierdo;
    }

    public void setIzquierdo(NodoArbolProducto izquierdo) {
        this.izquierdo = izquierdo;
    }

    public NodoArbolProducto getDerecho() {
        return derecho;
    }

    public void setDerecho(NodoArbolProducto derecho) {
        this.derecho = derecho;
    }

    /**
     * Revisa si el nodo es una hoja.
     *
     * @return true si no tiene ningún hijo
     */
    public boolean esHoja() {
        return izquierdo == null && derecho == null;
    }

    @Override
    public String toString() {
        return "NodoArbolProducto{" + producto + "}";
    }
}
