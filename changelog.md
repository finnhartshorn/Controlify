# Controlify {version}

This version has the following targets:
{targets}

[![](https://short.isxander.dev/bisect-img)](https://short.isxander.dev/bisect)

## 1.21.2/3 Support!

This update includes support for the latest version of Minecraft, including the new bundle item!

### Bundles

Controlify imitates the controller support for bundles offered by the Bedrock Edition of Minecraft.

By default, use the right stick whilst hovering over a bundle to navigate through the grid of items contained in the
bundle. Use X (or Square for PS users) to remove your selected item from the bundle, and use A (or X for PS users) to
add items to the bundle.

### Sodium

Support for Sodium 0.6 has been added for specifically the 1.21.2/3 target, **including NeoForge!**

Reese's Sodium Options is currently not supported by Sodium 0.6 targets of Controlify, until then,
the unmodded Sodium options is working fine. Reese's support will come soon.

## Gyro improvements

Thanks to the [PR by lukacsi](https://github.com/isXander/Controlify/pull/423), improvements to the gyro options
allow for gyro ratcheting as well as using the gyro button to toggle on/off.

## Changes

- Update to 1.21.3
- Add toggle to disable the enhanced Steam Deck driver in global settings.
- Fix test rumble option not working when not accessed from an in-game context ([#430](https://github.com/isXander/Controlify/pull/430))
- Fix sprint not working and look input getting stuck after tabbing out
- Fix crash on pause menu when mods remove some buttons
