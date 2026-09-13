# Corrección de Sincronización de Pagaré con el Servidor

Se detectó que el endpoint y la estructura de la petición utilizados para sincronizar la firma del pagaré no coinciden con los requeridos por el servidor. Se ajustará la interfaz de Retrofit y el repositorio para utilizar el endpoint correcto y enviar el ID del cliente en el cuerpo de la petición.

## Cambios Propuestos

### Capa de Red (Data Remote)

#### [MODIFY] [ClientesApi.kt](file:///C:/DESARROLLO/AppVentasSismantec/app/src/main/java/com/example/acae30/data/remote/api/clientes/ClientesApi.kt)
- Cambiar el endpoint de `clientes/actualizar_pagare/{idCliente}` a `clientes/actualizarPagare`.
- Modificar el método para que acepte un objeto `@Body` de tipo `ActualizarPagareFirmadoCliente`.

### Capa de Datos (Repository)

#### [MODIFY] [ClientesRepository.kt](file:///C:/DESARROLLO/AppVentasSismantec/app/src/main/java/com/example/acae30/data/repository/ClientesRepository.kt)
- Ajustar la llamada al repositorio para crear el objeto `ActualizarPagareFirmadoCliente(idCliente)` y enviarlo a la API.

## Plan de Verificación

### Verificación Técnica
- Ejecutar `gradlew app:assembleDebug` para validar la sintaxis.

### Verificación Manual
1. Realizar el flujo de firma del pagaré.
2. Verificar si el Toast de error desaparece y se muestra el mensaje de "PROCESO COMPLETO".
3. Confirmar en el servidor que el campo del pagaré se actualizó correctamente.
