package br.com.mura.betaminecraftmod.world;

import br.com.mura.betaminecraftmod.progression.CampaignRules;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;

/** Every portal geometry query is checked before it can request a locked chunk. */
public final class HistoricalPortalView implements HistoricalNetherPortalLayout.View {
    private final BlockGetter level;
    public HistoricalPortalView(BlockGetter level){this.level=level;}
    public static boolean historical(BlockGetter level){int era=CampaignRules.era(level);return era>=3&&era<=6&&(era!=6||CampaignRules.firstRelease(level).equals("1.0"));}
    @Override public boolean allowed(int x,int z){
        if(level instanceof net.minecraft.world.level.ServerLevelAccessor accessor){
            ServerLevel server=accessor.getLevel();
            if(!server.getWorldBorder().isWithinBounds(new BlockPos(x,64,z)))return false;
            if(server.getChunkSource().getGenerator() instanceof CampaignGenerationGate gate&&gate.locked(x>>4,z>>4))return false;
        }
        return true;
    }
    @Override public int block(int x,int y,int z){
        if(y<0||y>=128)return 0;if(!allowed(x,z))return -1;
        var state=level.getBlockState(new BlockPos(x,y,z));
        return state.isAir()?0:state.is(Blocks.OBSIDIAN)?49:state.is(Blocks.FIRE)?51:state.is(Blocks.NETHER_PORTAL)?90:1;
    }
    @Override public boolean solid(int x,int y,int z){throw new UnsupportedOperationException("Geometry view cannot choose a destination ground material");}
}
