# App Kotlin — Búsqueda en tiempo real de inventario

Fecha: 2026-09-30
Backend: `GET Inventario/busqueda/inventario?q=&take=` (`ObtenerProductoPorString`)
Regla acordada: caja vacía o < 3 letras → mostrar caché inicial (primeros 20), sin pedir filtrado.

## 1. Piezas necesarias

- `EditText` + `RecyclerView` (con `ListAdapter` + `DiffUtil`) + `TextView` hint
  ("Escribe al menos 3 letras") + indicador de carga.
- Retrofit:
  - `suspend fun buscar(@Query("q") q: String, @Query("take") take: Int = 20): List<InventarioTiempoReal>`
  - Página inicial: `GET Inventario/0/20` (o mismo endpoint con `q` vacío, según backend final).
- Estado en Fragment/ViewModel: `job: Job?`, `ultimoQuery = ""`, `cacheInicial: List<...>`.

## 2. Flujo en `doAfterTextChanged`

```kotlin
binding.txtBusquedaProducto.doAfterTextChanged { s ->
    val texto = s?.toString()?.trim().orEmpty()
    if (texto == ultimoQuery) return@doAfterTextChanged // distinct
    job?.cancel() // cancela delay o request en curso (suspend fun la aborta sola)
    job = viewLifecycleOwner.lifecycleScope.launch {
        when {
            texto.isEmpty() -> {
                ultimoQuery = ""
                ocultarHint()
                if (cacheInicial.isEmpty()) {
                    mostrarCarga()
                    cacheInicial = api.paginaInicial() // una sola vez
                    ocultarCarga()
                }
                adapter.submitList(cacheInicial)
            }
            texto.length < 3 -> {
                ultimoQuery = texto
                adapter.submitList(cacheInicial) // cero peticiones
                mostrarHint("Escribe al menos 3 letras")
            }
            else -> {
                delay(300) // debounce
                ultimoQuery = texto
                mostrarCarga()
                try {
                    val res = api.buscar(texto)
                    if (texto == ultimoQuery) { // anti-carrera
                        adapter.submitList(res)
                        ocultarHint()
                    }
                } catch (e: Exception) {
                    // Mantener lista anterior + Snackbar, nunca vaciar por fallo de red
                    if (e !is CancellationException) snackbar("Sin conexión, mostrando últimos resultados")
                } finally {
                    ocultarCarga()
                }
            }
        }
    }
}
```

## 3. Versión ideal (Flow)

`editText.textChanges().debounce(300).distinctUntilChanged().flatMapLatest { q -> api... }`
— `flatMapLatest` cancela sola la petición anterior, sin `job` manual.

## 4. Casos borde (checklist de revisión en código)

- [ ] `viewLifecycleOwner.lifecycleScope` (no `lifecycleScope` en Fragment).
- [ ] `cacheInicial` + `ultimoQuery` en `ViewModel` (`StateFlow`) para sobrevivir rotación.
- [ ] Validar `texto == ultimoQuery` antes de pintar (o `flatMapLatest`).
- [ ] Error de red: mantener lista + Snackbar, no vaciar.
- [ ] Timeouts OkHttp: connect 10s / read 15s.
- [ ] `ListAdapter` + `DiffUtil` para no parpadear la lista.
- [ ] Carga visible solo en rama 3+ letras y primera carga inicial.

## 5. Para la sesión del proyecto Kotlin

Revisar: archivo del `TextWatcher`, `actualizarListadeInventario()` (URL, `Call` vs `suspend`,
cancelación, validación de respuesta vieja), Retrofit service, OkHttp timeouts,
y dónde guardar `cacheInicial` (ViewModel).
