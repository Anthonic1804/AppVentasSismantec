# Walkthrough: Verificación del Tratamiento Contable de IVA Percibido e IVA Retenido

## Verificación Realizada
- Se validó que el cálculo de `totalFinal` en [CalcularTotalesFiscalesUseCase.kt](file:///C:/DESARROLLO/AppVentasSismantec/app/src/main/java/com/example/acae30/domain/usecase/pedidos/CalcularTotalesFiscalesUseCase.kt) aplica correctamente la regla fiscal:
  - **IVA Percibido:** Se **suma** (`+ ivaPerci`), cobrándose de forma anticipada.
  - **IVA Retenido:** Se **resta** (`- ivaReteni`), descontándose del monto a pagar ya que el comprador lo retiene para entregarlo a Hacienda.
- Compilación completada con éxito (`gradle_build("app:assembleDebug")`).
