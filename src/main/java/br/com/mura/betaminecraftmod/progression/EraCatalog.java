package br.com.mura.betaminecraftmod.progression;

import java.util.List;

/** Shared server/client chapter definitions. IDs match saved-world and network IDs. */
public final class EraCatalog {
    public enum Visual { INFDEV, PENDING, MODERN }
    public enum Audio { INFDEV_20100607, PENDING, MODERN }
    public record Rules(boolean hunger, boolean sprinting, boolean criticalHits, boolean attackCooldown, boolean offhand) { }
    public record Definition(int id, GenerationRouter.Era era, String translationKey, String reference,
            Rules rules, Visual visual, Audio audio, boolean terrainReady, boolean questsReady) {
        public List<String> resourcePacks() {
            return resourcePacks("1.0");
        }
        public List<String> resourcePacks(String firstRelease) {
            var appearance=EraAppearanceCatalog.get(id);
            var textures=appearance==null||id==6&&!firstRelease.equals("1.0")?List.<String>of():appearance.packs();
            var result=new java.util.ArrayList<String>();
            if(!textures.isEmpty())result.add(textures.getFirst());
            if(audio==Audio.INFDEV_20100607)result.add("resourcepacks/infdev_audio");
            if(textures.size()>1)result.addAll(textures.subList(1,textures.size()));
            return List.copyOf(result);
        }
        public boolean playable() {
            // Campaign readiness is separate from historical cosmetic completeness.
            // Partial textures/audio remain explicitly identified in their own catalogs.
            return terrainReady && questsReady;
        }
    }
    private static final String[] REFERENCES = {
        "Infdev 20100617-1531", "Infdev 20100630-1835", "Alpha 1.1.2_01",
        "Alpha 1.2 Halloween", "Beta before 1.8", "Beta 1.8 Adventure", "Release 1.0-1.2",
        "Release 1.3-1.7", "Release 1.8-1.9", "Release 1.10-1.12", "Release 1.13",
        "Release 1.14-1.15", "Release 1.16", "Release 1.17-1.18", "Release 1.19",
        "Release 1.20", "Release 1.21 / Bundles", "Pale Garden / Spring / Skies",
        "Copper / Mounts", "26.3"
    };
    private static final List<Definition> DEFINITIONS = java.util.stream.IntStream.range(0, GenerationRouter.Era.values().length)
        .mapToObj(id -> {
            var era = GenerationRouter.Era.values()[id];
            // Future chapter rules describe their end milestone, not every intermediate release.
            var rules = new Rules(id >= 5, id >= 5, id >= 5, id >= 8, id >= 8);
            return new Definition(id, era, "era.betaminecraftmod." + era.name().toLowerCase(java.util.Locale.ROOT), REFERENCES[id], rules,
                id <= 2 ? Visual.INFDEV : id == 19 ? Visual.MODERN : Visual.PENDING,
                id <= 2 ? Audio.INFDEV_20100607 : id == 19 ? Audio.MODERN : Audio.PENDING,
                id <= 6, id <= 6);
        }).toList();
    public static List<Definition> all() { return DEFINITIONS; }
    public static Definition get(int id) { return id >= 0 && id < DEFINITIONS.size() ? DEFINITIONS.get(id) : null; }
    private EraCatalog() { }
}
