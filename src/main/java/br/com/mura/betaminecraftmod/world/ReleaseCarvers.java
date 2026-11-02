package br.com.mura.betaminecraftmod.world;

import com.seedfinding.mccore.version.MCVersion;
import java.util.*;

/** Candidate pre-Aquatic carvers. Kept separate from surface generation and later carver families. */
final class ReleaseCarvers {
    private ReleaseCarvers() {}

    static TerrainVolume carve(TerrainVolume input, long seed, MCVersion version, int cx, int cz) {
        if(version.isNewerOrEqualTo(MCVersion.v1_13)&&version.isOlderThan(MCVersion.v1_17))
            return AquaticCarvers.carve(input,seed,version,cx,cz);
        if (version.isOlderThan(MCVersion.v1_7_2) || !version.isOlderThan(MCVersion.v1_13))
            return input;
        if (input.minY() != 0 || input.height() != 256)
            throw new IllegalArgumentException("Release carvers require the historical 256-high container");
        // Only known replaceable materials enter the old numerical workspace. Foreign materials
        // are opaque, not air; the output preserves every untouched palette entry exactly.
        byte[] before = new byte[65536];
        for (int x=0;x<16;x++) for (int z=0;z<16;z++) for (int y=0;y<256;y++)
            before[(x*16+z)*256+y] = switch (input.block(x,y,z)) {
                case "minecraft:air", "minecraft:cave_air" -> 0;
                case "minecraft:stone" -> 1;
                case "minecraft:grass_block" -> 2;
                case "minecraft:dirt" -> 3;
                case "minecraft:water" -> 9;
                default -> (byte)255;
            };
        byte[] after = before.clone();
        new AdventureCaves(seed,256,15,7,248).carve(after,cx,cz);
        new AdventureRavines(seed,256,248).carve(after,cx,cz);
        var palette = new ArrayList<>(input.palette());
        var ids = new HashMap<String,Short>();
        for (int i=0;i<palette.size();i++) ids.putIfAbsent(palette.get(i),(short)i);
        short[] output = new short[65536];
        for (int x=0;x<16;x++) for (int z=0;z<16;z++) for (int y=0;y<256;y++) {
            int index=(x*16+z)*256+y;
            String name=before[index]==after[index] ? input.block(x,y,z) : switch(after[index]) {
                case 0 -> "minecraft:air";
                case 2 -> "minecraft:grass_block";
                case 10 -> "minecraft:lava";
                default -> throw new IllegalStateException("Unexpected carver material");
            };
            Short id=ids.get(name);
            if(id==null){id=(short)palette.size();palette.add(name);ids.put(name,id);}
            output[index]=id;
        }
        return new TerrainVolume(0,256,palette,output);
    }
}
