# Release validation — Halfshade 1.1.0

Validated on 2026-11-08 with Java 21, Minecraft 1.21.1 and NeoForge 21.1.235.

| Origins (NeoForge) | Dedicated-server regression checks |
| --- | --- |
| 0.3.2 (CurseForge file 8758523) | 153 passed |
| 0.4-beta.1 (CurseForge file 8799407) | 153 passed |
| 0.4-beta.2 (CurseForge file 8842849) | 153 passed |

All three server runs used the same release JAR and the same separately built test JAR, with Jupiter 2.3.7. The checks include real data/registry decoding and damage processing as well as synthetic events for edge cases. Test sources and reproduction instructions are in `tests/`.

The standard Gradle build and separate integration test JAR build succeeded with JDK 21. The playable archive was checked for Java 21 bytecode, language key parity, MIT license, expanded version metadata and absence of test classes. The vignette PNG is byte-for-byte identical to 1.0.5; its renderer source is unchanged.

A client smoke run with Origins 0.3.2 and Shine 2.0.2 completed mod loading, resource reload and shader initialization without the previously reported shader crash. This is a startup check, not a full manual gameplay session.

Tests do not guarantee compatibility with future Origins releases, every modpack or every multiplayer interaction. The metadata accepts 0.3.2+ as requested; only the versions listed above were tested. NeoForge 21.1.228 remains the minimum requirement of the dependency, while these runs used 21.1.235.
