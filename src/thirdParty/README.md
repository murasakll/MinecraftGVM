# SeedFinding terrain adapter

Source: https://github.com/SeedFinding/mc_terrain_java
Commit: `a03e440ec5b282e399382f2cc5ad0db91b438d2e`; source version 1.171.1.
License: MIT, copyright 2021 KaptainWutax, Neil. Full notice is distributed at `META-INF/licenses/seedfinding-terrain.txt`.

The seven Java source files retain their upstream namespace. Dependencies are separately embedded via Jar-in-Jar; no original Mojang classes are redistributed.

Local changes:

- Lower the minimum constructor version to 1.7.2 for the candidate 1.7 terrain adapter; no claim of original-client equality.
- Remove repeated stderr warnings before 1.11; fidelity is documented by GenerationBases instead.
- In the bedrock pass convert chunk coordinates to block coordinates before reading and caching each column.
- In the biome-column loop pass the loop's actual block coordinates to the column generator and surface replacement, rather than repeating the requested target column.

A new engine instance is created per chunk request to keep upstream caches bounded. See docs/GENERATION_BASES.md for fidelity and missing carver/population work.
