# Walkthrough: Visualización de Totales Almacenados en Room al Ver Pedidos Guardados/Enviados (`from == "ver"`)

## Cambios Realizados

### 1. [Pedidos.kt](file:///C:/DESARROLLO/AppVentasSismantec/app/src/main/java/com/example/acae30/modelos/Pedidos.kt) & [PedidosController.kt](file:///C:/DESARROLLO/AppVentasSismantec/app/src/main/java/com/example/acae30/controllers/PedidosController.kt)
- Se añadió el mapeo de `Venta_exenta`, `Venta_nosujeta` e `Iva_retenido` (junto con `Iva_percibido`) al modelo de dominio `Pedidos` desde la consulta de cabecera en Room.

### 2. [Detallepedido.kt](file:///C:/DESARROLLO/AppVentasSismantec/app/src/main/java/com/example/acae30/ui/pedidos/Detallepedido.kt)
- Se actualizó el observador de `viewModel.infoPedido`: cuando `from == "ver"` (visualización de un pedido ya guardado o enviado), la interfaz carga y muestra los totales directamente desde los valores persistidos en la base de datos (Room) en lugar de recalcularlos dinámicamente.

---

## Verificación
- Compilación completada con éxito (`gradle_build("app:assembleDebug")`).
