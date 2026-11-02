package br.com.mura.betaminecraftmod.world;

import br.com.mura.betaminecraftmod.progression.EraCatalog;

public final class EraCatalogChecks {
    public static void run() {
        String[] savedIds = {"INFDEV_START", "INFDEV_LATE", "ALPHA_EARLY", "HALLOWEEN", "BETA_EARLY", "ADVENTURE",
            "RELEASE_EARLY", "REDSTONE", "BOUNTIFUL_COMBAT", "EXPLORATION_COLOR", "AQUATIC", "VILLAGE_BEES",
            "NETHER", "CAVES_CLIFFS", "WILD", "TRAILS", "TRICKY_BUNDLES", "PALE_SPRING_SKIES", "COPPER_MOUNTS", "CURRENT"};
        if (EraCatalog.all().size() != savedIds.length) throw new AssertionError("Chapter ID migration required");
        for (int id = 0; id < savedIds.length; id++) {
            var profile = EraCatalog.get(id);
            if (profile.id() != id || !profile.era().name().equals(savedIds[id])) throw new AssertionError("Saved era ID changed: " + id);
            if (id > 6 && profile.playable()) throw new AssertionError("Unimplemented campaign path became playable");
            if(id<=6&&!profile.playable())throw new AssertionError("Implemented campaign base blocked by cosmetic polish");
            if (id > 6 && !profile.resourcePacks().isEmpty()) throw new AssertionError("Unverified historical pack inherited");
            if(id>=3&&id<=4&&(profile.resourcePacks().size()!=2||!profile.resourcePacks().getFirst().contains("visual_")||!profile.resourcePacks().getLast().contains("chest_")))throw new AssertionError("Partial sprite/chest pack isolation");
            if(id>=5&&id<=6&&(profile.resourcePacks().size()!=1||!profile.resourcePacks().getFirst().contains("visual_")))throw new AssertionError("Unexpected later sprite pack");
        }
        if (!EraCatalog.get(1).questsReady() || !EraCatalog.get(1).terrainReady()) throw new AssertionError("Late chapter is not ready");
        if (!EraCatalog.get(1).playable() || !EraCatalog.get(1).resourcePacks().contains("resourcepacks/infdev_late")
                || EraCatalog.get(0).resourcePacks().contains("resourcepacks/infdev_late")) throw new AssertionError("Infdev chapter pack isolation");
        if (!EraCatalog.get(2).playable() || !EraCatalog.get(2).resourcePacks().contains("resourcepacks/alpha_early")
                || EraCatalog.get(1).resourcePacks().contains("resourcepacks/alpha_early")) throw new AssertionError("Alpha pack isolation");
        if (EraCatalog.get(-1) != null || EraCatalog.get(20) != null) throw new AssertionError("Unknown era accepted");
        if (EraCatalog.get(0).rules().hunger() || EraCatalog.get(0).rules().offhand()) throw new AssertionError("Modern Infdev rules");
        if (!EraCatalog.get(5).rules().hunger() || EraCatalog.get(5).rules().attackCooldown()) throw new AssertionError("Adventure/Combat boundary");
        if (!EraCatalog.get(8).rules().offhand() || !EraCatalog.get(8).rules().attackCooldown()) throw new AssertionError("Combat milestone");
        if (!EraCatalog.get(0).resourcePacks().contains("resourcepacks/infdev_audio")) throw new AssertionError("Missing Infdev audio pack");
        if(!EraCatalog.get(6).questsReady()||!EraCatalog.get(6).terrainReady())throw new AssertionError("Release base catalog missing");
        System.out.println("Era catalog: stable IDs, seven campaign bases, world-specific portal gate and separate cosmetic readiness passed.");
    }
}
