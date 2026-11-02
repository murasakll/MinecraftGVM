package br.com.mura.betaminecraftmod.world;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WallTorchBlock;

/** Release 1.0 death portal, at fixed Y=64 below the death position. */
public final class HistoricalEndPodium {
    private HistoricalEndPodium() { }
    public static boolean place(ServerLevel level,CampaignGenerationGate gate,int x,int z) {
        var plan=HistoricalEndPodiumLayout.plan(x,z);
        for(var p:plan.keySet())if(gate.locked(Math.floorDiv(p.x(),16),Math.floorDiv(p.z(),16)))return false;
        // Commit after checking the whole footprint; never query a locked neighboring chunk.
        for(var entry:plan.entrySet()) {
            var p=entry.getKey();var state=switch(entry.getValue()) {
                case 7->Blocks.BEDROCK.defaultBlockState();case 119->Blocks.END_PORTAL.defaultBlockState();
                case 122->Blocks.DRAGON_EGG.defaultBlockState();
                case 50->Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING,Direction.getNearest(p.x()-x,0,p.z()-z,Direction.NORTH));
                default->Blocks.AIR.defaultBlockState();
            };
            level.setBlock(new BlockPos(p.x(),p.y(),p.z()),state,2);
        }
        return true;
    }
}
