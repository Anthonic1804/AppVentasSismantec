# Histórico de Cambios y Reglas de Negocio - AppVentasSismantec

Este documento sirve como registro centralizado de la refactorización a MVVM, Clean Architecture y las reglas de negocio implementadas.

---

## 📋 Estado del Proyecto
- **Arquitectura:** MVVM + Clean Architecture.
- **Persistencia:** Room Database.
- **Red:** Retrofit 2.
- **Estado Global:** En Refactorización de Pedidos.

---

## 🛠️ Registro de Tareas

### Módulo: Clientes y Pagaré
- [x] Refactorización de `Clientes.kt` a MVVM.
- [x] Implementación de flujo directo a `verPagare.kt` (Lectura).
- [x] Validación robusta de términos de crédito para firma obligatoria.
- [x] Sincronización de firma con servidor vía API (JSON Body).

### Módulo: Agregar Productos
- [x] Refactorización de `Producto_agregar.kt` a MVVM.
- [x] Caso de Uso: `CalcularPrecioFinalUseCase` (Prioridad Personalizado sobre Lista).
- [x] Caso de Uso: `CalcularBonificacionesUseCase` (Reglas T, BC, BP, SB).
- [x] Caso de Uso: `ObtenerStockDesglosadoUseCase` (Manejo de Unidades y Fracciones sin decimales).
- [x] Validación de stock contra total en fracciones.
- [x] Soporte para Unidades de Medida Especiales (Sixpack, 2PACK, etc.).

### Módulo: Detalle de Pedido
- [/] Refactorización de `Detallepedido.kt` a MVVM.
- [x] Implementación de "Escudo de Carga" para Spinners (Evitar sobreescritura accidental).
- [x] Corrección de límites de ítems independientes por borrador.
- [x] Gestión de múltiples pedidos borradores en Venta Local.
- [ ] Corrección de integridad de montos y nombres de campos en envío a API. (En proceso)

---

## 📏 Reglas de Negocio Actuales

### 🛒 Venta y Precios
1. **Precio Personalizado:** Tiene prioridad absoluta sobre escalas y viñeta. **Solo aplica si la unidad es UNI**.
2. **Bonificaciones:** 
   - **T (Todos):** Prioridad Cliente, si no hay, usa Producto.
   - **BC (Cliente):** Solo tabla `cliente_precios`.
   - **BP (Producto):** Solo tabla `inventario`.
   - **SB (Sin Bonificación):** Siempre 0.
   - *Nota:* Solo aplica en base **UNI**.
3. **Existencias:**
   - Si `fraccion > 1`: Se muestra desglosado (Enteros UNI y FRA).
   - Si `fraccion <= 1`: Se muestra decimal en UNI, FRA queda en 0.

### 📄 Documentos y Límites
1. **Límite de Ítems:** Se define por tipo de documento (FC, CF, RC, RE) configurado en SharedPreferences.
2. **Validación de Crédito:** Se verifica saldo real en servidor + total pedido antes de enviar (solo si términos = "Credito").

### 📍 Venta Local
- Si `tipoVentaLocal` es true: Se salta `Visita.kt`, `idvisita = 0`, `gps = "0,0"`.
