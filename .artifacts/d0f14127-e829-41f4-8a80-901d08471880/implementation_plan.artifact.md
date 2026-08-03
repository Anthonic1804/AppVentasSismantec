# Corrección de NullPointerException tras enviar Pedido Local

Se corregirá un cierre inesperado (crash) que ocurre al finalizar el envío de un pedido en el modo de Venta Local. El error se debe a que el sistema intenta consultar una visita inexistente (ID 0) y accede a sus propiedades de forma insegura.

## Cambios Propuestos

### Módulo de Pedidos

#### [MODIFY] [Detallepedido.kt](file:///C:/DESARROLLO/AppVentasSismantec/app/src/main/java/com/example/acae30/ui/pedidos/Detallepedido.kt)
- **Localización**: Función `pedidoEnviado()`.
- **Cambios**:
    - Reemplazar el uso del operador `!!` en la variable `visita`.
    - Implementar una validación segura: Si `idvisita` es 0 o si la visita no existe en la base de datos (comportamiento normal en Venta Local), el sistema redirigirá directamente al listado de pedidos (`Pedido.kt`).
    - Solo si existe una visita y esta se encuentra **Abierta**, se redirigirá a `Visita.kt` (flujo de Venta Externa).
- **Estilo**: Se mantendrá el código original comentado para comparación y se explicará la lógica de redirección basada en la existencia de la visita.

## Plan de Verificación

### Verificación Manual
1. Realizar un pedido en modo **Venta Local** (`tipoVentaLocal = true`).
2. Al finalizar el envío, verificar que la aplicación redirija correctamente al listado de pedidos sin cerrarse.
3. Realizar un pedido en modo **Venta Externa** (`tipoVentaLocal = false`) con una visita abierta.
4. Verificar que la aplicación redirija a `Visita.kt` para continuar/finalizar la visita como de costumbre.
