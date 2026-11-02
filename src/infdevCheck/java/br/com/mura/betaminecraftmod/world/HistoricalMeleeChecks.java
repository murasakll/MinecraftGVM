package br.com.mura.betaminecraftmod.world;

import br.com.mura.betaminecraftmod.progression.HistoricalMeleeCatalog;
import java.nio.file.*;
import java.util.Arrays;

final class HistoricalMeleeChecks {
    static void run() throws Exception {
        var reference = Path.of("docs/reference/melee_damage.tsv");
        if (!Arrays.equals(Files.readAllBytes(reference), Files.readAllBytes(Path.of(
                "src/main/resources/data/betaminecraftmod/historical_combat/melee.tsv"))))
            throw new AssertionError("Melee extraction/runtime mismatch");
        int cases = 0;
        for (String line : Files.readAllLines(reference)) {
            if (line.isBlank() || line.startsWith("#")) continue;
            var fields = line.split("\\s+");
            if (HistoricalMeleeCatalog.damage(Integer.parseInt(fields[0]), fields[2], -99) != Integer.parseInt(fields[3]))
                throw new AssertionError("Original melee mismatch: " + line);
            cases++;
        }
        if (cases != 1154 || HistoricalMeleeCatalog.damage(5, "minecraft:diamond_sword", 0) != 10
                || HistoricalMeleeCatalog.damage(6, "minecraft:diamond_sword", 0) != 7
                || HistoricalMeleeCatalog.damage(6, "custom:diamond_sword", 42) != 42
                || HistoricalMeleeCatalog.active(6, "1.2.5") || HistoricalMeleeCatalog.active(7, "1.0")
                || !HistoricalMeleeCatalog.active(6, "1.0")) throw new AssertionError("Melee era boundary/fallback");
        System.out.println("HISTORICAL_MELEE SUCCESS: 1154 original getters, Adventure/release boundary and custom-item fallback");
    }
}
