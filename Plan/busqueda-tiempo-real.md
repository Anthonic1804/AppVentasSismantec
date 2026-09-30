# Búsqueda en tiempo real — Inventario (backend + app Kotlin)

Fecha: 2026-09-30
Endpoint: `GET Inventario/busqueda/inventario` (`ObtenerProductoPorString`)
App: Kotlin `txtBusquedaProducto.addTextChangedListener` + `actualizarListadeInventario(texto)`

## 1. Estado actual verificado

Backend (`InventarioController.cs`):
- `Where(x.Codigo.Trim() == busqueda.Trim() || x.Descripcion.Contains(busqueda))` + `OrderBy(Id)` + `Take(30)`.
- Problemas: `Trim()` sobre columna (no sargable), `Contains` = `LIKE '%q%'` (full scan por cada tecla),
  sin longitud mínima, ruta `{busqueda}` frágil con espacios/`/`, sin `CancellationToken`,
  proyección con `Trim()` sin null-check, `OrderBy(Id)` sin relevancia.
- Lo bueno: `AsNoTracking()` + proyección a `InventarioTiempoRealDTO` (mantener).

App (Kotlin):
- `job?.cancel() + lifecycleScope.launch { delay(300) ... }` = debounce manual correcto.
- Falta: `distinctUntilChanged`, longitud mínima, `viewLifecycleOwner.lifecycleScope` en Fragment,
  cancelación de la llamada Retrofit/OkHttp en curso (hoy solo se cancela el delay).

## 2. Recomendación backend (plan aprobado pendiente de implementar)

1. Cambiar a query string: `[HttpGet("busqueda/inventario")]` con `?q=` + `take=20` + `CancellationToken ct`.
2. Normalizar una vez: `q = q?.Trim()`; si `len < 2-3` devolver `Ok([])` sin tocar BD.
3. Query sargable: `Codigo == q` exacto OR `Codigo.StartsWith(q)` OR (si `len>=3`) `Descripcion.Contains(q)`.
   Sin `Trim()` sobre columna. `AsNoTracking()`, ordenar por relevancia, `Take(20)`.
4. Proyección null-safe (como en `Get()`).
5. Opción full-text (requiere DBA, fuera del repo — sin migrations):
   - Activar Full-Text en instancia + `FULLTEXT CATALOG` + `FULLTEXT INDEX inventario(Codigo, Descripcion)` + `START FULL POPULATION`.
   - Usar `EF.Functions.Contains(col, "\"q*\"")` para prefijo en tiempo real (mejor que `FreeText` por tecla).
   - Sanitizar `"`, `*`, `%`; mantener fallback a `LIKE` si FT falla o `q` muy corto.
   - Ranking opcional con `CONTAINSTABLE ... RANK` vía `FromSqlRaw`.
6. Índice mínimo aunque no haya FT: no-clustered en `Codigo` + `Descripcion`.

## 3. Recomendación app Kotlin (aplicar en el proyecto Android)

```kotlin
binding.txtBusquedaProducto.doAfterTextChanged { s ->
    val texto = s?.toString()?.trim().orEmpty()
    job?.cancel()
    if (texto == ultimoQuery) return@doAfterTextChanged // distinct
    job = viewLifecycleOwner.lifecycleScope.launch {
        delay(300)
        ultimoQuery = texto
        if (texto.length < 2) {
            mostrarListaBase() // o lista vacía, sin llamar a la API
        } else {
            actualizarListadeInventario(texto)
        }
    }
}
```

- En `actualizarListadeInventario`: guardar el `Call` de Retrofit y hacer `call?.cancel()` antes de encolar el nuevo.
- Ideal: migrar a `Flow { debounce(300).distinctUntilChanged().filter { it.length >= 2 }.flatMapLatest { api.buscar(it) } }`.
- Debounce 250-300 ms, minChars 2-3. El `actualizarListadeInventario(" ")` con espacio para vacíos se reemplaza por `mostrarListaBase()` explícito.

## 4. Para revisión directa en código (siguiente sesión)

Abrir sesión con el proyecto Kotlin como directorio y revisar:
- Archivo del `TextWatcher` (ver `doAfterTextChanged`, `job`, `ultimoQuery`, scope usado).
- `actualizarListadeInventario()` (construcción de la URL — migrar `{busqueda}` a `?q=`, cancelación del `Call`, manejo de respuesta vieja por token, estados loading/error).
- Retrofit service + OkHttp timeouts.

## 5. Verificación

- `dotnet build` → 0 errores.
- Medir plan de ejecución SQL antes/después (LIKE vs prefijo vs FT).
