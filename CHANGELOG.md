# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [2.1.0] - 2026-08-07

### Added

- Mortars can now be heated from below when Create isn't loaded, unlocking heated mixing recipes.
  - Heat sources are defined by the `#manual_labour:heat_sources` block tag.
- Added a config option to fill and drain the Mortar's fluid instantly, like a vanilla Cauldron.
- Added a config option to disable Manual Labour Ponder scenes.

### Changed

- Rewrote Millstone code.

## [2.0.0] - 2026-08-07

### Changed

- Create is now an optional dependency!
- Reworked the Workstone JEI category.
- Adjusted handheld pestle model.

### Fixed

- Fixed Workstone discarding excess manual sequenced assembly items.

## [1.1.0] - 2026-08-07

### Added

- Workstones now support dispensers.
- Hammers can now be displayed on Workstones (shift+right click).
- Added more action bar feedback for invalid Workstone recipes.
- Added hammering sounds for hammered blocks.

### Changed

- Workstone plate pressing now only succeeds 75% of the time (configurable).
    - Doesn't apply to sequenced assembly recipes.
- Workstone Comparator output scales based on the stored stack count ratio.

## [1.0.0] - 2026-08-06

- Initial release.