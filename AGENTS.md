# AGENTS.md — AppVentasSismantec (ACAE APP)

Single-module Android app (`:app`, package `com.example.acae30`; rootProject name `Acae30` is legacy).
Stack: AGP 8.9.1 / Kotlin 2.1.20 / compileSdk 36 / minSdk 26 / Java 17 / KSP + Room 2.7.0 / Retrofit 2.9.0 / ViewBinding + BuildConfig.

## Commands (Windows: use `.\gradlew.bat`)

- Verify (default): `.\gradlew.bat :app:assembleDebug`
- Release: `.\gradlew.bat :app:assembleRelease` (minify off)
- No CI, no lint/typecheck config. Only `ExampleUnitTest.kt` exists — no real test suite, so build is the verification step.

## Architecture (MVVM + Clean, mid-migration per `historico_cambios.md`)

- New code path: `ui/<feature>/` (Activity + ViewModel) → `domain/usecase/` (`ejecutar()` suspend fns) → `data/repository/` → `data/local/` (Room) / `data/remote/api/` (Retrofit).
- DI is manual via `ui/factories/*ViewModelFactory.kt` (no Hilt; Koin block in `MyApp.kt` is commented out). New UseCase/Repo must be wired in the matching Factory — see `ui/factories/ProductoAgregarViewModelFactory.kt`.
- Legacy dirs still present, do not extend: `controllers/`, `modelos/`, `listas/`, `Library/`, `Utilidades/`, `services/`.
- Server URL comes from SharedPreferences `CONFIG_SERVIDOR`; Retrofit via `data/remote/api/retrofit/RetrofitCliente.obtenerApi(servidorUrl, context)`.
- Room `data/local/appDatabase/AppDataBase.kt`: v4, `fallbackToDestructiveMigration()`, WAL enabled, singleton `getInstance(context)`; KSP `room.incremental=true`, `exportSchema=false`. Bump `version` on schema changes; destructive migration is accepted here.
- Timber only plants when `modoDesarrollo=true` in `CONFIG_SERVIDOR` (`MyApp.kt`).
- Local printer dep: `app/libs/escposprinter-release.aar` — do not remove.

## Business rules (from `historico_cambios.md` + UseCases — preserve)

- Precio personalizado wins over lista/escala, UNI only (`domain/usecase/inventario/CalcularPrecioFinalUseCase.kt`); price $0.00 blocks add.
- Bonificaciones T/BC/BP/SB computed on UNI base only (`CalcularBonificacionesUseCase.kt`).
- `fraccion > 1`: split UNI/FRA integers; else decimal UNI, FRA=0 (`ObtenerStockDesglosadoUseCase.kt`).
- Mayorista (`Mayorista='S'`) skips escala validation (unit match uses TRIM + price epsilon).
- Stock check must include duplicate lines of the same product + bonificadas.
- `Detallepedido`: "escudo de carga" guard on Spinners to avoid overwrite during load.
- Venta local (`tipoVentaLocal=true`): skip `Visita`, `idvisita=0`, `gps="0,0"`.
- Crédito: validate server balance + order total before send; item limit per doc type (FC/CF/RC/RE) from prefs.

## Env / gotchas

- Requires Android SDK (`local.properties` gitignored) + device/emulator.
- `usesCleartextTraffic=true`, Maps API key, BT/USB printer permissions (`AndroidManifest.xml`).
