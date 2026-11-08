# Halfshade 1.1.0

Stable release for Minecraft 1.21.1 / NeoForge.

## Changes

- Full English and Russian localization for the origin, all visible powers and ability messages.
- Fixed untranslated ability keys (including Last Breath) and descriptions discarded by the port's empty-power type.
- Hidden helper powers now stay hidden in the origin screen.
- Origins (NeoForge) 0.3.2 or newer is accepted instead of requiring exactly 0.3.
- Shadow Venom only triggers on direct sword strikes; reflected Thorns damage cannot apply it.
- Last Breath survives the player replacement on return from the End, preventing the exit portal from clearing its deadline.
- Reproducible Gradle build, source archive and a separate dedicated-server regression suite.

The vignette renderer and reduced-opacity texture are unchanged from 1.0.5. Existing origin IDs and balance are preserved.

## Requirements

Minecraft 1.21.1, NeoForge 21.1.228+ within the 21.1 branch, Origins (NeoForge) by IAFEnvoy 0.3.2+, and Jupiter. Install the playable JAR on both client and server. Do not install the source or test archives.

## Русский

Стабильный выпуск 1.1.0: английская и русская локализация, исправленные названия и описания способностей, скрытые служебные способности и поддержка Origins от 0.3.2. Яд больше не запускается от «Шипов». Выход из Края не сбрасывает «Последний вздох». Виньетка сохранена как в 1.0.5.

Лицензия MIT. Идея и баланс — автора проекта; реализация создана с помощью ИИ.

## Validation

153 server checks passed on each of Origins 0.3.2, 0.4-beta.1 and 0.4-beta.2 (459 passing assertions across three runs). Built with Java 21 / NeoForge 21.1.235. See `docs/testing.md` for scope and reproduction details.
