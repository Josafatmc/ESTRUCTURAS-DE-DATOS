package inventario.estructuras;

import inventario.modelo.Cliente;

/**
 * Nodo de la cola de clientes (ColaClientes). Funciona igual que el nodo de
 * la lista de productos: guarda un cliente y apunta al cliente que llegó
 * después de él.
 */
public class NodoCliente {

    /** El cliente que guarda este nodo. */
    private Cliente cliente;

    /** El cliente que llegó después; es null si este es el último de la cola. */
    private NodoCliente siguiente;

    /**
     * Crea un nodo que todavía no apunta a ningún otro.
     *
     * @param cliente cliente que va a guardar el nodo
     */
    public NodoCliente(Cliente cliente) {
        this.cliente = cliente;
        this.siguiente = null;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public NodoCliente getSiguiente() {
        return siguiente;
    }

    public void setSiguiente(NodoCliente siguiente) {
        this.siguiente = siguiente;
    }

    @Override
    public String toString() {
        return "NodoCliente{" + cliente + "}";
    }
}
