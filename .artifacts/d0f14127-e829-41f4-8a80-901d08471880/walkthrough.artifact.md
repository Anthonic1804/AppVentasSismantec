# Refactorización MVVM: Gestión de Productos en Pedidos

Se ha migrado la lógica de la pantalla `Producto_agregar.kt` hacia una arquitectura moderna, desacoplando las reglas de negocio de la interfaz de usuario.

## Resumen de Cambios

### 1. Capa de Dominio (Reglas de Negocio)
Se crearon Casos de Uso específicos para manejar las complejas reglas de precios y bonificaciones:
- **`CalcularPrecioFinalUseCase`**: Implementa la jerarquía donde el **Precio Personalizado** del cliente (tabla `cliente_precios`) tiene prioridad sobre cualquier otro precio.
- **`CalcularBonificacionesUseCase`**: Calcula automáticamente la cantidad de unidades gratis según la configuración del cliente o del producto.
- **`GestionarDetallePedidoUseCase`**: Orquesta la inserción y actualización en Room, asegurando que el total del pedido se recalcule automáticamente.

### 2. Capa de Presentación (MVVM)
- **`ProductoAgregarViewModel`**: Mantiene el estado reactivo de la pantalla. Al cambiar la cantidad o la unidad, dispara los Casos de Uso y expone el `Total` y el `Bonificado` de forma automática.
- **`ProductoAgregarViewModelFactory`**: Gestiona la inyección de dependencias (repositorios y casos de uso).

### 3. Capa de Interfaz (UI)
- **`Producto_agregar.kt`**:
    - Migrada a **View Binding**.
    - Ahora es una "Vista Pasiva": observa los cambios en el ViewModel y los muestra.
    - Se dejó el código anterior comentado para referencia.

### 4. Capa de Datos (Room & Repositories)
- **`InventarioRepository`**: Actualizado para obtener escalas de precios, unidades de medida y lotes directamente desde Room.
- **`ClientesRepository`**: Expone métodos para consultar precios y bonificaciones personalizadas.

## Verificación
- Compilación exitosa (`Build successful`).
- La lógica de "Precio Personalizado manda" está ahora centralizada en el dominio, facilitando futuras modificaciones.
