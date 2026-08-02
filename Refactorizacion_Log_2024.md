# Bitácora de Refactorización - Proyecto Acae30
**Fecha:** Actualizado según cambios realizados hoy

---

## 1. Módulo de Servidores (CRUD Completo)
Se migró la gestión de servidores de un controlador imperativo a una arquitectura reactiva.

- **Repositorio:** Se creó `ServidoresRepository.kt` para centralizar Room y Retrofit.
- **ViewModel:** Se implementó `ServidoresViewModel.kt` gestionando estados con `StateFlow`.
- **UI:** 
    - `NuevoServidor.kt` y `MenuServidores.kt` ahora observan flujos de datos.
    - Se eliminó el uso de `ConexionController` en favor de la nueva arquitectura.
- **Correcciones:** Se añadió lógica de "reset" en los flujos para permitir pruebas de conexión consecutivas sin bloqueos.

## 2. Módulo de Pedidos (Migración MVVM y Clean Architecture)
Se transformó la pantalla principal de pedidos para soportar procesos en segundo plano y actualizaciones en tiempo real.

### Capa de Datos (Data)
- **PedidosDao.kt:** Añadidos métodos para:
    - Obtención de lista observable (`Flow`).
    - Eliminación en cascada manual (primero detalles, luego encabezados) para evitar errores de `FOREIGN KEY`.
- **PedidosRepository.kt:** Centralización de lógica para consulta de DTE en servidor y limpieza de base de datos.

### Capa de Dominio (Domain)
- **SincronizarPedidosUseCase.kt:** Se encapsuló la lógica de sincronización. Ahora emite estados de progreso (`Iniciando`, `Procesando`, `Exito`, `Error`), separando la lógica de negocio de la interfaz.

### Capa de Presentación (Presentation)
- **PedidosViewModel.kt:** Coordina la sincronización y expone la lista de pedidos de Room.
- **PedidosAdapter.kt:** Refactorizado a `ListAdapter` con `DiffUtil`. Ahora las actualizaciones son parciales, animadas y mucho más eficientes.
- **Pedido.kt (Activity):** 
    - Se redujo drásticamente el código imperativo.
    - Se implementó `setupRecyclerView()` para inicialización única.
    - Se añadió `observarViewModel()` para reaccionar a cambios en la BD y progreso de sincronización.
    - Se implementó la **Sincronización Silenciosa** (segundo plano) para `tipoVentaLocal`.

## 3. Correcciones de Errores Críticos (Bug Fixes)
- **FOREIGN KEY Exception:** Corregido el orden de eliminación en `PedidosRepository` (Detalles -> Pedidos).
- **Actualización de Preferencias:** Se movió la lectura de `eliminarPedidosAutomaticos` a `onStart` para asegurar que los cambios realizados en Configuración se apliquen de inmediato al volver a la pantalla de Pedidos.
- **AlertDialogo:** Se añadió el método `isShowing()` para evitar crashes al intentar cerrar diálogos ya destruidos.

---

## Estado de Archivos
- **Nuevos Archivos:** `SincronizarPedidosUseCase.kt`, `PedidosViewModel.kt`, `PedidosViewModelFactory.kt`.
- **Archivos Deprecados (Mantenidos comentados):** `ConexionController.kt`, funciones antiguas en `Pedido.kt`.

**Próximos Pasos Sugeridos:**
1. Migrar la pantalla de `DetallePedido.kt` al mismo patrón.
2. Migrar el módulo de `Clientes.kt`.
3. Implementar Inyección de Dependencias (Hilt) para simplificar las Factories.
