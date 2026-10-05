# Aplicación de Gestión de Inventarios — Primer avance
**Universidad CENFOTEC** · Docente: Romario Salas Cerdas

**Integrantes:**
Josafat Mora Carballo
Jennifer Sibaja Marin
Djorkaeff Jiménez Carballo

## Descripción

Primer avance del sistema de ventas de productos en línea. Implementa la
`ListaProductos` como **lista enlazada simple** de objetos `Producto` y un
menú de consola (CLI) en la clase `Main` para interactuar con ella.

## Estructura del proyecto

```
GestionInventarios/
├── imagenes/                         ← imágenes de los productos (parte del proyecto)
└── src/inventario/
    ├── Main.java                     ← clase funcional: main() y menu()
    ├── modelo/Producto.java          ← entidad Producto
    ├── estructuras/NodoProducto.java ← clase nodo de la lista
    ├── estructuras/ListaProductos.java ← clase estructura (lista enlazada simple)
    └── util/
        ├── Consola.java              ← lectura y validación de datos del usuario
        └── GestorImagenes.java       ← validación de rutas dentro de GestionInventarios\GestionInventarios\imagenes
```