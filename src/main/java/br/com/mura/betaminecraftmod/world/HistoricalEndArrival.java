package br.com.mura.betaminecraftmod.world;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;

/** Recreates the historical platform only after checking the complete campaign footprint. */
public final class HistoricalEndArrival {
    private HistoricalEndArrival() { }
    public static boolean place(ServerLevel level,CampaignGenerationGate gate) {
        var plan=HistoricalEndArrivalLayout.spawn();
        for(var p:plan.blocks().keySet())if(gate.locked(Math.floorDiv(p.x(),16),Math.floorDiv(p.z(),16))||!level.getWorldBorder().isWithinBounds(new BlockPos(p.x(),p.y(),p.z())))return false;
        for(var entry:plan.blocks().entrySet()) {
            var p=entry.getKey();
            // Direct block replacement, as in the reference. Do not destroyBlock/drop modern block resources.
            level.setBlock(new BlockPos(p.x(),p.y(),p.z()),entry.getValue()==49?Blocks.OBSIDIAN.defaultBlockState():Blocks.AIR.defaultBlockState(),3);
        }
        return true;
    }
}
