# Dedicated-server regression checks

The checks are a separate development mod. They do not run on a client and are never bundled in the release JAR. Use only a disposable test server: the suite changes its time, places test blocks at (0,100,0), creates fake players and stops the server after the checks.

1. Build with JDK 21: `./gradlew build integrationJar` (Windows: `gradlew.bat`).
2. Prepare a separate NeoForge 21.1.235 server for Minecraft 1.21.1. Review and accept Minecraft's server EULA yourself if it has not already been accepted.
3. In that server's `mods` folder put the playable Halfshade JAR, `poluten-1.1.0-checks.jar`, Jupiter 2.3.7 and exactly one Origins version under test. Do not include unrelated mods.
4. Bind the server to `127.0.0.1`, use a separate port, and start it with Java 21 and `nogui`.
5. The server stops automatically. Require `PASS Origins ...` in `halfshade-checks.txt` and `HALFSHADE ALL CHECKS PASSED` in the log. A normal server exit code alone is **not** evidence that the assertions passed.
6. Repeat with Origins 0.3.2, 0.4-beta.1 and 0.4-beta.2 using the **same** built Halfshade and test JARs. Save the report/log between runs.

Coverage includes decoded power names/descriptions in both languages, helper visibility, all vanilla swords, an untagged mod sword, cows/horses, Wither damage/duration/shared cooldown, rejected attack types, interaction restrictions, hunger boundaries, exhaustion, phasing state, blindness, fire/fall/totem rules, Last Breath deadlines, End return and respawn cleanup.

These checks mix real registry loading and entity damage with synthetic NeoForge events for deterministic edge cases. They do not replace a full multiplayer play session or test arbitrary modpacks. Client rendering needs a separate client smoke test.
