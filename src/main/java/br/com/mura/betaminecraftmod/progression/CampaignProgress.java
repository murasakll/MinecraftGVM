package br.com.mura.betaminecraftmod.progression;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

public final class CampaignProgress extends SavedData {
    public static final SavedDataType<CampaignProgress> TYPE = new SavedDataType<>(
        Identifier.fromNamespaceAndPath("betaminecraftmod", "campaign"), CampaignProgress::new,
        RecordCodecBuilder.create(i -> i.group(
            Codec.INT.optionalFieldOf("rings", 0).forGetter(p -> p.rings),
            Codec.INT.optionalFieldOf("era", 0).forGetter(p -> p.era),
            Codec.DOUBLE.optionalFieldOf("center_x", 0.0).forGetter(p -> p.centerX),
            Codec.DOUBLE.optionalFieldOf("center_z", 0.0).forGetter(p -> p.centerZ),
            Codec.BOOL.optionalFieldOf("initialized", false).forGetter(p -> p.initialized)
        ).apply(i, CampaignProgress::new)));
    private int rings, era;
    private double centerX, centerZ;
    private boolean initialized;
    public CampaignProgress() { }
    private CampaignProgress(int rings, int era, double x, double z, boolean initialized) {
        this.rings = Math.clamp(rings, 0, ExplorationBands.MAX_RINGS);
        this.era = Math.max(0, era);
        centerX = x; centerZ = z; this.initialized = initialized;
    }
    public static CampaignProgress get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(TYPE);
    }
    public int era() { return era; }
    public int rings() { return rings; }
    public double centerX() { return centerX; }
    public double centerZ() { return centerZ; }
    public void applyBorder(ServerLevel level) {
        if (!initialized) {
            centerX = 0;
            centerZ = 0;
            initialized = true;
            setDirty();
        }
        ((br.com.mura.betaminecraftmod.world.InfdevChunkGenerator)level.getServer().overworld().getChunkSource().getGenerator()).unlockThrough(era);
        for(var dimension:level.getServer().getAllLevels())if(dimension.getChunkSource().getGenerator() instanceof br.com.mura.betaminecraftmod.world.CampaignGenerationGate gate)gate.unlockThrough(era);
        var border = level.getServer().overworld().getWorldBorder();
        border.setCenter(centerX, centerZ);
        border.setSize(ExplorationBands.diameter(rings));
        var nether=level.getServer().getLevel(net.minecraft.world.level.Level.NETHER);
        if(nether!=null&&nether.getChunkSource().getGenerator() instanceof br.com.mura.betaminecraftmod.world.HistoricalNetherChunkGenerator generator) {
            nether.getWorldBorder().setCenter(centerX/generator.ringScale(),centerZ/generator.ringScale());
            nether.getWorldBorder().setSize(ExplorationBands.diameter(rings)/generator.ringScale());
        }
        var end=level.getServer().getLevel(net.minecraft.world.level.Level.END);
        if(end!=null&&end.getChunkSource().getGenerator() instanceof br.com.mura.betaminecraftmod.world.HistoricalEndChunkGenerator) {
            end.getWorldBorder().setCenter(0,0);
            end.getWorldBorder().setSize(1000);
        }
    }
    /** Quest completion will call this method; never rewrites already generated chunks. */
    public boolean canUnlock(ServerLevel level) {
        if (rings >= ExplorationBands.MAX_RINGS) return false;
        if (!EraCatalog.get(rings + 1).playable()) return false;
        return rings+1!=6 || level.getServer().overworld().getChunkSource().getGenerator() instanceof br.com.mura.betaminecraftmod.world.InfdevChunkGenerator generator&&generator.hasReleasePortal();
    }
    public boolean unlockRing(ServerLevel level) {
        if(!canUnlock(level))return false;
        rings++;
        era = rings;
        setDirty();
        applyBorder(level);
        for (var player : level.getServer().getPlayerList().getPlayers()) {
            HistoricalItemEvents.normalize(player);
            // Recompute old output previews when an era changes its recipe ingredients/counts.
            player.inventoryMenu.slotsChanged(player.getInventory());
            if(player.containerMenu instanceof net.minecraft.world.inventory.CraftingMenu)
                player.containerMenu.slotsChanged(player.getInventory());
            net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player, EraPayload.forLevel(era,player.level()));
        }
        return true;
    }
}
