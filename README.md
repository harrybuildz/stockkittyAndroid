# StockKitty for Android 🐱📈

A stock watchlist app built with modern Android tooling: Jetpack Compose, multi-module
Clean Architecture, offline-first data, and full unit test coverage of the data and
presentation layers.

## Features

- **Watchlist** — track your stocks with live prices, pull-to-refresh, and cached
  quotes that keep working offline
- **Symbol search** — debounced-as-you-type lookup against Finnhub with one-tap add,
  and rows that update live when a symbol is already watched
- **Quote detail** — current price, day change, OHLC stats, company profile and logo,
  and a watchlist toggle

## Architecture

The app follows the [official architecture guidance](https://developer.android.com/topic/architecture)
(unidirectional data flow, reactive streams, layered modules), modeled after
[Now in Android](https://github.com/android/nowinandroid):

```
            ┌───────────────────────────────┐
            │              :app             │  navigation + DI graph
            └──────┬────────┬───────┬───────┘
                   │        │       │
       ┌───────────▼──┐ ┌───▼────┐ ┌▼───────────┐
       │ :feature:    │ │:feature│ │ :feature:  │   Compose screens +
       │  watchlist   │ │ :search│ │   quote    │   MVVM ViewModels
       └───────┬──────┘ └───┬────┘ └─────┬──────┘
               │            │            │
            ┌──▼────────────▼────────────▼──┐
            │          :core:data           │   offline-first repository
            └──────┬─────────────────┬──────┘
         ┌─────────▼──────┐  ┌───────▼─────────┐
         │ :core:network  │  │ :core:database  │   Retrofit + Finnhub │ Room
         └────────────────┘  └─────────────────┘
```

Supporting modules: `:core:model` (domain models), `:core:common` (Result wrapper,
dispatcher qualifiers), `:core:designsystem` (theme + shared components),
`:core:testing` (fakes and test rules).

### Key decisions

- **Offline-first**: Room is the single source of truth. The UI always renders from
  the database; the network only refreshes the cache. A failed sync degrades to a
  snackbar over cached prices instead of an error screen.
- **Unidirectional data flow**: ViewModels expose `StateFlow<UiState>` built with
  `stateIn(WhileSubscribed)`; Compose screens are stateless and testable, receiving
  state and emitting events.
- **Partial sync tolerance**: refreshing the watchlist fetches quotes concurrently
  and keeps whatever succeeded — one delisted ticker doesn't break the sync.
- **Testability by construction**: every boundary (network, DAOs, repository,
  dispatchers) is an interface or injected value with an in-memory fake in
  `:core:testing`.

## Tech stack

| | |
|---|---|
| Language | Kotlin 2.1 |
| UI | Jetpack Compose, Material 3 |
| Architecture | Multi-module, MVVM, unidirectional data flow |
| DI | Hilt |
| Async | Coroutines + Flow |
| Networking | Retrofit, OkHttp, kotlinx.serialization |
| Persistence | Room |
| Images | Coil |
| Testing | JUnit4, kotlinx-coroutines-test, Turbine |
| CI | GitHub Actions |

## Getting started

1. Get a free API key from [finnhub.io](https://finnhub.io)
2. Add it to `local.properties`:
   ```properties
   STOCKKITTY_FINNHUB_API_KEY=your_key_here
   ```
3. Build and run:
   ```bash
   ./gradlew assembleDebug
   ```

Run the unit tests with:

```bash
./gradlew testDebugUnitTest
```

## Roadmap

- [ ] Price history charts (requires a candle-data provider; Finnhub's free tier
      doesn't include `/stock/candle`)
- [ ] Periodic background refresh with WorkManager
- [ ] Convention plugins (`build-logic`) to deduplicate module build scripts
- [ ] Baseline Profiles and macrobenchmarks
