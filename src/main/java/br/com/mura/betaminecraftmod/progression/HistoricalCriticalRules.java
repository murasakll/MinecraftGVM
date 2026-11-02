package br.com.mura.betaminecraftmod.progression;

import java.util.function.IntUnaryOperator;

/** Original attack-prefix rules; flags are documented in critical_damage.tsv. */
public final class HistoricalCriticalRules {
    private HistoricalCriticalRules() {}
    public static boolean critical(int era, int flags) {
        if (era == 5) return (flags & 1) != 0 && (flags & (4|8|16)) == 0;
        return era == 6 && (flags & (2|128)) == (2|128) && (flags & (4|8|16|32|64)) == 0;
    }
    public static int damage(int era, int base, IntUnaryOperator random) {
        if (era == 5) return base * 3 / 2 + 1;
        if (era == 6) return base + random.applyAsInt(base / 2 + 2);
        return base;
    }
}
