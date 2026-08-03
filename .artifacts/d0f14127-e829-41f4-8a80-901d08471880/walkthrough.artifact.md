# Resumen de Corrección: Crash en Redirección Post-Envío

Se ha solucionado el cierre inesperado de la aplicación que ocurría al finalizar el envío de un pedido en el modo de Venta Local.

## Cambios Realizados

### [Detallepedido.kt](file:///C:/DESARROLLO/AppVentasSismantec/app/src/main/java/com/example/acae30/ui/pedidos/Detallepedido.kt)
- **Problema**: El método `pedidoEnviado()` intentaba consultar obligatoriamente una visita usando el ID. En ventas locales (`idvisita = 0`), esta consulta devolvía `null`, y el uso del operador `!!` provocaba el `NullPointerException`.
- **Solución**: Se implementó una lógica de navegación condicional y segura:
    - Si el pedido no tiene visita (Modo Local), la aplicación redirige directamente a la lista de pedidos.
    - Si existe una visita y está abierta, redirige a `Visita.kt`.
- **Estilo**: Se mantuvo el código anterior comentado para permitir la comparación técnica y se añadieron comentarios explicativos en español.

## Verificación
- **Compilación**: El proyecto compila correctamente (`Build successful`).
- **Navegación**: El flujo de salida tras enviar un pedido ahora es estable para ambos modos de venta (Local y Externo).
