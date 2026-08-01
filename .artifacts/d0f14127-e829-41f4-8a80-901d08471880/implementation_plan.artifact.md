# Corrección de NullPointerException en Sincronización de Pedidos

Se corregirá el crash reportado en `Pedido.kt` eliminando el uso inseguro del operador `!!` en el flujo de sincronización. Se implementará un manejo de nulos seguro para los datos provenientes de la API y de la base de datos local.

## Cambios Propuestos

### Módulo de Pedidos

#### [MODIFY] [Pedido.kt](file:///C:/DESARROLLO/AppVentasSismantec/app/src/main/java/com/example/acae30/ui/pedidos/Pedido.kt)
- **Localización**: Dentro de la función `sincronizacionDePedidos()`.
- **Cambios**:
    - Reemplazar `item.IdPedidoApp!!` por una verificación de nulidad previa.
    - Cambiar `pedido.pedidoDte!!` y `pedido.pedidoDteError!!` por comparaciones seguras (`== true`).
    - En la llamada a `actualizarInformacionPedidoTransmitido`, reemplazar todos los `!!` en los strings por el operador Elvis `?: ""` para evitar el crash si el servidor no devuelve esos campos.
    - En la llamada a `actualizarEstadoPedidoEnviado`, usar `pedido.idPedido ?: 0` en lugar de `pedido.idPedido!!`.
- **Estilo**: Se mantendrá el código original comentado para facilitar la comparación y se añadirán comentarios explicativos.

## Plan de Verificación

### Verificación Automatizada
- Ejecutar `gradlew app:assembleDebug` para asegurar que no hay errores de sintaxis.

### Verificación Manual
1. Intentar sincronizar pedidos cuando el servidor devuelve información incompleta o nula (por ejemplo, pedidos que no son DTE).
2. Verificar en el Logcat que no se produzca el `NullPointerException`.
3. Confirmar que los pedidos se marcan como sincronizados correctamente en la base de datos local.
