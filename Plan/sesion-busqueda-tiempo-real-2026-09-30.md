# Sesión búsqueda en tiempo real — Inventario (2026-09-30)

Backend: `GET Inventario/busqueda/inventario?q=&take=` (`ObtenerProductoPorString`).
App: `ui/inventario/InventarioTiempoReal.kt` + `controllers/InventarioTiempoRealController.kt`
+ `data/remote/api/inventario/InventarioApi.kt`.
Planes base: `Plan/busqueda-tiempo-real.md`, `Plan/app-busqueda-tiempo-real.md`.

## 1. Cambio de contrato (path → query)

Problema: la app usaba `@GET("inventario/busqueda/inventario/{busqueda}")` + `@Path`,
el backend/Swagger expone `?q=olla&take=20`. Con path había 404 y fallos con
espacios, `/`, `%`, y no se enviaba `take`.

Cambio en `InventarioApi.kt:78-83`:
```kotlin
@GET("inventario/busqueda/inventario")
suspend fun obtenerProductoPorString(
    @Query("q") busqueda: String,
    @Query("take") take: Int = 20
): List<InventarioTiempoRealDto>
```
Retrofit codifica `q` automáticamente. Se agregó `import retrofit2.http.Query`.

## 2. Controller: umbral + paginación inicial

`controllers/InventarioTiempoRealController.kt`:
- `obtenerInventarioPorDescripcion(ctx, busqueda, take = 20)`:
  `trim()` + `if (query.length < 3) return emptyList()` sin crear Retrofit ni tocar red.
  `CancellationException` se relanza (antes se tragaba y rompía `job.cancel()`).
- Nueva `obtenerPaginaInicial(ctx, offset = 0, limit = 20)`:
  usa el endpoint existente `GET inventario/{offset}/{limit}` y mapea
  `InventarioEntity → InventarioTiempoRealDto`
  (`id, codigo, descripcion, unidad_medida, nombre_fraccion, existencia,
  existencia_u → exitenciaFraccion, precio_u_iva → precioUiva`).
  `CancellationException` se relanza; otro error → `emptyList()` + log.

Regla acordada: caja vacía o < 3 letras → mostrar caché inicial (primeros 20),
cero peticiones de filtrado.

## 3. Activity: debounce, distinct, anti-carrera, caché

`ui/inventario/InventarioTiempoReal.kt` (es `AppCompatActivity`, por eso se usa
`lifecycleScope`; `viewLifecycleOwner` del plan no aplica):
- Campos: `job` (debounce), `searchJob` (cancela búsqueda anterior en vuelo),
  `ultimoQuery` (distinct), `searchVersion` (token anti-respuestas-viejas),
  `cacheInicial`, `cargandoCache`, `MIN_BUSQUEDA = 3`.
- `onCreate`: `LayoutManager` una sola vez + `cargarCacheInicial()` (antes se
  recreaba el LayoutManager por tecla y se pedía `actualizarListadeInventario(" ")`).
- `afterTextChanged`: `distinct` → `job?.cancel()` →
  vacío → `mostrarCache()`; `< 3` → `mostrarCache()` + `helperText`
  "Escribe al menos 3 letras" (se usa `lyBusquedaProducto.helperText`, el layout
  no tiene TextView de hint); `>= 3` → `delay(300)` + `actualizar(texto, ++version)`.
- `actualizarListadeInventario(busqueda, version)`: cancela `searchJob` previo,
  ignora `CancellationException`, descarta respuesta si `version != searchVersion`,
  en otro error mantiene la lista anterior + `Toast "Sin conexión..."` (nunca vacía
  por fallo de red).
- `cargarCacheInicial()`: una sola vez, con `alerta.Cargando()` solo en primera
  carga; si el usuario ya escribió `>= 3` no pinta por encima.
- `mostrarCache()`: invalida respuestas en vuelo, cancela `searchJob`, pinta
  `cacheInicial`, relanza carga si está vacía.
- `mostrarLista()`: si vacía → `adapter = null` (sin resultados); sin recrear LayoutManager.

## 4. Verificación

- `./gradlew :app:compileDebugKotlin -x lint --offline` → `BUILD SUCCESSFUL`
  (solo warnings preexistentes).
- Manual: abrir pantalla → 1 llamada `inventario/0/20`; escribir `olla` →
  `busqueda/inventario?q=olla&take=20`; 1-2 letras o vacío → 0 llamadas, vuelve
  la caché; tecleo rápido → solo pinta la última; sin red → conserva lista + Toast.

## 5. Pendiente / deuda conocida

- Migrar `listas/InventarioTiempoRealAdapter.kt` (`RecyclerView.Adapter` con lista
  inmutable) a `ListAdapter + DiffUtil` para evitar parpadeo (hoy se recrea el
  adapter por búsqueda; funcional pero no ideal).
- Timeouts OkHttp (`RetrofitCliente.kt:58-65` sin timeouts explícitos): no se tocó
  el cliente global para no afectar la sync masiva; si se quiere, usar cliente
  separado para búsqueda (connect 10s / read 15s).
- Versión ideal del plan (Flow `debounce().distinctUntilChanged().flatMapLatest()`,
  `cacheInicial` en ViewModel/`StateFlow` para rotación) queda como evolución;
  el `job + searchJob + searchVersion` actual es su equivalente manual.
