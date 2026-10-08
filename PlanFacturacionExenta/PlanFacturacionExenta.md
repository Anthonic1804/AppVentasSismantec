# Plan de Implementación: Facturación de Productos Exentos de IVA y No Sujetos

Esta funcionalidad permitirá procesar y facturar correctamente productos **Exentos (E)** y **No Sujetos (NS)** de IVA, almacenar adecuadamente los totales desglosados en la cabecera del pedido, gestionar pedidos para clientes de categoría **Exento**, y mejorar la visualización en el detalle del pedido mostrando el Precio Unitario de cada producto e incluyendo EXENTA y NO SUJETA en la tabla de totales.

## Reglas Generales de Desarrollo

1. **Preservación de Código Anterior:** Todo código existente que sea reemplazado o modificado no debe ser eliminado directamente; debe conservarse comentado para eventuales verificaciones y auditorías.
2. **Documentación de Código Nuevo:** Todo código nuevo que se incorpore debe contar con comentarios explícitos paso a paso detallando la función exacta que realiza.
3. **Confirmación Previa para Nuevos Archivos o Directorios:** Si durante la implementación se requiere crear un nuevo archivo o directorio, se solicitará confirmación previa al usuario antes de su creación.
4. **Seguimiento Progresivo:** La lista de tareas se mantendrá como documento vivo, actualizando las casillas de verificación (`[x]`) conforme se complete y verifique cada etapa.

---

## Reglas de Negocio a Implementar (Normativa 2.0)

### 1. Pedidos para Clientes Exentos
- Si la categoría del cliente (`Categoria_cliente` en la ficha del cliente) es `"Exento"` (o contiene `"EXENTO"`):
  - **Todo el pedido será Exento de IVA**, sea para Factura (FC), Crédito Fiscal (CF) o Recibo (**RC**).
  - En lugar de tomar `Precio_iva` / `Precio_u_iva`, se tomará el campo **`Precio` / `Precio_u`** (precio sin IVA) de la ficha del producto como base (`precioFinalVm`).
  - Al registrar en `PedidoDetalleEntity`:
    - El campo **`Precio_iva`** almacena dicho valor exento (`precioFinalVm`).
    - El campo **`Precio`** almacena `Precio_iva / 1.13` para mantener la simulación de precio base gravado en el esquema de la base de datos local.
  - No se desglosa IVA para ningún producto del pedido.

### 2. Productos Exentos (Cliente No Exento)
- Si el cliente no es Exento, pero el `TipoFiscal` del producto en la tabla `Inventario` es `"Exento"` (o `"E"`):
  - El producto se agregará al pedido sin IVA.
  - Se tomará el precio del campo **`Precio` / `Precio_u`** (precio sin IVA) de la ficha del producto como base (`precioFinalVm`).
  - Al registrar en `PedidoDetalleEntity`:
    - El campo **`Precio_iva`** almacena dicho valor exento (`precioFinalVm`).
    - El campo **`Precio`** almacena `Precio_iva / 1.13` para simular la base.

### 3. Productos No Sujetos (NS)
- Si el `TipoFiscal` del producto en la tabla `Inventario` es `"No Sujeto"` (o `"NS"`):
  - Aplica para Factura, Crédito Fiscal y Recibo (RC).
  - **No se desglosa IVA** (el valor no se divide entre 1.13).
  - Se toma como valor base el campo **`Precio_iva` / `Precio_u_iva`** (ejemplo: $25.00).
  - En el cálculo de totales del comprobante, su valor no suma a `Sumas Gravadas` ni a `IVA`, sino a la casilla de **Ventas No Sujetas**.

### Ejemplo de Totales en Crédito Fiscal (CF):
- **Producto Gravado (G):** Queso Crema, 1 unidad, `precio_u_iva` $11.30.
  - Sumas Gravadas: $10.00 | IVA (13%): $1.13 | Total: $11.30
- **Producto No Sujeto (NS):** 1 unidad, `precio_u_iva` $25.00.
  - Sumas Gravadas: $0.00 | IVA: $0.00 | No Sujetas: $25.00 | Total: $25.00
- **Totales Combinados del Pedido:**
  - Sumas Gravadas: $10.00
  - IVA (13%): $1.13
  - Ventas No Sujetas: $25.00
  - Total Final: $36.30

---

## Lista de Tareas (Checklist de Ejecución)

### FASE 0: Gestión de Versiones (Git)
- `[x]` **Tarea 0.1:** Crear y cambiar a una nueva rama de Git con nombre `FacturacionExenta`.

### FASE 1: Base de Datos y Entidades
- `[x]` **Tarea 1.1:** Agregar columnas `Venta_exenta` y `Venta_nosujeta` en [PedidosEntity.kt](file:///C:/DESARROLLO/AppVentasSismantec/app/src/main/java/com/example/acae30/data/local/entity/PedidosEntity.kt).
- `[x]` **Tarea 1.2:** Incrementar la versión de la base de datos Room (de 4 a 5) en [AppDataBase.kt](file:///C:/DESARROLLO/AppVentasSismantec/app/src/main/java/com/example/acae30/data/local/appDatabase/AppDataBase.kt).
- `[x]` **Tarea 1.3:** Actualizar el `@Query` de `actualizarTotalesFiscales` en [PedidosDao.kt](file:///C:/DESARROLLO/AppVentasSismantec/app/src/main/java/com/example/acae30/data/local/dao/PedidosDao.kt) para guardar `Venta_exenta` y `Venta_nosujeta`.
- `[x]` **Tarea 1.4:** Actualizar `actualizarTotalesFiscalesLocal` en [PedidosRepository.kt](file:///C:/DESARROLLO/AppVentasSismantec/app/src/main/java/com/example/acae30/data/repository/PedidosRepository.kt) y en [ActualizarTotalesFiscalesUseCase.kt](file:///C:/DESARROLLO/AppVentasSismantec/app/src/main/java/com/example/acae30/domain/usecase/pedidos/ActualizarTotalesFiscalesUseCase.kt).

### FASE 2: Asignación de Precios y Tipo Fiscal al Agregar Producto
- `[x]` **Tarea 2.1:** Detectar si la categoría del cliente es Exento (`esClienteExento`) en [ProductoAgregarViewModel.kt](file:///C:/DESARROLLO/AppVentasSismantec/app/src/main/java/com/example/acae30/ui/pedidos/ProductoAgregarViewModel.kt).
- `[x]` **Tarea 2.2:** Ajustar la selección y calculo de precio base en [ProductoAgregarViewModel.kt](file:///C:/DESARROLLO/AppVentasSismantec/app/src/main/java/com/example/acae30/ui/pedidos/ProductoAgregarViewModel.kt) según si es Gravado (`G`), Exento (`E`) o No Sujeto (`NS`).
- `[x]` **Tarea 2.3:** Guardar en `tipoFiscal` de `PedidoDetalleEntity` en [Producto_agregar.kt](file:///C:/DESARROLLO/AppVentasSismantec/app/src/main/java/com/example/acae30/ui/pedidos/Producto_agregar.kt) el tipo fiscal exacto de [InventarioEntity.kt](file:///C:/DESARROLLO/AppVentasSismantec/app/src/main/java/com/example/acae30/data/local/entity/InventarioEntity.kt) (o `"E"` si el cliente es Exento), registrando `Precio_iva` como el precio exento y `Precio` como `Precio_iva / 1.13` para simulación.

### FASE 3: Caso de Uso y Cálculo de Totales Fiscales
- `[x]` **Tarea 3.1:** Actualizar [CalcularTotalesFiscalesUseCase.kt](file:///C:/DESARROLLO/AppVentasSismantec/app/src/main/java/com/example/acae30/domain/usecase/pedidos/CalcularTotalesFiscalesUseCase.kt) para recibir el listado completo de productos del pedido y calcular por separado `sumasGravadas`, `iva`, `ventasExentas`, `ventasNoSujetas`, `ivaPercepcion` y `totalFinal`.
- `[x]` **Tarea 3.2:** Actualizar `recalcularTotales` en [DetallePedidoViewModel.kt](file:///C:/DESARROLLO/AppVentasSismantec/app/src/main/java/com/example/acae30/ui/pedidos/DetallePedidoViewModel.kt) para invocar la nueva versión del caso de uso de totales fiscales y guardar los 6 rubros en la cabecera.

### FASE 4: Interfaz de Usuario en Detalle del Pedido
- `[x]` **Tarea 4.1:** Modificar [detalle_pedido.xml](file:///C:/DESARROLLO/AppVentasSismantec/app/src/main/res/layout/detalle_pedido.xml) para agregar el campo de **Precio Unitario** (`@+id/txtprecioU`).
- `[x]` **Tarea 4.2:** Modificar [PedidoDetalleAdapter.kt](file:///C:/DESARROLLO/AppVentasSismantec/app/src/main/java/com/example/acae30/listas/PedidoDetalleAdapter.kt) para vincular y formatear el Precio Unitario (`Precio_venta`).
- `[x]` **Tarea 4.3:** Modificar [activity_detallepedido.xml](file:///C:/DESARROLLO/AppVentasSismantec/app/src/main/res/layout/activity_detallepedido.xml) para agregar los bloques de **EXENTA** (`txtExenta`) y **NO SUJETA** (`txtNoSujeta`) in la tabla inferior de totales.
- `[x]` **Tarea 4.4:** Modificar [Detallepedido.kt](file:///C:/DESARROLLO/AppVentasSismantec/app/src/main/java/com/example/acae30/ui/pedidos/Detallepedido.kt) para mostrar los montos desglosados en `txtExenta` y `txtNoSujeta`.

### FASE 5: Compilación y Verificación
- `[x]` **Tarea 5.1:** Ejecutar análisis estático y compilación de Gradle (`:app:assembleDebug`).
- `[x]` **Tarea 5.2:** Realizar pruebas manuales del flujo completo (Cliente Exento, Producto No Sujeto, Producto Exento y Producto Gravado).
