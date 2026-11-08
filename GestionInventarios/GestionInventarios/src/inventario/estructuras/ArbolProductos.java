package inventario.estructuras;

import inventario.modelo.Producto;
import inventario.util.Formato;

import java.util.function.Consumer;

/**
 * Inventario de la tienda, hecho como un árbol binario de búsqueda (ABB)
 * donde cada nodo guarda un producto.
 * <p>
 * Como llave usamos el nombre del producto, sin importar mayúsculas ni
 * espacios de más (igual que en la lista). Por eso no puede haber dos
 * productos con el mismo nombre en el inventario.
 * <p>
 * La regla del ABB es que, para cualquier nodo, todo lo que está a su
 * izquierda tiene un nombre menor y todo lo que está a su derecha tiene un
 * nombre mayor. Gracias a eso, cada vez que comparamos al buscar podemos
 * descartar una mitad completa del árbol. Además, si lo recorremos en
 * in-orden, los productos salen en orden alfabético.
 * <p>
 * Insertar, buscar y eliminar dependen de la altura del árbol: si está
 * balanceado es O(log n), pero si los productos se insertan ya ordenados el
 * árbol queda como una lista y se vuelve O(n). Los recorridos siempre son O(n).
 * <p>
 * Algo importante: no se le debe cambiar el nombre con setNombre a un
 * producto que ya está en el árbol, porque quedaría en un lugar que no le
 * corresponde. Para renombrarlo habría que eliminarlo y volverlo a insertar.
 */
public class ArbolProductos {

    /** La raíz del árbol. Si el inventario está vacío es null. */
    private NodoArbolProducto raiz;

    /** Cuántos productos distintos hay en el árbol. */
    private int tamanio;

    /** Crea un inventario vacío. */
    public ArbolProductos() {
        this.raiz = null;
        this.tamanio = 0;
    }

    // Consultas básicas

    /**
     * @return true si el árbol no tiene productos
     */
    public boolean estaVacio() {
        return raiz == null;
    }

    /**
     * @return cuántos productos distintos hay en el inventario
     */
    public int getTamanio() {
        return tamanio;
    }

    /**
     * Calcula la altura del árbol contando niveles: un árbol vacío mide 0 y
     * uno que solo tiene la raíz mide 1. Hay que visitar todos los nodos, O(n).
     *
     * @return la altura del árbol
     */
    public int getAltura() {
        return altura(raiz);
    }

    /**
     * El menor alfabéticamente siempre es el nodo que está más a la izquierda.
     *
     * @return el producto con el nombre menor, o null si el árbol está vacío
     */
    public Producto getMinimo() {
        return estaVacio() ? null : nodoMinimo(raiz).getProducto();
    }

    /**
     * El mayor alfabéticamente siempre es el nodo que está más a la derecha.
     *
     * @return el producto con el nombre mayor, o null si el árbol está vacío
     */
    public Producto getMaximo() {
        if (estaVacio()) {
            return null;
        }
        NodoArbolProducto actual = raiz;
        while (actual.getDerecho() != null) {
            actual = actual.getDerecho();
        }
        return actual.getProducto();
    }

    // Inserción

    /**
     * Inserta un producto en el lugar que le toca según su nombre.
     *
     * @param producto producto que se agrega al inventario
     * @throws IllegalArgumentException si el producto es null o ya hay uno con ese nombre
     */
    public void insertar(Producto producto) {
        if (producto == null) {
            throw new IllegalArgumentException("No se puede insertar un producto nulo en el inventario.");
        }
        raiz = insertar(raiz, producto);
        tamanio++;                       // si el nombre estaba repetido nunca llegamos aquí
    }

    /**
     * La parte recursiva de la inserción. Vamos bajando por la izquierda o
     * por la derecha según la comparación de nombres, hasta llegar a un
     * espacio vacío (null), y ahí creamos el nodo nuevo.
     *
     * @return la raíz del subárbol, ya con el producto insertado
     */
    private NodoArbolProducto insertar(NodoArbolProducto nodo, Producto producto) {
        if (nodo == null) {
            return new NodoArbolProducto(producto);                     // caso base: encontramos el espacio
        }
        int comparacion = compararLlaves(producto.getNombre(), nodo.getProducto().getNombre());
        if (comparacion < 0) {
            nodo.setIzquierdo(insertar(nodo.getIzquierdo(), producto));
        } else if (comparacion > 0) {
            nodo.setDerecho(insertar(nodo.getDerecho(), producto));
        } else {
            throw new IllegalArgumentException("Ya existe un producto llamado \""
                    + nodo.getProducto().getNombre() + "\" en el inventario.");
        }
        return nodo;
    }

    // Búsqueda

    /**
     * Busca un producto por su nombre.
     *
     * @param nombre nombre del producto (no importan las mayúsculas)
     * @return el producto, o null si no está
     */
    public Producto buscar(String nombre) {
        NodoArbolProducto nodo = buscarNodo(nombre);
        return nodo == null ? null : nodo.getProducto();
    }

    /**
     * @param nombre nombre del producto
     * @return true si el inventario tiene un producto con ese nombre
     */
    public boolean existe(String nombre) {
        return buscarNodo(nombre) != null;
    }

    /**
     * Busca los productos cuyo nombre contiene un texto. Aquí no nos sirve
     * el orden del árbol, porque "mouse" puede estar en cualquier parte del
     * nombre, entonces hay que revisar todos los nodos: O(n).
     *
     * @param texto pedazo del nombre que se busca
     * @return lista con lo que coincidió, en orden alfabético
     */
    public ListaProductos buscarCoincidencias(String texto) {
        ListaProductos coincidencias = new ListaProductos();
        recorrerInOrden(p -> {
            if (p.contieneEnNombre(texto)) {
                coincidencias.insertarAlFinal(p);
            }
        });
        return coincidencias;
    }

    /**
     * Arma el camino que sigue la búsqueda desde la raíz, para mostrarle al
     * usuario cómo funciona el ABB. Por ejemplo: "Manzana -> Peine -> Mouse".
     * Si no lo encuentra, el camino termina en el espacio vacío donde
     * debería estar el producto.
     *
     * @param nombre nombre que se busca
     * @return el camino y cuántas comparaciones se hicieron
     */
    public String generarRutaBusqueda(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return "(nombre vacío)";
        }
        StringBuilder ruta = new StringBuilder();
        int comparaciones = 0;
        NodoArbolProducto actual = raiz;
        while (actual != null) {
            comparaciones++;
            if (ruta.length() > 0) {
                ruta.append(" -> ");
            }
            ruta.append(actual.getProducto().getNombre());
            int comparacion = compararLlaves(nombre, actual.getProducto().getNombre());
            if (comparacion == 0) {
                return ruta + "   (" + comparaciones + " comparación(es))";
            }
            actual = comparacion < 0 ? actual.getIzquierdo() : actual.getDerecho();
        }
        return (ruta.length() == 0 ? "(árbol vacío)" : ruta + " -> [vacío]")
                + "   (" + comparaciones + " comparación(es), no encontrado)";
    }

    // Eliminación

    /**
     * Elimina un producto del inventario. Hay tres casos:
     * 1) el nodo es una hoja: simplemente se quita;
     * 2) el nodo tiene un solo hijo: el hijo toma su lugar;
     * 3) el nodo tiene dos hijos: ponemos en su lugar al sucesor in-orden
     *    (el menor del subárbol derecho) y después borramos al sucesor de
     *    donde estaba.
     *
     * @param nombre nombre del producto que se quiere eliminar
     * @return el producto eliminado, o null si no estaba
     */
    public Producto eliminar(String nombre) {
        Producto eliminado = buscar(nombre);
        if (eliminado == null) {
            return null;
        }
        raiz = eliminar(raiz, eliminado.getNombre());
        tamanio--;
        return eliminado;
    }

    /**
     * La parte recursiva de la eliminación.
     *
     * @return la raíz del subárbol después de eliminar
     */
    private NodoArbolProducto eliminar(NodoArbolProducto nodo, String nombre) {
        if (nodo == null) {
            return null;
        }
        int comparacion = compararLlaves(nombre, nodo.getProducto().getNombre());
        if (comparacion < 0) {
            nodo.setIzquierdo(eliminar(nodo.getIzquierdo(), nombre));
            return nodo;
        }
        if (comparacion > 0) {
            nodo.setDerecho(eliminar(nodo.getDerecho(), nombre));
            return nodo;
        }
        // Lo encontramos. Casos 1 y 2: si le falta un hijo, el otro sube a su lugar
        if (nodo.getIzquierdo() == null) {
            return nodo.getDerecho();
        }
        if (nodo.getDerecho() == null) {
            return nodo.getIzquierdo();
        }
        // Caso 3: tiene dos hijos, así que copiamos el sucesor y lo borramos del subárbol derecho
        NodoArbolProducto sucesor = nodoMinimo(nodo.getDerecho());
        nodo.setProducto(sucesor.getProducto());
        nodo.setDerecho(eliminar(nodo.getDerecho(), sucesor.getProducto().getNombre()));
        return nodo;
    }

    /** Deja el inventario vacío. */
    public void vaciar() {
        raiz = null;
        tamanio = 0;
    }

    // Recorridos

    /**
     * Recorrido in-orden (izquierda, raíz, derecha). En un ABB este recorrido
     * da los productos en orden alfabético. Le pasamos la acción que se
     * quiere hacer con cada producto, así reutilizamos el mismo recorrido
     * para imprimir, sumar o contar.
     *
     * @param accion lo que se hace con cada producto
     */
    public void recorrerInOrden(Consumer<Producto> accion) {
        inOrden(raiz, accion);
    }

    /**
     * Recorrido pre-orden (raíz, izquierda, derecha).
     *
     * @param accion lo que se hace con cada producto
     */
    public void recorrerPreOrden(Consumer<Producto> accion) {
        preOrden(raiz, accion);
    }

    /**
     * Recorrido post-orden (izquierda, derecha, raíz).
     *
     * @param accion lo que se hace con cada producto
     */
    public void recorrerPostOrden(Consumer<Producto> accion) {
        postOrden(raiz, accion);
    }

    /**
     * Devuelve los productos en orden alfabético dentro de una ListaProductos,
     * para poder mostrarlos numerados y que el usuario escoja uno por número.
     * Ojo: la lista tiene los mismos objetos del árbol, no copias, así que
     * no hay que renombrar productos a través de ella.
     *
     * @return lista con los productos en in-orden
     */
    public ListaProductos obtenerProductosEnOrden() {
        ListaProductos lista = new ListaProductos();
        recorrerInOrden(lista::insertarAlFinal);
        return lista;
    }

    /**
     * @return los nombres en pre-orden, separados por comas
     */
    public String obtenerNombresPreOrden() {
        StringBuilder sb = new StringBuilder();
        recorrerPreOrden(p -> agregarNombre(sb, p));
        return sb.toString();
    }

    /**
     * @return los nombres en in-orden (alfabético), separados por comas
     */
    public String obtenerNombresInOrden() {
        StringBuilder sb = new StringBuilder();
        recorrerInOrden(p -> agregarNombre(sb, p));
        return sb.toString();
    }

    /**
     * @return los nombres en post-orden, separados por comas
     */
    public String obtenerNombresPostOrden() {
        StringBuilder sb = new StringBuilder();
        recorrerPostOrden(p -> agregarNombre(sb, p));
        return sb.toString();
    }

    // Cálculos y reportes

    /**
     * Suma cuánto vale todo el inventario (precio por existencias de cada
     * producto). Usamos un arreglo de un solo espacio porque dentro de una
     * lambda no se puede modificar una variable local normal.
     *
     * @return el valor total del inventario
     */
    public double calcularValorInventario() {
        double[] total = {0};
        recorrerInOrden(p -> total[0] += p.calcularCostoTotal());
        return total[0];
    }

    /**
     * Cuenta los productos que se pueden vender, o sea, los que tienen
     * existencias y no están vencidos.
     *
     * @return cantidad de productos disponibles para la venta
     */
    public int contarProductosDisponibles() {
        int[] disponibles = {0};
        recorrerInOrden(p -> {
            if (p.tieneExistencias() && !p.estaVencido()) {
                disponibles[0]++;
            }
        });
        return disponibles[0];
    }

    /**
     * Arma la tabla del inventario en orden alfabético. Los números de la
     * primera columna son los mismos que tiene cada producto en
     * obtenerProductosEnOrden(), por eso el usuario puede escoger por número.
     *
     * @return la tabla lista para imprimir
     */
    public String generarReporteInventario() {
        final int[] anchos = {3, 26, 16, 12, 8, 20, 14};
        final String formatoFila = "| %3s | %-26s | %-16s | %12s | %8s | %-20s | %14s |%n";
        final String separador = Formato.separador(anchos);

        StringBuilder sb = new StringBuilder();
        sb.append(separador);
        sb.append(String.format(formatoFila, "#", "Producto", "Categoría", "Precio", "Unidades",
                "Vencimiento", "Valor"));
        sb.append(separador);

        if (estaVacio()) {
            sb.append(String.format("| %-" + (separador.trim().length() - 4) + "s |%n",
                    "El inventario no contiene productos."));
        } else {
            int[] posicion = {1};
            long[] totalUnidades = {0};
            recorrerInOrden(p -> {
                sb.append(String.format(formatoFila,
                        posicion[0]++,
                        Formato.recortar(p.getNombre(), 26),
                        Formato.recortar(p.getCategoria(), 16),
                        Producto.formatearMonto(p.getPrecio()),
                        p.tieneExistencias() ? String.valueOf(p.getCantidad()) : "AGOTADO",
                        p.getFechaVencimientoTexto(),
                        Producto.formatearMonto(p.calcularCostoTotal())));
                totalUnidades[0] += p.getCantidad();
            });
            sb.append(separador);
            sb.append(String.format(formatoFila, "", "TOTAL (" + tamanio + " producto(s))", "", "",
                    totalUnidades[0], "", Producto.formatearMonto(calcularValorInventario())));
        }
        sb.append(separador);
        return sb.toString();
    }

    /** Imprime la tabla del inventario en la consola. */
    public void imprimirInventario() {
        System.out.print(generarReporteInventario());
    }

    /**
     * Dibuja el árbol con texto para que se vea su forma. Al lado de cada
     * hijo ponemos (I) si es el izquierdo o (D) si es el derecho, y si un
     * nodo tiene solo un hijo mostramos el otro lado como [vacío]. Ejemplo:
     * <pre>
     * Manzana
     * |-- (I) Café
     * `-- (D) Peine
     * </pre>
     *
     * @return el dibujo del árbol
     */
    public String generarDiagrama() {
        if (estaVacio()) {
            return "(árbol vacío)";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(etiquetaNodo(raiz)).append("   <- raíz").append(System.lineSeparator());
        dibujarHijos(raiz, "", sb);
        return sb.toString();
    }

    /**
     * @return los productos en orden alfabético, uno por línea
     */
    @Override
    public String toString() {
        if (estaVacio()) {
            return "El inventario está vacío.";
        }
        return obtenerProductosEnOrden().toString();
    }

    // Métodos de apoyo (privados)

    /**
     * Compara dos nombres sin importar mayúsculas ni espacios de más.
     *
     * @return negativo si a va antes que b, 0 si son iguales, positivo si a va después
     */
    private static int compararLlaves(String a, String b) {
        return a.trim().compareToIgnoreCase(b.trim());
    }

    /** Búsqueda con un ciclo en vez de recursión; así no usamos la pila de llamadas. */
    private NodoArbolProducto buscarNodo(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return null;
        }
        NodoArbolProducto actual = raiz;
        while (actual != null) {
            int comparacion = compararLlaves(nombre, actual.getProducto().getNombre());
            if (comparacion == 0) {
                return actual;
            }
            actual = comparacion < 0 ? actual.getIzquierdo() : actual.getDerecho();
        }
        return null;
    }

    /** Baja siempre por la izquierda hasta el final: ese es el nodo con el nombre menor. */
    private static NodoArbolProducto nodoMinimo(NodoArbolProducto nodo) {
        NodoArbolProducto actual = nodo;
        while (actual.getIzquierdo() != null) {
            actual = actual.getIzquierdo();
        }
        return actual;
    }

    /** La altura de un nodo es 1 más la altura de su hijo más alto. */
    private static int altura(NodoArbolProducto nodo) {
        if (nodo == null) {
            return 0;
        }
        return 1 + Math.max(altura(nodo.getIzquierdo()), altura(nodo.getDerecho()));
    }

    private static void inOrden(NodoArbolProducto nodo, Consumer<Producto> accion) {
        if (nodo != null) {
            inOrden(nodo.getIzquierdo(), accion);
            accion.accept(nodo.getProducto());
            inOrden(nodo.getDerecho(), accion);
        }
    }

    private static void preOrden(NodoArbolProducto nodo, Consumer<Producto> accion) {
        if (nodo != null) {
            accion.accept(nodo.getProducto());
            preOrden(nodo.getIzquierdo(), accion);
            preOrden(nodo.getDerecho(), accion);
        }
    }

    private static void postOrden(NodoArbolProducto nodo, Consumer<Producto> accion) {
        if (nodo != null) {
            postOrden(nodo.getIzquierdo(), accion);
            postOrden(nodo.getDerecho(), accion);
            accion.accept(nodo.getProducto());
        }
    }

    /** Agrega un nombre al texto, con una coma antes si no es el primero. */
    private static void agregarNombre(StringBuilder sb, Producto producto) {
        if (sb.length() > 0) {
            sb.append(", ");
        }
        sb.append(producto.getNombre());
    }

    /** Texto que mostramos para cada nodo en el dibujo: el nombre y las unidades. */
    private static String etiquetaNodo(NodoArbolProducto nodo) {
        return nodo.getProducto().getNombre() + " [" + nodo.getProducto().getCantidad() + " u.]";
    }

    /** Dibuja los dos hijos de un nodo, siempre que tenga al menos uno. */
    private static void dibujarHijos(NodoArbolProducto nodo, String prefijo, StringBuilder sb) {
        if (nodo.esHoja()) {
            return;
        }
        dibujarRama(nodo.getIzquierdo(), "I", prefijo, false, sb);
        dibujarRama(nodo.getDerecho(), "D", prefijo, true, sb);
    }

    /**
     * Dibuja una rama y luego, de forma recursiva, todo lo que cuelga de
     * ella. El prefijo va creciendo con cada nivel para que se vea la sangría.
     */
    private static void dibujarRama(NodoArbolProducto nodo, String lado, String prefijo,
                                    boolean esUltima, StringBuilder sb) {
        sb.append(prefijo).append(esUltima ? "`-- " : "|-- ").append('(').append(lado).append(") ");
        if (nodo == null) {
            sb.append("[vacío]").append(System.lineSeparator());
            return;
        }
        sb.append(etiquetaNodo(nodo)).append(System.lineSeparator());
        dibujarHijos(nodo, prefijo + (esUltima ? "    " : "|   "), sb);
    }
}
