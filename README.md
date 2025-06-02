# spatial-id-kotlin

Spatial ID Library for Kotlin :heart:

- This is a Kotlin port of [the original JavaScript library](https://github.com/ouranos-gex/ouranos-gex-lib-for-JavaScript).
- Some APIs are not available.

## Installation

- Published via [JitPack](https://jitpack.io/#hakobune8/spatial-id-kotlin).
- Add the following to your Gradle `dependencies` block:

```
implementation 'com.github.hakobune8:spatial-id-kotlin:1.0.4'
```

## API List

### `Space` Constructors

- `Space.from(pathOrHash: String, zoom: Int? = null): Space`
- `Space.from(location: LngLatWithAltitude, zoom: Int? = null): Space`

Creates a `Space` data class instance, representing a spatial ID. If `zoom` is omitted, the default value of `25` will be used. The following input formats are supported:

- Path format: e.g. `/15/6/2844/17952`
- Hash format: e.g. `100213200122640`
- Geographic coordinates: e.g. `LngLatWithAltitude(lng = 135.0, lat = 35.0, alt = 10.0)`

### Others

The following APIs are available. For details, refer to the [original documentation](https://github.com/ouranos-gex/ouranos-gex-lib-for-JavaScript?tab=readme-ov-file#%E3%83%A1%E3%82%BD%E3%83%83%E3%83%89).

- `center: LngLatWithAltitude`
- `alt: Double`
- `zoom: Int`
- `zfxy: ZFXYTile`
- `id: String`
- `tilehash: String`
- `zfxyStr: String`
- `up(by: Int = 1): Space`
- `down(by: Int = 1): Space`
- `north(by: Int = 1): Space`
- `south(by: Int = 1): Space`
- `east(by: Int = 1): Space`
- `west(by: Int = 1): Space`
- `move(f: Int = 0, x: Int = 0, y: Int = 0): Space`
- `parent(atZoom: Int? = null)`
- `children(): List<Space>`
- `surroundings(): List<Space>`
- `vertices3d(): List<Triple<Double, Double, Double>>`
