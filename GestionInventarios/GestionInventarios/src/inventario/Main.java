package inventario;

import inventario.estructuras.ListaProductos;
import inventario.modelo.Producto;
import inventario.util.Consola;
import inventario.util.GestorImagenes;

import java.time.LocalDate;
import java.util.List;

public class Main {

    /** Lista enlazada simple con la que interactúa el usuario a través del menú. */
    private static final ListaProductos listaProductos = new ListaProductos();

    // Opciones del menú principal (constantes para evitar "números mágicos")
    private static final int OPCION_SALIR = 0;
    private static final int OPCION_INSERTAR_INICIO = 1;
    private static final int OPCION_INSERTAR_FINAL = 2;
    private static final int OPCION_INSERTAR_POSICION = 3;
    private static final int OPCION_MOSTRAR = 4;
    private static final int OPCION_BUSCAR = 5;
    private static final int OPCION_MODIFICAR = 6;
    private static final int OPCION_ELIMINAR = 7;
    private static final int OPCION_REPORTE = 8;
    private static final int OPCION_DATOS_EJEMPLO = 9;

    /** Constructor privado: la clase Main no necesita instanciarse. */
    private Main() {
    }

    public static void main(String[] args) {
        // 1. Mensaje de bienvenida
        Consola.mostrarTitulo("SISTEMA DE VENTAS EN LÍNEA - Gestión de Productos (Avance 1)");
        System.out.println("  Estructura utilizada: lista enlazada simple (ListaProductos)");

        // 2. Verificación del entorno: directorio de imágenes del proyecto
        if (!GestorImagenes.existeDirectorio()) {
            Consola.mostrarError("No se encontró el directorio \"" + GestorImagenes.DIRECTORIO_IMAGENES
                    + "/\". Ejecute el programa desde la raíz del proyecto para poder asociar imágenes.");
        }

        // 3. Ejecución del menú principal hasta que el usuario decida salir
        try {
            menu();
        } catch (Consola.EntradaFinalizadaException e) {
            System.out.println();
            Consola.mostrarInfo("Se cerró la entrada de datos. Finalizando el programa.");
        }

        // 4. Despedida
        System.out.println("\n¡Gracias por utilizar el sistema! Hasta pronto.");
    }

    /**
     * Menú principal de consola. Se repite hasta que el usuario elige la
     * opción "Salir" y delega cada opción en un método específico.
     */
    public static void menu() {
        int opcion;
        do {
            mostrarMenuPrincipal();
            opcion = Consola.leerEntero("Seleccione una opción: ", OPCION_SALIR, OPCION_DATOS_EJEMPLO);

            switch (opcion) {
                case OPCION_INSERTAR_INICIO -> insertarProducto(true);
                case OPCION_INSERTAR_FINAL -> insertarProducto(false);
                case OPCION_INSERTAR_POSICION -> insertarProductoEnPosicion();
                case OPCION_MOSTRAR -> mostrarProductos();
                case OPCION_BUSCAR -> buscarProducto();
                case OPCION_MODIFICAR -> modificarProducto();
                case OPCION_ELIMINAR -> eliminarProducto();
                case OPCION_REPORTE -> mostrarReporteCostos();
                case OPCION_DATOS_EJEMPLO -> cargarDatosEjemplo();
                case OPCION_SALIR -> {
                    if (!Consola.confirmar("¿Está seguro de que desea salir?")) {
                        opcion = -1;        // se cancela la salida y el menú continúa
                    }
                }
                default -> Consola.mostrarError("Opción no válida.");
            }

            if (opcion != OPCION_SALIR && opcion != -1) {
                Consola.pausar();
            }
        } while (opcion != OPCION_SALIR);
    }
    // Presentación del menú

    /** Imprime las opciones del menú principal junto con un resumen de la lista. */
    private static void mostrarMenuPrincipal() {
        Consola.mostrarTitulo("MENÚ PRINCIPAL");
        System.out.printf("  Productos en la lista: %d   |   Costo total acumulado: %s%n",
                listaProductos.getTamanio(), Producto.formatearMonto(listaProductos.calcularCostoTotal()));
        System.out.println("  --------------------------------------------------");
        System.out.println("  1. Insertar producto al INICIO de la lista");
        System.out.println("  2. Insertar producto al FINAL de la lista");
        System.out.println("  3. Insertar producto en una POSICIÓN específica");
        System.out.println("  4. Mostrar todos los productos");
        System.out.println("  5. Buscar un producto por nombre");
        System.out.println("  6. Modificar un producto (datos e imágenes)");
        System.out.println("  7. Eliminar productos");
        System.out.println("  8. Ver reporte de costos");
        System.out.println("  9. Cargar productos de ejemplo");
        System.out.println("  0. Salir");
        System.out.println("  --------------------------------------------------");
    }

    // Opción 1, 2 y 3: inserción
   
    /**
     * Solicita los datos de un producto nuevo y lo inserta al inicio o al
     * final de la lista.
     */
    private static void insertarProducto(boolean alInicio) {
        Consola.mostrarTitulo(alInicio ? "INSERTAR PRODUCTO AL INICIO" : "INSERTAR PRODUCTO AL FINAL");
        Producto producto = leerDatosProductoNuevo();
        if (producto == null) {
            return;
        }
        if (alInicio) {
            listaProductos.insertarAlInicio(producto);
        } else {
            listaProductos.insertarAlFinal(producto);
        }
        Consola.mostrarExito("Producto \"" + producto.getNombre() + "\" insertado al "
                + (alInicio ? "inicio" : "final") + " de la lista.");
        ofrecerAgregarImagenes(producto);
    }

    /** Solicita los datos de un producto nuevo y lo inserta en la posición que indique el usuario. */
    private static void insertarProductoEnPosicion() {
        Consola.mostrarTitulo("INSERTAR PRODUCTO EN UNA POSICIÓN");
        int tamanio = listaProductos.getTamanio();
        if (tamanio > 0) {
            listaProductos.imprimirLista();
        }
        int posicion = Consola.leerEntero(String.format(
                "Posición en la que quedará el producto (1 = inicio, %d = final): ", tamanio + 1), 1, tamanio + 1);

        Producto producto = leerDatosProductoNuevo();
        if (producto == null) {
            return;
        }
        listaProductos.insertarEnPosicion(posicion - 1, producto);   // la lista usa posiciones base 0
        Consola.mostrarExito("Producto \"" + producto.getNombre() + "\" insertado en la posición " + posicion + ".");
        ofrecerAgregarImagenes(producto);
    }

    private static Producto leerDatosProductoNuevo() {
        String nombre = Consola.leerTexto("Nombre del producto: ");
        if (listaProductos.existe(nombre)) {
            Consola.mostrarError("Ya existe un producto llamado \"" + nombre
                    + "\". Use la opción 6 si desea modificarlo.");
            return null;
        }
        double precio = Consola.leerDecimal("Precio unitario (₡): ", 0);
        String categoria = Consola.leerTexto("Categoría: ");
        LocalDate fechaVencimiento = Consola.leerFechaOpcional(
                "Fecha de vencimiento dd/MM/aaaa (deje en blanco si no aplica): ");
        int cantidad = Consola.leerEntero("Cantidad de unidades: ", 0, Integer.MAX_VALUE);

        return new Producto(nombre, precio, categoria, fechaVencimiento, cantidad);
    }

    /**
     * Pregunta al usuario si desea asociar imágenes al producto recién creado.
     *
     * @param producto producto recién insertado
     */
    private static void ofrecerAgregarImagenes(Producto producto) {
        while (Consola.confirmar("¿Desea agregar una imagen a \"" + producto.getNombre() + "\"?")) {
            agregarImagen(producto);
        }
    }

    // Opción 4 y 5: consulta
    
    /** Recorre la lista y muestra todos los productos. */
    private static void mostrarProductos() {
        Consola.mostrarTitulo("LISTA DE PRODUCTOS");
        listaProductos.imprimirLista();
    }

    /** Busca un producto por nombre y muestra su ficha completa. */
    private static void buscarProducto() {
        Consola.mostrarTitulo("BUSCAR PRODUCTO");
        if (avisarSiListaVacia()) {
            return;
        }
        String nombre = Consola.leerTexto("Nombre del producto a buscar: ");
        Producto producto = listaProductos.buscar(nombre);
        if (producto == null) {
            Consola.mostrarError("No se encontró ningún producto llamado \"" + nombre + "\".");
        } else {
            Consola.mostrarExito("Producto encontrado en la posición " + (listaProductos.indiceDe(nombre) + 1) + ":");
            System.out.println(producto.toDetalle());
        }
    }

    // Opción 6: modificación
    
    /**
     * Permite elegir un producto y modificar cualquiera de sus datos,
     * incluidas sus imágenes, mediante un submenú.
     */
    private static void modificarProducto() {
        Consola.mostrarTitulo("MODIFICAR PRODUCTO");
        if (avisarSiListaVacia()) {
            return;
        }
        Producto producto = seleccionarProducto("modificar");
        if (producto == null) {
            return;
        }

        int opcion;
        do {
            Consola.mostrarTitulo("MODIFICANDO: " + producto.getNombre());
            System.out.println(producto.toDetalle());
            System.out.println("  --------------------------------------------------");
            System.out.println("  1. Nombre");
            System.out.println("  2. Precio unitario");
            System.out.println("  3. Categoría");
            System.out.println("  4. Fecha de vencimiento");
            System.out.println("  5. Cantidad");
            System.out.println("  6. Agregar imagen");
            System.out.println("  7. Eliminar imagen");
            System.out.println("  0. Volver al menú principal");
            opcion = Consola.leerEntero("¿Qué desea modificar?: ", 0, 7);

            String nombre = producto.getNombre();
            try {
                switch (opcion) {
                    case 1 -> {
                        String nuevoNombre = Consola.leerTexto("Nuevo nombre: ");
                        listaProductos.modificarNombre(nombre, nuevoNombre);
                        Consola.mostrarExito("Nombre actualizado.");
                    }
                    case 2 -> {
                        listaProductos.modificarPrecio(nombre, Consola.leerDecimal("Nuevo precio unitario (₡): ", 0));
                        Consola.mostrarExito("Precio actualizado.");
                    }
                    case 3 -> {
                        listaProductos.modificarCategoria(nombre, Consola.leerTexto("Nueva categoría: "));
                        Consola.mostrarExito("Categoría actualizada.");
                    }
                    case 4 -> {
                        LocalDate fecha = Consola.leerFechaOpcional(
                                "Nueva fecha dd/MM/aaaa (deje en blanco si no aplica): ");
                        listaProductos.modificarFechaVencimiento(nombre, fecha);
                        Consola.mostrarExito("Fecha de vencimiento actualizada.");
                    }
                    case 5 -> {
                        listaProductos.modificarCantidad(nombre,
                                Consola.leerEntero("Nueva cantidad de unidades: ", 0, Integer.MAX_VALUE));
                        Consola.mostrarExito("Cantidad actualizada.");
                    }
                    case 6 -> agregarImagen(producto);
                    case 7 -> eliminarImagen(producto);
                    default -> { /* 0: volver al menú principal */ }
                }
            } catch (IllegalArgumentException e) {
                Consola.mostrarError(e.getMessage());
            }
        } while (opcion != 0);
    }

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

        // Si el usuario escribió un número válido, se toma la imagen de la lista mostrada
        String ruta = esNumeroEnRango(entrada, disponibles.size())
                ? disponibles.get(Integer.parseInt(entrada) - 1)
                : GestorImagenes.construirRuta(entrada);

        String error = GestorImagenes.validarRuta(ruta);
        if (error != null) {
            Consola.mostrarError(error);
        } else if (listaProductos.agregarImagen(producto.getNombre(), ruta)) {
            Consola.mostrarExito("Imagen \"" + ruta + "\" agregada a \"" + producto.getNombre() + "\".");
        } else {
            Consola.mostrarError("La imagen \"" + ruta + "\" ya estaba asociada a este producto.");
        }
    }

    /**
     * Elimina una de las imágenes asociadas al producto.
     */
    private static void eliminarImagen(Producto producto) {
        List<String> imagenes = producto.getListaImagenes();
        if (imagenes.isEmpty()) {
            Consola.mostrarInfo("El producto no tiene imágenes asociadas.");
            return;
        }
        for (int i = 0; i < imagenes.size(); i++) {
            System.out.printf("   %2d. %s%n", i + 1, imagenes.get(i));
        }
        int indice = Consola.leerEntero("Número de la imagen a eliminar (0 para cancelar): ", 0, imagenes.size());
        if (indice == 0) {
            Consola.mostrarInfo("Operación cancelada.");
            return;
        }
        String ruta = imagenes.get(indice - 1);
        listaProductos.eliminarImagen(producto.getNombre(), ruta);
        Consola.mostrarExito("Imagen \"" + ruta + "\" eliminada del producto.");
    }

    // Opción 7: eliminación

    /** Submenú para eliminar un producto por nombre, el primero, el último o todos. */
    private static void eliminarProducto() {
        Consola.mostrarTitulo("ELIMINAR PRODUCTOS");
        if (avisarSiListaVacia()) {
            return;
        }
        System.out.println("  1. Eliminar un producto específico");
        System.out.println("  2. Eliminar el primer producto (" + listaProductos.getPrimero().getNombre() + ")");
        System.out.println("  3. Eliminar el último producto (" + listaProductos.getUltimo().getNombre() + ")");
        System.out.println("  4. Vaciar la lista completa");
        System.out.println("  0. Cancelar");
        int opcion = Consola.leerEntero("Seleccione una opción: ", 0, 4);

        switch (opcion) {
            case 1 -> {
                Producto producto = seleccionarProducto("eliminar");
                if (producto != null && Consola.confirmar("¿Eliminar \"" + producto.getNombre() + "\"?")) {
                    listaProductos.eliminar(producto.getNombre());
                    Consola.mostrarExito("Producto \"" + producto.getNombre() + "\" eliminado.");
                }
            }
            case 2 -> {
                if (Consola.confirmar("¿Eliminar \"" + listaProductos.getPrimero().getNombre() + "\"?")) {
                    Consola.mostrarExito("Producto \"" + listaProductos.eliminarAlInicio().getNombre() + "\" eliminado.");
                }
            }
            case 3 -> {
                if (Consola.confirmar("¿Eliminar \"" + listaProductos.getUltimo().getNombre() + "\"?")) {
                    Consola.mostrarExito("Producto \"" + listaProductos.eliminarAlFinal().getNombre() + "\" eliminado.");
                }
            }
            case 4 -> {
                if (Consola.confirmar("¿Seguro que desea eliminar los " + listaProductos.getTamanio() + " productos?")) {
                    listaProductos.vaciar();
                    Consola.mostrarExito("La lista quedó vacía.");
                }
            }
            default -> Consola.mostrarInfo("Operación cancelada.");
        }
    }

    // Opción 8: reporte de costos

    /** Imprime el reporte de costos totales por producto y el costo acumulado de la lista. */
    private static void mostrarReporteCostos() {
        Consola.mostrarTitulo("REPORTE DE COSTOS");
        listaProductos.imprimirReporteCostos();
    }
    // Opción 9: datos de ejemplo

    private static void cargarDatosEjemplo() {
        Consola.mostrarTitulo("CARGAR PRODUCTOS DE EJEMPLO");
        Producto[] ejemplos = {
                new Producto("Laptop Lenovo IdeaPad 3", 389_900, "Electrónica", 5),
                new Producto("Mouse inalámbrico Logitech", 12_500, "Electrónica", 20),
                new Producto("Leche Dos Pinos 1L", 1_150, "Lácteos", LocalDate.now().plusDays(12), 48),
                new Producto("Café Britt 340g",  4_850, "Abarrotes", LocalDate.now().plusMonths(8), 30),
                new Producto("Manzana roja (unidad)", 450, "Frutas", LocalDate.now().plusDays(10), 100)
        };
        String[][] imagenesEjemplo = {
                {"GestionInventarios\\GestionInventarios\\imagenes\\laptop_1.png", "GestionInventarios\\GestionInventarios\\imagenes\\laptop_2.png"},
                {"GestionInventarios\\GestionInventarios\\imagenes\\mouse.png"},
                {"GestionInventarios\\GestionInventarios\\imagenes\\leche.png"},
                {"GestionInventarios\\GestionInventarios\\imagenes\\cafe.png"},
                {"GestionInventarios\\GestionInventarios\\imagenes\\manzana.png"}
        };

        int insertados = 0;
        for (int i = 0; i < ejemplos.length; i++) {
            Producto producto = ejemplos[i];
            if (listaProductos.existe(producto.getNombre())) {
                continue;
            }
            for (String ruta : imagenesEjemplo[i]) {
                if (GestorImagenes.validarRuta(ruta) == null) {   // solo se asocian imágenes que existen
                    producto.agregarImagen(ruta);
                }
            }
            listaProductos.insertarAlFinal(producto);
            insertados++;
        }
        Consola.mostrarExito(insertados + " producto(s) de ejemplo insertado(s) al final de la lista.");
        listaProductos.imprimirLista();
    }

    // Métodos auxiliares del menú

    private static Producto seleccionarProducto(String accion) {
        listaProductos.imprimirLista();
        String entrada = Consola.leerLinea("Número o nombre del producto a " + accion + " (Enter para cancelar): ");
        if (entrada.isEmpty()) {
            Consola.mostrarInfo("Operación cancelada.");
            return null;
        }
        Producto producto = esNumeroEnRango(entrada, listaProductos.getTamanio())
                ? listaProductos.obtener(Integer.parseInt(entrada) - 1)
                : listaProductos.buscar(entrada);
        if (producto == null) {
            Consola.mostrarError("No se encontró ningún producto con \"" + entrada + "\".");
        }
        return producto;
    }

    private static boolean avisarSiListaVacia() {
        if (listaProductos.estaVacia()) {
            Consola.mostrarInfo("La lista está vacía. Inserte productos (opciones 1-3) o cargue los de ejemplo (opción 9).");
            return true;
        }
        return false;
    }

    private static boolean esNumeroEnRango(String texto, int maximo) {
        try {
            int numero = Integer.parseInt(texto);
            return numero >= 1 && numero <= maximo;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
