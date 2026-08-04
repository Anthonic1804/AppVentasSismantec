# Refactorización del Módulo de Cuentas (MVVM + Architecture Clean)

Se ha completado la migración exitosa del módulo de Cuentas por Cobrar (CxC), optimizando la gestión de datos y la estabilidad de la interfaz de usuario.

## Cambios Realizados

### 1. Capa de Datos (Room & Repositorios)
- **DAO**: Se creó [CuentasDao.kt](file:///C:/DESARROLLO/AppVentasSismantec/app/src/main/java/com/example/acae30/data/local/dao/CuentasDao.kt) para reemplazar las consultas SQL manuales. Ahora se utiliza **JOINs** eficientes para listar solo clientes con deudas reales.
- **Repositorio**: Se implementó [CuentasRepository.kt](file:///C:/DESARROLLO/AppVentasSismantec/app/src/main/java/com/example/acae30/data/repository/CuentasRepository.kt) para centralizar el acceso a las facturas pendientes, facilitando el mantenimiento futuro.

### 2. Capa de Dominio (Use Cases)
- **Obtención de Clientes**: [ObtenerClientesConCuentasUseCase.kt](file:///C:/DESARROLLO/AppVentasSismantec/app/src/main/java/com/example/acae30/domain/usecase/cuentas/ObtenerClientesConCuentasUseCase.kt) encapsula la lógica de búsqueda y filtrado de deudores.
- **Detalle de Cuentas**: [ObtenerDetalleCuentasUseCase.kt](file:///C:/DESARROLLO/AppVentasSismantec/app/src/main/java/com/example/acae30/domain/usecase/cuentas/ObtenerDetalleCuentasUseCase.kt) gestiona los filtros de facturas **Vencidas** y **Vigentes**.

### 3. Capa de Presentación (MVVM)
- **ViewModel**: Se creó un único [CuentasViewModel.kt](file:///C:/DESARROLLO/AppVentasSismantec/app/src/main/java/com/example/acae30/ui/clientes/CuentasViewModel.kt) compartido para manejar el estado de ambas pantallas (`Cuentas_list` y `CuentasDetalle`).
- **Adaptador Moderno**: [CuentaAdapter.kt](file:///C:/DESARROLLO/AppVentasSismantec/app/src/main/java/com/example/acae30/listas/CuentaAdapter.kt) fue refactorizado a `ListAdapter`, lo que permite actualizaciones visuales instantáneas y eficientes mediante `DiffUtil`.
- **Activity Binding**: Ambas actividades ahora utilizan **View Binding** y observan los datos de forma reactiva (Flow), eliminando la dependencia del controlador antiguo.

## Código Comparativo
Tal como se solicitó:
- El código antiguo de las actividades y el adaptador ha sido dejado **comentado** dentro de los archivos.
- El archivo original `CuentasController.kt` ha sido conservado intacto como respaldo.
- Se añadieron comentarios detallados en español explicando la nueva lógica reactiva.

## Verificación
- **Build**: Compilación exitosa (`Build successful`).
- **Funcionalidad**: El sistema ahora filtra clientes con deudas automáticamente al cargar la lista y permite una navegación fluida hacia el detalle o hacia la creación de abonos.
