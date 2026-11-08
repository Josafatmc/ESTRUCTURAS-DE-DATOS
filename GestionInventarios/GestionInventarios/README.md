# Aplicación de Gestión de Inventarios — Segundo avance
**Universidad CENFOTEC** · SOFT-10 Estructuras de Datos · SCV5 · C3-2026 · Docente: Romario Salas Cerdas

**Integrantes:**
Josafat Mora Carballo
Jennifer Sibaja Marin
Djorkaeff Jiménez Carballo

## Descripción

Sistema de ventas de productos en línea que usa estructuras de datos dinámicas
implementadas a mano para controlar el inventario y los pedidos.

| Avance | Contenido |
|--------|-----------|
| 1 | `ListaProductos` como **lista enlazada simple** de objetos `Producto`. |
| 2 | Clases `Cliente`, `ColaClientes`, `Tienda` y `ArbolProductos`, y la compra mediante el **carrito** (`ListaProductos`) de cada cliente. |

### Requisitos del segundo avance y dónde se cumplen

| Requisito de la consigna | Implementación |
|---|---|
| Inventario almacenado en la `Tienda` como `ArbolProductos` | `Tienda.inventario` |
| `ArbolProductos` = árbol binario de búsqueda cuyo nodo es un `Producto` y cuya llave es el nombre | `ArbolProductos` + `NodoArbolProducto` |
| Cada `Cliente` almacena su `ListaProductos` personal llamada **carrito** | `Cliente.carrito` |
| Los productos del carrito se eligen del inventario de la `Tienda` | `Tienda.agregarAlCarrito(...)` |
| `ColaClientes` como **cola de prioridad** | `ColaClientes` + `NodoCliente` |
| `Cliente.prioridad` entera de 1 a 3 (1 básico, 2 afiliado, 3 premium) | `Cliente.PRIORIDAD_*` |
| Se atiende la prioridad más alta; en empate, el más cercano al frente | `ColaClientes.atender()` |
| La `Tienda` tiene un atributo `ColaClientes` | `Tienda.colaClientes` |
| `menu()` en la clase `Main` con insertar productos, insertar clientes (llenando el carrito) y atender al siguiente cliente (factura con costo total acumulado) | `Main.menu()` opciones 1, 6 y 8 |
| `main()` en `Main` que invoca el menú | `Main.main(...)` |

## Estructura del proyecto

```
GestionInventarios/
├── imagenes/                              ← imágenes de los productos
└── src/inventario/
    ├── Main.java                          ← clase funcional: main() y menu()
    ├── modelo/
    │   ├── Producto.java                  ← entidad Producto
    │   ├── Cliente.java                   ← entidad Cliente (prioridad + carrito)
    │   └── Tienda.java                    ← inventario (ABB) + cola de clientes + facturación
    ├── estructuras/
    │   ├── NodoProducto.java              ← nodo de la lista enlazada
    │   ├── ListaProductos.java            ← lista enlazada simple (carrito)
    │   ├── NodoArbolProducto.java         ← nodo del árbol binario de búsqueda
    │   ├── ArbolProductos.java            ← árbol binario de búsqueda (inventario)
    │   ├── NodoCliente.java               ← nodo de la cola
    │   └── ColaClientes.java              ← cola de prioridad de clientes
    └── util/
        ├── Consola.java                   ← lectura y validación de datos del usuario
        ├── Formato.java                   ← utilidades para las tablas de los reportes
        └── GestorImagenes.java            ← validación de rutas de imágenes
```

## Diseño de las estructuras de datos

### `ArbolProductos`: árbol binario de búsqueda (inventario)
- **Llave:** nombre del producto, sin distinguir mayúsculas de minúsculas. No se permiten llaves repetidas: si se inserta un nombre que ya existe, el menú ofrece **reabastecer** el producto.
- **Operaciones:** `insertar`, `buscar`, `existe`, `eliminar` (los 3 casos: hoja, un hijo y dos hijos con sucesor in-orden), `getMinimo`, `getMaximo`, `getAltura`, recorridos **in-orden / pre-orden / post-orden**, búsqueda parcial, valor del inventario, reporte en tabla y diagrama del árbol.
- **Complejidad:** O(h) para insertar, buscar y eliminar (O(log n) en promedio, O(n) si el árbol degenera). Recorridos O(n).

### `ColaClientes`: cola de prioridad
- Los clientes se guardan en **orden de llegada** (encolar al final en O(1)).
- `atender()` recorre desde el frente y elige la **prioridad más alta**. Usa la comparación estricta `>`, así que en un empate gana el que está **más cerca del frente**. El recorrido se detiene en cuanto encuentra un cliente premium (3), porque nadie lo puede superar. Complejidad O(n).
- `generarReporte()` muestra la cola en orden de llegada junto con el **turno de atención** que le corresponde a cada cliente, calculado en O(n) sin modificar la cola.

### `ListaProductos`: carrito de cada cliente
- Cada línea del carrito es una **copia** del producto del inventario. Su cantidad es la de unidades compradas, de modo que el costo total es precio × cantidad.
- Si se agrega otra vez un producto que ya está en el carrito, se suman las unidades en la misma línea.

### Flujo de compra (`Tienda`)
1. Al agregar un producto al carrito, las unidades se **apartan**: se descuentan del inventario para que no se puedan vender dos veces.
2. Si el cliente quita productos o cancela su registro, las unidades **vuelven al inventario**.
3. No se pueden vender productos **agotados** ni **vencidos**.
4. El cliente entra a la cola de prioridad solo si su carrito tiene al menos un producto.
5. Al atenderlo se emite una **factura** consecutiva (F-000001, …) con el detalle del carrito y el **costo total acumulado**.

## Menú principal

```
 INVENTARIO (árbol binario de búsqueda)
  1. Insertar producto al inventario
  2. Mostrar inventario (orden alfabético)
  3. Buscar producto en el inventario
  4. Eliminar producto del inventario
  5. Ver estructura del árbol de inventario
 CLIENTES (cola de prioridad)
  6. Registrar cliente en la cola (llenar su carrito)
  7. Mostrar cola de clientes
  8. Atender siguiente cliente (emitir factura)
 OTROS
  9. Cargar datos de ejemplo
  0. Salir
```

La opción 9 carga 7 productos, insertados en un orden que deja el árbol balanceado, y 5 clientes. Su orden de llegada es Ana (básica), Luis (premium), María (afiliada), Carlos (premium) y Sofía (básica). El orden de atención esperado es **Luis → Carlos → María → Ana → Sofía**.

## Ejecución

Requiere JDK 17 o superior. Desde la carpeta `GestionInventarios/GestionInventarios`:

```bash
javac -encoding UTF-8 -d out $(find src -name "*.java")
java -cp out inventario.Main
```

También se puede abrir la carpeta como proyecto en IntelliJ IDEA (`GestionInventarios.iml`) y ejecutar `inventario.Main`.

El programa busca solo la carpeta `imagenes`, ya sea que se ejecute desde `GestionInventarios/GestionInventarios` o desde la raíz del repositorio, así que funciona en cualquier computadora sin tener que cambiar rutas.
