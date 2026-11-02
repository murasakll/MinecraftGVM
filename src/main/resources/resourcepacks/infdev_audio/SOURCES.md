# Audio sources

Original Minecraft audio (Mojang / respective authors). These are not original recordings by this mod.

Reference: BetaCraft asset index 20100607, selected by the Infdev 20100617-1531 version metadata.
Index: https://files.betacraft.uk/launcher/v2/assets/indexes/20100607.json
Index SHA-1: d53120c37a59b6ae955de96f022e762979f033be

72 sound effects were fetched from https://resources.download.minecraft.net/ and validated by their archived SHA-1 hashes.
The original sound/ and newsound/ step variants are both retained, matching the reference sound pool registration.

The user supplied Door_opening_old.ogg and Door_closing_old.ogg. Their hashes exactly match the archived door recordings, with filenames reversed relative to the index; mapping follows SHA-1, not the supplied filename. Per user request, opening uses only Door_opening_old.ogg and closing uses only Door_closing_old.ogg. No random selection between the two recordings. Only the unavailable bow recording retains the current game event definition.
The archived music encodings were unavailable. Music uses the installed current-game Minecraft, Clark and Sweden recordings rather than bundling or claiming byte-identical historical encodings.

Reproduce: python tools/import_infdev_audio.py
Generated import manifest: .gradle/infdev-reference/audio-import-manifest.json

Chest opening/closing also use the supplied door recordings as a user-requested adaptation. Missing historical mob recordings retain vanilla events; the pack contains no empty sound events.

Recovered historical bow from Kartixx Alpha Sound Pack (Modrinth version SL8wsyyf), matching archived SHA-1 f1b527e059bb4abe3aed3acf6b5eb96f40b07f67. Only the original Minecraft recording was imported. Monster hurt/death reuse the reference random.hurt recording. Total: 73 archived effects; no missing effects from the selected index.
