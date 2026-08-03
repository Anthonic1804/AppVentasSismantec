# Corrección de visualización en la pantalla de Pedidos

El listado de pedidos no se muestra debido a un error en el archivo de diseño `activity_pedido.xml`. Se está utilizando un `CoordinatorLayout` como raíz, pero los elementos hijos utilizan atributos de `ConstraintLayout` (como `app:layout_constraint...`) y tamaños de `0dp`. En un `CoordinatorLayout`, estos atributos son ignorados, lo que resulta en que el `RecyclerView` tenga un tamaño de 0x0 y no sea visible.

## Cambios Propuestos

### Módulo de Interfaz de Usuario

#### [MODIFY] [activity_pedido.xml](file:///C:/DESARROLLO/AppVentasSismantec/app/src/main/res/layout/activity_pedido.xml)
- Cambiar el layout raíz de `androidx.coordinatorlayout.widget.CoordinatorLayout` a `androidx.constraintlayout.widget.ConstraintLayout`.
- Asegurar que todos los elementos hijos (Toolbar, RecyclerView y FABs) tengan las restricciones (`constraints`) correctas para un `ConstraintLayout`.
- Específicamente, para los `FloatingActionButton`, reemplazar `android:layout_gravity` por restricciones de borde inferior y derecho.

#### [MODIFY] [Pedido.kt](file:///C:/DESARROLLO/AppVentasSismantec/app/src/main/java/com/example/acae30/ui/pedidos/Pedido.kt)
- Verificar si hay algún código que dependa de `CoordinatorLayout` (aunque no parece haber ninguno en el uso actual del binding).

## Plan de Verificación

### Verificación Visual
1. Desplegar la aplicación en el dispositivo.
2. Navegar a la pantalla de Pedidos.
3. Verificar que el listado de pedidos sea visible y ocupe el espacio correcto debajo del encabezado.
4. Verificar que los botones flotantes (FAB) estén posicionados correctamente en la esquina inferior derecha.

### Verificación Técnica
- Ejecutar `ui_state` para confirmar que el `RecyclerView` ahora aparece en la jerarquía con dimensiones válidas.
- Confirmar que no hay errores de compilación tras el cambio de layout.
