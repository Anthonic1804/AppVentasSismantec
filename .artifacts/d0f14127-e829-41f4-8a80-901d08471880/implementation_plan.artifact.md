# Corrección de Crash en Diálogo de Reporte (UninitializedPropertyAccessException)

Se corregirá el error `UninitializedPropertyAccessException` que ocurre al intentar cambiar el texto de un diálogo que no ha sido inicializado completamente debido a la velocidad de actualización de los estados del reporte.

## Cambios Propuestos

### Componente de UI Común

#### [MODIFY] [AlertDialogo.kt](file:///C:/DESARROLLO/AppVentasSismantec/app/src/main/java/com/example/acae30/AlertDialogo.kt)
- Proteger el método `changeText()`.
- Se añadirá una verificación `if (::dialogo.isInitialized)` antes de intentar buscar la vista del mensaje.
- Se mantendrá el código original comentado para comparación.

### Módulo de Pedidos

#### [MODIFY] [Pedido.kt](file:///C:/DESARROLLO/AppVentasSismantec/app/src/main/java/com/example/acae30/ui/pedidos/Pedido.kt)
- En el método `manejarEstadoReporte`, asegurar que si el estado es `Descargando` o `Guardando` y el diálogo no está visible (por alguna carrera de hilos), se llame a `Cargando()` antes de intentar actualizar el mensaje.

## Plan de Verificación

### Verificación Manual
1. Abrir la pantalla de Pedidos.
2. Generar un reporte.
3. Verificar que los mensajes ("Descargando", "Procesando") se muestren correctamente sin que la aplicación se cierre.
