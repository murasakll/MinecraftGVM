package br.com.mura.betaminecraftmod.progression;

import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.phys.shapes.*;

public final class HistoricalBlockShapes {
    private static final VoxelShape[] STAIRS=new VoxelShape[8];
    static {for(int i=0;i<8;i++){var shape=Shapes.empty();for(var b:HistoricalStairGeometry.collision(i))shape=Shapes.or(shape,Shapes.box(b.x0(),b.y0(),b.z0(),b.x1(),b.y1(),b.z1()));STAIRS[i]=shape;}}
    private HistoricalBlockShapes() { }
    public static VoxelShape collision(BlockState state,BlockGetter view) {
        if(!state.is(Blocks.CHEST)&&!(state.getBlock() instanceof StairBlock))return null;
        int era=CampaignRules.era(view);
        if(state.is(Blocks.CHEST)&&HistoricalConstruction.chestHitbox(era))return Shapes.block();
        if(state.getBlock() instanceof StairBlock&&HistoricalConstruction.straightStairs(era)) {
            int meta=switch(state.getValue(StairBlock.FACING)){case EAST->0;case WEST->1;case SOUTH->2;case NORTH->3;default->throw new IllegalStateException();};
            if(!HistoricalConstruction.bottomStairs(era,CampaignRules.firstRelease(view))&&state.getValue(StairBlock.HALF)==Half.TOP)meta|=4;
            return STAIRS[meta];
        }
        return null;
    }
}
