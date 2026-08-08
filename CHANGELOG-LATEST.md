### Added

- Mortars can now be heated from below when Create isn't loaded, unlocking heated mixing recipes.
    - Heat sources are defined by the `#manual_labour:heat_sources` block tag.
- Added a config option to fill and drain the Mortar's fluid instantly, like a vanilla Cauldron.
- Added a config option to disable Manual Labour Ponder scenes.
- Mortar grinding and mixing recipes can now produce fluids through an optional `fluid_results` array.
    - `results` is now optional on both, so it's possible for a recipe to output only fluid.

### Changed

- Rewrote Millstone code.
- The Mortar now keeps separate input and output fluid tanks, like Create's Basin.

### Fixed

- Fluid results are no longer voided when the Mortar's tank was full or already held another fluid.