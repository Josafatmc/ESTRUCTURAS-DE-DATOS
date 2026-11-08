package inventario;

import inventario.estructuras.ArbolProductos;
import inventario.estructuras.ColaClientes;
import inventario.estructuras.ListaProductos;
import inventario.modelo.Cliente;
import inventario.modelo.Producto;
import inventario.modelo.Tienda;
import inventario.util.Consola;
import inventario.util.GestorImagenes;

import java.time.LocalDate;
import java.util.List;

/**
 * Clase funcional del programa (avance 2). Aquí están el main() y el
 * menu() que pide la consigna.
 * <p>
 * Desde el menú el usuario puede meter productos al inventario de la
 * tienda (el árbol), registrar clientes en la cola de prioridad llenando
 * primero su carrito con lo que hay en el inventario, y atender al
 * siguiente cliente, lo que imprime su factura.
 * <p>
 * Tratamos de que Main solo se encargue de pedir datos y mostrar
 * resultados. Las reglas de la compra (apartar unidades, no vender
 * vencidos, etc.) están en la clase Tienda.
 */
public class Main {

    /** La tienda con la que trabaja todo el programa: tiene el inventario y la cola. */
    private static final Tienda tienda = new Tienda("Tienda en Línea CENFOTEC");

    // Opciones del menú principal. Usamos constantes para no tener números sueltos en el switch
    private static final int OPCION_SALIR = 0;
    private static final int OPCION_INSERTAR_PRODUCTO = 1;
    private static final int OPCION_MOSTRAR_INVENTARIO = 2;
    private static final int OPCION_BUSCAR_PRODUCTO = 3;
    private static final int OPCION_ELIMINAR_PRODUCTO = 4;
    private static final int OPCION_VER_ARBOL = 5;
    private static final int OPCION_REGISTRAR_CLIENTE = 6;
    private static final int OPCION_MOSTRAR_COLA = 7;
    private static final int OPCION_ATENDER_CLIENTE = 8;
    private static final int OPCION_DATOS_EJEMPLO = 9;

    // Opciones del submenú del carrito
    private static final int CARRITO_CANCELAR = 0;
    private static final int CARRITO_AGREGAR = 1;
    private static final int CARRITO_QUITAR = 2;
    private static final int CARRITO_VER = 3;
    private static final int CARRITO_FINALIZAR = 4;

    /** Valor que usamos cuando el usuario se arrepiente de salir, para seguir sin hacer la pausa. */
    private static final int SALIDA_CANCELADA = -1;

    /** El constructor es privado porque nunca creamos objetos de Main. */
    private Main() {
    }

    /**
     * Aquí empieza el programa. Mostramos la bienvenida, revisamos que esté
     * la carpeta de imágenes, corremos el menú y al final mostramos un
     * resumen de lo que se vendió.
     *
     * @param args no los usamos
     */
    public static void main(String[] args) {
        // 1. Bienvenida y qué estructura usamos para cada cosa
        Consola.mostrarTitulo("SISTEMA DE VENTAS EN LÍNEA - " + tienda.getNombre() + " (Avance 2)");
        System.out.println("  Inventario      : árbol binario de búsqueda por nombre (ArbolProductos)");
        System.out.println("  Clientes        : cola de prioridad (ColaClientes)");
        System.out.println("  Carrito         : lista enlazada simple (ListaProductos)");

        // 2. Si no está la carpeta de imágenes avisamos, pero el programa sigue funcionando
        if (!GestorImagenes.existeDirectorio()) {
            Consola.mostrarError("No se encontró el directorio \"" + GestorImagenes.DIRECTORIO_IMAGENES
                    + "/\". Podrá usar el sistema, pero no asociar imágenes a los productos.");
        }

        // 3. El menú se repite hasta que el usuario escoja salir.
        //    Si se cierra la entrada (Ctrl+Z / Ctrl+D) terminamos sin mostrar un error
        try {
            menu();
        } catch (Consola.EntradaFinalizadaException e) {
            System.out.println();
            Consola.mostrarInfo("Se cerró la entrada de datos. Finalizando el programa.");
        }

        // 4. Despedida con el resumen del día
        System.out.printf("%nFacturas emitidas: %d   |   Total vendido: %s%n",
                tienda.getFacturasEmitidas(), Producto.formatearMonto(tienda.getTotalVendido()));
        System.out.println("¡Gracias por utilizar el sistema! Hasta pronto.");
    }

    /**
     * Menú principal. Muestra las opciones, lee la que escoge el usuario y
     * llama al método que se encarga de esa opción. Se repite hasta que el
     * usuario escoge 0 y confirma que quiere salir.
     */
    public static void menu() {
        int opcion;
        do {
            mostrarMenuPrincipal();
            opcion = Consola.leerEntero("Seleccione una opción: ", OPCION_SALIR, OPCION_DATOS_EJEMPLO);

            try {
                switch (opcion) {
                    case OPCION_INSERTAR_PRODUCTO -> insertarProductoEnInventario();
                    case OPCION_MOSTRAR_INVENTARIO -> mostrarInventario();
                    case OPCION_BUSCAR_PRODUCTO -> buscarProductoEnInventario();
                    case OPCION_ELIMINAR_PRODUCTO -> eliminarProductoDelInventario();
                    case OPCION_VER_ARBOL -> mostrarEstructuraArbol();
                    case OPCION_REGISTRAR_CLIENTE -> registrarClienteEnCola();
                    case OPCION_MOSTRAR_COLA -> mostrarColaClientes();
                    case OPCION_ATENDER_CLIENTE -> atenderSiguienteCliente();
                    case OPCION_DATOS_EJEMPLO -> cargarDatosEjemplo();
                    case OPCION_SALIR -> {
                        if (!confirmarSalida()) {
                            opcion = SALIDA_CANCELADA;        // se arrepintió, el menú sigue
                        }
                    }
                    default -> Consola.mostrarError("Opción no válida.");
                }
            } catch (IllegalArgumentException | IllegalStateException e) {
                // Por si alguna regla no se cumple: mostramos el mensaje y el programa sigue
                Consola.mostrarError(e.getMessage());
            }

            if (opcion != OPCION_SALIR && opcion != SALIDA_CANCELADA) {
                Consola.pausar();
            }
        } while (opcion != OPCION_SALIR);
    }

    // Presentación del menú

    /** Imprime el menú, con un resumen arriba de cómo está la tienda en este momento. */
    private static void mostrarMenuPrincipal() {
        ArbolProductos inventario = tienda.getInventario();
        ColaClientes cola = tienda.getColaClientes();

        Consola.mostrarTitulo("MENÚ PRINCIPAL - " + tienda.getNombre());
        System.out.printf("  Productos en inventario: %d (valor %s)%n",
                inventario.getTamanio(), Producto.formatearMonto(inventario.calcularValorInventario()));
        System.out.printf("  Clientes en cola: %d   |   Facturas emitidas: %d   |   Total vendido: %s%n",
                cola.getTamanio(), tienda.getFacturasEmitidas(), Producto.formatearMonto(tienda.getTotalVendido()));
        System.out.println("  --------------------------------------------------");
        System.out.println("  INVENTARIO (árbol binario de búsqueda)");
        System.out.println("   1. Insertar producto al inventario");
        System.out.println("   2. Mostrar inventario (orden alfabético)");
        System.out.println("   3. Buscar producto en el inventario");
        System.out.println("   4. Eliminar producto del inventario");
        System.out.println("   5. Ver estructura del árbol de inventario");
        System.out.println("  CLIENTES (cola de prioridad)");
        System.out.println("   6. Registrar cliente en la cola (llenar su carrito)");
        System.out.println("   7. Mostrar cola de clientes");
        System.out.println("   8. Atender siguiente cliente (emitir factura)");
        System.out.println("  OTROS");
        System.out.println("   9. Cargar datos de ejemplo");
        System.out.println("   0. Salir");
        System.out.println("  --------------------------------------------------");
    }

    // Opción 1: insertar un producto en el inventario

    /**
     * Pide los datos de un producto y lo mete al árbol del inventario. Si ya
     * hay un producto con ese nombre no lo duplicamos (en el árbol la llave
     * no se puede repetir); en cambio le preguntamos al usuario si quiere
     * sumarle unidades.
     */
    private static void insertarProductoEnInventario() {
        Consola.mostrarTitulo("INSERTAR PRODUCTO AL INVENTARIO");
        String nombre = Consola.leerTexto("Nombre del producto: ");

        Producto existente = tienda.buscarProductoEnInventario(nombre);
        if (existente != null) {
            Consola.mostrarInfo("Ya existe un producto con ese nombre en el inventario:");
            System.out.println(existente.toDetalle());
            if (Consola.confirmar("¿Desea reabastecerlo (sumar unidades a sus existencias)?")) {
                int unidades = Consola.leerEntero("Unidades por agregar: ", 1, Integer.MAX_VALUE);
                tienda.reabastecerProducto(existente.getNombre(), unidades);
                Consola.mostrarExito("Ahora hay " + existente.getCantidad() + " unidad(es) de \""
                        + existente.getNombre() + "\".");
            }
            return;
        }

        double precio = Consola.leerDecimal("Precio unitario (₡): ", 0);
        String categoria = Consola.leerTexto("Categoría: ");
        LocalDate fechaVencimiento = Consola.leerFechaOpcional(
                "Fecha de vencimiento dd/MM/aaaa (deje en blanco si no aplica): ");
        int cantidad = Consola.leerEntero("Cantidad de unidades en existencia: ", 0, Integer.MAX_VALUE);

        Producto producto = new Producto(nombre, precio, categoria, fechaVencimiento, cantidad);
        tienda.agregarProductoAlInventario(producto);
        Consola.mostrarExito("Producto \"" + producto.getNombre() + "\" insertado en el inventario.");
        if (producto.estaVencido()) {
            Consola.mostrarInfo("Atención: la fecha de vencimiento ya pasó; el producto no se podrá vender.");
        }
        ofrecerAgregarImagenes(producto);
    }

    /**
     * Después de crear el producto le preguntamos si quiere agregarle
     * imágenes, y seguimos preguntando hasta que diga que no.
     *
     * @param producto producto que se acaba de crear
     */
    private static void ofrecerAgregarImagenes(Producto producto) {
        while (Consola.confirmar("¿Desea agregar una imagen a \"" + producto.getNombre() + "\"?")) {
            agregarImagen(producto);
        }
    }

    /**
     * Muestra las imágenes que hay en la carpeta del proyecto y le agrega al
     * producto la que el usuario escoja, ya sea por número o escribiendo el
     * nombre del archivo.
     *
     * @param producto producto al que se le agrega la imagen
     */
    private static void agregarImagen(Producto producto) {
        List<String> disponibles = GestorImagenes.listarImagenesDisponibles();
        if (disponibles.isEmpty()) {
            Consola.mostrarInfo("No hay imágenes en el directorio \"" + GestorImagenes.DIRECTORIO_IMAGENES
                    + "/\". Copie allí las imágenes y vuelva a intentarlo.");
        } else {
            System.out.println("  Imágenes disponibles en el proyecto:");
            for (int i = 0; i < disponibles.size(); i++) {
                String marca = producto.getListaImagenes().contains(disponibles.get(i)) ? "  (ya asociada)" : "";
                System.out.printf("   %2d. %s%s%n", i + 1, disponibles.get(i), marca);
            }
        }

        String entrada = Consola.leerLinea("Número o nombre del archivo de la imagen (Enter para cancelar): ");
        if (entrada.isEmpty()) {
            Consola.mostrarInfo("Operación cancelada.");
            return;
        }

        // Si escribió un número de la lista tomamos esa imagen; si no, lo tratamos como nombre de archivo
        String ruta = esNumeroEnRango(entrada, disponibles.size())
                ? disponibles.get(Integer.parseInt(entrada) - 1)
                : GestorImagenes.construirRuta(entrada);

        String error = GestorImagenes.validarRuta(ruta);
        if (error != null) {
            Consola.mostrarError(error);
        } else if (producto.agregarImagen(ruta)) {
            Consola.mostrarExito("Imagen \"" + ruta + "\" agregada a \"" + producto.getNombre() + "\".");
        } else {
            Consola.mostrarError("La imagen \"" + ruta + "\" ya estaba asociada a este producto.");
        }
    }

    // Opciones 2 a 5: consultar y mantener el inventario

    /** Muestra todo el inventario en orden alfabético, que es lo que da el recorrido in-orden. */
    private static void mostrarInventario() {
        Consola.mostrarTitulo("INVENTARIO DE LA TIENDA (recorrido in-orden)");
        tienda.getInventario().imprimirInventario();
    }

    /**
     * Busca un producto por nombre. Además de la ficha mostramos el camino
     * que siguió la búsqueda en el árbol, para que se vea cómo funciona.
     * Si no hay un producto con ese nombre exacto, mostramos los que
     * contienen el texto, por si el usuario escribió solo una parte.
     */
    private static void buscarProductoEnInventario() {
        Consola.mostrarTitulo("BUSCAR PRODUCTO EN EL INVENTARIO");
        if (avisarSiInventarioVacio()) {
            return;
        }
        String nombre = Consola.leerTexto("Nombre del producto a buscar: ");
        Producto producto = tienda.buscarProductoEnInventario(nombre);
        System.out.println("  Ruta de búsqueda en el árbol: " + tienda.getInventario().generarRutaBusqueda(nombre));
        if (producto != null) {
            Consola.mostrarExito("Producto encontrado:");
            System.out.println(producto.toDetalle());
            return;
        }
        Consola.mostrarError("No hay ningún producto llamado exactamente \"" + nombre + "\".");
        ListaProductos coincidencias = tienda.getInventario().buscarCoincidencias(nombre);
        if (coincidencias.estaVacia()) {
            Consola.mostrarInfo("Tampoco hay productos cuyo nombre contenga \"" + nombre + "\".");
        } else {
            Consola.mostrarInfo("Productos cuyo nombre contiene \"" + nombre + "\":");
            for (int i = 0; i < coincidencias.getTamanio(); i++) {
                System.out.println(coincidencias.obtener(i).toDetalle());
                System.out.println();
            }
        }
    }

    /** Saca un producto del árbol del inventario, después de pedir confirmación. */
    private static void eliminarProductoDelInventario() {
        Consola.mostrarTitulo("ELIMINAR PRODUCTO DEL INVENTARIO");
        if (avisarSiInventarioVacio()) {
            return;
        }
        ListaProductos productos = tienda.getInventario().obtenerProductosEnOrden();
        tienda.getInventario().imprimirInventario();
        Producto producto = seleccionarProducto(productos, "eliminar");
        if (producto != null && Consola.confirmar("¿Eliminar \"" + producto.getNombre() + "\" del inventario?")) {
            tienda.eliminarProductoDelInventario(producto.getNombre());
            Consola.mostrarExito("Producto \"" + producto.getNombre() + "\" eliminado del inventario.");
        }
    }

    /** Dibuja el árbol y muestra sus datos: tamaño, altura, mínimo, máximo y los tres recorridos. */
    private static void mostrarEstructuraArbol() {
        Consola.mostrarTitulo("ESTRUCTURA DEL ÁRBOL DE INVENTARIO");
        ArbolProductos inventario = tienda.getInventario();
        System.out.println(inventario.generarDiagrama());
        if (inventario.estaVacio()) {
            return;
        }
        System.out.println("  --------------------------------------------------");
        System.out.println("  Cantidad de nodos : " + inventario.getTamanio());
        System.out.println("  Altura (niveles)  : " + inventario.getAltura());
        System.out.println("  Menor llave       : " + inventario.getMinimo().getNombre());
        System.out.println("  Mayor llave       : " + inventario.getMaximo().getNombre());
        System.out.println("  Pre-orden         : " + inventario.obtenerNombresPreOrden());
        System.out.println("  In-orden          : " + inventario.obtenerNombresInOrden());
        System.out.println("  Post-orden        : " + inventario.obtenerNombresPostOrden());
    }

    // Opción 6: registrar un cliente en la cola (llenando su carrito)

    /**
     * Registra a un cliente nuevo. Primero pedimos sus datos y su prioridad,
     * luego le dejamos llenar el carrito con lo que hay en el inventario, y
     * cuando termina lo metemos a la cola de prioridad. Si cancela, todo lo
     * que había apartado vuelve al inventario.
     * <p>
     * La identificación la revisamos de una vez al inicio, para no ponerlo a
     * llenar el carrito y al final decirle que ya estaba en la cola.
     */
    private static void registrarClienteEnCola() {
        Consola.mostrarTitulo("REGISTRAR CLIENTE EN LA COLA");
        if (tienda.getInventario().contarProductosDisponibles() == 0) {
            Consola.mostrarInfo("No hay productos disponibles para la venta. Inserte productos en el inventario "
                    + "(opción 1) o cargue los datos de ejemplo (opción 9).");
            return;
        }

        String identificacion = Consola.leerTexto("Identificación del cliente: ");
        if (tienda.getColaClientes().contiene(identificacion)) {
            Consola.mostrarError("Ya hay un cliente con la identificación \"" + identificacion + "\" en la cola.");
            return;
        }
        String nombre = Consola.leerTexto("Nombre del cliente: ");
        System.out.println("  Tipos de cliente: " + Cliente.PRIORIDAD_BASICO + " = Básico, "
                + Cliente.PRIORIDAD_AFILIADO + " = Afiliado a la tienda, "
                + Cliente.PRIORIDAD_PREMIUM + " = Premium");
        int prioridad = Consola.leerEntero("Prioridad del cliente: ", Cliente.PRIORIDAD_MINIMA, Cliente.PRIORIDAD_MAXIMA);

        Cliente cliente = new Cliente(identificacion, nombre, prioridad);
        if (llenarCarrito(cliente)) {
            tienda.registrarClienteEnCola(cliente);
            Consola.mostrarExito("Cliente \"" + cliente.getNombre() + "\" (" + cliente.getTipo()
                    + ") ingresó a la cola con " + cliente.getTotalArticulos() + " artículo(s) por "
                    + Producto.formatearMonto(cliente.calcularTotalCarrito()) + ".");
            Consola.mostrarInfo("Clientes en espera: " + tienda.getColaClientes().getTamanio()
                    + ". Próximo en ser atendido: " + tienda.verSiguienteCliente().getNombre() + ".");
        } else {
            tienda.vaciarCarrito(cliente);
            Consola.mostrarInfo("Registro cancelado. Los productos apartados se devolvieron al inventario.");
        }
    }

    /**
     * Submenú del carrito. Aquí el usuario va agregando y quitando productos
     * hasta que decide terminar o cancelar.
     *
     * @param cliente cliente al que se le llena el carrito
     * @return true si terminó la compra, false si la canceló
     */
    private static boolean llenarCarrito(Cliente cliente) {
        while (true) {
            Consola.mostrarTitulo("CARRITO DE " + cliente.getNombre().toUpperCase());
            System.out.printf("  Artículos: %d   |   Total acumulado: %s%n",
                    cliente.getTotalArticulos(), Producto.formatearMonto(cliente.calcularTotalCarrito()));
            System.out.println("  --------------------------------------------------");
            System.out.println("  1. Agregar producto del inventario al carrito");
            System.out.println("  2. Quitar producto del carrito");
            System.out.println("  3. Ver carrito");
            System.out.println("  4. Finalizar compra e ingresar a la cola");
            System.out.println("  0. Cancelar registro (devuelve los productos al inventario)");
            int opcion = Consola.leerEntero("Seleccione una opción: ", CARRITO_CANCELAR, CARRITO_FINALIZAR);

            try {
                switch (opcion) {
                    case CARRITO_AGREGAR -> agregarProductoAlCarrito(cliente);
                    case CARRITO_QUITAR -> quitarProductoDelCarrito(cliente);
                    case CARRITO_VER -> cliente.getCarrito().imprimirReporteCostos();
                    case CARRITO_FINALIZAR -> {
                        if (cliente.tieneCarritoVacio()) {
                            Consola.mostrarError("El carrito está vacío. Agregue al menos un producto.");
                        } else {
                            return true;
                        }
                    }
                    default -> {
                        if (Consola.confirmar("¿Seguro que desea cancelar el registro del cliente?")) {
                            return false;
                        }
                    }
                }
            } catch (IllegalArgumentException e) {
                Consola.mostrarError(e.getMessage());
            }
        }
    }

    /**
     * Muestra el inventario, deja escoger un producto y cuántas unidades, y
     * lo pasa al carrito. Las unidades se descuentan del inventario en ese
     * momento. Antes de pedir la cantidad revisamos que el producto no esté
     * agotado ni vencido, y la cantidad solo puede ir de 1 a lo que hay.
     *
     * @param cliente cliente que está comprando
     */
    private static void agregarProductoAlCarrito(Cliente cliente) {
        ArbolProductos inventario = tienda.getInventario();
        ListaProductos productos = inventario.obtenerProductosEnOrden();
        inventario.imprimirInventario();

        Producto producto = seleccionarProducto(productos, "agregar al carrito");
        if (producto == null) {
            return;
        }
        if (!producto.tieneExistencias()) {
            Consola.mostrarError("El producto \"" + producto.getNombre() + "\" está agotado.");
            return;
        }
        if (producto.estaVencido()) {
            Consola.mostrarError("El producto \"" + producto.getNombre() + "\" está vencido y no se puede vender.");
            return;
        }
        int cantidad = Consola.leerEntero(String.format("Cantidad (1 a %d): ", producto.getCantidad()),
                1, producto.getCantidad());
        tienda.agregarAlCarrito(cliente, producto.getNombre(), cantidad);
        Consola.mostrarExito(cantidad + " unidad(es) de \"" + producto.getNombre() + "\" agregada(s) al carrito.");
    }

    /**
     * Deja escoger un producto del carrito y cuántas unidades quitar. Lo que
     * se quita vuelve al inventario.
     *
     * @param cliente cliente dueño del carrito
     */
    private static void quitarProductoDelCarrito(Cliente cliente) {
        if (cliente.tieneCarritoVacio()) {
            Consola.mostrarInfo("El carrito está vacío.");
            return;
        }
        ListaProductos carrito = cliente.getCarrito();
        carrito.imprimirReporteCostos();
        Producto linea = seleccionarProducto(carrito, "quitar");
        if (linea == null) {
            return;
        }
        int unidades = Consola.leerEntero(String.format("Unidades por quitar (1 a %d): ", linea.getCantidad()),
                1, linea.getCantidad());
        int quitadas = tienda.quitarDelCarrito(cliente, linea.getNombre(), unidades);
        Consola.mostrarExito(quitadas + " unidad(es) de \"" + linea.getNombre() + "\" devuelta(s) al inventario.");
    }

    // Opciones 7 y 8: ver la cola y atender

    /** Muestra la cola en orden de llegada, con el turno en que va a ser atendido cada uno. */
    private static void mostrarColaClientes() {
        Consola.mostrarTitulo("COLA DE CLIENTES");
        ColaClientes cola = tienda.getColaClientes();
        cola.imprimirCola();
        if (!cola.estaVacia()) {
            System.out.printf("  Premium: %d   |   Afiliados: %d   |   Básicos: %d%n",
                    cola.contarPorPrioridad(Cliente.PRIORIDAD_PREMIUM),
                    cola.contarPorPrioridad(Cliente.PRIORIDAD_AFILIADO),
                    cola.contarPorPrioridad(Cliente.PRIORIDAD_BASICO));
            System.out.println("  Próximo en ser atendido: " + tienda.verSiguienteCliente());
            System.out.println("  (Se atiende primero la prioridad más alta; en empate, quien llegó antes.)");
        }
    }

    /** Atiende al cliente que sigue según la prioridad e imprime su factura. */
    private static void atenderSiguienteCliente() {
        Consola.mostrarTitulo("ATENDER SIGUIENTE CLIENTE");
        if (!tienda.hayClientesEnEspera()) {
            Consola.mostrarInfo("No hay clientes en espera. Registre clientes con la opción 6.");
            return;
        }
        Cliente siguiente = tienda.verSiguienteCliente();
        Consola.mostrarInfo("Atendiendo a " + siguiente.getNombre() + " (" + siguiente.getTipo()
                + ", prioridad " + siguiente.getPrioridad() + ").");
        System.out.println();
        System.out.print(tienda.atenderSiguienteCliente());
        Consola.mostrarInfo("Clientes que siguen en espera: " + tienda.getColaClientes().getTamanio() + ".");
    }

    // Opción 9: datos de ejemplo

    /**
     * Carga productos y clientes de ejemplo para poder probar rápido sin
     * tener que escribir todo a mano. Los productos los metemos en un orden
     * pensado para que el árbol quede balanceado, y los clientes llegan
     * mezclados para que se vea cómo funcionan la prioridad y el desempate.
     */
    private static void cargarDatosEjemplo() {
        Consola.mostrarTitulo("CARGAR DATOS DE EJEMPLO");

        // 1. Productos. Empezamos por el del medio alfabéticamente para que el árbol quede de 3 niveles
        int productos = 0;
        productos += cargarProductoEjemplo(new Producto("Manzana roja (unidad)", 450, "Frutas",
                LocalDate.now().plusDays(10), 100), "manzana.png");
        productos += cargarProductoEjemplo(new Producto("Laptop Lenovo IdeaPad 3", 389_900, "Electrónica", 5),
                "laptop_1.png", "laptop_2.png");
        productos += cargarProductoEjemplo(new Producto("Peine de madera", 2_300, "Cuidado personal", 25),
                "Peine.png");
        productos += cargarProductoEjemplo(new Producto("Café Britt 340g", 4_850, "Abarrotes",
                LocalDate.now().plusMonths(8), 30), "cafe.png");
        productos += cargarProductoEjemplo(new Producto("Leche Dos Pinos 1L", 1_150, "Lácteos",
                LocalDate.now().plusDays(12), 48), "leche.png");
        productos += cargarProductoEjemplo(new Producto("Mouse inalámbrico Logitech", 12_500, "Electrónica", 20),
                "mouse.png");
        productos += cargarProductoEjemplo(new Producto("Shampoo Sedal 400ml", 3_200, "Cuidado personal",
                LocalDate.now().plusYears(2), 40), "Shampoo.png");
        Consola.mostrarExito(productos + " producto(s) de ejemplo insertado(s) en el inventario.");

        // 2. Clientes con su carrito. Llegan en este orden: Ana, Luis, María, Carlos, Sofía.
        //    Se deberían atender así: Luis y Carlos (premium, en el orden en que llegaron),
        //    después María (afiliada) y al final Ana y Sofía (básicas, también por orden de llegada)
        int clientes = 0;
        clientes += cargarClienteEjemplo(new Cliente("1-0101-0101", "Ana Vargas", Cliente.PRIORIDAD_BASICO),
                "Leche Dos Pinos 1L", 2, "Manzana roja (unidad)", 6);
        clientes += cargarClienteEjemplo(new Cliente("2-0202-0202", "Luis Rojas", Cliente.PRIORIDAD_PREMIUM),
                "Laptop Lenovo IdeaPad 3", 1, "Mouse inalámbrico Logitech", 1);
        clientes += cargarClienteEjemplo(new Cliente("3-0303-0303", "María Solís", Cliente.PRIORIDAD_AFILIADO),
                "Café Britt 340g", 2, "Shampoo Sedal 400ml", 1);
        clientes += cargarClienteEjemplo(new Cliente("4-0404-0404", "Carlos Mora", Cliente.PRIORIDAD_PREMIUM),
                "Peine de madera", 1, "Café Britt 340g", 1);
        clientes += cargarClienteEjemplo(new Cliente("5-0505-0505", "Sofía Jiménez", Cliente.PRIORIDAD_BASICO),
                "Mouse inalámbrico Logitech", 2);
        Consola.mostrarExito(clientes + " cliente(s) de ejemplo registrado(s) en la cola.");

        System.out.println();
        tienda.getInventario().imprimirInventario();
        System.out.println();
        tienda.getColaClientes().imprimirCola();
    }

    /**
     * Mete un producto de ejemplo con sus imágenes, pero solo si todavía no
     * hay uno con ese nombre. Así, si se carga el ejemplo dos veces, no se
     * duplica nada.
     *
     * @param producto       producto de ejemplo
     * @param archivosImagen nombres de los archivos de imagen que le tocan
     * @return 1 si se insertó, 0 si ya estaba
     */
    private static int cargarProductoEjemplo(Producto producto, String... archivosImagen) {
        if (tienda.getInventario().existe(producto.getNombre())) {
            return 0;
        }
        for (String archivo : archivosImagen) {
            String ruta = GestorImagenes.construirRuta(archivo);
            if (GestorImagenes.validarRuta(ruta) == null) {      // solo agregamos imágenes que existen de verdad
                producto.agregarImagen(ruta);
            }
        }
        tienda.agregarProductoAlInventario(producto);
        return 1;
    }

    /**
     * Llena el carrito de un cliente de ejemplo y lo mete a la cola. Si algún
     * producto ya no alcanza (por ejemplo porque se eliminó o se agotó),
     * devolvemos lo que se había apartado y no registramos al cliente.
     *
     * @param cliente cliente de ejemplo
     * @param compras pares de datos: nombre del producto y cantidad
     * @return 1 si se registró, 0 si no
     */
    private static int cargarClienteEjemplo(Cliente cliente, Object... compras) {
        if (tienda.getColaClientes().contiene(cliente.getIdentificacion())) {
            return 0;
        }
        try {
            for (int i = 0; i < compras.length; i += 2) {
                tienda.agregarAlCarrito(cliente, (String) compras[i], (Integer) compras[i + 1]);
            }
            tienda.registrarClienteEnCola(cliente);
            return 1;
        } catch (IllegalArgumentException e) {
            tienda.vaciarCarrito(cliente);
            Consola.mostrarError("No se pudo registrar a " + cliente.getNombre() + ": " + e.getMessage());
            return 0;
        }
    }

    // Opción 0: salir

    /**
     * Pide confirmación antes de salir. Si todavía hay clientes en la cola
     * le avisamos al usuario, porque al salir se pierden.
     *
     * @return true si de verdad quiere salir
     */
    private static boolean confirmarSalida() {
        if (tienda.hayClientesEnEspera()) {
            Consola.mostrarInfo("Todavía hay " + tienda.getColaClientes().getTamanio()
                    + " cliente(s) en espera sin atender.");
        }
        return Consola.confirmar("¿Está seguro de que desea salir?");
    }

    // Métodos de apoyo del menú

    /**
     * Le pide al usuario que escoja un producto de una lista. Puede escribir
     * el número, el nombre completo o solo una parte del nombre. Si la parte
     * que escribió coincide con un solo producto lo tomamos directamente; si
     * coincide con varios se los mostramos para que sea más específico.
     * Este mismo método lo usamos para el inventario y para el carrito.
     *
     * @param lista  lista de donde se escoge
     * @param accion lo que se va a hacer con el producto (sale en el mensaje)
     * @return el producto escogido, o null si canceló o no se encontró
     */
    private static Producto seleccionarProducto(ListaProductos lista, String accion) {
        String entrada = Consola.leerLinea("Número o nombre del producto a " + accion + " (Enter para cancelar): ");
        if (entrada.isEmpty()) {
            Consola.mostrarInfo("Operación cancelada.");
            return null;
        }
        if (esNumeroEnRango(entrada, lista.getTamanio())) {
            return lista.obtener(Integer.parseInt(entrada) - 1);
        }
        Producto producto = lista.buscar(entrada);
        if (producto != null) {
            return producto;
        }
        ListaProductos coincidencias = lista.buscarCoincidencias(entrada);
        if (coincidencias.getTamanio() == 1) {
            producto = coincidencias.getPrimero();
            Consola.mostrarInfo("Se seleccionó \"" + producto.getNombre() + "\".");
            return producto;
        }
        if (coincidencias.estaVacia()) {
            Consola.mostrarError("No se encontró ningún producto con \"" + entrada + "\".");
        } else {
            Consola.mostrarError("Hay " + coincidencias.getTamanio() + " productos que contienen \"" + entrada
                    + "\". Escriba un nombre más específico o use el número:");
            System.out.println(coincidencias);
        }
        return null;
    }

    /**
     * Si el inventario está vacío se lo avisamos al usuario.
     *
     * @return true si el inventario está vacío
     */
    private static boolean avisarSiInventarioVacio() {
        if (tienda.getInventario().estaVacio()) {
            Consola.mostrarInfo("El inventario está vacío. Inserte productos (opción 1) o cargue los de ejemplo (opción 9).");
            return true;
        }
        return false;
    }

    /**
     * Revisa si un texto es un número entre 1 y el máximo. Lo usamos para
     * saber si el usuario escogió algo por número o escribió un nombre.
     *
     * @param texto  lo que escribió el usuario
     * @param maximo número más grande permitido
     * @return true si es un número dentro del rango
     */
    private static boolean esNumeroEnRango(String texto, int maximo) {
        try {
            int numero = Integer.parseInt(texto);
            return numero >= 1 && numero <= maximo;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
