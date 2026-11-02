package br.com.mura.betaminecraftmod.progression;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Per-item values obtained by executing the seven original item damage getters. */
public final class HistoricalMeleeCatalog {
    private static final Map<Integer, Map<String, Integer>> DAMAGE = load();
    private HistoricalMeleeCatalog() {}

    public static boolean active(int era, String firstRelease) {
        return era >= 0 && (era <= 5 || era == 6 && "1.0".equals(firstRelease));
    }

    /** Unknown/custom items keep their native damage instead of being guessed by name. */
    public static float damage(int era, String item, float nativeDamage) {
        Integer value = DAMAGE.getOrDefault(era, Map.of()).get(item);
        return value == null ? nativeDamage : value;
    }

    private static Map<Integer, Map<String, Integer>> load() {
        var result = new HashMap<Integer, Map<String, Integer>>();
        try (var stream = Objects.requireNonNull(HistoricalMeleeCatalog.class.getResourceAsStream(
                "/data/betaminecraftmod/historical_combat/melee.tsv"));
             var reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            for (String line; (line = reader.readLine()) != null;) {
                if (line.isBlank() || line.startsWith("#")) continue;
                String[] fields = line.split("\\s+");
                if (fields.length != 4) throw new IOException("Invalid melee row: " + line);
                int era = Integer.parseInt(fields[0]);
                int damage = Integer.parseInt(fields[3]);
                var values = result.computeIfAbsent(era, ignored -> new HashMap<>());
                Integer previous = values.putIfAbsent(fields[2], damage);
                if (previous != null && previous != damage) throw new IOException("Conflicting melee row: " + line);
            }
        } catch (IOException exception) {
            throw new ExceptionInInitializerError(exception);
        }
        var immutable = new HashMap<Integer, Map<String, Integer>>();
        result.forEach((era, values) -> immutable.put(era, Map.copyOf(values)));
        return Map.copyOf(immutable);
    }
}
