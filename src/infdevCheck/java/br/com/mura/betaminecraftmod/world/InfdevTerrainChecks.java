package br.com.mura.betaminecraftmod.world;

import java.security.MessageDigest;
import java.util.Arrays;
import java.util.HexFormat;

/** Golden hashes obtained independently from the archived 20100617-1531 generator before population. */
public final class InfdevTerrainChecks {
    public static void main(String[] args) throws Exception {
        ResourceReloadChecks.run();
        ExplorationChecks.run();
        ReleasePortalPlanChecks.run();
        QuestChecks.run();
        QuestTreeChecks.run();
        VisualChecks.run();
        BoundaryFogChecks.run();
        EraCatalogChecks.run();
        EraAppearanceChecks.run();
        HistoricalTextureReferenceChecks.run();
        HalloweenTerrainChecks.run();
        HalloweenPopulationSettingsChecks.run();
        HistoricalLakeChecks.run();
        HistoricalLargeOakChecks.run();
        HistoricalSmallTreeChecks.run();
        HistoricalNetherChecks.run();
        HistoricalEndChecks.run();
        HistoricalFortressChecks.run();
        HistoricalConstructionChecks.run();
        HistoricalSpriteChecks.run();
        HistoricalVegetationChecks.run();
        HistoricalLightingChecks.run();
        HistoricalEndArrivalChecks.run();
        HistoricalNetherPortalChecks.run();
        HistoricalPortalDestinationChecks.run();
        ReleaseBrewingCatalogChecks.run();
        ReleasePotionEffectChecks.run();
        HistoricalMeleeChecks.run();
        HistoricalCriticalChecks.run();
        HistoricalExtraKnockbackChecks.run();
        ReleaseEnchantmentDamageChecks.run();
        ReleaseEnchantingChecks.run();
        HistoricalExperienceChecks.run();
        HistoricalDeathExperienceChecks.run();
        BetaTerrainChecks.run();
        AdventureTerrainChecks.run();
        HistoricalMechanicsChecks.run();
        HistoricalHungerChecks.run();
        HistoricalSpawnChecks.run();
        ReleaseBaseChecks.run();
        ReleaseCarverChecks.run();
        AquaticCarverChecks.run();
        GenerationSeedChecks.run();
        long[] seeds = {0, 123456789, -987654321};
        int[][] positions = {{0, 0}, {1, -1}, {-7, 12}, {100, 100}};
        String[][] hashes = {
            {"90bb1c34d76351502deda3761c8851a069a3fe6aed1e5829fd413fdb84b77511", "a74ae93184917c8a4ffaee011e65cda6017ef01bd6d81c2fa6b46588e761da4e", "f92a9e3d1fe1a5a68fac299fab5a74e07e178ee4bff570b412a3bb4ce98bcd8f", "2b2ef3846e7e79c1839e538de8daa2ff51b9ab803fa7f52e2a719c89871b3b47"},
            {"483b65d4772207cbba1d7dfc0a3bf40a62ea303af2161f4973c38a0ada3ea9e8", "a79845500d0d306a80a8e96ffdb51cd50ebe3cd1378bd2fc8c0a88ddee9d61ea", "2712431166abffac0b287b433b6cc935a0c5159bad05cca3453ea4317c770703", "a3f170680e3985b6349c628df5cfa7bf9a0782a1b830f1a8fceab2d2332475b5"},
            {"c1c38e9fe8c1a5ea52810298dfa8d905abebb8f8013ad3460f57a14276b4067b", "0797cfea169a0a915a4e1f3308d78e4278eb97bc7e52af71004907263f3f9d43", "43f018934363ec499c9ed210f2297fee630a65adcb7a0bc548c4886a52189bc8", "b907ac3a56b28f9aaa2de794d273a78fadc3c2b2c6b04dfc009ce6670cd4c92c"}
        };
        for (int s = 0; s < seeds.length; s++) {
            InfdevTerrain terrain = new InfdevTerrain(seeds[s]);
            for (int p = 0; p < positions.length; p++) {
                int x = positions[p][0], z = positions[p][1];
                byte[] blocks = terrain.generate(x, z);
                String actual = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(blocks));
                if (!actual.equals(hashes[s][p])) throw new AssertionError("Historical terrain mismatch: seed=" + seeds[s] + " chunk=" + x + "," + z);
                // Repeat after another chunk: generation order and parallel consumers must not affect terrain.
                terrain.generate(z + 1, x - 1);
                if (!Arrays.equals(blocks, terrain.generate(x, z))) throw new AssertionError("Generation depends on call order");
                for (int bx = 0; bx < 16; bx++) for (int bz = 0; bz < 16; bz++)
                    if (blocks[InfdevTerrain.index(bx, 0, bz)] != 7) throw new AssertionError("Missing bedrock");
            }
        }
        String[] lateHashes = {
            "bfa414224e1edff1a48fc30481f14d3301042a7116d943200568ff5230aef947",
            "7377718b5b0ed409142f7afc00f957c592d28bd8edd683278c57978bea087aae",
            "edab3728e9dd720a9fee2cf399ae98c28d588f967f2b23fb5905a98c94468b1e",
            "c8b2635ead0f50763c8310eb860c9acd9e98cb94f5d0c5670b08585a8d4d3023",
            "153357ee2f8b6feb924c7eafc2cd587ad4f698f567b6ad265613ce168a1f31d8",
            "1d47ce08dd2e111e6a02954542943010e49ce398cc51770bba20dd21ea345e9f",
            "907aaa1f1d317d2f42198a2d223591e662be03a8e5ba62e51b979c22c8b48084",
            "f07bc0c7d8456f729e119535ddfba1818f9c32dd458fb30bf8a3bc178c2ba903",
            "2816a8273e3947589b7c8531b6f97a7d5e7c716e33d9736f72621c47a7877a80",
            "a1535b3bd467386fe4b824194aeaf1a9b0784736363227187cbe4a41b9ca3d70",
            "349bddd2d2124a373b047b03ce83014aefb642b717091b74df40bec64bda9fd1",
            "347ab7ebb85b383f6453606c515ea779b061357bd7974e634dfd7be6d0a23e25"
        };
        for (int seed = 0; seed < seeds.length; seed++) {
            var terrain = new InfdevTerrain(seeds[seed], true);
            for (int pos = 0; pos < positions.length; pos++) {
                byte[] blocks = terrain.generate(positions[pos][0], positions[pos][1]);
                String hash = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(blocks));
                if (!hash.equals(lateHashes[seed*4+pos])) throw new AssertionError("June 30 reference mismatch");
                terrain.generate(32, -33);
                if (!Arrays.equals(blocks, terrain.generate(positions[pos][0], positions[pos][1]))) throw new AssertionError("Late terrain depends on generation order");
            }
        }
        for (int seed=0;seed<seeds.length;seed++) for(int pos=0;pos<positions.length;pos++) {
            byte[] blocks=new InfdevTerrain(seeds[seed],2).generate(positions[pos][0],positions[pos][1]);
            String hash=HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(blocks));
            if(!hash.equals(lateHashes[seed*4+pos])) throw new AssertionError("Alpha normal-world terrain mismatch");
        }
        System.out.println("12 Alpha reference terrain snapshots passed.");
        System.out.println("12 June 30 historical terrain snapshots and generation-order checks passed.");
        System.out.println("12 historical terrain snapshots, generation order and bedrock checks passed.");
    }
}
