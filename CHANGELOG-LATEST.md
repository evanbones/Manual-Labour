### Added

- Mortars can now be heated from below when Create isn't loaded, unlocking heated mixing recipes.
    - Heat sources are defined by the `#manual_labour:heat_sources` block tag.
- Added a config option to fill and drain the Mortar's fluid instantly, like a vanilla Cauldron.
- Added a config option to disable Manual Labour Ponder scenes.
- Mortar grinding and mixing recipes can now produce fluids through an optional `fluid_results` array.
    - `results` is now optional on both, so it's possible for a recipe to output only fluid.
- Added the `manual_labour:manual_assembly` recipe type: a multistep Workstone process that needs the
  item hit a set number of times before it finishes. Works with or without Create.
- Mortar mixing recipes now take an optional `heat_requirement` of `none` or `heated`.
- Individual Create recipes can now be excluded from the Mortar and Workstone via a datapack file under
  `data/<namespace>/manual_labour/recipe_exclusions/`.
- Hammers, the Pestle, and the Ladle are now enchantable.
    - Hammers can take Unbreaking, Mending, and mining enchantments (Efficiency, Fortune/Silk Touch).
    - The Pestle and Ladle can take Unbreaking and Mending.
- Added Hammers, Pestles, and Ladles to `#c:tools`.
- Added Hammers to `#c:tools/hammer`.

### Changed

- Rewrote Millstone code.
- The Mortar now keeps separate input and output fluid tanks, like Create's Basin.
- The Manual Assembly JEI category no longer requires Create, and lists both Manual Labour assemblies
  and Create Sequenced Assemblies that can be finished by hand.
    - Its config toggle is no longer greyed out when Create is missing.
- Mortar Mixing in JEI now shows whether a recipe needs heat, along with the blocks that can supply it.
- JEI arrows and badges now have textures when Create isn't installed.
- Only pressing metal ingots into plates on the Workstone has a chance to fail (75%); every other Create
  Pressing recipe (e.g. dirt paths, cardboard) now always succeeds.

### Fixed

- Fluid results are no longer voided when the Mortar's tank was full or already held another fluid.
- A Blaze Burner set to Seething now counts as a heated source.
- Flint Hammers are now repaired with Flint instead of stone tool materials.
- Pestles can now be repaired with `#minecraft:stone_tool_materials`, and Ladles with
  `#minecraft:planks`.

### Removed

- Removed the `workstone_pressing_yield` config option (plate pressing is now a separate Workstone recipe).