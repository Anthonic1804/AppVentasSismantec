# Refactorización del Módulo de Cuentas (CxC) a MVVM y Arquitectura Limpia

Se migrarán las pantallas de listado de clientes con cuentas (`Cuentas_list`) y el detalle de cuentas (`CuentasDetalle`) hacia una arquitectura profesional utilizando Room, Repositorios, Casos de Uso y ViewModels.

## Proceso de Refactorización

1.  **Capa de Datos (Data Layer)**:
    *   Crear `CuentasDao` para manejar las consultas a la tabla `cuentas` y `clientes`.
    *   Crear `CuentasRepository` para abstraer la fuente de datos.
    *   Registrar `CuentasDao` en `AppDatabase`.

2.  **Capa de Dominio (Domain Layer)**:
    *   Crear `ObtenerClientesConCuentasUseCase`: Encapsula la lógica de obtener clientes que tienen facturas pendientes, permitiendo búsquedas por nombre.
    *   Crear `ObtenerDetalleCuentasUseCase`: Encapsula la lógica de obtener el detalle de facturas de un cliente con los filtros de "Vencidas", "Vigentes" o "Todas".

3.  **Capa de Presentación (Presentation Layer)**:
    *   Crear `CuentasViewModel`: Gestionará el estado de ambas pantallas, permitiendo una transición fluida y manteniendo la lógica de negocio fuera de las actividades.
    *   Refactorizar `Cuentas_list.kt` y `CuentasDetalle.kt` para usar View Binding y observar el estado del ViewModel.
    *   Refactorizar `CuentaAdapter.kt` a `ListAdapter` con `DiffUtil` para un rendimiento óptimo.

## Archivos a crear (Confirmación necesaria)

### Capa de Datos
1.  **[NEW] [CuentasDao.kt](file:///C:/DESARROLLO/AppVentasSismantec/app/src/main/java/com/example/acae30/data/local/dao/CuentasDao.kt)**
2.  **[NEW] [CuentasRepository.kt](file:///C:/DESARROLLO/AppVentasSismantec/app/src/main/java/com/example/acae30/data/repository/CuentasRepository.kt)**

### Capa de Dominio
3.  **[NEW] [ObtenerClientesConCuentasUseCase.kt](file:///C:/DESARROLLO/AppVentasSismantec/app/src/main/java/com/example/acae30/domain/usecase/cuentas/ObtenerClientesConCuentasUseCase.kt)**
4.  **[NEW] [ObtenerDetalleCuentasUseCase.kt](file:///C:/DESARROLLO/AppVentasSismantec/app/src/main/java/com/example/acae30/domain/usecase/cuentas/ObtenerDetalleCuentasUseCase.kt)**

### Capa de Presentación
5.  **[NEW] [CuentasViewModel.kt](file:///C:/DESARROLLO/AppVentasSismantec/app/src/main/java/com/example/acae30/ui/clientes/CuentasViewModel.kt)**
6.  **[NEW] [CuentasViewModelFactory.kt](file:///C:/DESARROLLO/AppVentasSismantec/app/src/main/java/com/example/acae30/ui/factories/CuentasViewModelFactory.kt)**

## Cambios en archivos existentes

- **[MODIFY] [AppDatabase.kt](file:///C:/DESARROLLO/AppVentasSismantec/app/src/main/java/com/example/acae30/data/local/appDatabase/AppDataBase.kt)**: Registrar el nuevo DAO.
- **[MODIFY] [Cuentas_list.kt](file:///C:/DESARROLLO/AppVentasSismantec/app/src/main/java/com/example/acae30/ui/clientes/Cuentas_list.kt)**: Migración a MVVM y observación de datos.
- **[MODIFY] [CuentasDetalle.kt](file:///C:/DESARROLLO/AppVentasSismantec/app/src/main/java/com/example/acae30/ui/clientes/CuentasDetalle.kt)**: Migración a MVVM y observación de datos.
- **[MODIFY] [CuentaAdapter.kt](file:///C:/DESARROLLO/AppVentasSismantec/app/src/main/java/com/example/acae30/listas/CuentaAdapter.kt)**: Actualización a `ListAdapter`.

---
> [!NOTE]
> Se mantendrá el código original comentado en las Actividades y el adaptador. Los archivos `CuentasController.kt` y `Cuentas_list.kt` originales (si se renombrara) se conservarán como respaldo.

## Plan de Verificación

### Verificación Técnica
- Ejecutar compilación exitosa con Gradle.
- Verificar que las consultas Room devuelvan los mismos resultados que el SQL manual actual.

### Verificación Manual
1. Abrir listado de Cuentas.
2. Buscar un cliente y verificar que aparezcan solo los que tienen deudas.
3. Entrar al detalle y probar los filtros (Todas, Vencidas, Vigentes).
4. Verificar que el total de la deuda se calcule correctamente en el detalle.
5. Probar el flujo de "Nuevo Abono" para asegurar que la redirección sea correcta.
