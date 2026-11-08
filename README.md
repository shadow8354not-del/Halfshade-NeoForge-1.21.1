# Halfshade — An Origins Addon

A fallen warrior, half human and half shadow. An addon for **Origins (NeoForge) by IAFEnvoy** and **Minecraft 1.21.1**. English and Russian localization are included.

## Install

Install the playable `poluten-1.1.0-neoforge-1.21.1.jar` on both client and server alongside:

- Minecraft Java Edition **1.21.1**
- NeoForge **21.1.228 or newer in the 21.1 branch** (built and tested on **21.1.235**)
- [Origins (NeoForge)](https://www.curseforge.com/minecraft/mc-mods/origins-neoforge) **0.3.2 or newer**
- [Jupiter](https://www.curseforge.com/minecraft/mc-mods/jupiter), the port's required dependency

The release accepts Origins 0.3.2 and newer. The tested versions and scope are recorded in [docs/testing.md](docs/testing.md). Future versions are not a compatibility guarantee. Other Origins ports are not supported.

Choose **Halfshade** / **Полутень** in the origin selection screen. Use the **Origins Primary Active** key to toggle shadow form. The internal IDs remain `poluten` and `poluten:poluten`, preserving existing saves.

## Powers and drawbacks

- Speed I at night; 7 hearts of base health.
- Shadow form phases through walls (sneak to phase down), prevents fall damage, and multiplies exhaustion by five.
- In shadow form, sword strikes apply Wither I for 3 seconds, with a shared 6-second cooldown. Attacks from inside blocks, item use, eating, drinking, block interaction and item pickup are blocked.
- A black vignette turns red at 10 hunger points (5 food icons). Shadow form ends at 6 points (3 icons), even inside blocks. Manual exit inside blocks is blocked.
- Leaving blocks in shadow form causes Blindness for 2 seconds. Direct sunlight ignites the shadow.
- Fire and lava damage are doubled. Totems of Undying do not work.
- **Last Breath:** lethal damage forces shadow form with 3.5 hearts, Strength II and Speed I. You can be finished off, and you die after 10 seconds regardless. Hunger, toggling the ability and leaving the End cannot cancel this timer.

Times assume 20 server ticks per second. Normal exhaustion uses saturation first; standing still does not add a separate hunger drain.

## Build and develop

Use **JDK 21**. The Gradle Wrapper downloads its dependencies on the first build:

```powershell
.\gradlew.bat build
```

Linux/macOS: `./gradlew build`.

The playable file is `build/libs/poluten-1.1.0-neoforge-1.21.1.jar`. The `-sources.jar` is for development and must not be installed as a mod.

Open the folder in IntelliJ IDEA as a Gradle project and select JDK 21. Use `runClient` / `runServer` for development. The stable release is built with Origins 0.3.2. To test another available version:

```powershell
.\gradlew.bat runClient -Porigins_file=8842849
```

CurseForge file IDs: `8758523` = 0.3.2, `8799407` = 0.4-beta.1, `8842849` = 0.4-beta.2. Optional `-Pextra_mod=C:\path\to\another-mod.jar` loads a local compatibility mod in development; it is never bundled.

See [tests/README.md](tests/README.md) for dedicated-server checks. The tests are a separate development mod and are not included in the playable JAR.

## Project layout

- `src/main/java/` — ability logic, display-only power codec and client vignette.
- `src/main/resources/` — origins, powers, English/Russian language files, metadata and textures.
- `tests/` — dedicated-server regression checks.
- `docs/` — release notes, project description, testing scope and icon.
- `tools/generate_resources.py` — regenerates only the vignette PNG. Edit JSON and translations directly in `src`.
- `releases/` — playable release JARs.

## По-русски

**Полутень** — происхождение воина, ставшего получеловеком-полутенью. В версии **1.1.0** добавлен английский перевод, исправлены названия и описания способностей, поддерживается Origins (NeoForge) **от 0.3.2**. Язык выбирается в настройках Minecraft. Для обновления замените старый JAR Halfshade и при необходимости обновите Origins; внутренние ID происхождения сохранены.

Исходники открываются в IntelliJ IDEA как проект Gradle. Нужен JDK 21. Команда сборки: `.\gradlew.bat build`.

## License and credits

MIT; see [LICENSE.txt](LICENSE.txt). The concept and balance decisions belong to the project author. Code, icon and parts of the text were created with AI assistance. Dependencies are distributed under their own licenses and are not bundled in Halfshade.

[CurseForge](https://www.curseforge.com/minecraft/mc-mods/halfshade-an-origins-addon) · [GitHub releases](https://github.com/shadow8354not-del/Halfshade-NeoForge-1.21.1/releases) · [Report a bug](https://github.com/shadow8354not-del/Halfshade-NeoForge-1.21.1/issues)
