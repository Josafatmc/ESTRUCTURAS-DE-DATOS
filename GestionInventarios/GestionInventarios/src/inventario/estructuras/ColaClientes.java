package inventario.estructuras;

import inventario.modelo.Cliente;
import inventario.modelo.Producto;
import inventario.util.Formato;

/**
 * Cola de prioridad de clientes, hecha con nodos enlazados.
 * <p>
 * Los clientes se guardan en el orden en que llegaron: siempre entran al
 * final y el frente es el que llegó primero. Lo que la hace "de prioridad"
 * es la forma de atender: no sale el del frente, sino el que tiene la
 * prioridad más alta (3 premium, 2 afiliado, 1 básico). Si hay varios con
 * la misma prioridad, sale el que está más cerca del frente, como pide la
 * consigna.
 * <p>
 * Cómo lo pensamos: encolar es O(1) porque tenemos la referencia al final.
 * Para atender recorremos la cola desde el frente buscando la prioridad más
 * alta, que en el peor caso es O(n). Si en el camino encontramos un cliente
 * premium paramos, porque nadie le gana y cualquier otro premium está más
 * atrás. Para el empate usamos la comparación con ">" y no con ">=": así,
 * si aparece otro con la misma prioridad, nos quedamos con el primero que
 * vimos, que es el más cercano al frente.
 * <p>
 * Otra opción era insertar a cada cliente ya en su lugar según la
 * prioridad, pero preferimos dejar la cola en el orden real de llegada,
 * porque así se la podemos mostrar al usuario tal como está.
 */
public class ColaClientes {

    /** El cliente que llegó primero. Si la cola está vacía es null. */
    private NodoCliente frente;

    /** El cliente que llegó de último. Si la cola está vacía es null. */
    private NodoCliente fin;

    /** Cuántos clientes hay esperando. */
    private int tamanio;

    /** Crea una cola vacía. */
    public ColaClientes() {
        this.frente = null;
        this.fin = null;
        this.tamanio = 0;
    }

    // Consultas básicas

    /**
     * @return true si no hay nadie esperando
     */
    public boolean estaVacia() {
        return frente == null;
    }

    /**
     * @return cuántos clientes hay en la cola
     */
    public int getTamanio() {
        return tamanio;
    }

    // Inserción

    /**
     * Mete a un cliente al final de la cola. Antes revisamos que no haya
     * otro con la misma identificación, porque si no se podría registrar
     * dos veces a la misma persona.
     *
     * @param cliente cliente que llega a la cola
     * @throws IllegalArgumentException si el cliente es null o su identificación ya está en la cola
     */
    public void encolar(Cliente cliente) {
        if (cliente == null) {
            throw new IllegalArgumentException("No se puede encolar un cliente nulo.");
        }
        if (contiene(cliente.getIdentificacion())) {
            throw new IllegalArgumentException("Ya hay un cliente con la identificación \""
                    + cliente.getIdentificacion() + "\" en la cola.");
        }
        NodoCliente nuevo = new NodoCliente(cliente);
        if (estaVacia()) {
            frente = nuevo;
        } else {
            fin.setSiguiente(nuevo);
        }
        fin = nuevo;
        tamanio++;
    }

    // Atención

    /**
     * Saca de la cola al cliente que le toca: el de prioridad más alta, y si
     * hay empate el que está más cerca del frente.
     *
     * @return el cliente atendido, o null si la cola está vacía
     */
    public Cliente atender() {
        if (estaVacia()) {
            return null;
        }
        NodoCliente anterior = buscarAnteriorAlSiguienteEnAtencion();
        NodoCliente elegido = (anterior == null) ? frente : anterior.getSiguiente();

        // Ahora soltamos el nodo elegido de la cadena
        if (anterior == null) {
            frente = elegido.getSiguiente();               // era el del frente
        } else {
            anterior.setSiguiente(elegido.getSiguiente()); // el anterior se salta al elegido
        }
        if (elegido == fin) {
            fin = anterior;                                // era el último, el anterior queda de último
        }
        elegido.setSiguiente(null);
        tamanio--;
        return elegido.getCliente();
    }

    /**
     * Dice quién va a ser el próximo en ser atendido, pero sin sacarlo de la cola.
     *
     * @return el próximo cliente, o null si la cola está vacía
     */
    public Cliente verSiguiente() {
        if (estaVacia()) {
            return null;
        }
        NodoCliente anterior = buscarAnteriorAlSiguienteEnAtencion();
        return (anterior == null) ? frente.getCliente() : anterior.getSiguiente().getCliente();
    }

    // Búsqueda y conteo

    /**
     * @param identificacion identificación del cliente
     * @return el cliente con esa identificación, o null si no está en la cola
     */
    public Cliente buscar(String identificacion) {
        for (NodoCliente actual = frente; actual != null; actual = actual.getSiguiente()) {
            if (actual.getCliente().tieneIdentificacion(identificacion)) {
                return actual.getCliente();
            }
        }
        return null;
    }

    /**
     * @param identificacion identificación del cliente
     * @return true si ese cliente ya está en la cola
     */
    public boolean contiene(String identificacion) {
        return buscar(identificacion) != null;
    }

    /**
     * @param prioridad prioridad que se quiere contar (1, 2 o 3)
     * @return cuántos clientes de la cola tienen esa prioridad
     */
    public int contarPorPrioridad(int prioridad) {
        int cantidad = 0;
        for (NodoCliente actual = frente; actual != null; actual = actual.getSiguiente()) {
            if (actual.getCliente().getPrioridad() == prioridad) {
                cantidad++;
            }
        }
        return cantidad;
    }

    /** Deja la cola vacía. */
    public void vaciar() {
        frente = null;
        fin = null;
        tamanio = 0;
    }

    // Reportes

    /**
     * Arma una tabla con los clientes en orden de llegada y, en la última
     * columna, el turno en que va a ser atendido cada uno.
     * <p>
     * Para calcular el turno sin tocar la cola hacemos dos pasadas. En la
     * primera contamos cuántos clientes hay de cada prioridad. En la segunda,
     * el turno de un cliente es: los que tienen prioridad más alta que él,
     * más los de su misma prioridad que llegaron antes, más uno. Así queda
     * en O(n) y no hay que simular la atención.
     *
     * @return la tabla lista para imprimir
     */
    public String generarReporte() {
        final int[] anchos = {7, 14, 22, 14, 9, 15, 8};
        final String formatoFila = "| %7s | %-14s | %-22s | %-14s | %9s | %15s | %8s |%n";
        final String separador = Formato.separador(anchos);

        StringBuilder sb = new StringBuilder();
        sb.append(separador);
        sb.append(String.format(formatoFila, "Llegada", "Identificación", "Cliente", "Tipo", "Artículos",
                "Total carrito", "Atención"));
        sb.append(separador);

        if (estaVacia()) {
            sb.append(String.format("| %-" + (separador.trim().length() - 4) + "s |%n",
                    "No hay clientes en espera."));
        } else {
            // Primera pasada: cuántos clientes hay de cada prioridad
            int[] porPrioridad = new int[Cliente.PRIORIDAD_MAXIMA + 1];
            for (NodoCliente actual = frente; actual != null; actual = actual.getSiguiente()) {
                porPrioridad[actual.getCliente().getPrioridad()]++;
            }
            // Segunda pasada: recorremos en orden de llegada y calculamos el turno de cada uno
            int[] vistosPorPrioridad = new int[Cliente.PRIORIDAD_MAXIMA + 1];
            int llegada = 1;
            for (NodoCliente actual = frente; actual != null; actual = actual.getSiguiente()) {
                Cliente c = actual.getCliente();
                int p = c.getPrioridad();
                int turno = ++vistosPorPrioridad[p];
                for (int mayor = p + 1; mayor <= Cliente.PRIORIDAD_MAXIMA; mayor++) {
                    turno += porPrioridad[mayor];
                }
                sb.append(String.format(formatoFila,
                        llegada++,
                        Formato.recortar(c.getIdentificacion(), 14),
                        Formato.recortar(c.getNombre(), 22),
                        c.getTipo() + " (" + p + ")",
                        c.getTotalArticulos(),
                        Producto.formatearMonto(c.calcularTotalCarrito()),
                        turno + "°"));
            }
        }
        sb.append(separador);
        return sb.toString();
    }

    /** Imprime la tabla de la cola en la consola. */
    public void imprimirCola() {
        System.out.print(generarReporte());
    }

    /**
     * @return los clientes en orden de llegada, uno por línea
     */
    @Override
    public String toString() {
        if (estaVacia()) {
            return "La cola de clientes está vacía.";
        }
        StringBuilder sb = new StringBuilder();
        int posicion = 1;
        for (NodoCliente actual = frente; actual != null; actual = actual.getSiguiente()) {
            sb.append(String.format("%3d. %s%n", posicion++, actual.getCliente()));
        }
        sb.append("Total de clientes en la cola: ").append(tamanio);
        return sb.toString();
    }

    // Métodos de apoyo (privados)

    /**
     * Recorre la cola desde el frente para encontrar al cliente que sigue
     * (prioridad más alta y, en empate, el más cercano al frente). Solo se
     * llama cuando la cola no está vacía.
     * <p>
     * Devolvemos el nodo anterior al elegido y no el elegido mismo, porque
     * para sacarlo de una lista simple necesitamos el anterior. Si devuelve
     * null es porque el elegido es el del frente.
     *
     * @return el nodo anterior al elegido, o null si el elegido es el frente
     */
    private NodoCliente buscarAnteriorAlSiguienteEnAtencion() {
        NodoCliente anteriorAlElegido = null;
        NodoCliente elegido = frente;
        NodoCliente anterior = frente;
        NodoCliente actual = frente.getSiguiente();

        // Si ya tenemos un premium podemos parar: nadie le gana
        while (actual != null && elegido.getCliente().getPrioridad() < Cliente.PRIORIDAD_MAXIMA) {
            if (actual.getCliente().getPrioridad() > elegido.getCliente().getPrioridad()) {
                elegido = actual;                 // con ">" en un empate se queda el que estaba más adelante
                anteriorAlElegido = anterior;
            }
            anterior = actual;
            actual = actual.getSiguiente();
        }
        return anteriorAlElegido;
    }
}
